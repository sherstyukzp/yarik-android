package com.yarik.android

import android.app.Application
import com.yarik.mesh.YarikMesh

class YarikApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        YarikMesh.initialize(this)
    }
}
