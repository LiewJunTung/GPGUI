package com.liewjuntung.screens

import java.util.*


// Operating systems.
private var os: OS? = null
val operatingSystem: OS?
    get() {
        if (os == null) {
            val operSys = System.getProperty("os.name").lowercase(Locale.getDefault())
            if (operSys.contains("win")) {
                os = OS.WINDOWS
            } else if (operSys.contains("nix") || operSys.contains("nux")
                || operSys.contains("aix")
            ) {
                os = OS.LINUX
            } else if (operSys.contains("mac")) {
                os = OS.MAC
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS
            }
        }
        return os
    }

enum class OS {
    WINDOWS, LINUX, MAC, SOLARIS
}
