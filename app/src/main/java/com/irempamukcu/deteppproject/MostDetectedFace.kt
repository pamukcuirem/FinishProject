package com.irempamukcu.deteppproject

import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class MostDetectedFace(private val firestore: FirebaseFirestore) {

    data class EmotionCount(
        val kidName: String,
        val mail: String,
        var videoUrl: String,
        var happyCount: Int = 0,
        var sadCount: Int = 0
    )

    private val TAG = "MostDetectedFace"

    fun analyzeEmotions() {
        firestore.collection("analyzes")
            .get()
            .addOnSuccessListener { documents ->
                val emotionCounts = mutableMapOf<String, EmotionCount>()

                for (document in documents) {
                    val kidName = document.getString("kidName") ?: continue
                    val mail = document.getString("mail") ?: continue
                    var videoUrl = document.getString("videoUrl") ?: continue
                    val emotion = document.getString("emotion") ?: continue

                    // Remove '.mp4' extension from videoUrl if it exists
                    if (videoUrl.endsWith(".mp4")) {
                        videoUrl = videoUrl.removeSuffix(".mp4")
                    }

                    val key = "$kidName|$mail|$videoUrl"
                    val count = emotionCounts.getOrPut(key) {
                        EmotionCount(kidName, mail, videoUrl)
                    }

                    if (emotion == "happy") {
                        count.happyCount++
                    } else if (emotion == "sad") {
                        count.sadCount++
                    }
                }

                for ((_, count) in emotionCounts) {
                    val finalEmotion = if (count.happyCount > count.sadCount) "happy" else "sad"

                    val kidAnalyzeData = hashMapOf(
                        "kidName" to count.kidName,
                        "mail" to count.mail,
                        "videoUrl" to count.videoUrl,
                        "emotion" to finalEmotion
                    )

                    checkAndSaveKidAnalyzeData(kidAnalyzeData)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting documents: ", exception)
            }
    }

    private fun checkAndSaveKidAnalyzeData(data: Map<String, Any>) {
        val kidName = data["kidName"] as String
        val mail = data["mail"] as String
        val videoUrl = data["videoUrl"] as String
        val emotion = data["emotion"] as String

        firestore.collection("kidAnalyzes")
            .whereEqualTo("kidName", kidName)
            .whereEqualTo("mail", mail)
            .whereEqualTo("videoUrl", videoUrl)
            .whereEqualTo("emotion", emotion)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    firestore.collection("kidAnalyzes")
                        .add(data)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error writing document", e)
                        }
                } else {
                    Log.d(TAG, "Document with the same data already exists.")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error checking document", e)
            }
    }
}
