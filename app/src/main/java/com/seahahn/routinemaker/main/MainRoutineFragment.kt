package com.seahahn.routinemaker.main

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.network.RetrofitServiceViewModel
import com.seahahn.routinemaker.util.AppVar.getDatePast
import com.seahahn.routinemaker.util.UserSetting.getRtListMode
import com.seahahn.routinemaker.util.UserSetting.setRtListMode
import java.time.LocalDate
import java.time.LocalDateTime


class MainRoutineFragment : Fragment(), CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private val TAG = this::class.java.simpleName
    private val rfServiceViewModel by activityViewModels<RetrofitServiceViewModel>() // 레트로핏 서비스 객체를 담기 위한 뷰모델
    lateinit var service : RetrofitService

    // 액티비티로부터 데이터를 가져오는 뷰모델
    private val rtTodoViewModel by activityViewModels<RtTodoViewModel>() // 루틴, 할 일 목록 데이터
    private val rtDoneViewModel by activityViewModels<RtDoneViewModel>() // 루틴, 할 일 목록 데이터(과거)
    private val dateViewModel by activityViewModels<DateViewModel>() // 날짜 데이터

    private lateinit var showAll: CheckBox
    private lateinit var deleteDone: Button

    private lateinit var rtAdapter: RtAdapter
    private lateinit var rtList: RecyclerView
    var mDatas = mutableListOf<RtData>()
    var showDatas = mutableListOf<RtData>()
    var pastDatas = mutableListOf<RtData>()
    var pastShowDatas = mutableListOf<RtData>()
    lateinit var it_mDatas : Iterator<RtData>
    lateinit var it_pastDatas : Iterator<RtData>

    val today = LocalDateTime.now()
    lateinit var parsedDate : LocalDate
    lateinit var dayOfWeek : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_main_routine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        d(TAG, "rt fragment onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        showAll = view.findViewById(R.id.showAll) // 전체 목록 보기 체크박스 초기화하기
        showAll.setOnCheckedChangeListener(this)

        deleteDone = view.findViewById(R.id.deleteDone)
        deleteDone.setOnClickListener(this)

        // 루틴, 할 일 목록 리사이클러뷰 및 어댑터 초기화하기
        rtList = view.findViewById(R.id.rtList) // 리사이클러뷰 초기화
        rtAdapter = RtAdapter() // 어댑터 초기화
        rtList.adapter = rtAdapter // 어댑터 연결
        showAll.isChecked = getRtListMode(view.context) // 전체 보기 선택했던 경우 다시 액티비티 열면 그대로 전체 보기 되도록 유지

        // 리사이클러뷰의 스크롤 위치 복구하는 기능
//        rtAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        rfServiceViewModel.rfService.observe(this) { rfService ->
            service = rfService
            rtAdapter.getService(service)
        }

        // 날짜 데이터 받기
        dateViewModel.selectedDate.observe(this) { date ->
            d(TAG, "루틴 프래그먼트 date : $date")
            // 사용자가 선택한 날짜를 루틴, 할 일 목록에 보내기
            // 선택한 날짜가 오늘 날짜면 루틴 체크박스 활성화, 다른 날짜면 비활성화
            rtAdapter.replaceDate(date)

            parsedDate = LocalDate.parse(date)
//            d(TAG, "루틴 프래그먼트 parsedDate 날짜: $parsedDate")
//            d(TAG, "루틴 프래그먼트 parsedDate 요일 : " + parsedDate.dayOfWeek)

            when (parsedDate.dayOfWeek.toString()) {
                "SUNDAY" -> dayOfWeek = "일"
                "MONDAY" -> dayOfWeek = "월"
                "TUESDAY" -> dayOfWeek = "화"
                "WEDNESDAY" -> dayOfWeek = "수"
                "THURSDAY" -> dayOfWeek = "목"
                "FRIDAY" -> dayOfWeek = "금"
                "SATURDAY" -> dayOfWeek = "토"
            }

            if(!getDatePast(this.requireContext())) {
                // 받은 날짜 정보에 해당하는 루틴 또는 할 일 목록 추려내기
                showDatas.clear()
                it_mDatas = mDatas.iterator()
                while (it_mDatas.hasNext()) {
                    val it_mData = it_mDatas.next()
                    //                    d(TAG, "it_mData : $it_mData")
                    // 루틴일 경우 : 오늘 날짜의 요일이 루틴 수행 요일과 맞으면 보여주기 / 할 일인 경우 : 날짜가 맞으면 보여주기
                    val timestamp = it_mData.createdAt.replace(" ", "T") // 루틴을 생성한 시점
                    if (it_mData.mType == "rt" &&
                        dayOfWeek in it_mData.mDays && // 사용자가 선택한 날짜의 요일이 루틴 수행 요일에 포함되면 목록에 출력함
                        (parsedDate.isAfter(LocalDateTime.parse(timestamp).toLocalDate()) // 사용자가 선택한 날짜가 루틴을 생성한 날짜와 같거나 이후일 경우 목록에 출력함
                                || parsedDate.isEqual(LocalDateTime.parse(timestamp).toLocalDate()))) {
                        showDatas.add(it_mData)
                    } else if (it_mData.mType == "todo" && it_mData.mDate == parsedDate.toString()) {
                        showDatas.add(it_mData)
                    }
                    // 할 일에 반복 여부 있는 경우에는 해당 할 일을 수행하고 나면 그 다음에 반복해야 할 요일의 날짜를 date에 집어넣어 수정한다
                }

                // 어댑터에 가져온 데이터 연결하여 출력하기
                // '전체 목록 보기' 여부에 따라 전체를 보여줄지, 해당 요일 데이터만 보여줄지 결정함
                if (showAll.isChecked) {
                    rtAdapter.replaceList(mDatas)
                    rtAdapter.setDayOfWeek(dayOfWeek)
                } else {
                    rtAdapter.replaceList(showDatas) // 받은 날짜 정보에 맞춰서 목록 띄우기
                    rtAdapter.setDayOfWeek(dayOfWeek)
                }
            } else {
                // 받은 날짜 정보에 해당하는 루틴 또는 할 일 목록 추려내기
                pastShowDatas.clear()
                it_pastDatas = pastDatas.iterator()
                while (it_pastDatas.hasNext()) {
                    val it_pastData = it_pastDatas.next()
                    //                    d(TAG, "it_mData : $it_mData")
                    // 루틴일 경우 : 오늘 날짜의 요일이 루틴 수행 요일과 맞으면 보여주기 / 할 일인 경우 : 날짜가 맞으면 보여주기
//                        val timestamp = it_pastData.createdAt.replace(" ", "T") // 루틴을 생성한 시점
                    if (it_pastData.mType == "rt" &&
                        dayOfWeek in it_pastData.mDays && // 사용자가 선택한 날짜의 요일이 루틴 수행 요일에 포함되면 목록에 출력함
                        (parsedDate.isEqual(LocalDate.parse(it_pastData.mDate)) // 사용자가 선택한 날짜가 루틴을 생성한 날짜와 같거나 이후일 경우 목록에 출력함
//                                    || parsedDate.isEqual(LocalDateTime.parse(timestamp).toLocalDate())))
                                )){
                        pastShowDatas.add(it_pastData)
                    } else if (it_pastData.mType == "todo" && it_pastData.mDate == parsedDate.toString()) {
                        pastShowDatas.add(it_pastData)
                    }
                    // 할 일에 반복 여부 있는 경우에는 해당 할 일을 수행하고 나면 그 다음에 반복해야 할 요일의 날짜를 date에 집어넣어 수정한다
                }

                // 어댑터에 가져온 데이터 연결하여 출력하기
                // '전체 목록 보기' 여부에 따라 전체를 보여줄지, 해당 요일 데이터만 보여줄지 결정함
                if (showAll.isChecked) {
                    rtAdapter.replaceList(mDatas)
                    rtAdapter.setDayOfWeek(dayOfWeek)
                } else {
                    rtAdapter.replaceList(pastShowDatas) // 받은 날짜 정보에 맞춰서 목록 띄우기
                    rtAdapter.setDayOfWeek(dayOfWeek)
                }
            }

        }

        // 루틴, 할 일 목록 데이터 가져오기
        rtTodoViewModel.gottenRtData.observe(this) { rtDatas ->
            d(TAG, "루틴 프래그먼트 rtDatas : $rtDatas")
            d(TAG, "과거 여부 : " + getDatePast(this.requireContext()))
            mDatas = rtDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기

            if(!getDatePast(this.requireContext())) {
                // 받은 날짜 정보에 해당하는 루틴 또는 할 일 목록 추려내기
                showDatas.clear()
                it_mDatas = mDatas.iterator()
                while (it_mDatas.hasNext()) {
                    val it_mData = it_mDatas.next()
                    //                    d(TAG, "it_mData : $it_mData")
                    // 루틴일 경우 : 오늘 날짜의 요일이 루틴 수행 요일과 맞으면 보여주기 / 할 일인 경우 : 날짜가 맞으면 보여주기
                    val timestamp = it_mData.createdAt.replace(" ", "T") // 루틴을 생성한 시점
                    if (it_mData.mType == "rt" &&
                        dayOfWeek in it_mData.mDays && // 사용자가 선택한 날짜의 요일이 루틴 수행 요일에 포함되면 목록에 출력함
                        (parsedDate.isAfter(LocalDateTime.parse(timestamp).toLocalDate()) // 사용자가 선택한 날짜가 루틴을 생성한 날짜와 같거나 이후일 경우 목록에 출력함
                                || parsedDate.isEqual(LocalDateTime.parse(timestamp).toLocalDate()))) {
                        showDatas.add(it_mData)
                    } else if (it_mData.mType == "todo" && it_mData.mDate == parsedDate.toString()) {
                        showDatas.add(it_mData)
                    }
                    // 할 일에 반복 여부 있는 경우에는 해당 할 일을 수행하고 나면 그 다음에 반복해야 할 요일의 날짜를 date에 집어넣어 수정한다
                }

                // 어댑터에 가져온 데이터 연결하여 출력하기
                // '전체 목록 보기' 여부에 따라 전체를 보여줄지, 해당 요일 데이터만 보여줄지 결정함
                if (showAll.isChecked) {
                    rtAdapter.replaceList(mDatas)
                    rtAdapter.setDayOfWeek(dayOfWeek)
                } else {
                    rtAdapter.replaceList(showDatas) // 받은 날짜 정보에 맞춰서 목록 띄우기
                    rtAdapter.setDayOfWeek(dayOfWeek)
                }
            }
        }

        // 루틴(과거 내역), 할 일 목록 데이터 가져오기
        rtDoneViewModel.gottenRtDataPast.observe(this) { rtDatas ->
            d(TAG, "루틴 프래그먼트 과거 : $rtDatas")
            d(TAG, "과거 여부 : " + getDatePast(this.requireContext()))
            pastDatas = rtDatas // 뷰모델에 저장해둔 루틴 및 할 일 목록 데이터 가져오기

            if(getDatePast(this.requireContext())) {
                // 받은 날짜 정보에 해당하는 루틴 또는 할 일 목록 추려내기
                pastShowDatas.clear()
                it_pastDatas = pastDatas.iterator()
                while (it_pastDatas.hasNext()) {
                    val it_pastData = it_pastDatas.next()
                    //                    d(TAG, "it_mData : $it_mData")
                    // 루틴일 경우 : 오늘 날짜의 요일이 루틴 수행 요일과 맞으면 보여주기 / 할 일인 경우 : 날짜가 맞으면 보여주기
//                        val timestamp = it_pastData.createdAt.replace(" ", "T") // 루틴을 생성한 시점
                    if (it_pastData.mType == "rt" &&
                        dayOfWeek in it_pastData.mDays && // 사용자가 선택한 날짜의 요일이 루틴 수행 요일에 포함되면 목록에 출력함
                        (parsedDate.isEqual(LocalDate.parse(it_pastData.mDate)) // 사용자가 선택한 날짜가 루틴을 생성한 날짜와 같거나 이후일 경우 목록에 출력함
//                                    || parsedDate.isEqual(LocalDateTime.parse(timestamp).toLocalDate())))
                                )){
                        pastShowDatas.add(it_pastData)
                    } else if (it_pastData.mType == "todo" && it_pastData.mDate == parsedDate.toString()) {
                        pastShowDatas.add(it_pastData)
                    }
                    // 할 일에 반복 여부 있는 경우에는 해당 할 일을 수행하고 나면 그 다음에 반복해야 할 요일의 날짜를 date에 집어넣어 수정한다
                }

                // 어댑터에 가져온 데이터 연결하여 출력하기
                // '전체 목록 보기' 여부에 따라 전체를 보여줄지, 해당 요일 데이터만 보여줄지 결정함
                if (showAll.isChecked) {
                    rtAdapter.replaceList(mDatas)
                    rtAdapter.setDayOfWeek(dayOfWeek)
                } else {
                    rtAdapter.replaceList(pastShowDatas) // 받은 날짜 정보에 맞춰서 목록 띄우기
                    rtAdapter.setDayOfWeek(dayOfWeek)
                }
            }
        }
    }

    // 전체 목록 보기 체크 여부에 따른 목록 출력 방식 정하기
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) { // 모든 루틴과 할 일을 보여줌
            rtAdapter.replaceList(mDatas)
            setRtListMode(buttonView!!.context, isChecked)
        } else { // 선택한 날짜의 루틴과 할 일을 보여줌
            if(!getDatePast(this.requireContext())) {
                rtAdapter.replaceList(showDatas)
            } else {
                rtAdapter.replaceList(pastShowDatas)
            }
            setRtListMode(buttonView!!.context, isChecked)
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            // 완료한 일정 삭제하기 버튼 누른 경우
            R.id.deleteDone -> showAlert(v.context)
        }
    }

    // 완료한 일정 삭제하기 버튼 눌렀을 때 출력할 경고창
    private fun showAlert(contextIn : Context) {
        val context = contextIn as MainActivity
        AlertDialog.Builder(context)
            .setTitle("삭제하기")
            .setMessage("정말 삭제하시겠어요?")
            .setPositiveButton("확인") { _: DialogInterface, _: Int ->
                val itemList = rtAdapter.returnList() // 현재 출력 중인 루틴(할 일) 목록 데이터 가져오기

                val items = itemList.iterator()
                while (items.hasNext()) {
                    val item = items.next()
                    if (item.mType == "todo" && item.done == 1) { // 완료한 할 일만 삭제함
                        context.deleteRt(service, item.id, context)
                    }
                }
            }
            .setNegativeButton("취소") { _: DialogInterface, _: Int -> }
            .show()
    }
}
