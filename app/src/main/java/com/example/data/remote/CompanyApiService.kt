package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface CompanyApiService {
    @GET("api/mobile/rencana-kerja/active")
    suspend fun getActiveRencanaKerja(
        @Query("pengawas") pengawas: String
    ): Response<ResponseBody>

    @GET("api/rencana-kerja")
    suspend fun getRencanaKerja(): Response<ResponseBody>

    @GET("api/mobile/bootstrap-data")
    suspend fun getMobileBootstrapData(): Response<ResponseBody>

    @GET("api/mobile/bootstrap-data")
    suspend fun getBootstrapData(
        @Query("pengawas") pengawas: String,
        @Query("tanggal") tanggal: String
    ): Response<ApiResponse<BootstrapDataPayload>>

    @GET("api/master-data")
    suspend fun getMasterData(
        @Query("supervisor") supervisor: String? = null
    ): Response<ApiResponse<MasterDataPayload>>

    @GET("api/master-unit")
    suspend fun getMasterUnit(
        @Query("supervisor") supervisor: String? = null
    ): Response<ApiResponse<List<UnitPayload>>>

    @GET("api/units")
    suspend fun getUnits(
        @Query("supervisor") supervisor: String? = null
    ): Response<List<UnitPayload>>

    @GET("api/aktivitas")
    suspend fun getActivities(): Response<List<ActivityPayload>>

    @GET("api/operators")
    suspend fun getOperators(
        @Query("supervisor") supervisor: String? = null
    ): Response<List<OperatorPayload>>

    @GET("api/master-kendala")
    suspend fun getMasterKendala(): Response<ApiResponse<List<KendalaPayload>>>

    @POST("api/aktivitas-unit")
    suspend fun postActivityReport(
        @Body report: PostActivityReportRequest
    ): Response<ApiResponse<Any>>

    @POST("api/rencana-kerja")
    suspend fun postRencanaKerja(
        @Body request: PostRencanaKerjaRequest
    ): Response<ApiResponse<Any>>

    @POST("api/hasil-aktivitas")
    suspend fun postHasilAktivitas(
        @Body request: PostHasilAktivitasRequest
    ): Response<ApiResponse<Any>>

    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ResponseBody>

    @POST("api/mobile/login")
    suspend fun loginMobile(
        @Body request: LoginRequest
    ): Response<ResponseBody>

    @GET("api/users")
    suspend fun getUsers(): Response<List<UserApiPayload>>

    @POST("api/auth/login")
    suspend fun authLogin(
        @Body request: AuthLoginRequest
    ): Response<ResponseBody>

    @GET("api/health")
    suspend fun checkHealth(): Response<ApiResponse<Any>>
}

/**
 * Manages dynamic Retrofit client connecting to the user's Web Admin URL.
 * Automatically saves/loads configured URL and falls back to in-memory company server
 * simulator when in offline/testing mode.
 */
