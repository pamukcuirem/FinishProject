package com.irempamukcu.deteppproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.irempamukcu.deteppproject.databinding.FragmentKidAnalyseBinding

class KidAnalyse : Fragment() {
    private lateinit var binding: FragmentKidAnalyseBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        firestore = Firebase.firestore
        binding = FragmentKidAnalyseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = binding.backKidAnalyse
        checkKidPermissionAndFetchData()

        backButton.setOnClickListener {
            val action = KidAnalyseDirections.actionKidAnalyseToAccount()
            findNavController().navigate(action)
        }
    }

    private fun checkKidPermissionAndFetchData() {
        val currentUser = auth.currentUser
        val currentMail = currentUser?.email

        if (currentMail == null) {
            Toast.makeText(context, "Bu maile kayıtlı analiz bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("kids").whereEqualTo("mail", currentMail).get()
            .addOnSuccessListener { kidSnapshot ->
                val kidsWithPermission = kidSnapshot.documents.filter {
                    it.getString("kidPermission") == "yes"
                }

                if (kidsWithPermission.isNotEmpty()) {
                    fetchData(kidsWithPermission)
                } else {
                    Toast.makeText(context, "Analiz ayarları açık çocuk bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Bir sorun oluştu.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchData(kidsWithPermission: List<DocumentSnapshot>) {
        val currentUser = auth.currentUser
        val currentMail = currentUser?.email

        if (currentMail == null) {
            Toast.makeText(context, "Böyle bir mail adresi bulamadık.", Toast.LENGTH_SHORT).show()
            return
        }

        val kidNames = kidsWithPermission.mapNotNull { it.getString("kidName") }

        val analyzeCollection = firestore.collection("kidAnalyzes")
        val videosCollection = firestore.collection("videos")

        if (kidNames.isNotEmpty()) {
            analyzeCollection.whereIn("kidName", kidNames).whereEqualTo("mail", currentMail).get()
                .addOnSuccessListener { analyzeSnapshot ->
                    val analyzes = analyzeSnapshot.documents.mapNotNull { doc ->
                        AnalyzeDisplay(
                            kidName = doc.getString("kidName") ?: "",
                            videoUrl = doc.getString("videoUrl") ?: "",
                            emotion = doc.getString("emotion") ?: "",
                            mail = doc.getString("mail") ?: ""
                        )
                    }

                    val videoUrls = analyzes.map { it.videoUrl }.distinct()

                    videosCollection.whereIn(FieldPath.documentId(), videoUrls).get()
                        .addOnSuccessListener { videoSnapshot ->
                            val videos = videoSnapshot.documents.mapNotNull { doc ->
                                VideoDisplay(
                                    videoUrl = doc.id,
                                    topic = doc.getString("videoTopic") ?: "",
                                    subTopic = doc.getString("videoSubTopic") ?: ""
                                )
                            }

                            val displayDataList: List<DisplayData> = analyzes.mapNotNull { analyze ->
                                val video = videos.find { it.videoUrl == analyze.videoUrl }
                                if (video != null) {
                                    DisplayData(
                                        kidName = analyze.kidName,
                                        topic = video.topic,
                                        subTopic = video.subTopic,
                                        emotion = analyze.emotion
                                    )
                                } else {
                                    null
                                }
                            }
                            updateRecyclerView(displayDataList)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Analizleri bulurken bir sıkıntı oluştu", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateRecyclerView(displayDataList: List<DisplayData>) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewKidAnalyse)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        val adapter = AnalyseAdapter(displayDataList)
        recyclerView?.adapter = adapter
    }
}
