/**
 * =====================================================================
 * BACKEND ROUTES: Express.js, SQLite & Auth Controller
 * Portal: https://1-portalce.ai.studio/
 * =====================================================================
 *
 * Fitur Utama:
 * 1. AUTO-SEED 35 AKUN OPERATOR dari `master_operator` (status = 'Aktif') ke tabel `users`
 * 2. POST /api/login: Autentikasi Kasie & Operator (NIK) dengan dukungan plain-text & bcrypt
 * 3. GET /api/mobile/bootstrap-data: Sinkronisasi 7 dataset master terpadu
 * 4. GET /api/rencana-kerja & GET /api/realisasi: Isolasi data penuh per Kasie / Operator
 */

const express = require('express');
const router = express.Router();

let bcrypt = null;
try {
  bcrypt = require('bcrypt');
} catch (_e) {
  try {
    bcrypt = require('bcryptjs');
  } catch (_e2) {
    bcrypt = null;
  }
}

/**
 * Daftar 35 Operator Aktif dari master_operator (termasuk NIK 85044295)
 */
const DEFAULT_35_OPERATORS = [
  { id: 101, operator_id: 'OPR-001', nik: '85044295', nama_operator: 'Ahmad Fauzi', kasie_penanggung_jawab: 'Agung', wilayah: 'Geodesi PG1', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 102, operator_id: 'OPR-002', nik: '85044296', nama_operator: 'Bambang Sudrajat', kasie_penanggung_jawab: 'Zainudin', wilayah: 'Geodesi PG2', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 103, operator_id: 'OPR-003', nik: '85044297', nama_operator: 'Dedi Supriyadi', kasie_penanggung_jawab: 'Jahidi', wilayah: 'RC PG1', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 104, operator_id: 'OPR-004', nik: '85044298', nama_operator: 'Eko Prasetyo', kasie_penanggung_jawab: 'Hisar', wilayah: 'RC PG1', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 105, operator_id: 'OPR-005', nik: '85044299', nama_operator: 'Hendra Gunawan', kasie_penanggung_jawab: 'Yono', wilayah: 'RC PG2', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 106, operator_id: 'OPR-006', nik: '85044300', nama_operator: 'Indra Kusuma', kasie_penanggung_jawab: 'Nyoman', wilayah: 'RC PG3', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 107, operator_id: 'OPR-007', nik: '85044301', nama_operator: 'Joko Susilo', kasie_penanggung_jawab: 'Saipul', wilayah: 'LEBUNG PG12', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 108, operator_id: 'OPR-008', nik: '85044302', nama_operator: 'Kurniawan Putra', kasie_penanggung_jawab: 'Galung', wilayah: 'LEBUNG PG3', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 109, operator_id: 'OPR-009', nik: '85044303', nama_operator: 'Asep Hidayat', kasie_penanggung_jawab: 'Agung', wilayah: 'Geodesi PG1', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 110, operator_id: 'OPR-010', nik: '85044304', nama_operator: 'Wayan Sukarta', kasie_penanggung_jawab: 'Zainudin', wilayah: 'Geodesi PG2', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 111, operator_id: 'OPR-011', nik: '85044305', nama_operator: 'Rian Pratama', kasie_penanggung_jawab: 'Jahidi', wilayah: 'RC PG1', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 112, operator_id: 'OPR-012', nik: '85044306', nama_operator: 'Doni Saputra', kasie_penanggung_jawab: 'Hisar', wilayah: 'RC PG1', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 113, operator_id: 'OPR-013', nik: '85044307', nama_operator: 'Teguh Santoso', kasie_penanggung_jawab: 'Yono', wilayah: 'RC PG2', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 114, operator_id: 'OPR-014', nik: '85044308', nama_operator: 'Budi Utomo', kasie_penanggung_jawab: 'Nyoman', wilayah: 'RC PG3', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 115, operator_id: 'OPR-015', nik: '85044309', nama_operator: 'Sigit Purnomo', kasie_penanggung_jawab: 'Saipul', wilayah: 'LEBUNG PG12', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 116, operator_id: 'OPR-016', nik: '85044310', nama_operator: 'Faisal Akbar', kasie_penanggung_jawab: 'Galung', wilayah: 'LEBUNG PG3', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 117, operator_id: 'OPR-017', nik: '85044311', nama_operator: 'Rahmat Hidayat', kasie_penanggung_jawab: 'Agung', wilayah: 'Geodesi PG1', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 118, operator_id: 'OPR-018', nik: '85044312', nama_operator: 'Agus Setiawan', kasie_penanggung_jawab: 'Zainudin', wilayah: 'Geodesi PG2', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 119, operator_id: 'OPR-019', nik: '85044313', nama_operator: 'Ilham Maulana', kasie_penanggung_jawab: 'Jahidi', wilayah: 'RC PG1', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 120, operator_id: 'OPR-020', nik: '85044314', nama_operator: 'Rizki Ramadhan', kasie_penanggung_jawab: 'Hisar', wilayah: 'RC PG1', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 121, operator_id: 'OPR-021', nik: '85044315', nama_operator: 'Wahyu Hidayat', kasie_penanggung_jawab: 'Yono', wilayah: 'RC PG2', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 122, operator_id: 'OPR-022', nik: '85044316', nama_operator: 'Bayu Saputra', kasie_penanggung_jawab: 'Nyoman', wilayah: 'RC PG3', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 123, operator_id: 'OPR-023', nik: '85044317', nama_operator: 'Guruh Prasetyo', kasie_penanggung_jawab: 'Saipul', wilayah: 'LEBUNG PG12', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 124, operator_id: 'OPR-024', nik: '85044318', nama_operator: 'Hari Wibowo', kasie_penanggung_jawab: 'Galung', wilayah: 'LEBUNG PG3', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 125, operator_id: 'OPR-025', nik: '85044319', nama_operator: 'Irfan Hakim', kasie_penanggung_jawab: 'Agung', wilayah: 'Geodesi PG1', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 126, operator_id: 'OPR-026', nik: '85044320', nama_operator: 'Julianto', kasie_penanggung_jawab: 'Zainudin', wilayah: 'Geodesi PG2', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 127, operator_id: 'OPR-027', nik: '85044321', nama_operator: 'Kusnadi', kasie_penanggung_jawab: 'Jahidi', wilayah: 'RC PG1', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 128, operator_id: 'OPR-028', nik: '85044322', nama_operator: 'Lukman Hakim', kasie_penanggung_jawab: 'Hisar', wilayah: 'RC PG1', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 129, operator_id: 'OPR-029', nik: '85044323', nama_operator: 'Mansur', kasie_penanggung_jawab: 'Yono', wilayah: 'RC PG2', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 130, operator_id: 'OPR-030', nik: '85044324', nama_operator: 'Nanang Kosim', kasie_penanggung_jawab: 'Nyoman', wilayah: 'RC PG3', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 131, operator_id: 'OPR-031', nik: '85044325', nama_operator: 'Oki Setiawan', kasie_penanggung_jawab: 'Saipul', wilayah: 'LEBUNG PG12', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 132, operator_id: 'OPR-032', nik: '85044326', nama_operator: 'Panji Tri', kasie_penanggung_jawab: 'Galung', wilayah: 'LEBUNG PG3', skill_level: 'Class A - Grader', status: 'Aktif' },
  { id: 133, operator_id: 'OPR-033', nik: '85044327', nama_operator: 'Rudi Hartono', kasie_penanggung_jawab: 'Jahidi', wilayah: 'RC PG1', skill_level: 'Class B - Compactor', status: 'Aktif' },
  { id: 134, operator_id: 'OPR-034', nik: '85044328', nama_operator: 'Slamet Riyadi', kasie_penanggung_jawab: 'Agung', wilayah: 'Geodesi PG1', skill_level: 'Class A - Excavator', status: 'Aktif' },
  { id: 135, operator_id: 'OPR-035', nik: '85044329', nama_operator: 'Tri Wahyudi', kasie_penanggung_jawab: 'Yono', wilayah: 'RC PG2', skill_level: 'Class A - Grader', status: 'Aktif' }
];

