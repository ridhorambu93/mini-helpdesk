# 📓 Daily Notes Protocol

## Trigger

Ketika developer mengetikkan salah satu dari:
- **"buat catatan hari ini"**
- **"catat hari ini"**
- **"buat task list hari ini"**
- **"dokumentasikan hari ini"**
- **"update catatan hari ini"**

Agent **wajib** membuat file dokumentasi secara otomatis tanpa perlu konfirmasi tambahan.

---

## Format Nama File

```
task-list-{DDMMYYYY}.md
```

Contoh: `task-list-08102026.md`

Tanggal diambil dari tanggal hari ini saat perintah diberikan.

---

## Lokasi File

```
{workspace_root}/bug-fix-and-feature/task-list-{DDMMYYYY}.md
```

---

## Struktur Wajib File

```markdown
# Task List — {DD Bulan YYYY}
**Project:** emr-app
**Sesi:** {ringkasan singkat topik sesi}

---

## 🐛 BUG FIX

### BF-XX — {Judul Bug}
**File:** `path/to/file`
**Severity:** High / Medium / Low
**Gejala:** {apa yang terlihat oleh user/developer}

**Root Cause:** {penjelasan teknis penyebab}

**Fix:** {penjelasan + code snippet sebelum/sesudah jika relevan}

---

## ✨ PENAMBAHAN FITUR

### FT-XX — {Judul Fitur}
**File:** `path/to/file`
**Deskripsi:** {apa yang ditambahkan}

**Detail Implementasi:** {penjelasan teknis}

---

## 📋 RINGKASAN FILE YANG DIUBAH

| File | Perubahan |
|---|---|
| `path/to/file` | BF-XX, FT-XX |

---

## 📝 CATATAN TEKNIS

{catatan penting, keputusan arsitektur, hal yang perlu diperhatikan ke depan}
```

---

## Aturan Pengisian

- **Pisahkan** Bug Fix dan Penambahan Fitur dalam section berbeda
- **Jika tidak ada bug fix** → tulis `*Tidak ada bug fix pada sesi ini.*`
- **Jika tidak ada fitur baru** → tulis `*Tidak ada penambahan fitur baru pada sesi ini.*`
- **Severity Bug:**
  - `High` → menyebabkan data salah / fitur tidak berfungsi
  - `Medium` → menyebabkan tampilan salah / UX terganggu
  - `Low` → kosmetik / minor
- **Selalu sertakan** file yang diubah di tabel ringkasan
- **Gunakan bahasa Indonesia** untuk semua deskripsi
- **Sertakan code snippet** `sebelum/sesudah` untuk bug fix yang melibatkan perubahan logika penting
