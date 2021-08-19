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
        private val groups: List<Long>? =
            if (setting.groups.isNotEmpty()) setting.groups.toMutableList() else null//群号列表。没有群号则为 null
        private var groupMessage: MutableMap<Long, MessageArray> = mutableMapOf()//所有群消息的入口

        /**添加一条群消息*/
        fun add(event: GroupMessageEvent) {
            //如果 groupMessage 中有这个群，就直接添加群消息
            groupMessage[event.group.id]?.let {
                it.add(event)
                return@add
            }
            //如果 groupMessage 中没有这个群，groups 也没有记录有这个群，就跳过
            groups?.let {
                if (event.group.id !in groups)
                    return@add
            }
            //如果 groupMessage 中没有这个群且 groups 为 null，就无差别记录群消息
            groupMessage[event.group.id] = MessageArray()//为这个群都新建一个群消息数组
            groupMessage[event.group.id]?.add(event)
        }

        /**进行一次防撤回*/
        suspend fun detect(event: MessageRecallEvent.GroupRecall) {
            groups?.let {//若 group 为 null 则进行检查
                if (event.group.id !in groups)// 若这个群不包含在所设置的 groups 内
                    return@detect
            }

            /**寻找历史记录*/
            fun find(): String? {
//            return messageArray.find { it?.ids.contentEquals(messageIds) ?: false }?.content
                return groupMessage[event.group.id]?.messageArray?.find {
                    it?.ids.contentEquals(event.messageIds)
                }?.content
            }

            if (event.operator?.id == event.authorId) {//如果是群员主动撤回自己消息
                find()?.let {//查找已存的历史记录中是否有这条消息
                    event.group.sendMessage("${event.author.nameCardOrNick}(${event.authorId})试图撤回消息：\n\n${it}")
                }
            }
        }

        private inner class MessageArray { //包含指定长度的群消息数组。各个群有各自一个对应的 messageArray
            var messageArray = arrayOfNulls<ContentAndIds>(length)
            var currentPosition = 0//群消息指针最开始为 0

            /**添加一条群消息*/
            fun add(event: GroupMessageEvent) {
                messageArray[currentPosition++] = ContentAndIds(event.message.contentToString(), event.message.ids)
                currentPosition %= length
            }

            inner class ContentAndIds(val content: String, val ids: IntArray) //用来存储一条群消息，包含文字信息与消息 id
        }
    }

    object Setting : ReadOnlyPluginConfig("Setting") {
        @ValueDescription("群历史消息暂存数量")
        val length: Int by value(20)

        @ValueDescription(
            """开启防撤回功能的群，多个群号用英文逗号隔开。
            群号为空则对所有群开启功能""")
        val groups: List<Long> by value()
    }
}

