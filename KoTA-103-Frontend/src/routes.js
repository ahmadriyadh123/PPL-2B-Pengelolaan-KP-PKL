// import React from 'react'
import lazyWithRetry from './lazyWithRetry'

// Page
const Dashboard = lazyWithRetry(() => import('./views/dashboard/Dashboard'))
const Profile = lazyWithRetry(() => import('./views/pages/Profile'))

// Pemetaan
const FinalisasiPemetaan = lazyWithRetry(() => import('./views/pemetaan/finalisasi/Finalisasi'))
const Perangkingan = lazyWithRetry(() => import('./views/pemetaan/perangkingan/Perangkingan'))
const HasilPemetaan = lazyWithRetry(() => import('./views/pemetaan/hasilPemetaan'))

// CV
const DetailCV = lazyWithRetry(() => import('./views/rekap/CV/detailCV'))
const RekapCV = lazyWithRetry(() => import('./views/rekap/CV/rekapCV'))
const PengumpulanCV = lazyWithRetry(() => import('./views/rekap/CV/pengumpulanCV'))
const DataCV = lazyWithRetry(() => import('./views/rekap/CV/dataMahasiswa'))
const UpdateCV = lazyWithRetry(() => import('./views/rekap/CV/updateCV'))

// Minat
const DetailMinat = lazyWithRetry(() => import('./views/rekap/minat/detailMinat'))
const RekapMinat = lazyWithRetry(() => import('./views/rekap/minat/rekapMinat'))
const PengumpulanMinat = lazyWithRetry(() => import('./views/rekap/minat/pengumpulanMinat'))
const UpdateMinat = lazyWithRetry(() => import('./views/rekap/minat/updateMinat'))

// Perusahaan
const ListPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/listPerusahaan'))
const UpdatePerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/updatePerusahaan'))
const DetailPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/detailPerusahaan'))
const DetailPrerequisitePerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/detailPrerequisite'))
const IdentitasPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/identitasPerusahaan'))
const PrerequisitePerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/prerequisitePerusahaan'))
const UpdatePrerequisite = lazyWithRetry(() => import('./views/rekap/perusahaan/updatePrerequisite'))
const TabelPrerequisite = lazyWithRetry(() => import('./views/rekap/perusahaan/tabelPrerequisite'))
const CreatePerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/createPerusahaan'))
const TabelPengajuanPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/tabelPengajuanPerusahaan'))
const DetailPengajuanPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/detailPengajuanPerusahaan'))
const EvaluasiPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/evaluasiPerusahaan'))
const FeedbackPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/feedbackPerusahaan'))
const DetailEvaluasiPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/detailEvaluasiPerusahaan'))
const CardEvaluasiPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/cardEvaluasiPerusahaan'))
const FormulirEvaluasiPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/formulirEvaluasiPerusahaan'))
const DetailFeedbackPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/detailFeedbackPerusahaan'))
const CardFeedbackPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/cardFeedbackPerusahaan'))
const FormulirFeedbackPerusahaan = lazyWithRetry(() => import('./views/rekap/perusahaan/formulirFeedbackPerusahaan'))

// Pengelolaan
const PengelolaanAkun = lazyWithRetry(() => import('./views/pengelolaan/pengelolaanAkun'))
const PengelolaanKegiatan = lazyWithRetry(() => import('./views/pengelolaan/pengelolaanKegiatan'))
const PengelolaanBobotKriteria = lazyWithRetry(() => import('./views/pengelolaan/pengelolaanBobotKriteria'))
const PengelolaanKriteriaPerusahaan = lazyWithRetry(() => import('./views/pengelolaan/pengelolaanKriteriaPerusahaan'))
const PengelolaanKompetensi = lazyWithRetry(() => import('./views/pengelolaan/pengelolaanKompetensi'))
const PengelolaanAspekPenilaianEvaluasi = lazyWithRetry(() => import('./views/pengelolaan/pengelolaanAspekPenilaianEvaluasi'))
const pengelolaanPertanyaanFeedback = lazyWithRetry(() => import('./views/pengelolaan/pengelolaanPertanyaanFeedback'))

