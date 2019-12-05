package com.twilio.video.app

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.runner.AndroidJUnitRunner
import com.squareup.rx2.idler.Rx2Idler
import com.twilio.video.app.idlingresource.IdlingResourceModule
import io.reactivex.plugins.RxJavaPlugins

class InstrumentationRunner : AndroidJUnitRunner() {

    override fun onStart() {
        val resource = CountingIdlingResource("Counting Resource")
        IdlingRegistry.getInstance().register(resource)
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("RxJava 2.x IO Scheduler"))

        ApplicationProvider.getApplicationContext<UiTestApp>().run {
            val applicationComponent = DaggerUITestComponent.builder()
                    .applicationModule(ApplicationModule(this))
                    .idlingResourceModule(IdlingResourceModule(CountingIdlingResourceWrapper(resource)))
                    .build()
            applicationComponent.inject(this)
        }

        super.onStart()
    }

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, UiTestApp::class.java.name, context)
    }


}