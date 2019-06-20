package xyz.velvetmilk.testingtool.di

import dagger.Component
import xyz.velvetmilk.testingtool.DaggerActivity

@ActivityScope
@Component(dependencies = [ApplicationComponent::class],
    modules = [ActivityModule::class])
interface ActivityComponent {

    @Component.Factory
    interface Factory {
        fun create(applicationComponent: ApplicationComponent, activityModule: ActivityModule): ActivityComponent
    }

    fun inject(activity: DaggerActivity)
}