//Monitoring
const MonitoringPelaksanaan = lazyWithRetry(() => import('./views/monitoring/dashboard/MonitoringPelaksanaan'))
const DashboardPanitia = lazyWithRetry(() => import('./views/monitoring/dashboard/dashboardPanitia'))
const DaftarPeserta = lazyWithRetry(() => import('./views/monitoring/daftarPeserta/daftarPeserta'))
const SetPembimbingJurusan = lazyWithRetry(() => import('./views/monitoring/pemetaanPembimbing/PemetaanPembimbingJurusan'))
const DashboardPeserta = lazyWithRetry(() => import('./views/monitoring/dashboard/dashboardPeserta'))
const DashboardPembimbing = lazyWithRetry(() => import('./views/monitoring/dashboard/dashboardPembimbing'))

//DOKUMEN PESERTA
const RekapDokumenPeserta = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/ListDokumenPeserta'))

//RPP
const ContohPengisianRPP = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/rpp/contohRPP'))
const RekapRPP = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/rpp/rekapRpp'))
const FormPengisianRPP = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/rpp/pengisianRpp'))
const DetailRPP = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/rpp/detailRPP'))

//LOGBOOK
const ReviewLogbook = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/logbook/reviewLogbook'))
const FormPengisianLogbook = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/logbook/pengisianLogbook'))
const FormEditLogbook = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/logbook/editLogbook'))
const FormPengisianSelfAssessment = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/selfassessment/pengisianSelfAssessment'))
const FormEditRPP =  lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/rpp/editRPP'))
const RekapSelfAssessment = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/selfassessment/rekapSelfAssessment'))
const DetailSelfAssessment = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/selfassessment/detailSelfAssessment'))
const UploadLaporan = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/laporan/uploadLaporan'))
const TambahLaporan= lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/laporan/tambahLaporan'))
const RekapLogbook = lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/logbook/rekapLogbook'))
const PenilaianLogbook = lazyWithRetry(() => import('./views/monitoring/penilaianArtifakPeserta/logbook/penilaianLogbook'))
const PembobotanSelfAssessment = lazyWithRetry(() => import('./views/monitoring/pengelolaanMonitoring/pengaturanPoinPenilaianSelfAssessment'))
const PengelolaanBobotFormPembimbing = lazyWithRetry(() => import('./views/monitoring/pengelolaanMonitoring/pengaturanBobotFormPembimbingJurusan'))
const PengeloaanDeadline = lazyWithRetry(() => import('./views/monitoring/pengelolaanMonitoring/pengaturanDeadline'))

//PENILAIAN
const RekapPenilaianPembimbingJurusan = lazyWithRetry(() => import('./views/monitoring/penilaianArtifakPeserta/rekapPenilaianPeserta'))
const RekapPenilaianSelfAssessment = lazyWithRetry(() => import('./views/monitoring/penilaianArtifakPeserta/selfAssessment/rekapNilaiSelfAssessment'))
const RekapLaporan= lazyWithRetry(() => import('./views/monitoring/pengisianDokumen/laporan/rekapLaporanPeserta'))
const FormPenilaianPembimbingJurusan= lazyWithRetry(() => import('./views/monitoring/penilaianArtifakPeserta/laporan/formPenilaianPembimbingJurusan'))
const RekapPenilaianLogbook= lazyWithRetry(() => import('./views/monitoring/penilaianArtifakPeserta/logbook/rekapNilaiLogbook'))
const PenilaianSelfAssessment= lazyWithRetry(() => import('./views/monitoring/penilaianArtifakPeserta/selfAssessment/penilaianSelfAssessment'))
const RekapPenilaianLaporan= lazyWithRetry(() => import('./views/monitoring/penilaianArtifakPeserta/laporan/rekapNilaiLaporan'))
const PenilaianDetailRPP= lazyWithRetry(() => import('./views/monitoring/penilaianArtifakPeserta/penilaianDokumenDetailRPP'))

