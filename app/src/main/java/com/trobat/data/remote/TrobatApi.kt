package com.trobat.data.remote

import com.trobat.data.remote.dto.CasoDto
import com.trobat.data.remote.dto.CasosPaginadosDto
import com.trobat.data.remote.dto.CrearResponseDto
import com.trobat.data.remote.dto.LoginRequestDto
import com.trobat.data.remote.dto.LogoutRequestDto
import com.trobat.data.remote.dto.MensajeResponseDto
import com.trobat.data.remote.dto.RegistroRequestDto
import com.trobat.data.remote.dto.ReporteDto
import com.trobat.data.remote.dto.ReportesPaginadosDto
import com.trobat.data.remote.dto.TokenResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TrobatApi {

    @POST("auth/registro")
    suspend fun registro(@Body body: RegistroRequestDto): Response<CrearResponseDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<TokenResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequestDto): Response<MensajeResponseDto>

    @GET("casos")
    suspend fun getCasos(
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<CasosPaginadosDto>

    @GET("casos/{id}")
    suspend fun getCaso(@Path("id") id: String): Response<CasoDto>

    @GET("casos/{id}/reportes")
    suspend fun getReportesDeCaso(@Path("id") id: String): Response<List<ReporteDto>>

    @Multipart
    @POST("reportes")
    suspend fun crearReporte(
        @Part foto: MultipartBody.Part?,
        @Part("datos") datos: RequestBody
    ): Response<CrearResponseDto>

    @GET("reportes")
    suspend fun getReportes(
        @Query("caso_id") casoId: String? = null,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<ReportesPaginadosDto>

    @GET("reportes/{id}")
    suspend fun getReporte(@Path("id") id: String): Response<ReporteDto>

    @PATCH("reportes/{id}/validar")
    suspend fun validarReporte(
        @Path("id") id: String,
        @Body body: Map<String, Boolean>
    ): Response<MensajeResponseDto>
}
