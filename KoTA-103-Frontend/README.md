# Frontend App JTK (KoTA 103)

Ini adalah bagian frontend untuk **Aplikasi Pengelolaan KP dan PKL** (Frontend JTK). Aplikasi ini dibangun menggunakan React.js dan CoreUI.

Dokumentasi ini dibuat untuk membantu developer menjalankan aplikasi ini di komputer atau laptop lain.

## Persyaratan Sistem
Sebelum menjalankan aplikasi, pastikan perangkat Anda sudah terinstal perangkat lunak berikut:
- **Node.js**: Versi 10 atau lebih baru (Disarankan versi 14.x atau 16.x untuk stabilitas React 17).
- **NPM**: Versi 6 atau lebih baru (Biasanya sudah otomatis terinstal bersama Node.js).
- **Yarn** (Opsional, Anda bisa menggunakan NPM atau Yarn).

## Langkah-langkah Menjalankan Aplikasi

### 1. Buka Terminal
Buka terminal (Command Prompt, PowerShell, atau Git Bash) dan pastikan posisi direktori/folder saat ini berada di dalam folder `KoTA-103-Frontend`.

### 2. Install Dependencies
Instal semua pustaka (library) yang dibutuhkan aplikasi dengan menjalankan salah satu perintah berikut:

Menggunakan NPM (Sangat disarankan menggunakan `--legacy-peer-deps` untuk menghindari error konflik versi pustaka/ERESOLVE):
```bash
npm install --legacy-peer-deps
```
Atau menggunakan Yarn:
```bash
yarn install
```

Tunggu hingga proses instalasi selesai (ini akan membuat folder `node_modules`).
Jika Anda mengalami error `'react-scripts' is not recognized`, itu berarti proses instalasi (`npm install`) belum berhasil dilakukan atau belum selesai. Pastikan Anda menjalankan perintah di atas terlebih dahulu.

### 3. Konfigurasi Environment Variable (`.env`)
Aplikasi ini membutuhkan variabel lingkungan untuk terhubung ke backend. 
Buatlah sebuah file baru bernama `.env` di dalam folder utama `KoTA-103-Frontend` (sejajar dengan file `package.json`), lalu salin dan tempelkan konfigurasi berikut ke dalamnya:

```env
PORT=60000
CHOKIDAR_USEPOLLING=true
REACT_APP_API_GATEWAY_URL=http://localhost:8080/
```

**Penjelasan Singkat:**
- `PORT=60000`: Menentukan bahwa frontend ini akan berjalan pada port 60000.
- `REACT_APP_API_GATEWAY_URL`: Merupakan alamat URL API backend. Jika backend Anda berjalan di port yang berbeda, ubah `8080` sesuai dengan port backend Anda.

### 4. Jalankan Aplikasi
Setelah dependensi terinstal dan file `.env` sudah diatur, jalankan aplikasi dalam mode *development* (pengembangan):

Menggunakan NPM (Khusus untuk Node.js versi 17 ke atas, Anda mungkin perlu mengatur `NODE_OPTIONS` agar tidak terkena error OpenSSL):

**Di Windows (PowerShell):**
```powershell
$env:NODE_OPTIONS='--openssl-legacy-provider'
npm start
```

**Di Windows (Command Prompt / CMD):**
```cmd
set NODE_OPTIONS=--openssl-legacy-provider
npm start
```

**Di Mac/Linux (atau Git Bash):**
```bash
export NODE_OPTIONS=--openssl-legacy-provider
npm start
```

*(Atau jika Anda menggunakan Node.js versi lama seperti versi 14/16, Anda bisa langsung menjalankan `npm start` atau `yarn start`)*

Aplikasi akan otomatis terbuka di browser pada alamat **http://localhost:60000** (atau `0.0.0.0:60000`). Jika Anda melakukan perubahan kode, halaman akan memuat ulang secara otomatis.

---

## Membangun Aplikasi untuk Production (Build)
Jika Anda ingin menyiapkan aplikasi untuk di-deploy ke server production, jalankan perintah:
```bash
npm run build
```
*(Atau `yarn build`)*

Perintah ini akan melakukan kompilasi dan optimasi kode, hasilnya akan tersimpan di folder `build/`.
