package xyz.velvetmilk.testingtool.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import xyz.velvetmilk.testingtool.ApplicationCounter

@Module
class ApplicationModule(private val application: Application) {

    @Provides
    @ApplicationScope
    fun provideApplicationContext(): Context = application.applicationContext

    @Provides
    @ApplicationScope
    fun provideApplicationCounter(): ApplicationCounter {
        return ApplicationCounter()
    }
}
