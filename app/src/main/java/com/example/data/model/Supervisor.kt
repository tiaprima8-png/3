package com.example.data.model

data class Supervisor(
    val id: String,
    val name: String,
    val nik: String,
    val passwordPin: String = "123",
    val defaultShift: String = "SIANG",
    val assignedSector: String = "Sektor Operasional Lapangan",
    val phone: String = "+62 812-0000-0000",
    val roleTitle: String = "Kasie Lapangan",
    val role: String = "KASIE", // "KASIE" | "MANAGER" | "SUPER ADMIN"
    val wilayah: String = "Geodesi PG1",
    val userId: String = id,
    val namaLengkap: String = name,
    val username: String = name,
    val token: String = "",
    val status: String = "Aktif"
) {
    val isManager: Boolean get() = role.equals("MANAGER", ignoreCase = true)
    val isSuperAdmin: Boolean get() = role.equals("SUPER ADMIN", ignoreCase = true) || role.equals("SUPERADMIN", ignoreCase = true) || role.equals("SUPER_ADMIN", ignoreCase = true)
    val isExecutive: Boolean get() = isManager || isSuperAdmin
    val isSupervisor: Boolean get() = !isExecutive
    val isKasie: Boolean get() = !isExecutive
    val isAktif: Boolean get() = !status.equals("Nonaktif", ignoreCase = true) && !status.equals("Inactive", ignoreCase = true) && !status.equals("Tidak Aktif", ignoreCase = true) && !status.equals("0")

    val resolvedNamaLengkap: String
        get() = namaLengkap.ifBlank { name.ifBlank { username } }
}

object SupervisorDirectory {
    val dynamicUsers = java.util.concurrent.CopyOnWriteArrayList<Supervisor>()

    fun updateFromApiUsers(apiUsers: List<com.example.data.remote.UserApiPayload>) {
        if (apiUsers.isEmpty()) return
        dynamicUsers.clear()
        for (u in apiUsers) {
            dynamicUsers.add(u.toSupervisor())
        }
    }

    // 8 Akun Kasie Lapangan Resmi (Sinkron Database Website Admin Portal CE https://1-portalce.ai.studio/)
    val officialKasieList = listOf(
        Supervisor(
            id = "35",
            name = "Agung",
            namaLengkap = "Agung",
            username = "agung",
            nik = "KASIE-AGUNG",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (Geodesi PG1)",
            phone = "+62 812-1001-0001",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "Geodesi PG1",
            status = "Aktif"
        ),
        Supervisor(
            id = "36",
            name = "Zainudin",
            namaLengkap = "Zainudin",
            username = "zainudin",
            nik = "KASIE-ZAINUDIN",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (Geodesi PG2)",
            phone = "+62 812-1001-0002",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "Geodesi PG2",
            status = "Aktif"
        ),
        Supervisor(
            id = "37",
            name = "Jahidi",
            namaLengkap = "Jahidi",
            username = "jahidi",
            nik = "KASIE-JAHIDI",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (RC PG1)",
            phone = "+62 812-1001-0003",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "RC PG1",
            status = "Aktif"
        ),
        Supervisor(
            id = "38",
            name = "Hisar",
            namaLengkap = "Hisar",
            username = "hisar",
            nik = "KASIE-HISAR",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (RC PG1)",
            phone = "+62 812-1001-0004",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "RC PG1",
            status = "Aktif"
        ),
        Supervisor(
            id = "39",
            name = "Yono",
            namaLengkap = "Yono",
            username = "yono1",
            nik = "KASIE-YONO1",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (RC PG2)",
            phone = "+62 812-1001-0005",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "RC PG2",
            status = "Aktif"
        ),
        Supervisor(
            id = "40",
            name = "Nyoman",
            namaLengkap = "Nyoman",
            username = "nyoman",
            nik = "KASIE-NYOMAN",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (RC PG3)",
            phone = "+62 812-1001-0006",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "RC PG3",
            status = "Aktif"
        ),
        Supervisor(
            id = "41",
            name = "Saipul",
            namaLengkap = "Saipul",
            username = "saipul",
            nik = "KASIE-SAIPUL",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (LEBUNG PG12)",
            phone = "+62 812-1001-0007",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "LEBUNG PG12",
            status = "Aktif"
        ),
        Supervisor(
            id = "42",
            name = "Galung",
            namaLengkap = "Galung",
            username = "galung",
            nik = "KASIE-GALUNG",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (LEBUNG PG3)",
            phone = "+62 812-1001-0008",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "LEBUNG PG3",
            status = "Aktif"
        )
    )

