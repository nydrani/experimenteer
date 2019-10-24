package xyz.velvetmilk.testingtool.di

import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import xyz.velvetmilk.testingtool.net.*
import xyz.velvetmilk.testingtool.services.ApplicationCounter

@ApplicationScope
@Component(modules = [ApplicationModule::class, NetworkModule::class])
interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        fun create(applicationModule: ApplicationModule, @BindsInstance networkModule: NetworkModule): ApplicationComponent
    }

    val applicationCounter: ApplicationCounter
    val okHttpClient: OkHttpClient
    val gsonCounterFactory: GsonConverterFactory
    val scalarsCounterFactory: ScalarsConverterFactory

    val rawClient: RawClient
    val rawServer: RawServer
    val secureClient: SecureClient
    val secureServer: SecureServer
    val sslManager: SslManager
}
