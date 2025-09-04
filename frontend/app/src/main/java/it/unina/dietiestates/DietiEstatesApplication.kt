package it.unina.dietiestates

import android.app.Application
import it.unina.dietiestates.di.initKoin
import org.koin.android.ext.koin.androidContext

class DietiEstatesApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@DietiEstatesApplication)
        }
    }
}