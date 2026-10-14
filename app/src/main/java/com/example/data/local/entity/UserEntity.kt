package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users_session")
data class UserEntity(
    @PrimaryKey
    val username: String,

    @ColumnInfo(name = "user_id")
    val id: String = "1",

    @ColumnInfo(name = "token")
    val token: String = "",

    @ColumnInfo(name = "name")
    val name: String = "",

    @ColumnInfo(name = "nama_lengkap")
    val namaLengkap: String = "",

    @ColumnInfo(name = "nik")
    val nik: String = "",

    @ColumnInfo(name = "role")
    @SerializedName("role")
    val role: String = "KASIE", // "KASIE" | "MANAGER" | "SUPER ADMIN"

    @ColumnInfo(name = "role_title")
    val roleTitle: String = "Kasie",

    @ColumnInfo(name = "wilayah")
    val wilayah: String? = "Geodesi 1", // Salah satu dari 8 Wilayah Kerja baru atau "Semua Wilayah"

    @ColumnInfo(name = "status")
    val status: String = "Aktif",

    @ColumnInfo(name = "default_shift")
    val defaultShift: String = "SIANG",

    @ColumnInfo(name = "assigned_sector")
    val assignedSector: String = "Sektor Operasional Lapangan",

    @ColumnInfo(name = "phone")
    val phone: String = "",

    @ColumnInfo(name = "is_active_session")
    val isActiveSession: Boolean = true,

    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: Long = System.currentTimeMillis()
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
