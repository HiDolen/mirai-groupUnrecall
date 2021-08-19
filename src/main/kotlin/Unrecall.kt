package org.example

import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.message.data.ids

object Unrecall {
    class GroupMessageHistory(setting: Setting) {
        private val length: Int = Setting.length//存储信息的数量
        private var messageArray: Array<ContentIds?> = arrayOfNulls<ContentIds>(length)
        private val groups: List<Long>? = if (setting.groups.isNotEmpty()) setting.groups.toMutableList() else null

        private var currentPosition = 0//群消息指针最开始为 0

        /**添加一条群消息*/
        fun add(event: GroupMessageEvent) {
            groups?.let {//若 group 为 null 则记录所有群的聊天记录
                if (event.group.id !in groups)// 若这个群不包含在所设置的 groups 内
                    return@add
            }
            messageArray[currentPosition++] = ContentIds(event.message.contentToString(), event.message.ids)
            currentPosition %= length
        }

        /**寻找历史记录*/
        private fun find(messageIds: IntArray): String? {
            return messageArray.find { it?.ids.contentEquals(messageIds) ?: false }?.content
        }

        /**进行一次防撤回*/
        suspend fun detect(event: MessageRecallEvent.GroupRecall) {
            groups?.let {//若 group 为 null 则进行检查
                if (event.group.id !in groups)// 若这个群不包含在所设置的 groups 内
                    return@detect
            }
            if (event.operator?.id == event.authorId) {//如果是群员主动撤回自己消息
                this.find(event.messageIds)?.let {//查找已存的历史记录中是否有这条消息
                    event.group.sendMessage("${event.author.nameCardOrNick}(${event.authorId})试图撤回消息：\n\n${it}")
                }
            }
        }

        class ContentIds(val content: String, val ids: IntArray)//用来存储文字信息，和消息 id
    }

    object Setting : ReadOnlyPluginConfig("Unrecall") {
        @ValueDescription("群历史消息暂存数量")
        val length: Int by value(100)

        @ValueDescription("进行监听的群。为空则监听所有")
        val groups: List<Long> by value()
    }
}

