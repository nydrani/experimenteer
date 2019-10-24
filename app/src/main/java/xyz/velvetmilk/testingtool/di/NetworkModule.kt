package xyz.velvetmilk.testingtool.di

import dagger.Module
import dagger.Provides
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import xyz.velvetmilk.testingtool.net.*
import javax.inject.Named
import javax.inject.Qualifier

@Module
class NetworkModule {

    @Provides
    @ApplicationScope
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor())
            .addNetworkInterceptor(GzipInterceptor())
            .build()
    }

    @Provides
    @ApplicationScope
    fun provideScalarsConverterFactory(): ScalarsConverterFactory {
        return ScalarsConverterFactory.create()
    }

    @Provides
    @ApplicationScope
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @ApplicationScope
    fun provideRawServer(): RawServer {
        return RawServer()
    }

    @Provides
    @ApplicationScope
    fun provideRawClient(): RawClient {
        return RawClient()
    }


    @Provides
    @ApplicationScope
    fun provideSecureClient(sslManager: SslManager): SecureClient {
        return SecureClient(sslManager)
    }


    @Provides
    @ApplicationScope
    fun provideSecureServer(sslManager: SslManager): SecureServer {
        return SecureServer(sslManager)
    }

    @Provides
    @ApplicationScope
    fun provideSSLManager(): SslManager {
        return SslManager()
    }
}
