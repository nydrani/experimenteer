package xyz.velvetmilk.testingtool.di

import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient
import xyz.velvetmilk.testingtool.ApplicationCounter

@ApplicationScope
@Component(modules = [ApplicationModule::class, NetworkModule::class])
interface ApplicationComponent {

    @Component.Factory
    interface Factory {
        fun create(applicationModule: ApplicationModule, @BindsInstance networkModule: NetworkModule): ApplicationComponent
    }

    val applicationCounter: ApplicationCounter
    val okHttpClient: OkHttpClient
}
