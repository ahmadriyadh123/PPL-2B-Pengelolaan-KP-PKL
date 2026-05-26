# Environment Setup — Security

Panduan setup environment variable untuk `account-service`. Dokumen ini wajib
dibaca sebelum menjalankan service di mesin baru (lokal, staging, atau
production).

> **Latar belakang:** Sebelumnya `jwt.secret` dan kredensial scheduled job
> di-hardcode di source code. Mulai sprint S2 (task S2-T05 & S2-T06) semua
> nilai sensitif dipindah ke environment variable.

---

## 1. Daftar Environment Variable

| Variable                  | Deskripsi                                              | Contoh                              | Wajib? |
| ------------------------- | ------------------------------------------------------ | ----------------------------------- | ------ |
| `JWT_SECRET`              | Kunci HS512, minimal 64 karakter (base64 dari 64 byte acak) | `1Q8UtWhO...Em0uXQ==` (64 byte b64) | Ya     |
| `JWT_ACCESS_EXP_MS`       | Lama access token dalam milidetik                      | `900000` (15 menit)                 | Tidak (default 900000) |
| `JWT_REFRESH_EXP_MS`      | Lama refresh token dalam milidetik                     | `604800000` (7 hari)                | Tidak (default 604800000) |
| `DB_HOST`                 | Host database MySQL                                    | `mysql`, `localhost`                | Tidak (default `mysql`) |
| `DB_PORT`                 | Port database                                          | `3306`                              | Tidak (default `3306`) |
| `DB_NAME`                 | Nama schema                                            | `db-account`                        | Tidak (default `db-account`) |
| `DB_USERNAME`             | Username DB                                            | `root`                              | Tidak (default `root`) |
| `DB_PASSWORD`             | Password DB                                            | (acak kuat di staging/prod)         | Tidak (default `root`) |
| `SCHEDULED_JOB_D3_USERNAME` | Username panitia D3 untuk job pembersihan akun       | `panitiad3`                         | Ya (kalau scheduler aktif) |
| `SCHEDULED_JOB_D3_PASSWORD` | Password panitia D3                                  | (acak kuat)                         | Ya (kalau scheduler aktif) |
| `SCHEDULED_JOB_D4_USERNAME` | Username panitia D4                                  | `panitiad4`                         | Ya (kalau scheduler aktif) |
| `SCHEDULED_JOB_D4_PASSWORD` | Password panitia D4                                  | (acak kuat)                         | Ya (kalau scheduler aktif) |

**Constraint validasi (fail-fast saat boot):**

- `JWT_SECRET` kosong → app gagal start dengan pesan jelas.
- `JWT_SECRET` < 64 karakter → app gagal start.
- `JWT_SECRET` masih bernilai default `"token"` → app gagal start.
- `JWT_REFRESH_EXP_MS` ≤ `JWT_ACCESS_EXP_MS` → app gagal start (refresh harus
  lebih panjang dari access, bukan kebalik).
- Kalau `SCHEDULED_JOB_*` kosong → job di-skip + `log.error` (bukan crash).

---

## 2. Generate Secret yang Aman

Selalu generate ulang untuk tiap environment. Jangan reuse antara lokal,
staging, dan production.

**Linux / macOS:**

```bash
openssl rand -base64 64
```

**Windows PowerShell:**

```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object {Get-Random -Min 0 -Max 255}))
```

Output ~88 karakter. Paste ke field `JWT_SECRET` di `.env` (atau secret
manager). Untuk password scheduled job, pakai password manager atau
`openssl rand -base64 24`.

---

## 3. Setup Lokal (Development)

1. Pastikan kamu sudah pull branch terbaru.
2. Copy template `.env`:

   ```bash
   cd account-service
   cp .env.example .env
   ```

3. Edit `.env`, isi minimal 3 field wajib:

   ```dotenv
   JWT_SECRET=<paste hasil openssl rand -base64 64>
   SCHEDULED_JOB_D3_USERNAME=panitiad3
   SCHEDULED_JOB_D3_PASSWORD=1234
   SCHEDULED_JOB_D4_USERNAME=panitiad4
   SCHEDULED_JOB_D4_PASSWORD=1234
   ```

   > Untuk dev lokal, password seed default DB (`1234`) cukup. Jangan dipakai
   > di environment lain.

4. Jalankan stack via Docker Compose (dari root repo):

   ```bash
   sudo docker compose up -d --build account-service
   ```

5. Cek log untuk konfirmasi boot sukses:

   ```bash
   sudo docker logs kpms-account-service --tail 20
   ```

   Expect: `Started AccountServiceApplication in X seconds`. Kalau muncul
   `IllegalStateException: JWT_SECRET tidak diset...`, balik ke step 3.

---

## 4. Setup Staging

- **Jangan** pakai `.env` file di server staging. Inject env var via mekanisme
  orchestrator (Docker secrets, Kubernetes Secrets, systemd `EnvironmentFile`,
  CI/CD secret store).
