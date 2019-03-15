package com.squarepolka.readyci.tasks.app.android

import com.squarepolka.readyci.util.Util
import java.io.File

object AndroidUtil {
    fun findAllApkOutputs(dir: String) = Util.findAllByExtension(File(dir), ".apk")
            .filter {
                it.absolutePath.contains("build/outputs")
            }
}