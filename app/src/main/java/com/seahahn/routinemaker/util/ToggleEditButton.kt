package com.seahahn.routinemaker.util

//import android.support.graphics.drawable.AnimatedVectorDrawableCompat
//import android.support.v7.widget.AppCompatImageButton
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log.d
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitClient
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.util.UserInfo.getUserId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Created by camerash on 4/26/18.
 * Controller button for controlling single / grouped ToggleEditTextView(s)
 */
class ToggleEditButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int): AppCompatImageButton(context, attrs, defStyleAttr) {

    private val TAG = this::class.java.simpleName
    private var service : RetrofitService
    private lateinit var retrofit : Retrofit

    private val tetvArrayList = arrayListOf<ToggleEditTextView>()
//    private val editToConfirmAnim = AnimatedVectorDrawableCompat.create(context, R.drawable.edit_to_confirm_anim)
//    private val confirmToEditAnim = AnimatedVectorDrawableCompat.create(context, R.drawable.confirm_to_edit_anim)

    private val editToConfirmAnim = AnimatedVectorDrawableCompat.create(context, R.drawable.edit_to_confirm)
    private val confirmToEditAnim = AnimatedVectorDrawableCompat.create(context, R.drawable.confirm_to_edit)
    private val initBtn = AnimatedVectorDrawableCompat.create(context, R.drawable.init_edit)

    private var onClickListener: OnClickListener? = null

    private var editing = false

    private var animationOffset = 100L

    private var isNick = false
    var textInput = ""
    var firstBind = false
    var ret = false

    companion object {
        const val SUPER_STATE_KEY = "super_state"
        const val EDIT_KEY = "edit"
    }

