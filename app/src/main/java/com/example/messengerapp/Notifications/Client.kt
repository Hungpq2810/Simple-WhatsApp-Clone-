import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response // Make sure this import is present
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // Or your preferred converter

object Client {

    // Keep track of the client to potentially reuse it (optional optimization)
    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var lastBaseUrl: String? = null
    private var lastAccessToken: String? = null

    fun getClient(baseUrl: String, accessToken: String): Retrofit {
        // --- Optimization: Reuse client if base URL and token haven't changed ---
        if (okHttpClient != null && retrofit != null && baseUrl == lastBaseUrl && accessToken == lastAccessToken) {
            // Log.d("Client", "Reusing existing OkHttpClient and Retrofit instance")
            return retrofit!!
        }
        // --- End Optimization ---

        // Log.d("Client", "Creating new OkHttpClient and Retrofit instance")

        // 1. Create an Interceptor to add headers
        val headerInterceptor = Interceptor { chain ->
            // Get the original request
            val originalRequest = chain.request()

            // Build a new request builder from the original request
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Authorization", "key=$accessToken")
            // **** REMOVED THE PROBLEMATIC .method() CALL ****

            val newRequest = requestBuilder.build()
            chain.proceed(newRequest)
        }

        // 2. Create OkHttpClient and add the interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            // Add other configurations if needed (e.g., timeouts, logging interceptor)
            // .connectTimeout(30, TimeUnit.SECONDS)
            // .readTimeout(30, TimeUnit.SECONDS)
            .build()

        // --- Store the created client and parameters for potential reuse ---
        okHttpClient = client
        lastBaseUrl = baseUrl
        lastAccessToken = accessToken
        // --- End Storing ---


        // 3. Create Retrofit instance using the configured OkHttpClient
        val newRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client) // Use the OkHttpClient with the interceptor
            .addConverterFactory(GsonConverterFactory.create()) // !! IMPORTANT: Add a converter factory (e.g., Gson, Moshi)
            .build()

        retrofit = newRetrofit
        return newRetrofit
    }
}