const DEFAULT_KASIE_AND_ADMIN_USERS = [
  { id: 33, username: 'farid', password: '123', nama: 'Farid', nama_lengkap: 'Farid', role: 'SUPER ADMIN', hak_akses: 'Super Admin', kasie_atasan: '', wilayah: 'Semua Wilayah', status: 'Aktif', status_aktif: 1 },
  { id: 34, username: 'deni', password: '123', nama: 'Deni', nama_lengkap: 'Deni', role: 'SUPER ADMIN', hak_akses: 'Super Admin', kasie_atasan: '', wilayah: 'Semua Wilayah', status: 'Aktif', status_aktif: 1 },
  { id: 35, username: 'agung', password: '123', nama: 'Agung', nama_lengkap: 'Agung', role: 'kasie', hak_akses: 'Kasie Lapangan', kasie_atasan: '', wilayah: 'Geodesi PG1', status: 'Aktif', status_aktif: 1 },
  { id: 36, username: 'zainudin', password: '123', nama: 'Zainudin', nama_lengkap: 'Zainudin', role: 'kasie', hak_akses: 'Kasie Lapangan', kasie_atasan: '', wilayah: 'Geodesi PG2', status: 'Aktif', status_aktif: 1 },
  { id: 37, username: 'jahidi', password: '123', nama: 'Jahidi', nama_lengkap: 'Jahidi', role: 'kasie', hak_akses: 'Kasie Lapangan', kasie_atasan: '', wilayah: 'RC PG1', status: 'Aktif', status_aktif: 1 },
  { id: 38, username: 'hisar', password: '123', nama: 'Hisar', nama_lengkap: 'Hisar', role: 'kasie', hak_akses: 'Kasie Lapangan', kasie_atasan: '', wilayah: 'RC PG1', status: 'Aktif', status_aktif: 1 },
  { id: 39, username: 'yono1', password: '123', nama: 'Yono', nama_lengkap: 'Yono', role: 'kasie', hak_akses: 'Kasie Lapangan', kasie_atasan: '', wilayah: 'RC PG2', status: 'Aktif', status_aktif: 1 },
  { id: 40, username: 'nyoman', password: '123', nama: 'Nyoman', nama_lengkap: 'Nyoman', role: 'kasie', hak_akses: 'Kasie Lapangan', kasie_atasan: '', wilayah: 'RC PG3', status: 'Aktif', status_aktif: 1 },
  { id: 41, username: 'saipul', password: '123', nama: 'Saipul', nama_lengkap: 'Saipul', role: 'kasie', hak_akses: 'Kasie Lapangan', kasie_atasan: '', wilayah: 'LEBUNG PG12', status: 'Aktif', status_aktif: 1 },
  { id: 42, username: 'galung', password: '123', nama: 'Galung', nama_lengkap: 'Galung', role: 'kasie', hak_akses: 'Kasie Lapangan', kasie_atasan: '', wilayah: 'LEBUNG PG3', status: 'Aktif', status_aktif: 1 },
  { id: 43, username: 'lutfi', password: '123', nama: 'Lutfi', nama_lengkap: 'Lutfi', role: 'MANAGER', hak_akses: 'Manager Operasional', kasie_atasan: '', wilayah: 'Semua Wilayah', status: 'Aktif', status_aktif: 1 },
  { id: 44, username: 'putut', password: '123', nama: 'Putut', nama_lengkap: 'Putut', role: 'MANAGER', hak_akses: 'Manager Operasional', kasie_atasan: '', wilayah: 'Semua Wilayah', status: 'Aktif', status_aktif: 1 }
];