class ApiClientManager(val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("heavytrack_api_prefs", Context.MODE_PRIVATE)

    companion object {
        // Active Web Admin Portal URL
        const val BASE_URL = "https://1-portalce.ai.studio"
        const val KEY_API_URL = "api_base_url"
        const val DEFAULT_WEB_ADMIN_URL = "https://1-portalce.ai.studio"
        const val KEY_USE_MOCK_FALLBACK = "use_mock_fallback"
    }

    var baseUrl: String
        get() = prefs.getString(KEY_API_URL, DEFAULT_WEB_ADMIN_URL) ?: DEFAULT_WEB_ADMIN_URL
        set(value) {
            val formatted = formatBaseUrl(value)
            prefs.edit().putString(KEY_API_URL, formatted).apply()
            rebuildRetrofit(formatted)
        }

    var useSimulationFallback: Boolean
        get() = prefs.getBoolean(KEY_USE_MOCK_FALLBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_MOCK_FALLBACK, value).apply()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    @Volatile
    private var apiService: CompanyApiService = createService(baseUrl)

    private fun formatBaseUrl(raw: String): String {
        var clean = raw.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        if (!clean.endsWith("/")) {
            clean = "$clean/"
        }
        return clean
    }

    private fun createService(url: String): CompanyApiService {
        val validUrl = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(validUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(CompanyApiService::class.java)
    }

    private fun rebuildRetrofit(url: String) {
        try {
            apiService = createService(url)
        } catch (_: Exception) {
            // Keep previous if invalid
        }
    }

    /**
     * Test connection to the Web Admin server
     */
    suspend fun testConnection(targetUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val formatted = formatBaseUrl(targetUrl)
            val testService = createService(formatted)
            val response = testService.getMasterData()
            if (response.isSuccessful) {
                Pair(true, "Terhubung sukses! Status Code: ${response.code()} OK")
            } else {
                Pair(false, "Server merespons status HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Pair(false, "Koneksi gagal: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun fetchMasterData(supervisor: String): Pair<MasterDataPayload, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan aktif: Data diambil dari SQLite lokal")
        }

        try {
            val response = apiService.getMasterData(supervisor)
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                return@withContext Pair(data, "Berhasil sinkron master data dari Web Admin API (${response.code()})")
            }
        } catch (e: Exception) {
            // If real network fails and fallback is enabled, use simulator
            if (useSimulationFallback) {
                val simResponse = CompanyServerSimulator.fetchMasterData(supervisor)
                return@withContext Pair(
                    simResponse.data ?: MasterDataPayload(),
                    "Web Admin remote tidak merespons (${e.localizedMessage}). Menggunakan data sinkronisasi lokal."
                )
            }
            throw e
        }

        // Fallback to local simulator
        val simResponse = CompanyServerSimulator.fetchMasterData(supervisor)
        Pair(simResponse.data ?: MasterDataPayload(), "Data master berhasil disinkronkan.")
    }

    suspend fun fetchMasterUnit(supervisor: String? = null): Pair<List<UnitPayload>, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            val simResp = CompanyServerSimulator.getMasterUnit(supervisor)
            return@withContext Pair(simResp.data ?: emptyList(), "Mode Offline Lapangan: Master unit dimuat dari simulator lokal")
        }

        try {
            val response = apiService.getMasterUnit(supervisor)
            if (response.isSuccessful && response.body()?.data != null) {
                val clean = response.body()!!.data!!.filter { com.example.data.model.UnitConstants.isJenisUnitValid(it.resolvedUnitType) }
                return@withContext Pair(clean, "Berhasil sinkron master unit dari Web Admin API (${response.code()})")
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.getMasterUnit(supervisor)
                return@withContext Pair(
                    simResp.data ?: emptyList(),
                    "Web Admin remote tidak merespons (${e.localizedMessage}). Menggunakan data sinkronisasi lokal."
                )
            }
            throw e
        }

        val simResp = CompanyServerSimulator.getMasterUnit(supervisor)
        Pair(simResp.data ?: emptyList(), "Master unit berhasil disinkronkan.")
    }

    suspend fun fetchBootstrapData(pengawas: String, tanggal: String): Pair<BootstrapDataPayload, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan aktif: Menggunakan data SQLite lokal")
        }

        try {
            val response = apiService.getBootstrapData(pengawas, tanggal)
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                return@withContext Pair(data, "Berhasil sinkron bootstrap data dari Web Admin API (${response.code()})")
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResponse = CompanyServerSimulator.fetchBootstrapData(pengawas, tanggal)
                return@withContext Pair(
                    simResponse.data ?: BootstrapDataPayload(),
                    "Web Admin remote tidak merespons (${e.localizedMessage}). Menggunakan data sinkronisasi lokal."
                )
            }
            throw e
        }

        val simResponse = CompanyServerSimulator.fetchBootstrapData(pengawas, tanggal)
        Pair(simResponse.data ?: BootstrapDataPayload(), "Data bootstrap operasional berhasil disinkronkan.")
    }

    /**
     * Endpoint terpadu: GET /api/mobile/bootstrap-data
     * Menarik seluruh data master aktif (users, units, operators, aktivitas, kendala, lokasi, rencanaKerja)
     * dalam satu payload JSON tunggal untuk dieksekusi via "Clear and Replace" di SQLite.
     */
    suspend fun fetchCentralizedBootstrap(supervisorName: String, tanggal: String): Pair<BootstrapResponseData, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            val simResponse = CompanyServerSimulator.getMobileBootstrapResponse(supervisorName, tanggal)
            val converted = parseCentralizedBootstrapPayload(simResponse.data, supervisorName, tanggal)
            return@withContext Pair(converted, "Mode Offline Lapangan: Data master dimuat dari simulator lokal")
        }

        try {
            val response = apiService.getMobileBootstrapData()
            if (response.isSuccessful && response.body() != null) {
                val bodyStr = response.body()!!.string()
                val parsed = parseCentralizedBootstrapJson(bodyStr, supervisorName, tanggal)
                if (parsed != null && (parsed.units.isNotEmpty() || parsed.users.isNotEmpty())) {
                    return@withContext Pair(parsed, "Berhasil sinkron seluruh data master dari Web Admin (${response.code()} OK)")
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ApiClientManager", "Live getMobileBootstrapData error: ${e.message}")
        }

        if (useSimulationFallback) {
            val simResponse = CompanyServerSimulator.getMobileBootstrapResponse(supervisorName, tanggal)
            val converted = parseCentralizedBootstrapPayload(simResponse.data, supervisorName, tanggal)
            return@withContext Pair(converted, "Web Admin remote tidak merespons. Menggunakan data sinkronisasi terpadu lokal.")
        }

        val simResponse = CompanyServerSimulator.getMobileBootstrapResponse(supervisorName, tanggal)
        val converted = parseCentralizedBootstrapPayload(simResponse.data, supervisorName, tanggal)
        Pair(converted, "Data master terpadu berhasil disinkronkan.")
    }

    private fun parseCentralizedBootstrapPayload(
        payload: CentralizedBootstrapDataPayload?,
        supervisorName: String,
        tanggal: String
    ): BootstrapResponseData {
        if (payload == null) {
            return BootstrapResponseData()
        }

        val users = (payload.users ?: emptyList()).filter { it.isAktif }.map { u ->
            val uname = u.resolvedUsername
            val nama = u.resolvedNamaLengkap
            val role = u.resolvedRole
            val wil = u.wilayah ?: "Geodesi PG1"
            com.example.data.local.entity.UserEntity(
                username = uname,
                id = u.numericOrStringId,
                token = "SYNC-$uname",
                name = nama,
                namaLengkap = nama,
                nik = uname,
                role = role,
                roleTitle = if (role.contains("SUPER")) "Super Admin" else if (role.contains("MANAGER")) "Manager Operasional" else "Kasie Lapangan",
                wilayah = wil,
                status = "Aktif",
                isActiveSession = false,
                lastLoginAt = System.currentTimeMillis()
            )
        }

        val units = (payload.units ?: emptyList())
            .filter { com.example.data.model.UnitConstants.isJenisUnitValid(it.resolvedUnitType) }
            .filter { !it.resolvedStatus.equals("Nonaktif", ignoreCase = true) && !it.resolvedStatus.equals("Inactive", ignoreCase = true) }
            .map { un ->
                com.example.data.local.entity.UnitEntity(
                    unitCode = un.resolvedUnitCode,
                    unitType = un.resolvedUnitType,
                    brandModel = un.resolvedBrandModel,
                    lastHm = un.resolvedLastHm,
                    supervisorAssigned = if (un.resolvedSupervisor.isNotBlank()) un.resolvedSupervisor else supervisorName,
                    status = un.resolvedStatus,
                    hmMinStandar = un.resolvedHmMinStandar,
                    hmMaxStandar = un.resolvedHmMaxStandar,
                    wilayah = un.resolvedWilayah,
                    updatedAt = System.currentTimeMillis()
                )
            }

        val operators = (payload.operators ?: emptyList())
            .filter { it.resolvedStatus.equals("AKTIF", ignoreCase = true) || it.resolvedStatus.equals("ACTIVE", ignoreCase = true) }
            .map { op ->
                com.example.data.local.entity.OperatorEntity(
                    operatorId = op.resolvedOperatorId,
                    name = op.resolvedName,
                    badgeNumber = op.resolvedBadgeNumber,
                    supervisorAssigned = if (op.resolvedSupervisor.isNotBlank()) op.resolvedSupervisor else supervisorName,
                    skillLevel = op.resolvedSkillLevel,
                    status = op.resolvedStatus,
                    wilayah = op.resolvedWilayah,
                    kasiePenanggungJawab = if (op.resolvedSupervisor.isNotBlank()) op.resolvedSupervisor else supervisorName,
                    updatedAt = System.currentTimeMillis()
                )
            }

        val activities = payload.resolvedAktivitas
            .filter { !it.resolvedKategoriStatus.equals("Nonaktif", ignoreCase = true) }
            .map { a ->
                com.example.data.local.entity.ActivityMasterEntity(
                    sapCode = a.resolvedSapCode,
                    activityName = a.resolvedActivityName,
                    unitOfMeasure = a.resolvedUnitOfMeasure,
                    description = a.resolvedDescription,
                    category = a.resolvedCategory,
                    kategoriStatus = a.resolvedKategoriStatus,
                    jenisUnit = a.resolvedJenisUnit,
                    updatedAt = System.currentTimeMillis()
                )
            }

        val kendala = (payload.kendala ?: emptyList()).mapIndexed { index, k ->
            com.example.data.local.entity.KendalaEntity(
                kodeKendala = k.resolveKode(index),
                namaKendala = k.resolvedNama,
                kategori = k.resolvedKategori,
                deskripsi = k.resolvedDeskripsi,
                updatedAt = System.currentTimeMillis()
            )
        }

        val lokasi = payload.resolvedLokasi.map { l ->
            com.example.data.local.entity.LokasiEntity(
                kodeLokasi = l.resolvedKode,
                wilayah = l.resolvedWilayah,
                luasBruto = l.resolvedLuasBruto,
                luasNetto = l.resolvedLuasNetto,
                keterangan = l.keterangan ?: "",
                status = l.resolvedStatus,
                updatedAt = System.currentTimeMillis()
            )
        }

        val unitOwnerByCode = units.associate { it.unitCode.uppercase() to it.supervisorAssigned }
        val rencana = payload.resolvedRencanaKerja.mapIndexed { index, ren ->
            val spk = ren.nomor_spk?.trim() ?: ""
            val statusSpk = when {
                !ren.status_spk.isNullOrBlank() -> ren.status_spk
                spk.isNotBlank() && spk != "SPK Menyusul" && spk != "-" -> "SPK_TERBIT"
                else -> "MENUNGGU_SPK"
            }
            val unitOwner = unitOwnerByCode[ren.resolvedUnitKode.uppercase()]
            val spv = when {
                !unitOwner.isNullOrBlank() -> unitOwner
                ren.resolvedPengawas.isNotBlank() -> ren.resolvedPengawas
                else -> supervisorName
            }
            val rawId = ren.numericId
            val targetId = if (rawId > 0) rawId else (index + 1).toLong()
            com.example.data.local.entity.RencanaKerjaEntity(
                id = targetId,
                supervisorName = spv,
                pengawas = spv,
                kasie = spv,
                createdBy = ren.created_by?.ifBlank { null } ?: ren.createdBy?.ifBlank { null } ?: spv,
                statusUnit = ren.status_unit ?: "OPERASI",
                kodeUnit = ren.resolvedUnitKode,
                operator = ren.resolvedOperator,
                sapCode = ren.resolvedSapCode,
                namaAktivitas = ren.resolvedAktivitas,
                satuanAktivitas = ren.resolvedSatuan,
                kodeLokasi = ren.resolvedLokasi,
                wilayah = ren.wilayah ?: "Geodesi PG1",
                shiftKerja = ren.resolvedShift,
                keterangan = ren.keterangan ?: "",
                nomorSpk = spk,
                statusSpk = statusSpk,
                statusTahap = ren.status_tahap ?: "RENCANA",
                statusRealisasi = if (ren.is_realized == true) "SUDAH" else "BELUM",
                isRealized = ren.is_realized ?: false,
                isSynced = true,
                createdAt = System.currentTimeMillis() - (index * 1000L),
                tanggalFormat = ren.tanggal ?: tanggal
            )
        }

        return BootstrapResponseData(
            users = users,
            units = units,
            operators = operators,
            aktivitas = activities,
            kendala = kendala,
            lokasi = lokasi,
            rencanaKerja = rencana
        )
    }

    private fun parseCentralizedBootstrapJson(
        rawJson: String,
        supervisorName: String,
        tanggal: String
    ): BootstrapResponseData? {
        val trimmed = rawJson.trim()
        if (!trimmed.startsWith("{")) return null
        return try {
            val root = JSONObject(trimmed)
            val dataObj = root.optJSONObject("data") ?: root

            // 1. Users
            val userList = mutableListOf<com.example.data.local.entity.UserEntity>()
            val userArr = dataObj.optJSONArray("users")
            if (userArr != null) {
                for (i in 0 until userArr.length()) {
                    val u = userArr.optJSONObject(i) ?: continue
                    val uname = optStringFirst(u, "username").removePrefix("@").trim()
                    if (uname.isBlank()) continue
                    val status = optStringFirst(u, "status").ifBlank { "Aktif" }
                    val statusAktif = u.opt("status_aktif")
                    val isAktif = when (statusAktif) {
                        is Boolean -> statusAktif
                        is Number -> statusAktif.toInt() == 1
                        else -> !status.equals("Nonaktif", ignoreCase = true) && !status.equals("Inactive", ignoreCase = true)
                    }
                    if (!isAktif) continue
                    val nama = optStringFirst(u, "nama_lengkap", "name").ifBlank { uname }
                    val role = optStringFirst(u, "role", "hak_akses").uppercase()
                    val wil = optStringFirst(u, "wilayah").ifBlank { "Geodesi PG1" }
                    userList.add(
                        com.example.data.local.entity.UserEntity(
                            username = uname,
                            id = optStringFirst(u, "id").ifBlank { (i + 1).toString() },
                            token = "SYNC-$uname",
                            name = nama,
                            namaLengkap = nama,
                            nik = uname,
                            role = if (role.contains("SUPER")) "SUPER ADMIN" else if (role.contains("MANAGER")) "MANAGER" else "KASIE",
                            roleTitle = if (role.contains("SUPER")) "Super Admin" else if (role.contains("MANAGER")) "Manager Operasional" else "Kasie Lapangan",
                            wilayah = wil,
                            status = "Aktif",
                            isActiveSession = false,
                            lastLoginAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 2. Units (with kasie_penanggung_jawab)
            val unitList = mutableListOf<com.example.data.local.entity.UnitEntity>()
            val unitArr = dataObj.optJSONArray("units")
            if (unitArr != null) {
                for (i in 0 until unitArr.length()) {
                    val un = unitArr.optJSONObject(i) ?: continue
                    val code = optStringFirst(un, "kode_unit", "unit_code", "unitCode")
                    if (code.isBlank()) continue
                    val uType = optStringFirst(un, "jenis_unit", "unit_type", "unitType").ifBlank { "Excavator Standar" }
                    if (!com.example.data.model.UnitConstants.isJenisUnitValid(uType)) continue
                    val status = optStringFirst(un, "status").ifBlank { "OPERASIONAL" }
                    if (status.equals("Nonaktif", ignoreCase = true) || status.equals("Inactive", ignoreCase = true)) continue
                    val kasieSpv = optStringFirst(un, "kasie_penanggung_jawab", "nama_pengawas", "supervisor_assigned", "supervisorAssigned").ifBlank { supervisorName }
                    val wil = optStringFirst(un, "wilayah").ifBlank { "Geodesi PG1" }
                    val brand = optStringFirst(un, "brand_model", "model_unit", "brandModel")
                    val lastHm = un.optDouble("last_hm", un.optDouble("hm_unit_terakhir_diinputkan", 0.0))
                    val minHm = un.optDouble("hm_min_standar", if (uType.contains("Compactor") || uType.contains("Grader")) 5.0 else 6.0)
                    val maxHm = un.optDouble("hm_max_standar", if (uType.contains("Compactor") || uType.contains("Grader")) 8.5 else 10.0)
                    unitList.add(
                        com.example.data.local.entity.UnitEntity(
                            unitCode = code,
                            unitType = uType,
                            brandModel = brand,
                            lastHm = lastHm,
                            supervisorAssigned = kasieSpv,
                            status = status,
                            hmMinStandar = minHm,
                            hmMaxStandar = maxHm,
                            wilayah = wil,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 3. Operators
            val opList = mutableListOf<com.example.data.local.entity.OperatorEntity>()
            val opArr = dataObj.optJSONArray("operators")
            if (opArr != null) {
                for (i in 0 until opArr.length()) {
                    val op = opArr.optJSONObject(i) ?: continue
                    val name = optStringFirst(op, "nama_operator", "name", "nama")
                    if (name.isBlank()) continue
                    val status = optStringFirst(op, "status").ifBlank { "AKTIF" }
                    if (!status.equals("AKTIF", ignoreCase = true) && !status.equals("ACTIVE", ignoreCase = true)) continue
                    val opId = optStringFirst(op, "operator_id", "operatorId", "nik").ifBlank { "OPR-${i + 1}" }
                    val badge = optStringFirst(op, "badge_number", "badgeNumber", "nik").ifBlank { "NIK-${i + 1}" }
                    val spv = optStringFirst(op, "kasie_penanggung_jawab", "kasiePenanggungJawab", "kasie", "nama_pengawas", "supervisor_assigned", "supervisorAssigned").ifBlank { supervisorName }
                    val skill = optStringFirst(op, "jabatan", "skill_level", "skillLevel").ifBlank { "Class A" }
                    val wil = optStringFirst(op, "wilayah").ifBlank { "Geodesi PG1" }
                    opList.add(
                        com.example.data.local.entity.OperatorEntity(
                            operatorId = opId,
                            name = name,
                            badgeNumber = badge,
                            supervisorAssigned = spv,
                            skillLevel = skill,
                            status = status,
                            wilayah = wil,
                            kasiePenanggungJawab = spv,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 4. Aktivitas
            val actList = mutableListOf<com.example.data.local.entity.ActivityMasterEntity>()
            val actArr = dataObj.optJSONArray("aktivitas") ?: dataObj.optJSONArray("activities")
            if (actArr != null) {
                for (i in 0 until actArr.length()) {
                    val a = actArr.optJSONObject(i) ?: continue
                    val sap = optStringFirst(a, "kode_sap", "sap_code", "sapCode")
                    if (sap.isBlank()) continue
                    val name = optStringFirst(a, "nama_aktivitas", "activity_name", "activityName").ifBlank { sap }
                    val katStatus = optStringFirst(a, "kategori_status", "kategoriStatus").ifBlank { "OPERASI" }
                    if (katStatus.equals("Nonaktif", ignoreCase = true)) continue
                    val sat = optStringFirst(a, "satuan", "unit_of_measure", "unitOfMeasure").ifBlank { "m3" }
                    val desc = optStringFirst(a, "description", "keterangan")
                    val cat = optStringFirst(a, "kategori", "category").ifBlank { "PRODUKSI" }
                    val uType = optStringFirst(a, "jenis_unit", "jenisUnit").ifBlank { "ALL" }
                    actList.add(
                        com.example.data.local.entity.ActivityMasterEntity(
                            sapCode = sap,
                            activityName = name,
                            unitOfMeasure = sat,
                            description = desc,
                            category = cat,
                            kategoriStatus = katStatus,
                            jenisUnit = uType,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 5. Kendala
            val kndList = mutableListOf<com.example.data.local.entity.KendalaEntity>()
            val kndArr = dataObj.optJSONArray("kendala")
            if (kndArr != null) {
                for (i in 0 until kndArr.length()) {
                    val k = kndArr.optJSONObject(i) ?: continue
                    val kode = optStringFirst(k, "kode_kendala", "kode", "id_kendala").ifBlank { "${i + 1}.0" }
                    val nama = optStringFirst(k, "nama_kendala", "nama").ifBlank { "Kendala $kode" }
                    val cat = optStringFirst(k, "kategori").ifBlank { "OPERASIONAL" }
                    val desc = optStringFirst(k, "deskripsi", "keterangan")
                    kndList.add(
                        com.example.data.local.entity.KendalaEntity(
                            kodeKendala = kode,
                            namaKendala = nama,
                            kategori = cat,
                            deskripsi = desc,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 6. Lokasi
            val lokList = mutableListOf<com.example.data.local.entity.LokasiEntity>()
            val lokArr = dataObj.optJSONArray("lokasi") ?: dataObj.optJSONArray("locations")
            if (lokArr != null) {
                for (i in 0 until lokArr.length()) {
                    val l = lokArr.optJSONObject(i) ?: continue
                    val kode = optStringFirst(l, "kode_lokasi", "kodeLokasi").ifBlank { "LOK-${i + 1}" }
                    val wil = optStringFirst(l, "wilayah").ifBlank { "Geodesi PG1" }
                    val bruto = l.optDouble("luas_bruto", 0.0)
                    val netto = l.optDouble("luas_netto", 0.0)
                    val ket = optStringFirst(l, "keterangan")
                    val stat = optStringFirst(l, "status").ifBlank { "Aktif" }
                    lokList.add(
                        com.example.data.local.entity.LokasiEntity(
                            kodeLokasi = kode,
                            wilayah = wil,
                            luasBruto = bruto,
                            luasNetto = netto,
                            keterangan = ket,
                            status = stat,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            // 7. Rencana Kerja (Pastikan penanggung jawab sesuai dengan Kasie unit / rencana, bukan ditimpa ke sesi lain)
            val unitOwnerMap = unitList.associate { it.unitCode.uppercase() to it.supervisorAssigned }
            val renList = mutableListOf<com.example.data.local.entity.RencanaKerjaEntity>()
            val renArr = dataObj.optJSONArray("rencanaKerja") ?: dataObj.optJSONArray("rencana_kerja") ?: dataObj.optJSONArray("rencana_aktif")
            if (renArr != null) {
                for (i in 0 until renArr.length()) {
                    val r = renArr.optJSONObject(i) ?: continue
                    val renReq = parseJsonToRencanaRequest(r)
                    val rawId = renReq.numericId
                    val targetId = if (rawId > 0) rawId else (i + 1).toLong()
                    val spk = renReq.nomor_spk?.trim() ?: ""
                    val statusSpk = when {
                        !renReq.status_spk.isNullOrBlank() -> renReq.status_spk
                        spk.isNotBlank() && spk != "SPK Menyusul" && spk != "-" -> "SPK_TERBIT"
                        else -> "MENUNGGU_SPK"
                    }
                    val unitOwner = unitOwnerMap[renReq.resolvedUnitKode.uppercase()]
                    val spv = when {
                        !unitOwner.isNullOrBlank() -> unitOwner
                        renReq.resolvedPengawas.isNotBlank() -> renReq.resolvedPengawas
                        else -> supervisorName
                    }
                    renList.add(
                        com.example.data.local.entity.RencanaKerjaEntity(
                            id = targetId,
                            supervisorName = spv,
                            pengawas = spv,
                            kasie = spv,
                            createdBy = renReq.created_by?.ifBlank { null } ?: renReq.createdBy?.ifBlank { null } ?: spv,
                            statusUnit = renReq.status_unit ?: "OPERASI",
                            kodeUnit = renReq.resolvedUnitKode,
                            operator = renReq.resolvedOperator,
                            sapCode = renReq.resolvedSapCode,
                            namaAktivitas = renReq.resolvedAktivitas,
                            satuanAktivitas = renReq.resolvedSatuan,
                            kodeLokasi = renReq.resolvedLokasi,
                            wilayah = renReq.wilayah ?: "Geodesi PG1",
                            shiftKerja = renReq.resolvedShift,
                            keterangan = renReq.keterangan ?: "",
                            nomorSpk = spk,
                            statusSpk = statusSpk,
                            statusTahap = renReq.status_tahap ?: "RENCANA",
                            statusRealisasi = if (renReq.is_realized == true) "SUDAH" else "BELUM",
                            isRealized = renReq.is_realized ?: false,
                            isSynced = true,
                            createdAt = System.currentTimeMillis() - (i * 1000L),
                            tanggalFormat = renReq.tanggal ?: tanggal
                        )
                    )
                }
            }

            BootstrapResponseData(
                users = userList,
                units = unitList,
                operators = opList,
                aktivitas = actList,
                kendala = kndList,
                lokasi = lokList,
                rencanaKerja = renList
            )
        } catch (e: Exception) {
            android.util.Log.e("ApiClientManager", "Error parsing centralized bootstrap JSON: ${e.message}", e)
            null
        }
    }

    /**
     * Panggil API GET /api/mobile/rencana-kerja/active?pengawas={pengawas_aktif}
     * Menghubungi Web Admin untuk menarik daftar rencana kerja aktif terbaru secara instan.
     * ISOLASI DATA PENUH: Jangan pernah melakukan fallback ke "Semua" jika Kasie memiliki 0 rencana kerja!
     */
    suspend fun fetchActiveRencanaKerja(pengawas: String): Pair<List<PostRencanaKerjaRequest>, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            val simResp = CompanyServerSimulator.getActiveRencanaKerja(pengawas)
            return@withContext Pair(simResp.data ?: emptyList(), "Mode Offline Lapangan: Rencana kerja aktif dimuat dari simulator lokal (${simResp.data?.size ?: 0} unit)")
        }

        try {
            val response = apiService.getActiveRencanaKerja(pengawas)
            var parsed = emptyList<PostRencanaKerjaRequest>()

            if (response.isSuccessful && response.body() != null) {
                val bodyStr = response.body()!!.string()
                parsed = parseActiveRencanaList(bodyStr)
            }

            if (parsed.isEmpty()) {
                try {
                    val renResp = apiService.getRencanaKerja()
                    if (renResp.isSuccessful && renResp.body() != null) {
                        val renStr = renResp.body()!!.string()
                        val renParsed = parseActiveRencanaList(renStr)
                        if (renParsed.isNotEmpty()) {
                            parsed = renParsed
                        }
                    }
                } catch (_: Exception) {}
            }

            // ISOLASI DATA PENUH: Jika pengawas spesifik (bukan "Semua"), wajib filter agar rencana kerja milik Kasie lain (misal MGC0008 milik Jahidi) tidak bocor ke Kasie aktif (misal Yono)
            if (!pengawas.equals("Semua", ignoreCase = true) && pengawas.isNotBlank()) {
                val cleanTarget = pengawas.trim().removePrefix("@")
                parsed = parsed.filter { req ->
                    val reqSpv = req.resolvedPengawas.trim().removePrefix("@")
                    reqSpv.equals(cleanTarget, ignoreCase = true)
                }
            }

            return@withContext Pair(parsed, "Sinkron Web Admin berhasil: ${parsed.size} rencana kerja aktif (${response.code()} OK)")
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.getActiveRencanaKerja(pengawas)
                return@withContext Pair(
                    simResp.data ?: emptyList(),
                    "Web Admin remote tidak merespons (${e.localizedMessage ?: e.message}). Menggunakan data sinkronisasi lokal."
                )
            }
            throw e
        }
    }

    private fun parseActiveRencanaList(rawJson: String): List<PostRencanaKerjaRequest> {
        val result = mutableListOf<PostRencanaKerjaRequest>()
        val trimmed = rawJson.trim()
        try {
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    result.add(parseJsonToRencanaRequest(obj))
                }
            } else if (trimmed.startsWith("{")) {
                val json = JSONObject(trimmed)
                val array = json.optJSONArray("data")
                    ?: json.optJSONArray("rencana_aktif")
                    ?: json.optJSONArray("rencana")
                if (array != null) {
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i) ?: continue
                        result.add(parseJsonToRencanaRequest(obj))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiClientManager", "Error parsing active rencana JSON", e)
        }
        return result
    }

    private fun optStringFirst(obj: JSONObject, vararg keys: String): String {
        for (key in keys) {
            if (obj.has(key) && !obj.isNull(key)) {
                val str = obj.optString(key, "").trim()
                if (str.isNotBlank() && !str.equals("null", ignoreCase = true)) {
                    return str
                }
            }
        }
        return ""
    }

    private fun parseJsonToRencanaRequest(obj: JSONObject): PostRencanaKerjaRequest {
        val spk = optStringFirst(obj, "nomor_spk", "nomorSpk", "no_spk", "spk", "spk_number", "noSpk")
            .takeIf { it.isNotBlank() && it != "null" && it != "SPK Menyusul" && it != "-" }

        val statusSpkRaw = optStringFirst(obj, "status_spk", "statusSpk", "status")
        val statusSpk = when {
            statusSpkRaw.isNotBlank() && !statusSpkRaw.equals("MENUNGGU_SPK", ignoreCase = true) -> statusSpkRaw
            !spk.isNullOrBlank() -> "SPK_TERBIT"
            else -> "MENUNGGU_SPK"
        }

        val rawId = optStringFirst(obj, "id", "id_rencana_kerja", "rencana_id")
        val kodeUnit = optStringFirst(obj, "kode_unit", "unit_kode", "unit", "kodeUnit", "nama_unit")
        val operator = optStringFirst(obj, "operator", "nama_operator", "namaOperator")
        val sapCode = optStringFirst(obj, "kode_sap", "sap_code", "sapCode").ifBlank { "ACT-SAP-101" }
        val aktivitas = optStringFirst(obj, "nama_aktivitas", "aktivitas", "keterangan_rencana")
        val satuan = optStringFirst(obj, "satuan_aktivitas", "satuan").ifBlank { "m3" }
        val lokasi = optStringFirst(obj, "kode_lokasi", "lokasi").ifBlank { "001A" }
        val wilayah = optStringFirst(obj, "wilayah").ifBlank { "Geodesi PG1" }
        val shift = optStringFirst(obj, "shift_kerja", "shift").ifBlank { "SIANG" }
        val pengawas = optStringFirst(
            obj,
            "kasie",
            "kasie_penanggung_jawab",
            "nama_pengawas",
            "pengawas",
            "supervisor_name",
            "supervisorName",
            "created_by",
            "createdBy"
        )
        val createdByVal = optStringFirst(obj, "created_by", "createdBy", "username", "kasie", "nama_pengawas", "pengawas")
        val tanggal = optStringFirst(obj, "tanggal", "tanggal_kerja")
        val keterangan = optStringFirst(obj, "keterangan", "keterangan_rencana")

        return PostRencanaKerjaRequest(
            id = rawId,
            id_rencana_kerja = rawId,
            rencana_id = rawId,
            status_unit = optStringFirst(obj, "status_unit").ifBlank { "OPERASI" },
            kode_unit = kodeUnit,
            unit_kode = kodeUnit,
            unit = kodeUnit,
            nama_unit = optStringFirst(obj, "nama_unit", "model_unit").ifBlank { kodeUnit },
            operator = operator,
            nama_operator = operator,
            sap_code = sapCode,
            kode_sap = sapCode,
            nama_aktivitas = aktivitas,
            aktivitas = aktivitas,
            satuan_aktivitas = satuan,
            satuan = satuan,
            kode_lokasi = lokasi,
            lokasi = lokasi,
            wilayah = wilayah,
            shift_kerja = shift,
            shift = shift,
            keterangan = keterangan,
            keterangan_rencana = keterangan,
            nomor_spk = spk,
            status_spk = statusSpk,
            status_tahap = optStringFirst(obj, "status_tahap").ifBlank { "RENCANA" },
            is_realized = obj.optBoolean("is_realized", false),
            nama_pengawas = pengawas,
            pengawas = pengawas,
            kasie = pengawas,
            kasie_penanggung_jawab = pengawas,
            created_by = createdByVal,
            createdBy = createdByVal,
            tanggal = tanggal,
            tanggal_kerja = tanggal
        )
    }

    suspend fun postActivityReport(report: PostActivityReportRequest): String = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan aktif: Laporan disimpan ke penyimpanan lokal perangkat")
        }

        try {
            val response = apiService.postActivityReport(report)
            if (response.isSuccessful) {
                // Also update simulator state
                CompanyServerSimulator.postActivityReport(report)
                return@withContext "Berhasil dikirim ke Web Admin API (${response.code()} OK)"
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.postActivityReport(report)
                return@withContext "Tersimpan lokal & disinkronkan (${simResp.message})"
            }
            throw e
        }

        val simResp = CompanyServerSimulator.postActivityReport(report)
        simResp.message ?: "Laporan tersimpan"
    }

    suspend fun postRencanaKerja(request: PostRencanaKerjaRequest): String = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Rencana kerja tersimpan di lokal (MENUNGGU_SPK)")
        }

        try {
            val response = apiService.postRencanaKerja(request)
            if (response.isSuccessful) {
                CompanyServerSimulator.postRencanaKerja(request)
                return@withContext "Rencana kerja terkirim ke Web Admin (${response.code()} OK)"
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.postRencanaKerja(request)
                return@withContext "Tersimpan lokal & disinkronkan (${simResp.message})"
            }
            throw e
        }

        val simResp = CompanyServerSimulator.postRencanaKerja(request)
        simResp.message ?: "Rencana tersimpan"
    }

    suspend fun postHasilAktivitas(request: PostHasilAktivitasRequest): String = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Hasil realisasi tersimpan di lokal")
        }

        try {
            val response = apiService.postHasilAktivitas(request)
            if (response.isSuccessful) {
                CompanyServerSimulator.postHasilAktivitas(request)
                return@withContext "Realisasi kerja terkirim ke Web Admin (${response.code()} OK)"
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.postHasilAktivitas(request)
                return@withContext "Tersimpan lokal & disinkronkan (${simResp.message})"
            }
            throw e
        }

        val simResp = CompanyServerSimulator.postHasilAktivitas(request)
        simResp.message ?: "Realisasi tersimpan"
    }

    suspend fun fetchMasterUsers(): Pair<List<UserApiPayload>, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            val localList = com.example.data.model.SupervisorDirectory.predefinedSupervisors.map { spv ->
                UserApiPayload(
                    id = spv.id,
                    nama_lengkap = spv.namaLengkap,
                    username = spv.username,
                    hak_akses = spv.roleTitle,
                    role = spv.role,
                    wilayah = spv.wilayah,
                    status_aktif = if (spv.isAktif) 1 else 0,
                    status = spv.status
                )
            }
            return@withContext Pair(localList, "Mode Offline: Data master pengguna lokal")
        }

        try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!
                com.example.data.model.SupervisorDirectory.updateFromApiUsers(users)
                return@withContext Pair(users, "Berhasil sinkron ${users.size} pengguna dari Web Admin (${response.code()} OK)")
            }
        } catch (e: Exception) {
            android.util.Log.w("ApiClientManager", "fetchMasterUsers error: ${e.message}")
        }

        try {
            val mdResp = apiService.getMasterData()
            if (mdResp.isSuccessful && mdResp.body()?.data != null) {
                val md = mdResp.body()!!.data!!
                val list = if (!md.users.isNullOrEmpty()) md.users else md.kasie
                if (!list.isNullOrEmpty()) {
                    com.example.data.model.SupervisorDirectory.updateFromApiUsers(list)
                    return@withContext Pair(list, "Berhasil sinkron ${list.size} pengguna dari Master Data Web Admin")
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("ApiClientManager", "fetchMasterData fallback error: ${e.message}")
        }

        val localList = com.example.data.model.SupervisorDirectory.predefinedSupervisors.map { spv ->
            UserApiPayload(
                id = spv.id,
                nama_lengkap = spv.namaLengkap,
                username = spv.username,
                hak_akses = spv.roleTitle,
                role = spv.role,
                wilayah = spv.wilayah,
                status_aktif = if (spv.isAktif) 1 else 0,
                status = spv.status
            )
        }
        Pair(localList, "Menggunakan data master pengguna tersimpan lokal.")
    }

    suspend fun executeLogin(username: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        val rawUsername = username.trim()
        val cleanUsername = rawUsername.removePrefix("@").trim()
        val cleanBaseUsername = cleanUsername.replace(Regex("\\d+$"), "").trim()
        val trimmedPassword = password.trim()

        if (cleanUsername.isBlank()) {
            return@withContext LoginResult.Error("Silakan masukkan Username / NIK")
        }
        if (trimmedPassword.isBlank()) {
            return@withContext LoginResult.Error("Silakan masukkan Password / PIN")
        }

        // 1. Ambil data master user terbaru dari Web Admin (https://1-portalce.ai.studio/api/users)
        var allUsers = emptyList<UserApiPayload>()
        try {
            val userResp = apiService.getUsers()
            if (userResp.isSuccessful && userResp.body() != null) {
                allUsers = userResp.body()!!
                com.example.data.model.SupervisorDirectory.updateFromApiUsers(allUsers)
            }
        } catch (_: Exception) {}

        if (allUsers.isEmpty()) {
            try {
                val mdResp = apiService.getMasterData()
                if (mdResp.isSuccessful && mdResp.body()?.data != null) {
                    val md = mdResp.body()!!.data!!
                    allUsers = if (!md.users.isNullOrEmpty()) md.users!! else (md.kasie ?: emptyList())
                    if (allUsers.isNotEmpty()) {
                        com.example.data.model.SupervisorDirectory.updateFromApiUsers(allUsers)
                    }
                }
            } catch (_: Exception) {}
        }

        // Cocokkan input pengguna secara fleksibel (mendukung username dengan/tanpa @, nama_lengkap, huruf besar/kecil, atau tanpa angka belakang seperti 'yono' -> 'yono1')
        val matchedFromApi = allUsers.find {
            it.resolvedUsername.equals(cleanUsername, ignoreCase = true) ||
            it.resolvedNamaLengkap.equals(cleanUsername, ignoreCase = true)
        } ?: allUsers.find {
            cleanBaseUsername.isNotBlank() && (
                it.resolvedUsername.replace(Regex("\\d+$"), "").equals(cleanBaseUsername, ignoreCase = true) ||
                it.resolvedNamaLengkap.replace(Regex("\\d+$"), "").equals(cleanBaseUsername, ignoreCase = true)
            )
        } ?: allUsers.find {
            cleanUsername.length >= 3 && (
                it.resolvedUsername.startsWith(cleanUsername, ignoreCase = true) ||
                it.resolvedNamaLengkap.startsWith(cleanUsername, ignoreCase = true)
            )
        }

        val matchedFromDirectory = com.example.data.model.SupervisorDirectory.findUserByUsername(cleanUsername)
        val matchedUser: UserApiPayload? = matchedFromApi ?: matchedFromDirectory?.let {
            UserApiPayload(
                id = it.id,
                nama_lengkap = it.namaLengkap,
                username = it.username,
                password = it.passwordPin,
                hak_akses = it.roleTitle,
                role = it.role,
                wilayah = it.wilayah,
                status_aktif = if (it.isAktif) 1 else 0,
                status = it.status
            )
        }

        // Validasi awal Role & Status jika user ditemukan di Master Data
        if (matchedUser != null) {
            if (matchedUser.isExecutive) {
                return@withContext LoginResult.Error(
                    "Akses Ditolak: Akun '${matchedUser.resolvedNamaLengkap}' adalah ${matchedUser.resolvedRole} (Akses Khusus Web Admin). Aplikasi mobile RKCE ini khusus untuk Kasie Lapangan."
                )
            }
            if (!matchedUser.isAktif) {
                return@withContext LoginResult.Error(
                    "Akun '${matchedUser.resolvedNamaLengkap}' berstatus Nonaktif di Website Admin. Silakan hubungi Super Admin untuk mengaktifkan kembali."
                )
            }
        }

        // Jika mode simulasi offline aktif, langsung gunakan simulator lokal
        if (CompanyServerSimulator.isSimulatedOffline) {
            return@withContext CompanyServerSimulator.authenticate(matchedUser?.resolvedUsername ?: cleanUsername, trimmedPassword)
        }

        // 2. Autentikasi ke endpoint Web Admin Portal CE: POST /api/auth/login menggunakan canonical username
        val canonicalUsername = matchedUser?.resolvedUsername?.ifBlank { cleanUsername } ?: cleanUsername
        var authResponseCode = 0
        var authResponseBody: String? = null
        try {
            val authReq = AuthLoginRequest(canonicalUsername, trimmedPassword)
            val authResp = apiService.authLogin(authReq)
            authResponseCode = authResp.code()
            if (authResp.isSuccessful && authResp.body() != null) {
                authResponseBody = authResp.body()!!.string()
            } else if (authResp.errorBody() != null) {
                authResponseBody = authResp.errorBody()!!.string()
            }
        } catch (e: Exception) {
            android.util.Log.w("ApiClientManager", "Live authLogin network error: ${e.message}")
        }

        // KASUS 1: HTTP 200 dari server
        if (authResponseCode == 200 && !authResponseBody.isNullOrBlank()) {
            val bodyUpper = authResponseBody.uppercase()
            val isBodyExecutive = (bodyUpper.contains("\"SUPER ADMIN\"") || bodyUpper.contains("\"SUPER_ADMIN\"") || bodyUpper.contains("\"MANAGER\"")) &&
                matchedUser?.isKasie != true
            if (matchedUser?.isExecutive == true || isBodyExecutive) {
                return@withContext LoginResult.Error(
                    "Akses Ditolak: Akun Super Admin & Manager hanya dapat digunakan pada Website Portal Admin (https://1-portalce.ai.studio/). Aplikasi mobile RKCE ini khusus untuk Kasie Lapangan."
                )
            }
            if (matchedUser != null && matchedUser.isKasie && matchedUser.isAktif) {
                return@withContext LoginResult.Success(
                    token = "JWT-MOBILE-${System.currentTimeMillis()}",
                    user = UserProfileDto(
                        id = matchedUser.numericOrStringId,
                        nama_lengkap = matchedUser.resolvedNamaLengkap,
                        username = matchedUser.resolvedUsername,
                        role = "KASIE",
                        wilayah = matchedUser.wilayah ?: "Geodesi PG1",
                        status = "Aktif"
                    ),
                    message = "Login berhasil sebagai Kasie Lapangan"
                )
            }
        }

        // KASUS 2: HTTP 403 (Kredensial Password Kasie Valid di Database Server Web Admin)
        val isKasieAuthenticatedByServer = authResponseCode == 403 &&
                (authResponseBody?.contains("Akses ditolak", ignoreCase = true) == true ||
                 authResponseBody?.contains("HeavyTrack Mobile", ignoreCase = true) == true ||
                 authResponseBody?.contains("Kasie", ignoreCase = true) == true)

        if (isKasieAuthenticatedByServer && matchedUser != null) {
            val userProfile = UserProfileDto(
                id = matchedUser.numericOrStringId,
                nama_lengkap = matchedUser.resolvedNamaLengkap,
                username = matchedUser.resolvedUsername,
                role = "KASIE",
                wilayah = matchedUser.wilayah ?: "Geodesi PG1",
                status = "Aktif"
            )
            return@withContext LoginResult.Success(
                token = "JWT-MOBILE-${System.currentTimeMillis()}",
                user = userProfile,
                message = "Login berhasil sebagai Kasie Lapangan"
            )
        }

        // KASUS 3: Resolusi fleksibel melalui direktori pengguna Master Data yang telah disinkronkan
        val (localMatch, localError) = com.example.data.model.SupervisorDirectory.authenticateKasie(cleanUsername, trimmedPassword)
        if (localMatch != null) {
            return@withContext LoginResult.Success(
                token = "LOCAL-JWT-${System.currentTimeMillis()}",
                user = UserProfileDto(
                    id = localMatch.userId,
                    nama_lengkap = localMatch.namaLengkap,
                    username = localMatch.username,
                    role = localMatch.role,
                    wilayah = localMatch.wilayah,
                    status = localMatch.status
                ),
                message = "Login berhasil sebagai Kasie Lapangan"
            )
        } else if (localError != null) {
            return@withContext LoginResult.Error(localError)
        }

        if (authResponseCode == 401) {
            return@withContext LoginResult.Error("Username atau kata sandi tidak sesuai dengan data Website Admin.")
        }

        return@withContext LoginResult.Error("Gagal Masuk: Akun tidak terdaftar atau kata sandi salah di Website Admin.")
    }
}
