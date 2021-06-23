package com.seahahn.routinemaker.sns.chat

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.impl.WorkDatabaseMigrations.MIGRATION_1_2
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

@Entity(tableName = "chat_room")
data class ChatRoom(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "is_groupchat") val isGroupchat: Boolean,
    @ColumnInfo(name = "host_id") val hostId: Int,
    @ColumnInfo(name = "audience_id") val audienceId: Int,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "lastmsg") val lastmsg: String,
    @ColumnInfo(name = "lastmsg_at") val lastmsgAt: String,
    @ColumnInfo(name = "msg_badge") val msgBadge: Int
)

@Dao
interface ChatDao {

    @Insert
    fun insertChatMsg(chatMsg: ChatMsg)

    @Insert
    fun insertChatRoom(chatRoom: ChatRoom)

    @Update
    fun updateBadge(chatRoom: ChatRoom)

    @Delete
    fun deletechatroom(chatRoom: ChatRoom)

//    @Delete
//    fun deletechatMsg(chatMsg: ChatMsg)
//
//    @Delete
//    fun deletechatMsgs(vararg chatMsg: ChatMsg)

//    @Query("SELECT * FROM chat_room WHERE is_groupchat=:isGroupchat AND user_id=:userId AND audience_id=:audienceId")
//    fun getChatRoomId(isGroupchat : Boolean, userId : Int, audienceId : Int): ChatMsg

    @Query("SELECT * FROM chat_room WHERE id = :id")
    fun getChatroom(id: Int): ChatRoom

    @Query("SELECT * FROM chat_room ORDER BY lastmsg_at ASC")
    fun getChatrooms(): LiveData<MutableList<ChatRoom>>

    @Query("SELECT * FROM chat_msg WHERE room_id = :roomId ORDER BY created_at ASC")
    fun getChatMsgs(roomId: Int): LiveData<MutableList<ChatMsg>>
//    fun getChatMsgsDUC(roomId : Int) =
//        getChatMsgs(roomId).distinctUntilChanged()

    @Query("SELECT * FROM chat_msg WHERE room_id = :roomId ORDER BY created_at DESC LIMIT 1")
    fun getLastChatMsg(roomId: Int): LiveData<ChatMsg>
//    fun getLastChatMsgDUC(roomId : Int) =
//        getLastChatMsg(roomId).distinctUntilChanged()
}

@Database(entities = [ChatMsg::class, ChatRoom::class], version = 3)
abstract class ChatDataBase: RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        private var chatDBInstance: ChatDataBase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `chat_room` " +
                        "(`id` INTEGER NOT NULL, " +
                        "`is_groupchat` INTEGER NOT NULL, " +
                        "`host_id` INTEGER NOT NULL, " +
                        "`audience_id` INTEGER NOT NULL, " +
                        "`created_at` TEXT NOT NULL, " +
                        "`lastmsg` TEXT NOT NULL, " +
                        "`lastmsg_at` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE chat_room ADD COLUMN msg_badge INTEGER DEFAULT 0 NOT NULL")
            }
        }

        fun getInstance(context: Context): ChatDataBase? {
            if(chatDBInstance == null) {
                synchronized(ChatDataBase::class){
                    chatDBInstance = Room.databaseBuilder(context.applicationContext, ChatDataBase::class.java, "rtmaker.db")
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .build()
                }
            }
            return chatDBInstance
        }
    }
}
