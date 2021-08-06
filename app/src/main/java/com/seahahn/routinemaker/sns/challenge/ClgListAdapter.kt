package com.seahahn.routinemaker.sns.challenge

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.nhn.android.idp.common.logger.Logger.d
import com.seahahn.routinemaker.R
import com.seahahn.routinemaker.network.RetrofitService
import com.seahahn.routinemaker.sns.ChallengeData
import com.seahahn.routinemaker.util.Sns
import java.util.*

class ClgListAdapter(mContext: Context) : RecyclerView.Adapter<ClgListViewHolder>(), Filterable {

    private val TAG = this::class.java.simpleName
    lateinit var service : RetrofitService
    val context = mContext as Sns

    //데이터들을 저장하는 변수
    private var originalData = mutableListOf<ChallengeData>()
    private var data = mutableListOf<ChallengeData>()
    var filteredData = mutableListOf<ChallengeData>()
    var resultData = mutableListOf<ChallengeData>()

    //ViewHolder에 쓰일 Layout을 inflate하는 함수
    //ViewGroup의 context를 사용하여 특정 화면에서 구현할 수 있도록 함
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClgListViewHolder {
//        d(TAG, "rt onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clg, parent, false)
        return ClgListViewHolder(view)
    }

    //ViewHolder에서 데이터 묶는 함수가 실행되는 곳
    override fun onBindViewHolder(holder: ClgListViewHolder, position: Int) {
//        d(TAG, "rt onBindViewHolder")
        holder.getService(service)
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun replaceList(newList: MutableList<ChallengeData>) {
//        d(TAG, "rt replaceList")
        data = newList.toMutableList()
        //어댑터의 데이터가 변했다는 notify를 날린다
        notifyDataSetChanged()
    }

    fun saveOriginalList(list: MutableList<ChallengeData>) {
        originalData = list.toMutableList()
    }

    fun returnList(): MutableList<ChallengeData> {
        return data
    }

    fun getService(serviceInput : RetrofitService) {
        service = serviceInput
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                d(TAG, "constraint : $constraint")
                filteredData.clear()
                if (constraint?.isEmpty() == true) {
                    filteredData.addAll(originalData)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    for (item in originalData) {
                        if (item.title.lowercase(Locale.getDefault()).contains(filterPattern)) {
                            filteredData.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredData
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                d(TAG, "published")
                resultData.clear()
                resultData.addAll(results!!.values as Collection<ChallengeData>)
                replaceList(resultData)

                // 출력할 데이터가 없으면 "데이터가 없습니다"를 표시함
                d(TAG, "groupListAdapter.itemCount : $itemCount")
                if (itemCount == 0) {
                    context.viewEmptyList.visibility = View.VISIBLE
                } else {
                    context.viewEmptyList.visibility = View.GONE
                }
            }
        }
    }
}