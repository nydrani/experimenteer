package xyz.velvetmilk.testingtool.di

import android.app.Activity
import dagger.Module
import dagger.Provides
import xyz.velvetmilk.testingtool.ActivityCounter

@Module
class ActivityModule(private val activity: Activity) {

    @Provides
    @ActivityScope
    fun provideActivity(): Activity = activity

    @Provides
    @ActivityScope
    fun provideActivityCounter(): ActivityCounter {
        return ActivityCounter()
    }
}
