package com.ruflo.footballquiz

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.ruflo.footballquiz.core.AppContainer
import com.ruflo.footballquiz.sync.SyncScheduler

class FootballQuizApplication : Application() {

    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components { add(SvgDecoder.Factory()) }
                .build()
        )
        SyncScheduler.schedulePeriodicSync(this)
    }
}
