package com.ruflo.footballquiz

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.ruflo.footballquiz.core.AppContainer
import com.ruflo.footballquiz.sync.SyncScheduler
import okhttp3.OkHttpClient

class FootballQuizApplication : Application() {

    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components { add(SvgDecoder.Factory()) }
                // Some badge CDNs (e.g. assets.football-logos.cc) 403 OkHttp's default
                // "okhttp/x.x.x" User-Agent as bot traffic; a browser-like one passes.
                .okHttpClient {
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            chain.proceed(
                                chain.request().newBuilder()
                                    .header(
                                        "User-Agent",
                                        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                                            "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
                                    )
                                    .build()
                            )
                        }
                        .build()
                }
                .build()
        )
        SyncScheduler.schedulePeriodicSync(this)
    }
}