const routes = [
  // Page Utama
  { path: '/', exact: true, name: 'Beranda' },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard, protected: true, roles: ['0', '1', '2', '3', '4'] },
  { path: '/profile', name: 'Profile', component: Profile, protected: true, roles: ['0', '1', '2', '3', '4'] },

  // Pemetaan (Hanya Panitia)
  { path: '/hasilPemetaan', name: 'Hasil Pemetaan', component: HasilPemetaan, exact: true, protected: true, roles: ['0'] },
  { path: '/pemetaan', name: 'Pemetaan', component: FinalisasiPemetaan, exact: true, protected: true, roles: ['0'] },
  { path: '/pemetaan/perangkingan', name: 'Perangkingan Mahasiswa', component: Perangkingan, exact: true, protected: true, roles: ['0'] },
  { path: '/pemetaan/finalisasi', name: 'Pemilihan Mahasiswa', component: FinalisasiPemetaan, exact: true, protected: true, roles: ['0'] },

  // CV (Mahasiswa)
  { path: '/CV', name: 'CV', component: PengumpulanCV, exact: true, protected: true, roles: ['1'] },
  { path: '/CV/detailCV', name: 'Detail CV', component: DetailCV, exact: true, protected: true, roles: ['1'] },
  { path: '/CV/detailCV/:id', name: ':id', component: DetailCV, protected: true, roles: ['1'] },
  { path: '/CV/updateCV', name: 'Ubah Data CV', component: UpdateCV, exact: true, protected: true, roles: ['1'] },
  { path: '/CV/updateCV/:id', name: ':id', component: UpdateCV, protected: true, roles: ['1'] },

  // Rekap Data CV / Mahasiswa (Panitia, Perusahaan, Kaprodi)
  { path: '/dataMahasiswa', name: 'Data Mahasiswa', component: DataCV, exact: true, protected: true, roles: ['0', '2'] },
  { path: '/dataMahasiswa/detailCV', name: 'Detail CV', component: DetailCV, exact: true, protected: true, roles: ['0', '2'] },
  { path: '/dataMahasiswa/detailCV/:id', name: ':id', component: DetailCV, exact: true, protected: true, roles: ['0', '2'] },
  { path: '/rekapCV', name: 'Rekap CV', component: RekapCV, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/rekapCV/detailCV', name: 'Detail CV', component: DetailCV, protected: true, roles: ['0', '3'] },

  // Minat / Pemilihan Perusahaan (Mahasiswa)
  { path: '/pemilihanPerusahaan', name: 'Pemilihan Perusahaan', component: PengumpulanMinat, exact: true, protected: true, roles: ['1'] },
  { path: '/pemilihanPerusahaan/detailMinat', name: 'Detail Pemilihan Perusahaan', component: DetailMinat, protected: true, roles: ['1'] },
  { path: '/pemilihanPerusahaan/updateMinat', name: 'Ubah Data Pemilihan Perusahaan', component: UpdateMinat, protected: true, roles: ['1'] },

  // Rekap Minat (Panitia & Kaprodi)
  { path: '/rekapMinat', name: 'Rekap Minat', component: RekapMinat, exact: true, protected: true, roles: ['0', '3'] },

  // Perusahaan & Evaluasi (Panitia, Perusahaan, Kaprodi)
  { path: '/dataEvaluasiPerusahaan', name: 'Data Evaluasi Perusahaan', component: CardEvaluasiPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/dataEvaluasiPerusahaan/formulirEvaluasiPerusahaan', name: 'Formulir Evaluasi Perusahaan', component: FormulirEvaluasiPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/dataEvaluasiPerusahaan/formulirEvaluasiPerusahaan/:id', name: ':id', component: FormulirEvaluasiPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/dataEvaluasiPerusahaan/detailEvaluasiPerusahaan', name: 'Detail Evaluasi Perusahaan', component: DetailEvaluasiPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/dataEvaluasiPerusahaan/detailEvaluasiPerusahaan/:id', name: ':id', component: DetailEvaluasiPerusahaan, exact: true, protected: true, roles: ['2'] },

  { path: '/evaluasiPeserta', name: 'Evaluasi Peserta', component: EvaluasiPerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/evaluasiPeserta/detailEvaluasi', name: 'Detail Evaluasi Peserta', component: DetailEvaluasiPerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/evaluasiPeserta/detailEvaluasi/:id', name: ':id', component: DetailEvaluasiPerusahaan, exact: true, protected: true, roles: ['0', '3'] },

  { path: '/feedbackPerusahaan', name: 'Feedback Perusahaan', component: FeedbackPerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/feedbackPerusahaan/detailFeedback', name: 'Detail Feedback Perusahaan', component: DetailFeedbackPerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/feedbackPerusahaan/detailFeedback/:id', name: ':id', component: DetailFeedbackPerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/dataFeedbackPerusahaan', name: 'Formulir Feedback Pelaksanaan Magang', component: CardFeedbackPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/dataFeedbackPerusahaan/formulirFeedbackPerusahaan', name: 'Isi Formulir Feedback Pelaksanaan Magang', component: FormulirFeedbackPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/dataFeedbackPerusahaan/formulirFeedbackPerusahaan/:id', name: ':id', component: FormulirFeedbackPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/dataFeedbackPerusahaan/detailFeedbackPerusahaan', name: 'Detail Formulir Feedback Pelaksanaan Magang', component: DetailFeedbackPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/dataFeedbackPerusahaan/detailFeedbackPerusahaan/:id', name: ':id', component: DetailFeedbackPerusahaan, exact: true, protected: true, roles: ['2'] },

  { path: '/formulirKesediaan', name: 'Formulir Kesediaan Perusahaan', component: PrerequisitePerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/formulirKesediaan/prerequisite', name: 'Prerequisite', component: DetailPrerequisitePerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/formulirKesediaan/prerequisite/:id', name: ':id', component: DetailPrerequisitePerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/formulirKesediaan/updatePrerequisite', name: 'Ubah Data Prerequisite', component: UpdatePrerequisite, exact: true, protected: true, roles: ['2'] },
  { path: '/formulirKesediaan/updatePrerequisite/:id', name: ':id', component: UpdatePrerequisite, exact: true, protected: true, roles: ['2'] },

  { path: '/hasilEvaluasiPerusahaan', name: 'Hasil Evaluasi Perusahaan', component: DetailEvaluasiPerusahaan, exact: true, protected: true, roles: ['1'] },

  { path: '/listPerusahaan', name: 'List Perusahaan', component: ListPerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/createPerusahaan', name: 'Tambah Perusahaan', component: CreatePerusahaan, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/detailPerusahaan', name: 'Detail Perusahaan', component: DetailPerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/detailPerusahaan/:id', name: ':id', component: DetailPerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/detailPerusahaan/prerequisite', name: 'Prerequisite', component: DetailPrerequisitePerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/detailPerusahaan/prerequisite/:id', name: ':id', component: DetailPrerequisitePerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/detailPerusahaan/updatePrerequisite', name: 'Ubah Data Prerequisite', component: UpdatePrerequisite, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/detailPerusahaan/updatePrerequisite/:id', name: ':id', component: UpdatePrerequisite, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/detailPerusahaan/updatePerusahaan', name: 'Ubah Data Perusahaan', component: UpdatePerusahaan, exact: true, protected: true, roles: ['0', '3'] },
  { path: '/listPerusahaan/detailPerusahaan/updatePerusahaan/:id', name: ':id', component: UpdatePerusahaan, exact: true, protected: true, roles: ['0', '3'] },

  { path: '/pengajuanPerusahaan', name: 'Pengajuan Perusahaan', component: TabelPengajuanPerusahaan, exact: true, protected: true, roles: ['0'] },
  { path: '/pengajuanPerusahaan/detailPengajuanPerusahaan', name: 'Detail Pengajuan Perusahaan', component: DetailPengajuanPerusahaan, exact: true, protected: true, roles: ['0'] },
  { path: '/pengajuanPerusahaan/detailPengajuanPerusahaan/:id', name: ':id', component: DetailPengajuanPerusahaan, exact: true, protected: true, roles: ['0'] },

  { path: '/profilPerusahaan', name: 'Profil Perusahaan', component: IdentitasPerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/profilPerusahaan/updatePerusahaan', name: 'Ubah Data Perusahaan', component: UpdatePerusahaan, exact: true, protected: true, roles: ['2'] },
  { path: '/profilPerusahaan/updatePerusahaan/:id', name: ':id', component: UpdatePerusahaan, protected: true, roles: ['2'] },

  { path: '/prerequisitePerusahaan', name: 'Prerequisite Perusahaan', component: TabelPrerequisite, exact: true, protected: true, roles: ['1'] },

  // Pengelolaan (Panitia & Kaprodi)
  { path: '/pengelolaanAkun', name: 'Pengelolaan Akun', component: PengelolaanAkun, protected: true, roles: ['0', '3'] },
  { path: '/pengelolaanKegiatan', name: 'Pengelolaan Kegiatan', component: PengelolaanKegiatan, protected: true, roles: ['0'] }, // Dikunci murni hanya untuk Panitia '0'
  { path: '/pengelolaanBobotKriteria', name: 'Pengelolaan Bobot Kriteria Perangkingan', component: PengelolaanBobotKriteria, exact: true, protected: true, roles: ['0'] },
  { path: '/pengelolaanKriteriaPerusahaan', name: 'Pengelolaan Kriteria Perusahaan', component: PengelolaanKriteriaPerusahaan, exact: true, protected: true, roles: ['0'] },
  { path: '/pengelolaanKompetensi', name: 'Pengelolaan Kompetensi', component: PengelolaanKompetensi, exact: true, protected: true, roles: ['0', '1', '2', '3'] },
  { path: '/pengelolaanAspekPenilaianEvaluasi', name: 'Pengelolaan Aspek Penilaian Evaluasi', component: PengelolaanAspekPenilaianEvaluasi, exact: true, protected: true, roles: ['0'] },
  { path: '/pengelolaanPertanyaanFeedback', name: 'Pengelolaan Pertanyaan Feedback', component: pengelolaanPertanyaanFeedback, exact: true, protected: true, roles: ['0'] },

  // MONITORING PELAKSANAAN
  { path: '/dashboardPeserta', name: 'Dashboard Peserta', component: DashboardPeserta, exact: true, protected: true, roles: ['1'] },
  { path: '/monitoringPelaksanaan', name: 'Monitoring Pelaksanaan', component: MonitoringPelaksanaan, exact: true, protected: true, roles: ['0'] },
  { path: '/dashboardPanitia', name: 'Monitoring Dashboard', component: DashboardPanitia, exact: true, protected: true, roles: ['0'] },
  { path: '/dashboardPembimbing', name: 'Monitoring Dashboard', component: DashboardPembimbing, exact: true, protected: true, roles: ['4'] },
  { path: '/daftarPeserta', name: 'Daftar Peserta', component: DaftarPeserta, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/daftarPeserta/dashboardPeserta/:nim', name: ':nim', component: DashboardPeserta, exact: true, protected: true, roles: ['0', '4'] },

  // PEMETAAN PEMBIMBING JURUSAN (Panitia)
  { path: '/pengaturanDaftarPembimbingJurusan', name: 'Pengaturan Pembimbing Jurusan', component: SetPembimbingJurusan, exact: true, protected: true, roles: ['0'] },

  /** RPP */
  { path: '/rencanaPenyelesaianProyek', name: 'Rencana Penyelesaian Proyek', component: RekapRPP, exact: true, protected: true, roles: ['1'] },
  { path: '/rencanaPenyelesaianProyek/:id/formPengisianRPP', name: 'FormPengisianRPP', component: FormPengisianRPP, exact: true, protected: true, roles: ['1'] },
  { path: '/rencanaPenyelesaianProyek/:id/formPengisianRPP/contohPengisianRPP', name: 'Contoh RPP', component: ContohPengisianRPP, exact: true, protected: true, roles: ['1'] },
  { path: '/rencanaPenyelesaianProyek/detail/:id?', name: ':id', component: DetailRPP, exact: true, protected: true, roles: ['1'] },
  { path: '/rencanaPenyelesaianProyek/edit/:id?', name: ':id', component: FormEditRPP, exact: true, protected: true, roles: ['1'] },

  /** LOGBOOK */
  { path: '/logbook', name: 'Logbook', component: RekapLogbook, exact: true, protected: true, roles: ['1'] },
  { path: '/logbook/detaillogbook/:id', name: ':id', component: ReviewLogbook, exact: true, protected: true, roles: ['1'] },
  { path: '/logbook/formlogbook', name: 'Form Logbook', component: FormPengisianLogbook, exact: true, protected: true, roles: ['1'] },
  { path: '/logbook/formlogbook/:id', name: ':id', component: FormPengisianLogbook, exact: true, protected: true, roles: ['1'] },
  { path: '/logbook/formEditLogbook/:id', name: ':id', component: FormEditLogbook, exact: true, protected: true, roles: ['1'] },
  { path: '/logbook/formEditLogbook/reviewEdit/:id', name: ':id', component: ReviewLogbook, exact: true, protected: true, roles: ['1'] },

  /** SELF ASSESSMENT */
  { path: '/selfAssessment', name: 'Self Assessment', component: RekapSelfAssessment, exact: true, protected: true, roles: ['1'] },
  { path: '/selfAssessment/formSelfAssessment', name: 'Form Self Assessment', component: FormPengisianSelfAssessment, exact: true, protected: true, roles: ['1'] },
  { path: '/selfAssessment/formSelfAssessment/detail/:id', name: ':id', component: DetailSelfAssessment, exact: true, protected: true, roles: ['1'] },

  /** LAPORAN */
  { path: '/laporan', name: 'Laporan', component: RekapLaporan, exact: true, protected: true, roles: ['1'] },
  { path: '/laporan/submissionLaporan', name: 'Pengumpulan Laporan', component: TambahLaporan, exact: true, protected: true, roles: ['1'] },
  { path: '/laporan/submissionLaporan/:id', name: ':id', component: UploadLaporan, exact: true, protected: true, roles: ['1'] },

  // DOKUMEN PESERTA (Panitia & Pembimbing)
  { path: '/rekapDokumenPeserta', name: 'Rekap Dokumen Peserta', component: RekapDokumenPeserta, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/logbookPeserta/:id', name: ':id', component: RekapLogbook, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/logbookPeserta/:nim/detail/:id', name: ':id', component: ReviewLogbook, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/logbookPeserta/:nim/nilai/:id', name: 'Penilaian Logbook', component: PenilaianLogbook, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/logbookPeserta/:nim/nilai/:idlogbook/detailRPP/:id', name: ':id', component: PenilaianDetailRPP, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/rppPeserta/:id', name: ':id', component: RekapRPP, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/rppPeserta/:nim/detail/:id', name: ':id', component: DetailRPP, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/selfAssessmentPeserta/:id', name: ':id', component: RekapSelfAssessment, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/selfAssessmentPeserta/:id/rekapProgres', name: ':id', component: RekapPenilaianSelfAssessment, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/selfAssessmentPeserta/:nim/rekapPenilaianSelfAssessment', name: ':nim', component: RekapPenilaianSelfAssessment, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/selfAssessmentPeserta/:nim/detail/:id', name: ':id', component: DetailSelfAssessment, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/selfAssessmentPeserta/:nim/penilaian/:id', name: ':id', component: PenilaianSelfAssessment, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/selfAssessmentPeserta/:nim/penilaian/:idsa/detailRPP/:id', name: ':id', component: PenilaianDetailRPP, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/laporan/:id', name: ':id', component: RekapLaporan, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapDokumenPeserta/laporan/:nim/nilai/:id', name: ':id', component: FormPenilaianPembimbingJurusan, exact: true, protected: true, roles: ['0', '4'] },

  /** REKAP PENILAIAN */
  { path: '/rekapPenilaianPeserta', name: 'Rekap Penilaian Peserta', component: RekapPenilaianPembimbingJurusan, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapPenilaianPeserta/logbook/:id', name: ':id', component: RekapPenilaianLogbook, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapPenilaianPeserta/selfassessment/:id', name: ':id', component: RekapPenilaianSelfAssessment, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapPenilaianPeserta/laporan/:id', name: ':id', component: RekapPenilaianLaporan, exact: true, protected: true, roles: ['0', '4'] },

  { path: '/rekapFormPenilaianPembimbingJurusan', name: 'Rekap Penilaian Pembimbing Jurusan', component: RekapPenilaianPembimbingJurusan, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/uploadLaporan', name: 'Dokumen Laporan KP/PKL', component: UploadLaporan, exact: true, protected: true, roles: ['1'] },
  { path: '/rekapLogbook', name: 'Logbook', component: RekapLogbook, exact: true, protected: true, roles: ['0', '4'] },
  { path: '/rekapLogbook/detailLogbook/:id', name: ':id', component: ReviewLogbook, exact: true, protected: true, roles: ['0', '4'] },

  /** PENILAIAN */
  { path: '/rekapLogbook/penilaianLogbook', name: 'Penilaian Logbook', component: PenilaianLogbook, exact: true, protected: true, roles: ['0', '4'] },

  /** PENGATURAN MONITORING */
  { path: '/pembobotanSelfAssessment', name: 'Poin Penilaian Self Assessment', component: PembobotanSelfAssessment, exact: true, protected: true, roles: ['0'] },
  { path: '/pengelolaanPoinFormPembimbing', name: 'Poin Penilaian Pembimbing Jurusan', component: PengelolaanBobotFormPembimbing, exact: true, protected: true, roles: ['0'] },
  { path: '/pengelolaanDeadline', name: 'Pengeloaan Deadline', component: PengeloaanDeadline, exact: true, protected: true, roles: ['0'] },
]

export default routes