    constructor(context: Context): this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet): this(context, attrs, 0)

    init {
        d(TAG, "init")
        // 레트로핏 통신 연결
        service = initRetrofit()

        if(attrs != null) {
            val styled = getContext().obtainStyledAttributes(attrs, R.styleable.ToggleEditButton)

            initAttributes(styled)

            styled.recycle()
        }

        super.setOnClickListener {
            d(TAG, "click")
//            setEditing(service, textInput, !editing, true, isNick)
            onClickListener?.onClick(it)
        }
    }

    private fun initAttributes(styled: TypedArray) {
        d(TAG, "initAttributes")
        val tint = styled.getColor(R.styleable.ToggleEditButton_teb_tint, Color.BLACK)
        setTint(tint)

        val editing = styled.getBoolean(R.styleable.ToggleEditButton_teb_edit, false)
        d(TAG, "initAttributes editing : $editing")
        setEditing(service, textInput, editing, false, isNick)

        val offset = styled.getInteger(R.styleable.ToggleEditButton_teb_animationOffset, 100).toLong()
        setAnimationOffset(offset)
    }

    private fun resetButton(animate: Boolean) {
        d(TAG, "resetButton animate : $animate")
        d(TAG, "resetButton this.editing : "+this.editing)
        editToConfirmAnim?.stop()
        confirmToEditAnim?.stop()

        if(animate) {
            d(TAG, "animate")
            setImageDrawable(if (this.editing) editToConfirmAnim else confirmToEditAnim)
            if (this.editing) editToConfirmAnim?.start() else confirmToEditAnim?.start()
        } else {
            d(TAG, "!animate")
            setImageDrawable(initBtn)
        }
    }

    fun getEditing(): Boolean = this.editing

    fun setEditing(service: RetrofitService, text: String, editing: Boolean, animate: Boolean, isNick: Boolean) {
        d(TAG, "setEditing")
        d(TAG, "setEditing this.editing : " + this.editing)
        d(TAG, "setEditing tetvArrayList : $tetvArrayList")

        if(isNick) {
            d(TAG, "ret : $ret")
            if(ret) {
                d(TAG, "닉네임 체크 완료")
                this.editing = editing
                if(animate) {
                    d(TAG, "setEditing ret animate")
                    d(TAG, "setEditing tetvArrayList : $tetvArrayList")
                    tetvArrayList.forEachIndexed { i, tetv ->
                        tetv.setEditTextEnabled(this.editing)
                        Handler().postDelayed({ tetv.setEditing(this.editing, animate, isNick) }, animationOffset*i)
                    }
                    if(firstBind) unbindAll()
                    resetButton(animate)
                } else {
                    d(TAG, "setEditing ret !animate")
                    tetvArrayList.forEach { it.setEditing(this.editing, animate, isNick) }
                    resetButton(animate)
                }
            }
        } else {
            this.editing = editing
            if(animate) {
                d(TAG, "setEditing !ret animate")
                tetvArrayList.forEachIndexed { i, tetv ->
                    tetv.setEditTextEnabled(this.editing)
                    Handler().postDelayed({ tetv.setEditing(this.editing, animate, isNick) }, animationOffset*i)
                }
                if(firstBind) unbindAll()
                resetButton(animate)
            } else {
                d(TAG, "setEditing !ret !animate")
                tetvArrayList.forEach { it.setEditing(this.editing, animate, isNick) }
                resetButton(animate)
            }
        }
    }

    fun setTint(color: Int) {
        editToConfirmAnim?.setTint(color)
        confirmToEditAnim?.setTint(color)
        initBtn?.setTint(color)
    }

    fun bind(text: String, toggleEditTextView: ToggleEditTextView) {
        d(TAG, "bind")
        textInput = text
        d(TAG, "textInput : $textInput")
        d(TAG, "toggleEditTextView : $toggleEditTextView")

        if(!tetvArrayList.contains(toggleEditTextView)) tetvArrayList.add(toggleEditTextView)
        d(TAG, "tetvArrayList : $tetvArrayList")
        tetvArrayList.forEach {
            if(it.id == R.id.nick) {
                d(TAG, "bind isNick")
                isNick = true
                it.setEditing(this.editing, false, isNick)
            } else {
                d(TAG, "bind !isNick")
                isNick = false
                it.setEditing(this.editing, false, isNick)
                if(firstBind) {
                    setEditing(service, textInput, !editing, true, isNick)
                    d(TAG, "this.editing : "+this.editing)
                    if(!this.editing) changeInfo(service, "intro", text)
                }
            }
        }
        d(TAG, "firstBind : $firstBind")
        if(firstBind && isNick) {
            checkNick(service, textInput)
        }
        firstBind = true
    }

    fun unbind(toggleEditTextView: ToggleEditTextView) {
        tetvArrayList.remove(toggleEditTextView)
    }

    fun unbindAll() {
        tetvArrayList.clear()
    }

    fun getAnimationOffset(): Long = animationOffset

    fun setAnimationOffset(offset: Long) {
        this.animationOffset = offset
    }

    override fun setOnClickListener(l: OnClickListener?) {
        this.onClickListener = l
    }

    override fun onSaveInstanceState(): Parcelable {
        d(TAG, "onSaveInstanceState")
        d(TAG, "onSaveInstanceState getEditing() : "+getEditing())
        val bundle = Bundle()
        bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
        bundle.putBoolean(EDIT_KEY, getEditing())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        d(TAG, "onRestoreInstanceState")
        var superState = state
        if (state is Bundle) {
            superState = state.getParcelable(SUPER_STATE_KEY)!!
            d(TAG, "state.getBoolean(EDIT_KEY) : "+state.getBoolean(EDIT_KEY))
            setEditing(service, textInput, state.getBoolean(EDIT_KEY),false, isNick)
        }
        super.onRestoreInstanceState(superState)
    }

    // 레트로핏 객체 생성 및 API 연결
    fun initRetrofit(): RetrofitService {
        retrofit = RetrofitClient.getInstance()
        service = retrofit.create(RetrofitService::class.java)

        return service
    }

    private fun checkNick(service : RetrofitService, nick : String) : Boolean {
        d(TAG, "checkNick")
        // 서버와 통신하여 닉네임 중복 확인
        d(TAG, "닉네임 체크 : $nick")
        val oriNick = UserInfo.getUserNick(context)
        d(TAG, "oriNick : $oriNick")
        if(nick.isEmpty()) {
            d(TAG, "nick empty")
            Toast.makeText(context, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            ret = false // 닉네임을 입력하지 않은 경우 여기서 빠져나감. return 안하면 onResponse에서 null받았다고 하면서 에러 발생.
            setEditing(service, textInput, !editing, true, isNick)
            return false
        } else if(nick.contains(" ") || nick.length < 2 || nick.length > 10) {
            d(TAG, "nick has space")
            Toast.makeText(context, "닉네임은 2~10자 이내로\n공백 없이 입력해주세요", Toast.LENGTH_SHORT).show()
            ret = false // 닉네임을 입력하지 않은 경우 여기서 빠져나감. return 안하면 onResponse에서 null받았다고 하면서 에러 발생.
            setEditing(service, textInput, !editing, true, isNick)
            return false
        } else if(nick == oriNick) {
            d(TAG, "nick ori")
            // 닉네임 수정 안하고 그대로 둔 경우
            ret = true
            setEditing(service, textInput, !editing, true, isNick)
            return true
        }
        service.checkNick(nick).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "닉네임 체크 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "닉네임 체크 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                d(TAG, msg)
//                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                ret = result
                d(TAG, "check ret : $ret")
                setEditing(service, textInput, !editing, true, isNick)
                if(result) {
                    changeInfo(service, "nick", nick)
                }
            }
        })
        return false
    }

    private fun changeInfo(service : RetrofitService, subject : String, content: String) {
        val id = getUserId(context)
        service.changeInfo(id, subject, content).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                d(TAG, "사용자 정보 수정 실패 : {$t}")
            }

            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                d(TAG, "사용자 정보 수정 요청 성공")
                val gson = Gson().fromJson(response.body().toString(), JsonObject::class.java)
                val msg = gson.get("msg").asString
                val result = gson.get("result").asBoolean
                d(TAG, msg)
                if(result) {
                    if(subject == "nick") UserInfo.setUserNick(context, content)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}