package xyz.velvetmilk.testingtool.di

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import xyz.velvetmilk.testingtool.net.RawClient
import xyz.velvetmilk.testingtool.net.RawServer

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
    fun provideInsecureServer(): RawServer {
        return RawServer()
    }

    @Provides
    @ApplicationScope
    fun provideInsecureClient(): RawClient {
        return RawClient()
    }
}
