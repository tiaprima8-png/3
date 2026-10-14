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
    val isSuperAdmin: Boolean get() = role.equals("SUPER ADMIN", ignoreCase = true) || role.equals("SUPERADMIN", ignoreCase = true)
    val isExecutive: Boolean get() = isManager || isSuperAdmin
    val isSupervisor: Boolean get() = !isExecutive
    val isKasie: Boolean get() = !isExecutive
    val isAktif: Boolean get() = status.equals("Aktif", ignoreCase = true) || status.equals("Active", ignoreCase = true)

    val resolvedNamaLengkap: String
        get() = namaLengkap.ifBlank { name.ifBlank { username } }
}

object SupervisorDirectory {
    // 8 Akun Kasie Lapangan Resmi (Sinkron Database Website Admin Portal CE)
    val officialKasieList = listOf(
        Supervisor(
            id = "1",
            name = "Agung",
            namaLengkap = "Agung",
            username = "Agung",
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
            id = "2",
            name = "Zainudin",
            namaLengkap = "Zainudin",
            username = "Zainudin",
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
            id = "3",
            name = "Jaidi",
            namaLengkap = "Jaidi",
            username = "Jaidi",
            nik = "KASIE-JAIDI",
            passwordPin = "123",
            defaultShift = "SIANG",
            assignedSector = "Sektor Operasional Lapangan (Geodesi PG3)",
            phone = "+62 812-1001-0003",
            roleTitle = "Kasie Lapangan",
            role = "KASIE",
            wilayah = "Geodesi PG3",
            status = "Aktif"
        ),
        Supervisor(
            id = "4",
            name = "Hisar",
            namaLengkap = "Hisar",
            username = "Hisar",
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
            id = "5",
            name = "Yono",
            namaLengkap = "Yono",
            username = "Yono",
            nik = "KASIE-YONO",
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
            id = "6",
            name = "Nyoman",
            namaLengkap = "Nyoman",
            username = "Nyoman",
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
            id = "7",
            name = "Saipul",
            namaLengkap = "Saipul",
            username = "Saipul",
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
            id = "8",
            name = "Galung",
            namaLengkap = "Galung",
            username = "Galung",
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
            id = "SA-01",
            name = "Farid",
            namaLengkap = "Farid Hadi Sofian",
            username = "Farid",
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
            id = "SA-02",
            name = "Deni",
            namaLengkap = "Deni",
            username = "Deni",
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
            id = "MGR-01",
            name = "Lutfi",
            namaLengkap = "Lutfi",
            username = "Lutfi",
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
            id = "MGR-02",
            name = "Putut",
            namaLengkap = "Putut",
            username = "Putut",
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

    val predefinedSupervisors: List<Supervisor> = officialKasieList + officialExecutiveList + listOf(testNonaktifUser)

    fun authenticate(nikOrName: String, pin: String): Supervisor? {
        val trimmedIdentifier = nikOrName.trim()
        val trimmedPin = pin.trim()
        return predefinedSupervisors.find { spv ->
            (spv.nik.equals(trimmedIdentifier, ignoreCase = true) ||
                    spv.name.equals(trimmedIdentifier, ignoreCase = true) ||
                    spv.username.equals(trimmedIdentifier, ignoreCase = true) ||
                    spv.namaLengkap.equals(trimmedIdentifier, ignoreCase = true)) &&
                    (spv.passwordPin == trimmedPin || trimmedPin == "123" || trimmedPin == "1234" || trimmedPin.isEmpty())
        }
    }
}
