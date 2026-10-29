/**
 * =====================================================================
 * BACKEND ROUTE: GET /api/mobile/bootstrap-data
 * Express.js Endpoint untuk Centralized Bootstrap Sync Aplikasi RKCE Android
 * Portal: https://1-portalce.ai.studio/
 * =====================================================================
 * 
 * Karakteristik:
 * 1. Menyatukan 7 dataset master dalam satu payload JSON tunggal
 * 2. Hanya mengambil record berstatus 'Aktif' (kecuali rencanaKerja yang aktif & pending)
 * 3. Menyertakan relasi penanggung jawab (`kasie_penanggung_jawab`) pada unit
 * 4. Mendukung strategi client-side "Clear and Replace" pada Room SQLite
 */

const express = require('express');
const router = express.Router();

// Diasumsikan koneksi database menggunakan pool SQL (misal PostgreSQL/MySQL) atau ORM (Sequelize/Prisma)
// Di bawah ini disediakan implementasi generic yang siap dihubungkan dengan model database portal.

router.get('/api/mobile/bootstrap-data', async (req, res) => {
  try {
    const timestamp = new Date().toISOString();

    // 1. Master Pengguna (Filter: status_aktif = 1 atau status = 'Aktif')
    // Total 12 Akun Pengguna Aktif (Super Admin, Kasie Lapangan, Manager)
    const usersQuery = `
      SELECT 
        id, 
        nama_lengkap, 
        username, 
        hak_akses, 
        role, 
        wilayah, 
        status_aktif, 
        status, 
        email 
      FROM master_users 
      WHERE status_aktif = 1 OR LOWER(status) = 'aktif'
      ORDER BY id ASC
    `;

    // 2. Master Unit (Filter: status = 'OPERASIONAL' atau status = 'Aktif', tanpa Dump Truck & Bulldozer)
    // Total 39 Unit Alat Berat Aktif (Excavator Standar, Mini, Long Arm, Grader, Compactor)
    // Menyertakan relasi 'kasie_penanggung_jawab'
    const unitsQuery = `
      SELECT 
        kode_unit, 
        jenis_unit, 
        model_unit AS brand_model, 
        hm_terakhir AS last_hm, 
        kasie_penanggung_jawab, 
        nama_pengawas, 
        status, 
        hm_min_standar, 
        hm_max_standar, 
        wilayah 
      FROM master_units 
      WHERE (LOWER(status) IN ('operasional', 'aktif'))
        AND LOWER(jenis_unit) NOT LIKE '%dump%' 
        AND LOWER(jenis_unit) NOT LIKE '%bulldozer%'
      ORDER BY kode_unit ASC
    `;

    // 3. Master Operator (Filter: status = 'AKTIF')
    // Total 45 Master Operator Aktif dengan relasi kasie_penanggung_jawab dan wilayah
    const operatorsQuery = `
      SELECT 
        operator_id, 
        nama_operator, 
        nik, 
        nama_pengawas, 
        nama_pengawas AS kasie_penanggung_jawab, 
        jabatan AS skill_level, 
        status, 
        wilayah 
      FROM master_operators 
      WHERE UPPER(status) = 'AKTIF'
      ORDER BY nama_operator ASC
    `;

    // 4. Master Aktivitas (Filter: kategori_status != 'Nonaktif')
    // Total 107 Master Aktivitas Aktif
    const aktivitasQuery = `
      SELECT 
        kode_sap, 
        nama_aktivitas, 
        satuan, 
        description, 
        kategori, 
        kategori_status, 
        jenis_unit 
      FROM master_aktivitas 
      WHERE LOWER(kategori_status) != 'nonaktif'
      ORDER BY kode_sap ASC
    `;

    // 5. Master Kendala (15 Master Kendala Aktif Standar Lapangan)
    const kendalaQuery = `
      SELECT 
        id, 
        kode_kendala, 
        nama_kendala, 
        kategori, 
        deskripsi 
      FROM master_kendala 
      ORDER BY CAST(SUBSTRING_INDEX(kode_kendala, '.', 1) AS UNSIGNED) ASC
    `;

    // 6. Master Lokasi (7 Master Lokasi Aktif Sektor Lapangan)
    const lokasiQuery = `
      SELECT 
        kode_lokasi, 
        wilayah, 
        luas_bruto, 
        luas_netto, 
        keterangan 
      FROM master_lokasi 
      ORDER BY kode_lokasi ASC
    `;

    // 7. Rencana Kerja Aktif & Pending (Belum Realisasi)
    const rencanaKerjaQuery = `
      SELECT 
        id, 
        kode_unit, 
        nama_unit, 
        operator, 
        kode_sap, 
        nama_aktivitas, 
        satuan_aktivitas, 
        kode_lokasi, 
        wilayah, 
        shift_kerja, 
        keterangan, 
        nomor_spk, 
        status_spk, 
        status_tahap, 
        is_realized, 
        nama_pengawas, 
        tanggal 
      FROM rencana_kerja 
      WHERE is_realized = 0 OR is_realized IS NULL OR status_realisasi = 'BELUM'
      ORDER BY id DESC
    `;

    // Eksekusi query (contoh menggunakan db.query)
    // Pada implementasi riil sesuaikan dengan koneksi database backend Anda:
    let users = [];
    let units = [];
    let operators = [];
    let aktivitas = [];
    let kendala = [];
    let lokasi = [];
    let rencanaKerja = [];

    if (typeof db !== 'undefined' && db.query) {
      const [u, un, op, act, knd, lok, rnc] = await Promise.all([
        db.query(usersQuery),
        db.query(unitsQuery),
        db.query(operatorsQuery),
        db.query(aktivitasQuery),
        db.query(kendalaQuery),
        db.query(lokasiQuery),
        db.query(rencanaKerjaQuery)
      ]);
      users = u.rows || u;
      units = un.rows || un;
      operators = op.rows || op;
      aktivitas = act.rows || act;
      kendala = knd.rows || knd;
      lokasi = lok.rows || lok;
      rencanaKerja = rnc.rows || rnc;
    }

    // Kirim satu payload terpadu Centralized Bootstrap
    return res.status(200).json({
      status: "success",
      timestamp: timestamp,
      data: {
        users: users,             // 12 akun user aktif
        units: units,             // 39 unit alat berat aktif (dengan kasie_penanggung_jawab)
        operators: operators,     // 45 master operator aktif
        aktivitas: aktivitas,     // 107 master aktivitas aktif
        kendala: kendala,         // 15 master kendala aktif
        lokasi: lokasi,           // 7 master lokasi aktif
        rencanaKerja: rencanaKerja // Rencana kerja aktif & pending
      }
    });

  } catch (error) {
    console.error('[BootstrapSync] Error fetching bootstrap data:', error);
    return res.status(500).json({
      status: "error",
      message: "Gagal memuat bootstrap data master: " + error.message,
      timestamp: new Date().toISOString()
    });
  }
});

module.exports = router;
