package com.bitchat.android

import android.app.Application
import com.bitchat.android.mesh.MeshRuntime

/**
 * Legacy application hook retained for upstream UI/tests inside :yarik-mesh.
 * The Yarik app shell uses [com.yarik.mesh.YarikMesh] instead.
 */
class BitchatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MeshRuntime.start(this)
    }
}
