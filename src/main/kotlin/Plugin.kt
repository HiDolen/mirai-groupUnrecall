package org.example

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

object Plugin : KotlinPlugin(
    JvmPluginDescription(
        id = "org.example.plugin",
        version = "1.0-SNAPSHOT",
    )) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
    }
}