package xyz.velvetmilk.testingtool.di

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import xyz.velvetmilk.testingtool.net.*

@Module
class NetworkModule {

    @Provides
    @ApplicationScope
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor())
            .build()
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
    fun provideSecureClient(sslManager: SSLManager): SecureClient {
        return SecureClient(sslManager)
    }


    @Provides
    @ApplicationScope
    fun provideSecureServer(sslManager: SSLManager): SecureServer {
        return SecureServer(sslManager)
    }

    @Provides
    @ApplicationScope
    fun provideSSLManager(): SSLManager {
        return SSLManager()
    }
}
