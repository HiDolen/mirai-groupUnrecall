package org.example

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.info

object Plugin : KotlinPlugin(
    JvmPluginDescription(
        id = "org.HiDolen.mirai-groupUnrecall",
        name="groupUnrecall",
        version = "0.5",
    )) {
    override fun onEnable() {
        logger.info { "groupUnrecall Plugin loaded" }

        //防撤回模块初始化
        Unrecall.Setting.reload()
        val unrecall = Unrecall.GroupMessageHistory(Unrecall.Setting)



        globalEventChannel().subscribeAlways<GroupMessageEvent> {//群消息
            var content = message.contentToString()

            //防撤回模块存储群消息
            unrecall.add(this)

            //回复“hi bot”
            if (message.contentToString() == "hi bot") {
                //群内发送
                group.sendMessage(PlainText("hi ") + At(sender))//回应那个人的打招呼
//                //向发送者私聊发送消息
                sender.sendMessage("hi")

                //不继续处理
                return@subscribeAlways
            }
        }

        //消息撤回
        globalEventChannel().subscribeAlways<MessageRecallEvent.GroupRecall> {
            //防撤回
            unrecall.detect(this)
        }
    }
}