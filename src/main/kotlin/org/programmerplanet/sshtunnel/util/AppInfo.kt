package org.programmerplanet.sshtunnel.util

import java.util.Properties

object AppInfo {
    val title: String
    val version: String
    val site: String

    init {
        val props = Properties()
        AppInfo::class.java.getResourceAsStream("/appinfo.properties")?.use {
            props.load(it)
        }
        title = props.getProperty("app.title") ?: "SSH Tunnel NG"
        version = props.getProperty("app.version") ?: "0.1"
        site = props.getProperty("app.site") ?: "https://example.com"
    }
}
