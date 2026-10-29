package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users_session WHERE is_active_session = 1 ORDER BY last_login_at DESC LIMIT 1")
    fun getActiveUserSession(): Flow<UserEntity?>

    @Query("SELECT * FROM users_session WHERE is_active_session = 1 ORDER BY last_login_at DESC LIMIT 1")
    suspend fun getActiveUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("UPDATE users_session SET is_active_session = 0")
    suspend fun clearActiveSessions()

    @Query("SELECT * FROM users_session WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("UPDATE users_session SET wilayah = :wilayah WHERE username = :username")
    suspend fun updateWilayah(username: String, wilayah: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users_session ORDER BY username ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users_session ORDER BY username ASC")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("DELETE FROM users_session")
    suspend fun deleteAll()
}