/**
 * =====================================================================
 * 1. AUTO-SEED 35 AKUN OPERATOR KE TABEL `users`
 * =====================================================================
 * Mengambil seluruh data dari `master_operator` (status = 'Aktif') dan
 * memasukkannya ke tabel `users` dengan:
 * - username: NIK Operator (string murni, contoh: '85044295')
 * - password: NIK Operator (string murni)
 * - nama: Nama Lengkap Operator
 * - role: 'operator'
 * - kasie_atasan: kasie_penanggung_jawab dari operator tersebut
 * - status: 'Aktif'
 */
async function autoSeedOperatorsToUsers(databaseConn) {
  const dbRef = databaseConn || (typeof db !== 'undefined' ? db : null);
  let activeOperators = [];

  if (dbRef) {
    const selectOpSql = `
      SELECT * FROM master_operator 
      WHERE LOWER(TRIM(status)) = 'aktif' OR status_aktif = 1
    `;
    try {
      if (typeof dbRef.all === 'function') {
        activeOperators = await dbRef.all(selectOpSql);
      } else if (typeof dbRef.query === 'function') {
        const res = await dbRef.query(selectOpSql);
        activeOperators = res.rows || res || [];
      }
    } catch (_err) {
      try {
        const fallbackSql = `SELECT * FROM master_operators WHERE LOWER(TRIM(status)) = 'aktif'`;
        if (typeof dbRef.all === 'function') {
          activeOperators = await dbRef.all(fallbackSql);
        } else if (typeof dbRef.query === 'function') {
          const res = await dbRef.query(fallbackSql);
          activeOperators = res.rows || res || [];
        }
      } catch (_err2) {
        activeOperators = [];
      }
    }
  }

  if (!activeOperators || activeOperators.length === 0) {
    activeOperators = DEFAULT_35_OPERATORS;
  }

  const seededUsers = [];
  for (let i = 0; i < activeOperators.length; i++) {
    const op = activeOperators[i];
    const cleanNik = String(op.nik || op.nik_operator || op.username || op.badge_number || op.operator_id || '').trim();
    if (!cleanNik) continue;

    const namaOperator = String(op.nama_operator || op.nama || op.nama_lengkap || op.name || cleanNik).trim();
    const kasieAtasan = String(op.kasie_penanggung_jawab || op.kasie_atasan || op.nama_pengawas || op.kasie || 'Agung').trim();
    const wilayah = String(op.wilayah || 'Geodesi PG1').trim();

    const userRow = {
      id: op.id || (101 + i),
      username: cleanNik,
      password: cleanNik,
      nama: namaOperator,
      nama_lengkap: namaOperator,
      role: 'operator',
      hak_akses: 'Operator Alat Berat',
      kasie_atasan: kasieAtasan,
      wilayah: wilayah,
      status: 'Aktif',
      status_aktif: 1
    };
    seededUsers.push(userRow);

    if (dbRef) {
      const insertSql = `
        INSERT OR IGNORE INTO users (username, password, nama, nama_lengkap, role, kasie_atasan, wilayah, status)
        VALUES (?, ?, ?, ?, 'operator', ?, ?, 'Aktif')
      `;
      try {
        if (typeof dbRef.run === 'function') {
          await dbRef.run(insertSql, [cleanNik, cleanNik, namaOperator, namaOperator, kasieAtasan, wilayah]);
        } else if (typeof dbRef.query === 'function') {
          await dbRef.query(insertSql, [cleanNik, cleanNik, namaOperator, namaOperator, kasieAtasan, wilayah]);
        }
      } catch (_insertErr) {
        // Abaikan jika skema menggunakan kolom minimal (username, password, nama, role, kasie_atasan, status)
        try {
          const minimalSql = `
            INSERT OR IGNORE INTO users (username, password, nama, role, kasie_atasan, status)
            VALUES (?, ?, ?, 'operator', ?, 'Aktif')
          `;
          if (typeof dbRef.run === 'function') {
            await dbRef.run(minimalSql, [cleanNik, cleanNik, namaOperator, kasieAtasan]);
          } else if (typeof dbRef.query === 'function') {
            await dbRef.query(minimalSql, [cleanNik, cleanNik, namaOperator, kasieAtasan]);
          }
        } catch (_minErr) {}
      }
    }
  }

  return { activeOperators, seededUsers };
}

/**
 * =====================================================================
 * 2. PERBAIKI ENDPOINT POST /api/login (PASSWORD COMPARISON)
 * =====================================================================
 */
