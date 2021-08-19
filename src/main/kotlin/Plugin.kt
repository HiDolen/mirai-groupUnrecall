package org.example

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.info

object Plugin : KotlinPlugin(
    JvmPluginDescription(
        id = "org.HiDolen.mirai-groupUnrecall",
        name="groupUnrecall",
        version = "0.5",
    )) {
    override fun onEnable() {
        logger.info { "groupUnrecall Plugin loaded" }

        Unrecall.Setting.reload()//加载配置文件
        val unrecall = Unrecall.GroupMessageHistory(Unrecall.Setting)//防撤回模块初始化

        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            //防撤回模块存储群消息
            unrecall.add(this)
        }

        globalEventChannel().subscribeAlways<MessageRecallEvent.GroupRecall> {
            //尝试防撤回
            unrecall.detect(this)
        }
    }
}