- Generate ulang `JWT_SECRET`. Tidak boleh sama dengan dev.
- Password DB & scheduled job: simpan di password manager tim. Rotasi minimal
  tiap 90 hari atau setelah ada anggota tim keluar.
- Gunakan akun DB read-write minimal-privilege, bukan `root`.
- `JWT_ACCESS_EXP_MS=900000` dan `JWT_REFRESH_EXP_MS=604800000` (default
  cukup; kalau perlu lebih ketat, turunkan access ke 5 menit).

Contoh definisi env di docker-compose override (`docker-compose.staging.yml`):

```yaml
services:
  account-service:
    environment:
      JWT_SECRET: ${JWT_SECRET}
      SCHEDULED_JOB_D3_USERNAME: ${JOB_D3_USERNAME}
      SCHEDULED_JOB_D3_PASSWORD: ${JOB_D3_PASSWORD}
      SCHEDULED_JOB_D4_USERNAME: ${JOB_D4_USERNAME}
      SCHEDULED_JOB_D4_PASSWORD: ${JOB_D4_PASSWORD}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
```

Variabel di sebelah kanan (`${...}`) di-resolve dari shell pipeline CI/CD,
**bukan** dari file `.env` yang ke-commit.

---

## 5. Setup Production

Ikuti semua aturan staging, plus:

- Simpan secret di vault yang ter-audit (HashiCorp Vault, AWS Secrets Manager,
  GCP Secret Manager). File `.env` di disk = tidak boleh.
- Aktifkan TLS untuk koneksi DB (`useSSL=true`).
- Gunakan akun DB terpisah dari staging.
- Audit log harus capture event "JWT_SECRET tidak diset" dari startup —
  artinya ada misconfiguration deploy yang harus segera di-rollback.
- Rotasi `JWT_SECRET` saat:
  - Anggota tim dengan akses production keluar.
  - Ada indikasi kebocoran (token leaked, repo accidentally public, dll.).
  - Audit periodik (rekomendasi: tiap 6 bulan).
  - **Catatan:** rotasi `JWT_SECRET` melogout semua user — koordinasikan
    dengan Scrum Master sebelum eksekusi.

---

## 6. Contoh Konfigurasi Aman

Satu set lengkap untuk staging (nilai dummy, ganti dengan hasil generate
sendiri):

```dotenv
# JWT — generate ulang dengan: openssl rand -base64 64
JWT_SECRET=GnX2pRb9w4Yt7LkF1VhMcDjE5sNqA8oUiKvP3zZmTuS6yJxBcWlRdHaQfOgYn
JWT_ACCESS_EXP_MS=900000
JWT_REFRESH_EXP_MS=604800000

# Database
DB_HOST=db.staging.internal
DB_PORT=3306
DB_NAME=db-account
DB_USERNAME=account_app
DB_PASSWORD=Q9!nT4xK7vP2wRzE

# Scheduled job
SCHEDULED_JOB_D3_USERNAME=panitiad3
SCHEDULED_JOB_D3_PASSWORD=H8r#BnVw5TpKqLcM
SCHEDULED_JOB_D4_USERNAME=panitiad4
SCHEDULED_JOB_D4_PASSWORD=Yz2$eFs7XdGjAwQv
```

---

## 7. Troubleshooting

| Gejala                                                 | Sebab                          | Solusi                                              |
| ------------------------------------------------------ | ------------------------------ | --------------------------------------------------- |
| `IllegalStateException: JWT_SECRET tidak diset`         | Env var `JWT_SECRET` kosong    | Isi `.env` atau env orchestrator                    |
| `JWT_SECRET terlalu pendek (X karakter)`                | Secret < 64 karakter           | Generate ulang dengan `openssl rand -base64 64`     |
| `JWT_REFRESH_EXP_MS (X) harus lebih besar dari ...`     | Refresh ≤ access               | Pastikan refresh > access (default sudah benar)     |
| Log `Scheduled job credentials tidak diset. Job dilewati.` | `SCHEDULED_JOB_*` kosong       | Isi 4 var scheduled job di env                      |
| User existing tiba-tiba semua ter-logout                | `JWT_SECRET` baru di-rotasi    | Expected behavior. Komunikasikan ke user sebelum rotasi |

---

## 8. Referensi

- Task S2-T05 — Pindah `jwt.secret` ke environment variable
- Task S2-T06 — Hapus hardcoded credential di scheduled job
- Task S2-T07 — Setup `.env.example` + dokumentasi (dokumen ini)
- File terkait:
  - `account-service/.env.example`
  - `account-service/src/main/resources/application.properties`
  - `account-service/src/main/java/com/jtk/ps/api/util/JwtUtil.java`
  - `account-service/src/main/java/com/jtk/ps/api/service/AccountService.java`
  - `docker-compose.yml`
