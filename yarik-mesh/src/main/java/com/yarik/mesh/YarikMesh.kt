package com.yarik.mesh

import android.app.Application
import com.bitchat.android.mesh.MeshRuntime

/**
 * Public entry point for the Yarik app shell to start mesh / Nostr background runtime.
 */
object YarikMesh {
    fun initialize(application: Application) {
        MeshRuntime.start(application)
    }
}
