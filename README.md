# Aplikasi-Pengelolaan-KP-dan-PKL

Aplikasi pengelolaan KP dan PKL berbasis microservices (Spring Boot + Eureka + API Gateway).

## Menjalankan Dengan Docker (Direkomendasikan)

Semua service bisa dijalankan sekaligus dengan satu perintah via Docker Compose.

### Prasyarat

- Docker Desktop (Compose v2)

### Jalankan

```bash
docker compose up --build
```

Perintah ini akan menjalankan:

- MySQL
- service-registry (Eureka)
- account-service
- participant-service
- company-service
- management-content-service
- mapping-service
- grade-service
- api-gateway

### Endpoint penting

- Eureka: http://localhost:8761
- API Gateway: http://localhost:8080

Contoh akses melalui gateway:

- http://localhost:8080/account/**
- http://localhost:8080/participant/**
- http://localhost:8080/company/**
- http://localhost:8080/management-content/**
- http://localhost:8080/mapping/**
- http://localhost:8080/grade/**

### Hentikan

```bash
docker compose down
```

Jika ingin sekaligus menghapus volume database:

```bash
docker compose down -v
```

## Menjalankan Tanpa Docker

1. Jalankan MySQL lokal pada port 3306.
2. Buat database:
	- db-account
	- db-participant
	- db-company
	- db-management-content
	- db-mapping
	- db-grade
3. Jalankan service satu per satu dengan Maven Wrapper dari folder masing-masing:

```bash
mvnw.cmd spring-boot:run
```

Urutan disarankan:

1. service-registry
2. account-service
3. participant-service
4. company-service
5. management-content-service
6. mapping-service
7. grade-service
8. api-gateway


