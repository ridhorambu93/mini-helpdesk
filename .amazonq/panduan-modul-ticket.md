# 🎫 Panduan Implementasi Modul Ticket — Mini Helpdesk BE

Ikuti step ini **secara berurutan**. Setiap step ada penjelasan singkat + kode lengkap yang bisa langsung ditulis manual.

---

## 📁 File yang akan dibuat

```
model/          → Ticket.java
repository/     → TicketRepository.java
dto/request/    → CreateTicketRequest.java
                → UpdateTicketRequest.java
dto/response/   → TicketResponse.java
service/        → TicketService.java
controller/     → TicketController.java
```

---

## STEP 1 — Model: `Ticket.java`

**Lokasi:** `src/main/java/com/bjb/helpdesk/model/Ticket.java`

**Kenapa:** Ini representasi tabel `tickets` di database. Hibernate akan auto-create tabel ini karena `ddl-auto: update` di `application.yml`.

```java
package com.bjb.helpdesk.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public enum Category {
        IT, HR, FINANCE, FACILITY, OTHER
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
}
```

---

## STEP 2 — Repository: `TicketRepository.java`

**Lokasi:** `src/main/java/com/bjb/helpdesk/repository/TicketRepository.java`

**Kenapa:** Spring Data JPA auto-generate query dari nama method. Tidak perlu tulis SQL manual.

```java
package com.bjb.helpdesk.repository;

import com.bjb.helpdesk.model.Ticket;
import com.bjb.helpdesk.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByCreatedBy(User createdBy, Pageable pageable);
    Page<Ticket> findByStatus(Ticket.Status status, Pageable pageable);
    Page<Ticket> findByAssignedTo(User assignedTo, Pageable pageable);
}
```

---

## STEP 3 — DTO Request: `CreateTicketRequest.java`

**Lokasi:** `src/main/java/com/bjb/helpdesk/dto/request/CreateTicketRequest.java`

**Kenapa:** DTO memisahkan data input dari entity. Validasi dilakukan di sini sebelum masuk service.

```java
package com.bjb.helpdesk.dto.request;

import com.bjb.helpdesk.model.Ticket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateTicketRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Ticket.Category category;

    @NotNull
    private Ticket.Priority priority;
}
```

---

## STEP 4 — DTO Request: `UpdateTicketRequest.java`

**Lokasi:** `src/main/java/com/bjb/helpdesk/dto/request/UpdateTicketRequest.java`

**Kenapa:** Update ticket punya field berbeda dari create — bisa update status dan assignee, khusus untuk ADMIN/DIVISION_HEAD.

```java
package com.bjb.helpdesk.dto.request;

import com.bjb.helpdesk.model.Ticket;
import lombok.Getter;

@Getter
public class UpdateTicketRequest {
    private String title;
    private String description;
    private Ticket.Category category;
    private Ticket.Priority priority;
    private Ticket.Status status;
    private Long assignedToId;
}
```

---

## STEP 5 — DTO Response: `TicketResponse.java`

**Lokasi:** `src/main/java/com/bjb/helpdesk/dto/response/TicketResponse.java`

**Kenapa:** Tidak boleh expose entity langsung dari controller. TicketResponse mengontrol data apa yang dikirim ke FE.

```java
package com.bjb.helpdesk.dto.response;

import com.bjb.helpdesk.model.Ticket;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private Ticket.Category category;
    private Ticket.Priority priority;
    private Ticket.Status status;
    private String createdBy;
    private String assignedTo;
    private Instant createdAt;
    private Instant updatedAt;

    public static TicketResponse from(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .createdBy(ticket.getCreatedBy().getUsername())
                .assignedTo(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
```

---

## STEP 6 — Service: `TicketService.java`

**Lokasi:** `src/main/java/com/bjb/helpdesk/service/TicketService.java`

**Kenapa:** Semua business logic ada di sini. Controller hanya delegasi ke service.

Aturan bisnis yang diterapkan:
- Hanya ADMIN/DIVISION_HEAD yang bisa update status dan assign ticket
- STAFF hanya bisa lihat tiket miliknya sendiri
- ADMIN/DIVISION_HEAD bisa lihat semua tiket
- Hanya pembuat tiket atau ADMIN yang bisa hapus tiket

