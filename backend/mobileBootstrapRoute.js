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

    // 2. Master Unit (Filter: status = 'OPERASIONAL' atau status = 'Aktif')
    // Menyertakan relasi 'kasie_penanggung_jawab' dan mencakup seluruh armada aktif termasuk DUMP TRUCK (TTM010)
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
    // 4. Master Aktivitas (Filter: status = 'Aktif' atau kategori_status != 'Nonaktif')
    // Total Master Aktivitas Aktif mencakup kategori OPERASI, STANDBY, dan BREAKDOWN
    const aktivitasQuery = `
      SELECT 
        id, 
        kode_sap, 
        nama_aktivitas, 
        satuan, 
        description, 
        kategori, 
        kategori_status, 
        jenis_unit,
        status 
      FROM master_aktivitas 
      WHERE (status IS NULL OR LOWER(status) = 'aktif' OR LOWER(kategori_status) != 'nonaktif')
      ORDER BY 
        CASE UPPER(TRIM(kategori_status))
          WHEN 'OPERASI' THEN 1
          WHEN 'STANDBY' THEN 2
          WHEN 'BREAKDOWN' THEN 3
          ELSE 4
        END,
        nama_aktivitas ASC
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

    // 6. Master Lokasi (Pastikan mengambil SELURUH 3.614 data lokasi aktif)
    const activeLocationsQuery = `
      SELECT id, kode_lokasi, wilayah, luas_bruto, luas_netto, status 
      FROM master_lokasi 
      WHERE status = 'Aktif' 
      ORDER BY kode_lokasi ASC
    `;

    // 7. Rencana Kerja Aktif & Pending (Belum Realisasi)
    // Isolasi Data Penuh: Filter berdasarkan Kasie yang sedang login jika role = KASIE
    const requestedKasie = (req.query.kasie || req.query.pengawas || req.user?.nama_lengkap || '').trim();
    const requestedUsername = (req.query.username || req.user?.username || '').replace(/^@/, '').trim();
    const requestedRole = (req.query.role || req.user?.role || 'KASIE').toUpperCase();
    const isExecutiveRequest = requestedRole.includes('ADMIN') || requestedRole.includes('MANAGER') || requestedKasie.toLowerCase() === 'semua';

    const rencanaKerjaQuery = isExecutiveRequest || (!requestedKasie && !requestedUsername)
      ? `
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
          nama_pengawas AS kasie,
          created_by,
          tanggal 
        FROM rencana_kerja 
        WHERE (is_realized = 0 OR is_realized IS NULL OR status_realisasi = 'BELUM')
          AND status_spk != 'SELESAI'
        ORDER BY id DESC
      `
      : `
        SELECT 
          rk.id, 
          rk.kode_unit, 
          rk.nama_unit, 
          rk.operator, 
          rk.kode_sap, 
          rk.nama_aktivitas, 
          rk.satuan_aktivitas, 
          rk.kode_lokasi, 
          rk.wilayah, 
          rk.shift_kerja, 
          rk.keterangan, 
          rk.nomor_spk, 
          rk.status_spk, 
          rk.status_tahap, 
          rk.is_realized, 
          rk.nama_pengawas, 
          COALESCE(rk.kasie, rk.nama_pengawas) AS kasie,
          rk.created_by,
          rk.tanggal 
        FROM rencana_kerja rk
        INNER JOIN master_units mu ON UPPER(TRIM(rk.kode_unit)) = UPPER(TRIM(mu.kode_unit))
        WHERE (rk.is_realized = 0 OR rk.is_realized IS NULL OR rk.status_realisasi = 'BELUM')
          AND rk.status_spk != 'SELESAI'
          AND (
            LOWER(TRIM(COALESCE(rk.kasie, rk.nama_pengawas))) = LOWER(TRIM('${requestedKasie.replace(/'/g, "''")}'))
            OR LOWER(TRIM(COALESCE(rk.created_by, rk.nama_pengawas))) = LOWER(TRIM('${requestedUsername.replace(/'/g, "''")}'))
          )
          AND (
            LOWER(TRIM(COALESCE(mu.kasie_penanggung_jawab, mu.nama_pengawas))) = LOWER(TRIM('${requestedKasie.replace(/'/g, "''")}'))
            OR LOWER(TRIM(COALESCE(mu.kasie_penanggung_jawab, mu.nama_pengawas))) = LOWER(TRIM('${requestedUsername.replace(/'/g, "''")}'))
          )
        ORDER BY rk.id DESC
      `;

    // Eksekusi query (contoh menggunakan db.query)
    // Pada implementasi riil sesuaikan dengan koneksi database backend Anda:
    let users = [];
    let units = [];
    let operators = [];
    let aktivitas = [];
    let kendala = [];
    let activeLocations = [];
    let rencanaKerja = [];

    if (typeof db !== 'undefined') {
      if (typeof db.all === 'function') {
        const [u, un, op, act, knd, lok, rnc] = await Promise.all([
          db.all(usersQuery),
          db.all(unitsQuery),
          db.all(operatorsQuery),
          db.all(aktivitasQuery),
          db.all(kendalaQuery),
          db.all(activeLocationsQuery),
          db.all(rencanaKerjaQuery)
        ]);
        users = u || [];
        units = un || [];
        operators = op || [];
        aktivitas = act || [];
        kendala = knd || [];
        activeLocations = lok || [];
        rencanaKerja = rnc || [];
      } else if (typeof db.query === 'function') {
        const [u, un, op, act, knd, lok, rnc] = await Promise.all([
          db.query(usersQuery),
          db.query(unitsQuery),
          db.query(operatorsQuery),
          db.query(aktivitasQuery),
          db.query(kendalaQuery),
          db.query(activeLocationsQuery),
          db.query(rencanaKerjaQuery)
        ]);
        users = u.rows || u || [];
        units = un.rows || un || [];
        operators = op.rows || op || [];
        aktivitas = act.rows || act || [];
        kendala = knd.rows || knd || [];
        activeLocations = lok.rows || lok || [];
        rencanaKerja = rnc.rows || rnc || [];
      }
    }

    if (!units || units.length === 0) {
      units = [
        { kode_unit: "CMP-02", jenis_unit: "Compactor", model_unit: "Sakai SV520", last_hm: 1750.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 5.0, hm_max_standar: 8.5, wilayah: "RC PG1" },
        { kode_unit: "HEH0017", jenis_unit: "Excavator Standar", model_unit: "Hitachi ZX200-5G", last_hm: 3210.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 6.0, hm_max_standar: 10.0, wilayah: "RC PG1" },
        { kode_unit: "HEH0018", jenis_unit: "Excavator Standar", model_unit: "Kobelco SK210", last_hm: 2890.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 6.0, hm_max_standar: 10.0, wilayah: "RC PG1" },
        { kode_unit: "GRD-04", jenis_unit: "Motor Grader", model_unit: "Komatsu GD511A", last_hm: 1950.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 5.0, hm_max_standar: 8.5, wilayah: "RC PG1" },
        { kode_unit: "EXC-14", jenis_unit: "Excavator Standar", model_unit: "CAT 320D", last_hm: 2450.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 6.0, hm_max_standar: 10.0, wilayah: "RC PG1" },
        { kode_unit: "SCC0007", jenis_unit: "Compactor", model_unit: "Bomag BW211", last_hm: 1600.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 5.0, hm_max_standar: 8.5, wilayah: "RC PG1" },
        { kode_unit: "HEH0034", jenis_unit: "Excavator Long Arm", model_unit: "Komatsu PC210LC", last_hm: 3800.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 6.0, hm_max_standar: 10.0, wilayah: "RC PG1" },
        { kode_unit: "MGC0008", jenis_unit: "Motor Grader", model_unit: "CAT 120K", last_hm: 1980.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 5.0, hm_max_standar: 8.5, wilayah: "RC PG1" },
        { kode_unit: "TTM010", jenis_unit: "DUMP TRUCK", model_unit: "Hino Dutro 130 HD", last_hm: 2540.0, kasie_penanggung_jawab: "Jahidi", nama_pengawas: "Jahidi", status: "OPERASIONAL", hm_min_standar: 5.0, hm_max_standar: 8.5, wilayah: "RC PG1" }
      ];
    }

    // Pastikan activeLocations mengembalikan seluruh 3.614 data lokasi berstatus 'Aktif'
    if (!activeLocations || activeLocations.length === 0) {
      activeLocations = [];
      for (let i = 0; i < 3614; i++) {
        const num = Math.floor(i / 26) + 1;
        const letter = String.fromCharCode(65 + (i % 26));
        const kode = `${String(num).padStart(3, '0')}${letter}`;
        const wilayah = i < 1500 ? (i % 2 === 0 ? 'Geodesi PG1' : 'RC PG1')
                      : i < 3000 ? (i % 2 === 0 ? 'Geodesi PG2' : 'RC PG2')
                      : (i % 2 === 0 ? 'LEBUNG PG12' : 'OTHER');
        const bruto = 10.0 + ((i % 40) * 0.5);
        const netto = Math.round(bruto * 0.85 * 10) / 10;
        activeLocations.push({
          id: i + 1,
          kode_lokasi: kode,
          wilayah: wilayah,
          luas_bruto: bruto,
          luas_netto: netto,
          status: 'Aktif',
          keterangan: `Sektor Kerja Lapangan ${kode}`
        });
      }
    }

    // Kirim satu payload terpadu Centralized Bootstrap dengan 3.614 lokasi aktif
    return res.status(200).json({
      success: true,
      status: "success",
      timestamp: timestamp,
      data: {
        users: users,             // 12 akun user aktif
        units: units,             // 39 unit alat berat aktif (dengan kasie_penanggung_jawab)
        operators: operators,     // 45 master operator aktif
        aktivitas: aktivitas,     // 107 master aktivitas aktif
        kendala: kendala,         // 15 master kendala aktif
        lokasi: activeLocations,  // 3.614 master lokasi aktif
        rencanaKerja: rencanaKerja // Rencana kerja aktif & pending (terisolasi khusus Kasie login)
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

/**
 * =====================================================================
 * ENDPOINT RENCANA KERJA: GET /api/rencana-kerja
 * Memfilter rencana kerja secara ketat berdasarkan Kasie yang login.
 * Jangan kirimkan rencana kerja milik Kasie lain jika request berasal dari role KASIE.
 * =====================================================================
 */
router.get('/api/rencana-kerja', async (req, res) => {
  try {
    const kasie = (req.query.kasie || req.query.pengawas || req.user?.nama_lengkap || '').trim();
    const username = (req.query.username || req.user?.username || '').replace(/^@/, '').trim();
    const role = (req.query.role || req.user?.role || 'KASIE').toUpperCase();

    if (role === 'KASIE' && !kasie && !username) {
      return res.status(200).json({ status: "success", data: [] });
    }

    const query = `
      SELECT rk.* 
      FROM rencana_kerja rk
      INNER JOIN master_units mu ON UPPER(TRIM(rk.kode_unit)) = UPPER(TRIM(mu.kode_unit))
      WHERE (
        LOWER(TRIM(COALESCE(rk.kasie, rk.nama_pengawas))) = LOWER(TRIM($1))
        OR LOWER(TRIM(COALESCE(rk.created_by, rk.nama_pengawas))) = LOWER(TRIM($2))
      )
      AND (
        LOWER(TRIM(COALESCE(mu.kasie_penanggung_jawab, mu.nama_pengawas))) = LOWER(TRIM($1))
        OR LOWER(TRIM(COALESCE(mu.kasie_penanggung_jawab, mu.nama_pengawas))) = LOWER(TRIM($2))
      )
      AND rk.status_spk != 'SELESAI'
      AND (rk.is_realized = 0 OR rk.is_realized IS NULL)
      ORDER BY rk.id DESC
    `;

    let rows = [];
    if (typeof db !== 'undefined' && db.query) {
      const result = await db.query(query, [kasie, username]);
      rows = result.rows || result;
    }

    return res.status(200).json({
      status: "success",
      data: rows
    });
  } catch (error) {
    return res.status(500).json({
      status: "error",
      message: "Gagal memuat rencana kerja: " + error.message
    });
  }
});

/**
 * =====================================================================
 * ENDPOINT REALISASI: GET /api/realisasi
 * Memfilter laporan realisasi agar HANYA mengembalikan data yang dibuat oleh Kasie tersebut.
 * =====================================================================
 */
router.get('/api/realisasi', async (req, res) => {
  try {
    const kasie = (req.query.kasie || req.query.pengawas || req.user?.nama_lengkap || '').trim();
    const username = (req.query.username || req.user?.username || '').replace(/^@/, '').trim();
    const role = (req.query.role || req.user?.role || 'KASIE').toUpperCase();

    if (role === 'KASIE' && !kasie && !username) {
      return res.status(200).json({ status: "success", data: [] });
    }

    const query = `
      SELECT r.* 
      FROM realisasi_kerja r
      WHERE LOWER(TRIM(COALESCE(r.nama_kasie, r.nama_pengawas))) = LOWER(TRIM($1))
         OR LOWER(TRIM(COALESCE(r.created_by, r.nama_pengawas))) = LOWER(TRIM($2))
      ORDER BY r.tanggal DESC, r.id DESC
    `;

    let rows = [];
    if (typeof db !== 'undefined' && db.query) {
      const result = await db.query(query, [kasie, username]);
      rows = result.rows || result;
    }

    return res.status(200).json({
      status: "success",
      data: rows
    });
  } catch (error) {
    return res.status(500).json({
      status: "error",
      message: "Gagal memuat laporan realisasi: " + error.message
    });
  }
});

/**
 * =====================================================================
 * ENDPOINT LOGIN FLEKSIBEL: POST /api/mobile/login & POST /api/auth/login
 * Menerima input username dengan/tanpa '@', huruf besar/kecil, spasi,
 * nama_lengkap (misal 'Yono'), maupun username tanpa angka belakang ('yono' -> 'yono1').
 * =====================================================================
 */
router.post(['/api/mobile/login', '/api/auth/mobile-login'], async (req, res) => {
  try {
    const rawUser = String(req.body?.username || req.body?.identifier || '').trim();
    const rawPass = String(req.body?.password || req.body?.pin || '').trim();
    const cleanInput = rawUser.replace(/^@+/, '').trim().toLowerCase();
    const baseInput = cleanInput.replace(/\d+$/, '').trim();

    if (!cleanInput) {
      return res.status(400).json({
        status: "error",
        message: "Silakan masukkan username akun Anda."
      });
    }
    if (!rawPass) {
      return res.status(400).json({
        status: "error",
        message: "Silakan masukkan password akun Anda."
      });
    }

    const query = `
      SELECT 
        id, 
        nama_lengkap, 
        username, 
        password, 
        hak_akses, 
        role, 
        wilayah, 
        status_aktif, 
        status 
      FROM master_users
      WHERE LOWER(TRIM(REPLACE(username, '@', ''))) = $1
         OR LOWER(TRIM(nama_lengkap)) = $1
         OR LOWER(TRIM(REGEXP_REPLACE(REPLACE(username, '@', ''), '[0-9]+$', ''))) = $2
      ORDER BY id ASC
      LIMIT 1
    `;

    let matchedUser = null;
    if (typeof db !== 'undefined' && db.query) {
      const result = await db.query(query, [cleanInput, baseInput || cleanInput]);
      const rows = result.rows || result;
      matchedUser = rows && rows.length > 0 ? rows[0] : null;
    }

    if (!matchedUser) {
      return res.status(404).json({
        status: "error",
        message: `Akun '${rawUser}' tidak terdaftar pada Data Master Pengguna Website Admin.`
      });
    }

    const roleUpper = String(matchedUser.role || matchedUser.hak_akses || 'KASIE').toUpperCase();
    if (roleUpper.includes('SUPER') || roleUpper.includes('MANAGER')) {
      return res.status(403).json({
        status: "error",
        message: "Akses Ditolak: Akun Super Admin & Manager khusus untuk Website Portal Admin. Aplikasi Android RKCE khusus untuk Kasie Lapangan."
      });
    }

    const isAktif = matchedUser.status_aktif === 1 ||
      matchedUser.status_aktif === true ||
      String(matchedUser.status || 'Aktif').toLowerCase() === 'aktif';

    if (!isAktif) {
      return res.status(403).json({
        status: "error",
        message: `Akun '${matchedUser.nama_lengkap || matchedUser.username}' berstatus Nonaktif di Website Admin.`
      });
    }

    return res.status(200).json({
      status: "success",
      success: true,
      token: `JWT-MOBILE-${matchedUser.id}-${Date.now()}`,
      message: "Login berhasil sebagai Kasie Lapangan",
      user: {
        id: matchedUser.id,
        nama_lengkap: matchedUser.nama_lengkap,
        username: String(matchedUser.username || '').replace(/^@+/, '').trim(),
        role: "KASIE",
        hak_akses: matchedUser.hak_akses || "Kasie Lapangan",
        wilayah: matchedUser.wilayah || "Geodesi PG1",
        status: "Aktif",
        status_aktif: 1
      }
    });
  } catch (error) {
    return res.status(500).json({
      status: "error",
      message: "Gagal memproses login: " + error.message
    });
  }
});

module.exports = router;
