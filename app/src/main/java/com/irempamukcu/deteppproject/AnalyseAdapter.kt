package com.irempamukcu.deteppproject

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnalyseAdapter(private val displayDataList: List<DisplayData>) : RecyclerView.Adapter<AnalyseAdapter.AnalyseViewHolder>() {

    class AnalyseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val analyseName: TextView = itemView.findViewById(R.id.kidNameAnalyseRecyclerView)
        val topic: TextView = itemView.findViewById(R.id.topicAnalyseRecyclerView)
        val info: TextView = itemView.findViewById(R.id.informationAnaylseRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_anaylse, parent, false)
        return AnalyseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return displayDataList.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: AnalyseViewHolder, position: Int) {
        val displayData = displayDataList[position]
        holder.analyseName.text = displayData.kidName.uppercase()
        holder.topic.text = displayData.topic

        val topic = displayData.topic
        val subTopic = displayData.subTopic
        val emotion = displayData.emotion

        if(emotion.equals("happy")){
            val backColor = "#FF8674"
            holder.analyseName.setBackgroundColor(Color.parseColor(backColor))
            holder.topic.setBackgroundColor(Color.parseColor(backColor))
            val myInfo =  "Çocuğunuz $topic konusuna ilgi duyuyor olabilir. Onu özellikle $subTopic konusuna yönlendirmek isteyebilirsiniz."
            holder.info.text = myInfo
        }else{
            val backColor = "#333333"
            val textColor = "#FFFFFF"
            holder.itemView.setBackgroundColor(Color.parseColor(backColor))
            holder.analyseName.setBackgroundColor(Color.parseColor(backColor))
            holder.analyseName.setTextColor(Color.parseColor(textColor))
            holder.topic.setBackgroundColor(Color.parseColor(backColor))
            holder.topic.setTextColor(Color.parseColor(textColor))
            val myInfo = "Çocuğunuz $topic konusuna ilgi duyduğu hakkında bir veriye ulaşamadık. Başka konularda videolara yönlendirebilirsiniz."
            holder.info.text = myInfo

        }





    }
}