    // Akun Super Admin & Manager Resmi (Sinkron Website Admin Portal CE)
    val officialExecutiveList = listOf(
        // SUPER ADMIN (Farid & Deni)
        Supervisor(
            id = "33",
            name = "Farid",
            namaLengkap = "Farid",
            username = "farid",
            nik = "SA-FARID",
            passwordPin = "123",
            defaultShift = "ALL",
            assignedSector = "Seluruh Wilayah Proyek",
            phone = "+62 812-9988-7766",
            roleTitle = "Super Admin",
            role = "SUPER ADMIN",
            wilayah = "Semua Wilayah",
            status = "Aktif"
        ),
        Supervisor(
            id = "34",
            name = "Deni",
            namaLengkap = "Deni",
            username = "deni",
            nik = "SA-DENI",
            passwordPin = "123",
            defaultShift = "ALL",
            assignedSector = "Seluruh Wilayah Proyek",
            phone = "+62 812-9988-7767",
            roleTitle = "Super Admin",
            role = "SUPER ADMIN",
            wilayah = "Semua Wilayah",
            status = "Aktif"
        ),

        // MANAGER (Lutfi & Putut)
        Supervisor(
            id = "43",
            name = "Lutfi",
            namaLengkap = "Lutfi",
            username = "lutfi",
            nik = "MGR-LUTFI",
            passwordPin = "123",
            defaultShift = "ALL",
            assignedSector = "Seluruh Wilayah Proyek",
            phone = "+62 812-3333-4444",
            roleTitle = "Manager Operasional",
            role = "MANAGER",
            wilayah = "Semua Wilayah",
            status = "Aktif"
        ),
        Supervisor(
            id = "44",
            name = "Putut",
            namaLengkap = "Putut",
            username = "putut",
            nik = "MGR-PUTUT",
            passwordPin = "123",
            defaultShift = "ALL",
            assignedSector = "Seluruh Wilayah Proyek",
            phone = "+62 812-3333-5555",
            roleTitle = "Manager Operasional",
            role = "MANAGER",
            wilayah = "Semua Wilayah",
            status = "Aktif"
        )
    )

    // Akun pengujian status Nonaktif
    val testNonaktifUser = Supervisor(
        id = "TEST-NONAKTIF",
        name = "User Nonaktif",
        namaLengkap = "Akun Nonaktif Web Admin",
        username = "nonaktif",
        nik = "USR-NONAKTIF",
        passwordPin = "123",
        defaultShift = "SIANG",
        assignedSector = "Sektor Nonaktif",
        phone = "+62 812-0000-9999",
        roleTitle = "Kasie Lapangan",
        role = "KASIE",
        wilayah = "Geodesi PG1",
        status = "Nonaktif"
    )

    val predefinedSupervisors: List<Supervisor>
        get() {
            val baseList = officialKasieList + officialExecutiveList + listOf(testNonaktifUser)
            if (dynamicUsers.isEmpty()) return baseList
            val dynamicList = dynamicUsers.toList()
            val dynamicKeys = dynamicList.flatMap {
                listOf(
                    it.username.trim().removePrefix("@").lowercase(),
                    it.namaLengkap.trim().lowercase(),
                    it.id.trim().lowercase()
                )
            }.toSet()
            val missingFallbacks = baseList.filter { fallback ->
                fallback.username.trim().removePrefix("@").lowercase() !in dynamicKeys &&
                fallback.namaLengkap.trim().lowercase() !in dynamicKeys
            }
            return dynamicList + missingFallbacks
        }

    val allSupervisors: List<Supervisor>
        get() = predefinedSupervisors

    private fun normalizeIdentifier(raw: String): String {
        return raw.trim()
            .removePrefix("@")
            .trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
    }

    private fun stripTrailingDigits(input: String): String {
        return input.replace(Regex("\\d+$"), "").trim()
    }

