package com.kppkl.authcommons.constants;

/**
 * S3-T01 — Konstanta nama authority/role yang dipakai seluruh service.
 * Satu sumber kebenaran (Single Source of Truth) untuk nama role,
 * menghindari typo yang tersebar di banyak service.
 */
public final class AuthorityConstants {

    // Mencegah instansiasi (Utility Class Pattern)
    private AuthorityConstants() {
        throw new IllegalStateException("Utility class");
    }

    /** Dosen pembimbing / panitia KP-PKL (id_role = 0) */
    public static final String ROLE_COMMITTEE        = "COMMITTEE";

    /** Mahasiswa peserta KP-PKL (id_role = 1) */
    public static final String ROLE_PARTICIPANT      = "PARTICIPANT";

    /** Mitra perusahaan (id_role = 2) */
    public static final String ROLE_COMPANY          = "COMPANY";

    /** Ketua program studi (id_role = 3) */
    public static final String ROLE_HEAD_STUDY_PROGRAM = "HEAD_STUDY_PROGRAM";
}