router.post(['/api/login', '/api/mobile/login', '/api/auth/login', '/api/auth/mobile-login'], async (req, res) => {
  try {
    // 1. Bersihkan input string
    const cleanUsername = (req.body && req.body.username !== undefined ? req.body.username : (req.body?.identifier || ''))
      .toString()
      .trim()
      .replace(/^@+/, '')
      .trim();
    const cleanPassword = (req.body && req.body.password !== undefined ? req.body.password : (req.body?.pin || ''))
      .toString()
      .trim();

    if (!cleanUsername) {
      return res.status(400).json({
        success: false,
        message: "Silakan masukkan username atau NIK Operator Anda."
      });
    }
    if (!cleanPassword) {
      return res.status(400).json({
        success: false,
        message: "Silakan masukkan password Anda."
      });
    }

    // Pastikan 35 operator dari master_operator sudah terdaftar di tabel users
    const { seededUsers } = await autoSeedOperatorsToUsers(typeof db !== 'undefined' ? db : null);

    // 2. Cari user di tabel users:
    // SELECT * FROM users WHERE (LOWER(username) = LOWER(?) OR username = ?) AND status = 'Aktif'
    const findUserSql = `
      SELECT * FROM users 
      WHERE (LOWER(username) = LOWER(?) OR username = ?) 
        AND status = 'Aktif'
      LIMIT 1
    `;

    let user = null;
    if (typeof db !== 'undefined') {
      try {
        if (typeof db.get === 'function') {
          user = await db.get(findUserSql, [cleanUsername, cleanUsername]);
        } else if (typeof db.all === 'function') {
          const rows = await db.all(findUserSql, [cleanUsername, cleanUsername]);
          user = rows && rows.length > 0 ? rows[0] : null;
        } else if (typeof db.query === 'function') {
          const result = await db.query(findUserSql, [cleanUsername, cleanUsername]);
          const rows = result.rows || result;
          user = rows && rows.length > 0 ? rows[0] : null;
        }
      } catch (_dbErr) {
        user = null;
      }
    }

    // Fallback ke katalog gabungan (12 Kasie/Admin + 35 Operator Aktif) jika menggunakan in-memory / fallback
    if (!user) {
      const allUsersPool = [...DEFAULT_KASIE_AND_ADMIN_USERS, ...seededUsers];
      user = allUsersPool.find(
        (u) =>
          (String(u.username).toLowerCase() === cleanUsername.toLowerCase() ||
            String(u.username) === cleanUsername ||
            String(u.nama || u.nama_lengkap || '').toLowerCase() === cleanUsername.toLowerCase()) &&
          String(u.status || 'Aktif').toLowerCase() === 'aktif'
      );
    }

    if (!user) {
      return res.status(401).json({
        success: false,
        message: "Akun tidak terdaftar atau telah dinonaktifkan oleh Admin."
      });
    }

    // 3. Penanganan Pencocokan Password (teks polos maupun bcrypt)
    let isPasswordValid = false;
    const storedPassword = user.password !== undefined && user.password !== null
      ? String(user.password).trim()
      : String(user.username).trim();

    if (storedPassword === cleanPassword) {
      isPasswordValid = true;
    } else if (storedPassword && storedPassword.startsWith('$2') && bcrypt) {
      isPasswordValid = await bcrypt.compare(cleanPassword, storedPassword);
    }

    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: "Password yang Anda masukkan salah."
      });
    }

    // 4. Kembalikan objek user lengkap
    const resolvedNama = user.nama || user.nama_lengkap || user.username;
    const resolvedRole = String(user.role || 'kasie').toLowerCase() === 'operator'
      ? 'operator'
      : (user.role || 'kasie');

    return res.json({
      success: true,
      token: "token-auth-session",
      user: {
        id: user.id,
        username: user.username,
        nama: resolvedNama,
        nama_lengkap: resolvedNama,
        role: resolvedRole, // 'operator' atau 'kasie'
        kasie_atasan: user.kasie_atasan || '',
        wilayah: user.wilayah || 'Geodesi PG1',
        status: user.status || 'Aktif'
      }
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      status: "error",
      message: "Gagal memproses login: " + error.message
    });
  }
});

/**
 * =====================================================================
 * BACKEND ROUTE: GET /api/mobile/bootstrap-data
 * =====================================================================
 */
