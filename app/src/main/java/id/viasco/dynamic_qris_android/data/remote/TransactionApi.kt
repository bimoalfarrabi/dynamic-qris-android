package id.viasco.dynamic_qris_android.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TransactionApi {

    @GET("transactions")
    suspend fun list(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("page") page: Int = 1,
    ): PaginatedResponse<TransactionDto>

    @GET("transactions/{id}")
    suspend fun show(@Path("id") id: String): SingleResponse<TransactionDto>

    @POST("transactions")
    suspend fun create(@Body body: CreateTransactionRequest): SingleResponse<TransactionDto>

    @POST("transactions/{id}/cancel")
    suspend fun cancel(@Path("id") id: String): SingleResponse<TransactionDto>
}
