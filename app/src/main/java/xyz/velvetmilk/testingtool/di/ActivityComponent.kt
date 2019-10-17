package xyz.velvetmilk.testingtool.di

import dagger.Component
import xyz.velvetmilk.testingtool.*

@ActivityScope
@Component(dependencies = [ApplicationComponent::class],
    modules = [ActivityModule::class])
interface ActivityComponent {

    @Component.Factory
    interface Factory {
        fun create(applicationComponent: ApplicationComponent, activityModule: ActivityModule): ActivityComponent
    }

    fun inject(activity: DaggerActivity)
    fun inject(activity: SocketActivity)
    fun inject(activity: SecureSocketActivity)
    fun inject(activity: PlayServicesActivity)
    fun inject(activity: RNGActivity)
}
