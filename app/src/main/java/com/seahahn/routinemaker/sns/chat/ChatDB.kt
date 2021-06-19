package com.seahahn.routinemaker.sns.chat

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Entity(tableName = "chat_msg")
data class ChatMsg(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "writer_id") val writerId: Int,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "content_type") val contentType: Int,
    @ColumnInfo(name = "room_id") val roomId: Int,
    @ColumnInfo(name = "created_at") val createdAt: String
)

//@Entity(tableName = "chat_room")
//data class ChatRoom(
//    @PrimaryKey
//    @ColumnInfo(name = "id") val id: Int,
//    @ColumnInfo(name = "is_groupchat") val isGroupchat: Boolean,
//    @ColumnInfo(name = "user_id") val userId: Int,
//    @ColumnInfo(name = "audience_id") val audienceId: Int,
//    @ColumnInfo(name = "member_list") val memberList: String,
//    @ColumnInfo(name = "created_at") val createdAt: String
//)

@Dao
interface ChatDao {

    @Insert
    fun insertChatMsg(chatMsg: ChatMsg)
//
//    @Insert
//    fun insertChatRoom(chatRoom: ChatRoom)

    @Update
    fun updatechatMsg(chatMsg: ChatMsg)

    @Delete
    fun deletechatMsg(chatMsg: ChatMsg)

    @Delete
    fun deletechatMsgs(vararg chatMsg: ChatMsg)

//    @Query("SELECT * FROM chat_room WHERE is_groupchat=:isGroupchat AND user_id=:userId AND audience_id=:audienceId")
//    fun getChatRoomId(isGroupchat : Boolean, userId : Int, audienceId : Int): ChatMsg

    @Query("SELECT * FROM chat_msg WHERE room_id = :roomId ORDER BY created_at ASC")
    fun getChatMsgs(roomId: Int): LiveData<MutableList<ChatMsg>>
//    fun getChatMsgsDUC(roomId : Int) =
//        getChatMsgs(roomId).distinctUntilChanged()

    @Query("SELECT * FROM chat_msg WHERE room_id = :roomId ORDER BY created_at ASC LIMIT 1")
    fun getLastChatMsg(roomId: Int): LiveData<ChatMsg>
//    fun getLastChatMsgDUC(roomId : Int) =
//        getLastChatMsg(roomId).distinctUntilChanged()
}

@Database(entities = [ChatMsg::class], version = 1)
abstract class ChatDataBase: RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        private var chatDBInstance: ChatDataBase? = null

        fun getInstance(context: Context): ChatDataBase? {
            if(chatDBInstance == null) {
                synchronized(ChatDataBase::class){
                    chatDBInstance = Room.databaseBuilder(context.applicationContext, ChatDataBase::class.java, "rtmaker.db").build()
                }
            }
            return chatDBInstance
        }
    }
}