```java
package com.bjb.helpdesk.service;

import com.bjb.helpdesk.dto.request.CreateTicketRequest;
import com.bjb.helpdesk.dto.request.UpdateTicketRequest;
import com.bjb.helpdesk.dto.response.TicketResponse;
import com.bjb.helpdesk.model.Ticket;
import com.bjb.helpdesk.model.User;
import com.bjb.helpdesk.repository.TicketRepository;
import com.bjb.helpdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketResponse create(String username, CreateTicketRequest request) {
        User user = getUser(username);
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .status(Ticket.Status.OPEN)
                .createdBy(user)
                .build();
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    public Page<TicketResponse> getAll(String username, Pageable pageable) {
        User user = getUser(username);
        boolean isPrivileged = user.getRole() == User.Role.ADMIN
                || user.getRole() == User.Role.DIVISION_HEAD;

        if (isPrivileged) return ticketRepository.findAll(pageable).map(TicketResponse::from);
        return ticketRepository.findByCreatedBy(user, pageable).map(TicketResponse::from);
    }

    public TicketResponse getById(String username, Long id) {
        User user = getUser(username);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket tidak ditemukan"));

        boolean isPrivileged = user.getRole() == User.Role.ADMIN
                || user.getRole() == User.Role.DIVISION_HEAD;
        boolean isOwner = ticket.getCreatedBy().getUsername().equals(username);

        if (!isPrivileged && !isOwner)
            throw new RuntimeException("Akses ditolak");

        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse update(String username, Long id, UpdateTicketRequest request) {
        User user = getUser(username);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket tidak ditemukan"));

        boolean isPrivileged = user.getRole() == User.Role.ADMIN
                || user.getRole() == User.Role.DIVISION_HEAD;
        boolean isOwner = ticket.getCreatedBy().getUsername().equals(username);

        if (!isPrivileged && !isOwner)
            throw new RuntimeException("Akses ditolak");

        if (request.getTitle() != null) ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getCategory() != null) ticket.setCategory(request.getCategory());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());

        if (isPrivileged) {
            if (request.getStatus() != null) ticket.setStatus(request.getStatus());
            if (request.getAssignedToId() != null) {
                User assignee = userRepository.findById(request.getAssignedToId())
                        .orElseThrow(() -> new RuntimeException("User assignee tidak ditemukan"));
                ticket.setAssignedTo(assignee);
            }
        }

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public void delete(String username, Long id) {
        User user = getUser(username);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket tidak ditemukan"));

        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isOwner = ticket.getCreatedBy().getUsername().equals(username);

        if (!isAdmin && !isOwner)
            throw new RuntimeException("Akses ditolak");

        ticketRepository.delete(ticket);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan"));
    }
}
```

---

## STEP 7 — Controller: `TicketController.java`

**Lokasi:** `src/main/java/com/bjb/helpdesk/controller/TicketController.java`

**Kenapa:** Controller hanya terima request, delegasi ke service, dan kembalikan response. Tidak ada logic di sini.

`@AuthenticationPrincipal UserDetails` → Spring inject user yang sedang login dari JWT token secara otomatis.

```java
package com.bjb.helpdesk.controller;

import com.bjb.helpdesk.dto.ApiResponse;
import com.bjb.helpdesk.dto.request.CreateTicketRequest;
import com.bjb.helpdesk.dto.request.UpdateTicketRequest;
import com.bjb.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTicketRequest request) {
        var data = ticketService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Ticket berhasil dibuat", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAll(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var data = ticketService.getAll(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        var data = ticketService.getById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("OK", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody UpdateTicketRequest request) {
        var data = ticketService.update(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Ticket berhasil diupdate", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        ticketService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Ticket berhasil dihapus", null));
    }
}
```

---

## STEP 8 — Cek SecurityConfig

**Tidak perlu diubah.** `.anyRequest().authenticated()` sudah otomatis cover `/tickets/**`.

---

## ✅ STEP 9 — Test dengan Postman

Urutan test yang benar:

### 1. Register user
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "budi",
  "email": "budi@bjb.co.id",
  "password": "password123"
}
```

### 2. Login → ambil accessToken
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "budi",
  "password": "password123"
}
```
Simpan `accessToken` dari response.

### 3. Buat ticket
```
POST http://localhost:8080/api/tickets
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "title": "Laptop tidak bisa menyala",
  "description": "Laptop saya tiba-tiba mati dan tidak bisa dinyalakan kembali",
  "category": "IT",
  "priority": "HIGH"
}
```

### 4. Get semua ticket
```
GET http://localhost:8080/api/tickets?page=0&size=10
Authorization: Bearer <accessToken>
```

### 5. Get ticket by ID
```
GET http://localhost:8080/api/tickets/1
Authorization: Bearer <accessToken>
```

### 6. Update ticket (title & priority)
```
PUT http://localhost:8080/api/tickets/1
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "priority": "CRITICAL"
}
```

### 7. Delete ticket
```
DELETE http://localhost:8080/api/tickets/1
Authorization: Bearer <accessToken>
```

---

## 🔍 Checklist Sebelum Test

- [ ] Semua 7 file sudah dibuat
- [ ] BE sudah di-restart (`mvn spring-boot:run`)
- [ ] PostgreSQL jalan dan `bjb_helpdesk_db` sudah ada
- [ ] Tidak ada error merah di console saat startup
- [ ] Tabel `tickets` muncul otomatis di database (cek via pgAdmin/DBeaver)