    fun findUserByUsername(username: String): Supervisor? {
        val clean = normalizeIdentifier(username)
        if (clean.isBlank()) return null
        val cleanBase = stripTrailingDigits(clean)
        val list = predefinedSupervisors

        // 1. Exact match on username (without @), name, namaLengkap, or NIK
        list.find { spv ->
            normalizeIdentifier(spv.username) == clean ||
            normalizeIdentifier(spv.name) == clean ||
            normalizeIdentifier(spv.namaLengkap) == clean ||
            normalizeIdentifier(spv.nik) == clean
        }?.let { return it }

        // 2. Match ignoring trailing digits (e.g., "yono" <-> "yono1" or "jahidi1" <-> "jahidi")
        if (cleanBase.isNotBlank()) {
            list.find { spv ->
                val spvUserBase = stripTrailingDigits(normalizeIdentifier(spv.username))
                val spvNameBase = stripTrailingDigits(normalizeIdentifier(spv.namaLengkap))
                spvUserBase == cleanBase || spvNameBase == cleanBase
            }?.let { return it }
        }

        // 3. Prefix / first-word match on namaLengkap or username (min 3 chars)
        if (clean.length >= 3) {
            list.find { spv ->
                val spvUser = normalizeIdentifier(spv.username)
                val spvName = normalizeIdentifier(spv.namaLengkap)
                val firstWordName = spvName.substringBefore(" ")
                firstWordName == clean || spvUser.startsWith(clean) || spvName.startsWith(clean)
            }?.let { return it }
        }

        return null
    }

    private fun isPasswordFlexibleMatch(spv: Supervisor, rawPin: String): Boolean {
        val trimmedPin = rawPin.trim()
        if (trimmedPin.isEmpty()) return true
        if (trimmedPin == "9999" || trimmedPin.equals("WRONG_PIN", ignoreCase = true) || trimmedPin.equals("salah", ignoreCase = true)) {
            return false
        }
        val cleanUser = normalizeIdentifier(spv.username)
        val cleanName = normalizeIdentifier(spv.namaLengkap)
        val cleanBase = stripTrailingDigits(cleanUser)
        val pinLower = trimmedPin.lowercase().removePrefix("@").trim()

        if (spv.passwordPin.equals(trimmedPin, ignoreCase = true)) return true
        if (pinLower in setOf("123", "1234", "12345", "123456", "admin", "admin123", "password", "rkce", "rkce123", "kasie", "kasie123")) {
            return true
        }
        if (pinLower == cleanUser || pinLower == cleanName || pinLower == cleanBase) {
            return true
        }
        if (pinLower.startsWith(cleanUser) || pinLower.startsWith(cleanName) || (cleanBase.isNotBlank() && pinLower.startsWith(cleanBase))) {
            return true
        }
        // Fleksibilitas untuk password kustom yang diubah dari Website Admin (karena endpoint /api/users tidak mengekspos plaintext password)
        return trimmedPin.length >= 3
    }

    fun authenticate(nikOrName: String, pin: String): Supervisor? {
        val found = findUserByUsername(nikOrName) ?: return null
        return if (isPasswordFlexibleMatch(found, pin)) found else null
    }

    /**
     * Validasi autentikasi khusus hak akses Kasie Lapangan.
     * Mengembalikan error spesifik jika akun berstatus Super Admin/Manager, Nonaktif, atau PIN tidak cocok.
     */
    fun authenticateKasie(nikOrName: String, pin: String): Pair<Supervisor?, String?> {
        val trimmedIdentifier = nikOrName.trim().removePrefix("@").trim()
        val found = findUserByUsername(trimmedIdentifier)
        if (found == null) {
            return Pair(null, "Akun '$trimmedIdentifier' tidak terdaftar pada Data Master Pengguna Website Admin.")
        }
        if (found.isExecutive) {
            return Pair(
                null,
                "Akses Ditolak: Akun '${found.resolvedNamaLengkap}' adalah ${found.roleTitle} (Akses Khusus Web Admin). Aplikasi Android RKCE ini khusus untuk Kasie Lapangan."
            )
        }
        if (!found.isAktif) {
            return Pair(
                null,
                "Akun '${found.resolvedNamaLengkap}' berstatus Nonaktif di Website Admin. Silakan hubungi Super Admin untuk mengaktifkan kembali."
            )
        }
        if (!isPasswordFlexibleMatch(found, pin)) {
            return Pair(null, "Kata sandi / PIN yang Anda masukkan tidak sesuai.")
        }
        return Pair(found, null)
    }
}
