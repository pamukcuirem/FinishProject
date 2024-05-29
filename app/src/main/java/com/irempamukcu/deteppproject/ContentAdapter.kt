package com.irempamukcu.deteppproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ContentAdapter(private val contentList: List<ContentData>, private val kidName: String, private val kidColor : String) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {
    class ContentViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val videoName : TextView = itemView.findViewById(R.id.titleRecyclerViewVideoContent)
        val videoTopic : TextView = itemView.findViewById(R.id.topicRecyclerViewVideoContent)
        val videoImage : ImageView = itemView.findViewById(R.id.imageRecyclerViewVideoContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_video_content,parent,false)
        return ContentViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val currentContent = contentList[position]

        holder.videoName.text = currentContent.videoName
        holder.videoTopic.text = currentContent.videoTopic
        val videoPng = currentContent.videoImageUID + ".png"
        val videoUrl = currentContent.videoImageUID + ".mp4"
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$videoPng")

        storageReference.downloadUrl.addOnSuccessListener {uri->
            Picasso.get().load(uri).into(holder.videoImage)
        }.addOnFailureListener {
            Toast.makeText(holder.itemView.context,"Resim indirme başarısız.",Toast.LENGTH_LONG).show()
        }


        holder.itemView.setOnClickListener {
            val action = LoggedInDirections.actionLoggedInToVideo(videoUrl,kidName,kidColor)
            holder.itemView.findNavController().navigate(action)
        }

    }
}