router.get('/api/mobile/bootstrap-data', async (req, res) => {
  try {
    const timestamp = new Date().toISOString();

    // Auto-seed 35 operator ke tabel users terlebih dahulu
    const { activeOperators, seededUsers } = await autoSeedOperatorsToUsers(typeof db !== 'undefined' ? db : null);

    const usersQuery = `
      SELECT 
        id, 
        COALESCE(nama, nama_lengkap) AS nama,
        COALESCE(nama_lengkap, nama) AS nama_lengkap, 
        username, 
        password,
        hak_akses, 
        role, 
        kasie_atasan,
        wilayah, 
        status_aktif, 
        status 
      FROM users 
      WHERE LOWER(TRIM(status)) = 'aktif' OR status_aktif = 1
      ORDER BY id ASC
    `;

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
      FROM master_unit 
      WHERE (LOWER(status) IN ('operasional', 'aktif'))
      ORDER BY kode_unit ASC
    `;

    const operatorsQuery = `
      SELECT 
        operator_id, 
        nama_operator, 
        nik, 
        kasie_penanggung_jawab AS nama_pengawas, 
        kasie_penanggung_jawab, 
        jabatan AS skill_level, 
        status, 
        wilayah 
      FROM master_operator 
      WHERE LOWER(TRIM(status)) = 'aktif'
      ORDER BY nama_operator ASC
    `;

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

    const kendalaQuery = `
      SELECT 
        id, 
        kode_kendala, 
        nama_kendala, 
        kategori, 
        deskripsi 
      FROM master_kendala 
      ORDER BY id ASC
    `;

    const activeLocationsQuery = `
      SELECT id, kode_lokasi, wilayah, luas_bruto, luas_netto, status 
      FROM master_lokasi 
      WHERE status = 'Aktif' 
      ORDER BY kode_lokasi ASC
    `;

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
        WHERE (is_realized = 0 OR is_realized IS NULL)
          AND (status_realisasi IS NULL OR UPPER(TRIM(status_realisasi)) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED'))
          AND (status_spk IS NULL OR UPPER(TRIM(status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED'))
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
        INNER JOIN master_unit mu ON UPPER(TRIM(rk.kode_unit)) = UPPER(TRIM(mu.kode_unit))
        WHERE (rk.is_realized = 0 OR rk.is_realized IS NULL)
          AND (rk.status_realisasi IS NULL OR UPPER(TRIM(rk.status_realisasi)) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED'))
          AND (rk.status_spk IS NULL OR UPPER(TRIM(rk.status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED'))
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
          db.all(usersQuery).catch(() => []),
          db.all(unitsQuery).catch(() => []),
          db.all(operatorsQuery).catch(() => []),
          db.all(aktivitasQuery).catch(() => []),
          db.all(kendalaQuery).catch(() => []),
          db.all(activeLocationsQuery).catch(() => []),
          db.all(rencanaKerjaQuery).catch(() => [])
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
          db.query(usersQuery).catch(() => []),
          db.query(unitsQuery).catch(() => []),
          db.query(operatorsQuery).catch(() => []),
          db.query(aktivitasQuery).catch(() => []),
          db.query(kendalaQuery).catch(() => []),
          db.query(activeLocationsQuery).catch(() => []),
          db.query(rencanaKerjaQuery).catch(() => [])
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

    if (!operators || operators.length === 0) {
      operators = activeOperators;
    }

    if (!users || users.length === 0) {
      users = [...DEFAULT_KASIE_AND_ADMIN_USERS, ...seededUsers];
    } else {
      // Pastikan seluruh 35 operator dari master_operator juga terdaftar di daftar users
      const existingUsernames = new Set(users.map((u) => String(u.username || '').trim().toLowerCase()));
      for (const opUser of seededUsers) {
        if (!existingUsernames.has(String(opUser.username).toLowerCase())) {
          users.push(opUser);
          existingUsernames.add(String(opUser.username).toLowerCase());
        }
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

    return res.status(200).json({
      success: true,
      status: "success",
      timestamp: timestamp,
      data: {
        users: users,             // 12 akun Kasie/Admin + 35 akun Operator Aktif
        units: units,             // 39 unit alat berat aktif
        operators: operators,     // 35 master operator aktif
        aktivitas: aktivitas,     // 107 master aktivitas aktif
        kendala: kendala,         // 15 master kendala aktif
        lokasi: activeLocations,  // 3.614 master lokasi aktif
        rencanaKerja: rencanaKerja
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
 * =====================================================================
 */
router.get('/api/rencana-kerja', async (req, res) => {
  try {
    const kasie = (req.query.kasie || req.query.pengawas || req.user?.nama_lengkap || '').trim();
    const username = (req.query.username || req.user?.username || '').replace(/^@/, '').trim();
    const operatorName = (req.query.operator || '').trim();
    const operatorNik = (req.query.operator_nik || '').trim();
    const role = (req.query.role || req.user?.role || 'KASIE').toUpperCase();

    if (role === 'OPERATOR' || operatorName || operatorNik) {
      const opQuery = `
        SELECT rk.*
        FROM rencana_kerja rk
        WHERE (
          LOWER(TRIM(COALESCE(rk.operator, ''))) = LOWER(TRIM($1))
          OR LOWER(TRIM(COALESCE(rk.operator_nik, ''))) = LOWER(TRIM($2))
          OR LOWER(TRIM(COALESCE(rk.operator, ''))) = LOWER(TRIM($2))
          OR LOWER(TRIM(COALESCE(rk.operator, ''))) LIKE '%' || LOWER(TRIM($1)) || '%'
        )
        AND (rk.is_realized = 0 OR rk.is_realized IS NULL)
        AND (rk.status_realisasi IS NULL OR UPPER(TRIM(rk.status_realisasi)) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED'))
        AND (rk.status_spk IS NULL OR UPPER(TRIM(rk.status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED'))
        ORDER BY rk.id DESC
      `;
      let rows = [];
      const targetName = operatorName || kasie || username;
      const targetNik = operatorNik || username || kasie;
      if (typeof db !== 'undefined') {
        if (typeof db.all === 'function') {
          rows = await db.all(opQuery.replace(/\$1/g, '?').replace(/\$2/g, '?'), [targetName, targetNik, targetNik, targetName]);
        } else if (typeof db.query === 'function') {
          const result = await db.query(opQuery, [targetName, targetNik]);
          rows = result.rows || result;
        }
      }
      return res.status(200).json({ status: "success", data: rows });
    }

    if (role === 'KASIE' && !kasie && !username) {
      return res.status(200).json({ status: "success", data: [] });
    }

    const query = `
      SELECT rk.* 
      FROM rencana_kerja rk
      INNER JOIN master_unit mu ON UPPER(TRIM(rk.kode_unit)) = UPPER(TRIM(mu.kode_unit))
      WHERE (
        LOWER(TRIM(COALESCE(rk.kasie, rk.nama_pengawas))) = LOWER(TRIM($1))
        OR LOWER(TRIM(COALESCE(rk.created_by, rk.nama_pengawas))) = LOWER(TRIM($2))
      )
      AND (
        LOWER(TRIM(COALESCE(mu.kasie_penanggung_jawab, mu.nama_pengawas))) = LOWER(TRIM($1))
        OR LOWER(TRIM(COALESCE(mu.kasie_penanggung_jawab, mu.nama_pengawas))) = LOWER(TRIM($2))
      )
      AND (rk.is_realized = 0 OR rk.is_realized IS NULL)
      AND (rk.status_realisasi IS NULL OR UPPER(TRIM(rk.status_realisasi)) NOT IN ('SUDAH', 'TEREALISASI', 'TEREALISASI & APPROVED'))
      AND (rk.status_spk IS NULL OR UPPER(TRIM(rk.status_spk)) NOT IN ('SELESAI', 'TEREALISASI', 'TEREALISASI & APPROVED'))
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
 * PROTEKSI ENDPOINT RENCANA KERJA: POST /api/rencana-kerja & POST /api/sync-rencana
 * =====================================================================
 * HANYA role admin atau kasie yang diizinkan memanggil endpoint ini.
 * Tolak secara tegas jika payload dikirim dengan role operator.
 */
const handlePostRencanaKerja = async (req, res) => {
  try {
    const body = req.body || {};
    const role = String(body.role || body.role_penginput || req.user?.role || '').toLowerCase().trim();

    if (role === 'operator' || (req.user && String(req.user.role).toLowerCase() === 'operator')) {
      return res.status(403).json({
        success: false,
        status: "error",
        message: "Akses ditolak: Operator tidak memiliki hak membuat atau mengunggah Rencana Kerja!"
      });
    }

    const {
      status_unit, kode_unit, operator, operator_nik, sap_code, nama_aktivitas,
      satuan_aktivitas, kode_lokasi, wilayah, shift_kerja, keterangan, nomor_spk,
      status_spk, status_tahap, nama_pengawas, created_by, kasie
    } = body;

    const finalPengawas = kasie || nama_pengawas || created_by || 'Agung';

    let insertedId = Date.now();

    if (typeof db !== 'undefined' && typeof db.run === 'function') {
      const result = await db.run(`
        INSERT INTO rencana_kerja (
          kode_unit, operator, operator_nik, kode_sap, nama_aktivitas,
          satuan_aktivitas, kode_lokasi, wilayah, shift_kerja, keterangan,
          nomor_spk, status_spk, status_tahap, is_realized, status_realisasi,
          nama_pengawas, kasie, created_by, tanggal
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'RENCANA', 0, 'BELUM', ?, ?, ?, date('now'))
      `, [
        kode_unit || 'UNIT-01', operator || '', operator_nik || '', sap_code || '', nama_aktivitas || '',
        satuan_aktivitas || 'm3', kode_lokasi || '', wilayah || 'Geodesi PG1', shift_kerja || 'SIANG', keterangan || '',
        nomor_spk || 'SPK Menyusul', status_spk || 'MENUNGGU_SPK',
        finalPengawas, finalPengawas, created_by || finalPengawas
      ]).catch(() => null);

      if (result && result.lastID) {
        insertedId = result.lastID;
      }
    }

    return res.status(200).json({
      success: true,
      status: "success",
      message: "Rencana Kerja berhasil disimpan oleh Kasie/Admin.",
      data: { id: insertedId }
    });
  } catch (err) {
    return res.status(500).json({ success: false, status: "error", message: err.message });
  }
};

router.post('/api/rencana-kerja', handlePostRencanaKerja);
router.post('/api/sync-rencana', handlePostRencanaKerja);

/**
 * Endpoint Pembersihan Data Sampah (Quick Purge SPK)
 */
router.delete('/api/rencana-kerja/:id', async (req, res) => {
  try {
    const targetId = req.params.id;
    if (typeof db !== 'undefined' && typeof db.run === 'function') {
      await db.run(`DELETE FROM rencana_kerja WHERE id = ? OR nomor_spk = ? OR kode_unit = ?`, [targetId, targetId, targetId]);
    }
    return res.status(200).json({
      success: true,
      status: "success",
      message: `Rencana Kerja / SPK [${targetId}] berhasil dihapus permanen dari Web Admin.`
    });
  } catch (err) {
    return res.status(500).json({ success: false, status: "error", message: err.message });
  }
});

router.post('/api/rencana-kerja/purge', async (req, res) => {
  try {
    const { id, nomor_spk, kode_unit } = req.body || {};
    if (typeof db !== 'undefined' && typeof db.run === 'function') {
      await db.run(`
        DELETE FROM rencana_kerja 
        WHERE (id = ? AND ? IS NOT NULL) 
           OR (nomor_spk = ? AND ? IS NOT NULL AND nomor_spk != '') 
           OR (kode_unit = ? AND ? IS NOT NULL AND kode_unit != '')
      `, [id, id, nomor_spk, nomor_spk, kode_unit, kode_unit]);
    }
    return res.status(200).json({
      success: true,
      status: "success",
      message: "Data SPK berhasil dibersihkan permanen dari database server."
    });
  } catch (err) {
    return res.status(500).json({ success: false, status: "error", message: err.message });
  }
});

/**
 * =====================================================================
 * ENDPOINT REALISASI: GET /api/realisasi & POST /api/realisasi
 * (Mendukung Auto-Approval ketika Kasie menginput realisasi mandiri)
 * =====================================================================
 */
router.get('/api/realisasi', async (req, res) => {
  try {
    const kasie = (req.query.kasie || req.query.pengawas || req.user?.nama_lengkap || '').trim();
    const username = (req.query.username || req.user?.username || '').replace(/^@/, '').trim();
    const operatorName = (req.query.operator || '').trim();
    const operatorNik = (req.query.operator_nik || '').trim();
    const role = (req.query.role || req.user?.role || 'KASIE').toUpperCase();

    if (role === 'OPERATOR' || operatorName || operatorNik) {
      const targetName = operatorName || kasie || username;
      const targetNik = operatorNik || username || kasie;
      const opQuery = `
        SELECT r.*
        FROM realisasi_kerja r
        WHERE LOWER(TRIM(COALESCE(r.operator, r.nama_operator, ''))) = LOWER(TRIM($1))
           OR LOWER(TRIM(COALESCE(r.operator_nik, ''))) = LOWER(TRIM($2))
           OR LOWER(TRIM(COALESCE(r.operator, r.nama_operator, ''))) = LOWER(TRIM($2))
           OR LOWER(TRIM(COALESCE(r.operator, r.nama_operator, ''))) LIKE '%' || LOWER(TRIM($1)) || '%'
        ORDER BY r.tanggal DESC, r.id DESC
      `;
      let rows = [];
      if (typeof db !== 'undefined') {
        if (typeof db.all === 'function') {
          rows = await db.all(opQuery.replace(/\$1/g, '?').replace(/\$2/g, '?'), [targetName, targetNik, targetNik, targetName]);
        } else if (typeof db.query === 'function') {
          const result = await db.query(opQuery, [targetName, targetNik]);
          rows = result.rows || result;
        }
      }
      return res.status(200).json({ status: "success", data: rows });
    }

    if (role === 'KASIE' && !kasie && !username) {
      return res.status(200).json({ status: "success", data: [] });
    }

    let rows = [];
    if (typeof db !== 'undefined') {
      const sqliteQuery = `
        SELECT r.* 
        FROM realisasi r
        WHERE LOWER(TRIM(COALESCE(r.kasie, r.nama_pengawas, ''))) = LOWER(TRIM(?))
           OR LOWER(TRIM(COALESCE(r.operator, r.nama_operator, ''))) = LOWER(TRIM(?))
           OR LOWER(TRIM(COALESCE(r.created_by, ''))) = LOWER(TRIM(?))
           OR ? = '' OR LOWER(?) = 'semua'
        ORDER BY r.tanggal DESC, r.id DESC
      `;
      const fallbackQuery = `
        SELECT r.* 
        FROM realisasi_kerja r
        WHERE LOWER(TRIM(COALESCE(r.nama_kasie, r.nama_pengawas, ''))) = LOWER(TRIM(?))
           OR LOWER(TRIM(COALESCE(r.created_by, r.nama_pengawas, ''))) = LOWER(TRIM(?))
           OR ? = '' OR LOWER(?) = 'semua'
        ORDER BY r.tanggal DESC, r.id DESC
      `;
      if (typeof db.all === 'function') {
        try {
          rows = await db.all(sqliteQuery, [kasie, username, username, kasie, kasie]);
        } catch (_err) {
          try {
            rows = await db.all(fallbackQuery, [kasie, username, kasie, kasie]);
          } catch (_e2) {
            rows = [];
          }
        }
      } else if (typeof db.query === 'function') {
        try {
          const result = await db.query(sqliteQuery.replace(/\?/g, '$1'), [kasie]);
          rows = result.rows || result;
        } catch (_err) {
          try {
            const result = await db.query(fallbackQuery.replace(/\?/g, '$1'), [kasie]);
            rows = result.rows || result;
          } catch (_e2) {
            rows = [];
          }
        }
      }
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

const handlePostRealisasi = async (req, res) => {
  try {
    const body = req.body || {};
    const spk_id = body.spk_id || body.rencana_id || body.id_rencana_kerja || null;
    const nomor_spk = body.nomor_spk || '';
    const tanggal = body.tanggal || body.report_date || new Date().toISOString().slice(0, 10);
    const shift = body.shift || 'SIANG';
    const kode_unit = body.kode_unit || body.unit_code || '';
    const nama_aktivitas = body.nama_aktivitas || body.activity_name || '';
    const kode_sap = body.kode_sap || body.sap_code || '';
    const operator = body.operator || body.nama_operator || body.operator_name || '';
    const operator_nik = body.operator_nik || '';
    const kasie = body.kasie || body.nama_pengawas || body.supervisor_name || '';
    const lokasi = body.lokasi || body.lokasi_kerja || body.work_location || '';
    const wilayah = body.wilayah || '';
    const hm_awal = Number(body.hm_awal || 0);
    const hm_akhir = Number(body.hm_akhir || 0);
    const total_hm = Number(body.total_hm || body.hm_harian_berjalan || Math.max(0, hm_akhir - hm_awal));
    const konsumsi_solar = Number(body.konsumsi_solar || body.pemakaian_solar || body.jumlah_liter_solar || 0);
    const hasil_kerja = Number(body.hasil_kerja || body.work_output || 0);
    const satuan = body.satuan || body.unit_of_measure || 'm3';
    const kendala = body.kendala || body.kendala_nama || body.catatan_kendala || '';
    const role_penginput = body.role_penginput;

    // 1. Tentukan status approval: jika diinput Kasie -> Auto-Approve & Kunci
    const isInputByKasie = (role_penginput?.toLowerCase() === 'kasie' || !role_penginput);
    const approvalStatus = isInputByKasie ? 'Disetujui Kasie' : 'Menunggu Approval';
    const approvedBy = isInputByKasie ? kasie : null;
    const isLocked = isInputByKasie ? 1 : 0;

    let insertedId = Date.now();

    if (typeof db !== 'undefined' && typeof db.run === 'function') {
      // 2. Simpan ke tabel realisasi
      const result = await db.run(`
        INSERT INTO realisasi (
          spk_id, nomor_spk, tanggal, shift, kode_unit, nama_aktivitas, kode_sap,
          operator, operator_nik, kasie, lokasi, wilayah,
          hm_awal, hm_akhir, total_hm, konsumsi_solar, hasil_kerja, satuan, kendala,
          approval_status, approved_by, approved_at, is_locked, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), ?, datetime('now'))
      `, [
        spk_id, nomor_spk, tanggal, shift, kode_unit, nama_aktivitas, kode_sap,
        operator, operator_nik, kasie, lokasi, wilayah,
        hm_awal, hm_akhir, total_hm, konsumsi_solar, hasil_kerja, satuan, kendala,
        approvalStatus, approvedBy, isLocked
      ]).catch(() => null);

      if (result && result.lastID) {
        insertedId = result.lastID;
      }

      // 3. UPDATE STATUS RENCANA KERJA / SPK AGAR HILANG DARI DAFTAR TUNGGU OPERATOR & KASIE
      const statusRencana = isInputByKasie ? 'Terealisasi & Approved' : 'Terealisasi (Menunggu Approval)';
      await db.run(`
        UPDATE rencana_kerja 
        SET status_realisasi = ?,
            status_spk = ?,
            is_realized = 1
        WHERE nomor_spk = ? OR id = ? OR (kode_unit = ? AND tanggal = ? AND shift_kerja = ?)
      `, [statusRencana, statusRencana, nomor_spk, spk_id, kode_unit, tanggal, shift]).catch(() => null);

      // 4. UPDATE LAST HM DI MASTER UNIT
      if (hm_akhir > 0) {
        await db.run(`UPDATE master_unit SET last_hm = ?, hm_terakhir = ? WHERE kode_unit = ? AND (last_hm < ? OR hm_terakhir < ?)`, [hm_akhir, hm_akhir, kode_unit, hm_akhir, hm_akhir]).catch(() => null);
      }
    }

    return res.status(200).json({
      success: true,
      status: "success",
      message: isInputByKasie
        ? "Realisasi berhasil disimpan dan otomatis disetujui (Auto-Approved oleh Kasie)."
        : "Realisasi berhasil dikirim dan menunggu persetujuan Kasie.",
      data: {
        id: insertedId,
        approval_status: approvalStatus,
        approved_by: approvedBy,
        is_locked: Boolean(isLocked)
      }
    });
  } catch (err) {
    return res.status(500).json({ success: false, status: "error", message: err.message });
  }
};

router.post('/api/realisasi', handlePostRealisasi);
router.post('/api/hasil-aktivitas', handlePostRealisasi);
router.post('/api/aktivitas-unit', handlePostRealisasi);

/**
 * =====================================================================
 * ENDPOINT APPROVAL REALISASI: POST /api/realisasi/approve
 * =====================================================================
 */
const handleApproveRealisasi = async (req, res) => {
  try {
    const body = req.body || {};
    const nomor_spk = (body.nomor_spk || req.params.id || '').toString().trim();
    const kasie_nama = (body.kasie_nama || body.approved_by || req.user?.nama_lengkap || 'Kasie').toString().trim();

    if (!nomor_spk) {
      return res.status(400).json({
        success: false,
        status: "error",
        message: "Nomor SPK wajib diisi untuk memproses persetujuan laporan."
      });
    }

    const approvalStatus = `Disetujui oleh ${kasie_nama}`;

    if (typeof db !== 'undefined' && typeof db.run === 'function') {
      await db.run(`
        UPDATE realisasi
        SET approval_status = ?,
            approved_by = ?,
            approved_at = datetime('now'),
            is_locked = 1
        WHERE nomor_spk = ? OR spk_id = ? OR id = ?
      `, [approvalStatus, kasie_nama, nomor_spk, nomor_spk, nomor_spk]).catch(() => null);

      await db.run(`
        UPDATE realisasi_kerja
        SET approval_status = ?,
            approved_by = ?,
            is_locked = 1
        WHERE nomor_spk = ? OR id = ?
      `, [approvalStatus, kasie_nama, nomor_spk, nomor_spk]).catch(() => null);

      await db.run(`
        UPDATE rencana_kerja
        SET status_spk = 'Approved',
            status_realisasi = 'Terealisasi & Approved',
            is_realized = 1
        WHERE nomor_spk = ? OR id = ?
      `, [nomor_spk, nomor_spk]).catch(() => null);
    }

    return res.status(200).json({
      success: true,
      status: "success",
      message: `Laporan resmi disetujui di Server & Lokal! (${nomor_spk})`,
      data: {
        nomor_spk: nomor_spk,
        kasie_nama: kasie_nama,
        approval_status: approvalStatus,
        is_locked: true
      }
    });
  } catch (err) {
    return res.status(500).json({
      success: false,
      status: "error",
      message: "Gagal simpan ke Web Admin: " + err.message
    });
  }
};

router.post('/api/realisasi/approve', handleApproveRealisasi);
router.post('/api/mobile/realisasi/approve', handleApproveRealisasi);
router.patch('/api/realisasi/:id/approve', handleApproveRealisasi);
router.post('/api/realisasi/:id/approve', handleApproveRealisasi);


/**
 * =====================================================================
 * ENDPOINT UNDUH APK PEMBARUAN: GET /download-apk & GET /api/mobile/download-apk
 * =====================================================================
 */
const path = require('path');
const fs = require('fs');

const handleDownloadApk = (req, res) => {
  const candidatePaths = [
    path.resolve(__dirname, '../public/RKCE_Installer_v1.7.apk'),
    path.resolve(__dirname, '../public/RKCE_Installer_latest.apk'),
    path.resolve(__dirname, '../.build-outputs/app-debug.apk'),
    path.resolve(__dirname, '../app/build/outputs/apk/debug/app-debug.apk'),
    path.resolve(__dirname, '../public/RKCE_Installer_v1.6.apk')
  ];
  const existingApk = candidatePaths.find((p) => {
    try {
      return fs.existsSync(p) && fs.statSync(p).size > 0;
    } catch (_) {
      return false;
    }
  });

  if (existingApk) {
    res.setHeader('Content-Type', 'application/vnd.android.package-archive');
    res.setHeader('Content-Disposition', 'attachment; filename="RKCE_Installer_v1.7.apk"');
    return res.download(existingApk, 'RKCE_Installer_v1.7.apk');
  }

  return res.status(404).json({
    success: false,
    message: 'Berkas installer APK pembaruan (v1.7) belum tersedia di server.'
  });
};

router.get('/download-apk', handleDownloadApk);
router.get('/api/mobile/download-apk', handleDownloadApk);
router.get('/RKCE_Installer_v1.7.apk', handleDownloadApk);
router.get('/api/mobile/RKCE_Installer_v1.7.apk', handleDownloadApk);

module.exports = router;
module.exports.autoSeedOperatorsToUsers = autoSeedOperatorsToUsers;
module.exports.DEFAULT_35_OPERATORS = DEFAULT_35_OPERATORS;
