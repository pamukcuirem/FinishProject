package com.irempamukcu.deteppproject

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.irempamukcu.deteppproject.databinding.FragmentAddKidBinding
import java.util.concurrent.TimeUnit

class AddKid : Fragment() {

    private lateinit var binding: FragmentAddKidBinding
    private lateinit var auth: FirebaseAuth
    private var colorInfo = "blue"
    private var permission = true
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            colorInfo = AddKidArgs.fromBundle(it).color
        }
        auth = Firebase.auth
        firestore = Firebase.firestore
        binding = FragmentAddKidBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkColor(colorInfo)

        val changeProfileButton = binding.changeAddKid
        val saveButton = binding.save
        val backButton = binding.backAddKid

        binding.inputAgeAddKid.setOnClickListener {
            binding.inputAgeAddKid.inputType = InputType.TYPE_CLASS_NUMBER
        }

        val permissionButton = binding.permissionButtonAddKid
        permissionButton.setOnClickListener {
            permissionCheck(permission)
        }

        changeProfileButton.setOnClickListener {
            val action = AddKidDirections.actionAddKidToChangeProfilePicture("add_kid", "")
            findNavController().navigate(action)
        }

        saveButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    saveButton.setImageResource(R.drawable.saveclick)
                    saveData()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    saveButton.setImageResource(R.drawable.save)
                    true
                }
                else -> false
            }
        }

        backButton.setOnClickListener {
            val action = AddKidDirections.actionAddKidToUser()
            findNavController().navigate(action)
        }

        schedulePeriodicAgeUpdateWorker()
    }

    private fun checkColor(colorInfo: String) {
        when (colorInfo) {
            "blue" -> binding.profileAddKid.setImageResource(R.drawable.blue)
            "red" -> binding.profileAddKid.setImageResource(R.drawable.red)
            "green" -> binding.profileAddKid.setImageResource(R.drawable.green)
            "yellow" -> binding.profileAddKid.setImageResource(R.drawable.yellow)
            "darkGreen" -> binding.profileAddKid.setImageResource(R.drawable.darkgreen)
            "pink" -> binding.profileAddKid.setImageResource(R.drawable.pink)
            "purple" -> binding.profileAddKid.setImageResource(R.drawable.purple)
            "orange" -> binding.profileAddKid.setImageResource(R.drawable.orange)
            else -> Toast.makeText(requireContext(), "Bir şeyler ters gitti.", Toast.LENGTH_LONG).show()
        }
    }

    private fun permissionCheck(permission: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val phonePermission = sharedPreferences.getString("phonePermission", "no")

        if (phonePermission == "yes") {
            if (permission) {
                this.permission = !permission
                binding.permissionButtonAddKid.setImageResource(R.drawable.permissionoff)
            } else {
                this.permission = !permission
                binding.permissionButtonAddKid.setImageResource(R.drawable.permissionon)
            }
        } else {
            this.permission = false
            binding.permissionButtonAddKid.setImageResource(R.drawable.permissionoff)
        }
    }

    private fun checkData(): Boolean {
        val kidName = binding.inputNameAddKid.text.toString()
        val kidAge = binding.inputAgeAddKid.text.toString()
        val kidGender = binding.inputGenderAddKid.text.toString()
        val currentUser = auth.currentUser
        val mail = currentUser?.email

        if (kidName.isBlank() || kidAge.isBlank() || kidGender.isBlank()) {
            Toast.makeText(requireContext(), "Lütfen boş bırakmayınız.", Toast.LENGTH_LONG).show()
            return false
        }

        if (mail == null) {
            Toast.makeText(requireContext(), "Lütfen mail bilgilerinizi kontrol ediniz.", Toast.LENGTH_LONG).show()
            return false
        }

        try {
            val age = kidAge.toInt()
            if (age !in 4..18) {
                Toast.makeText(requireContext(), "Lütfen geçerli bir yaş girin. Yaş verisi 5 ile 18 arasında olmak zorundadır.", Toast.LENGTH_LONG).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lütfen sayı değeri girdiğinizden emin olunuz.", Toast.LENGTH_LONG).show()
            return false
        }

        if (kidGender.equals("Kız", ignoreCase = true) || kidGender.equals("Erkek", ignoreCase = true)) {
            // Valid gender
        } else {
            Toast.makeText(requireContext(), "Cinsiyet verisi Kız ya da Erkek şeklinde olmalıdır.", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun saveData() {
        if (checkData()) {
            val kidName = binding.inputNameAddKid.text.toString()
            val kidAge = binding.inputAgeAddKid.text.toString().toInt()
            val kidGender = binding.inputGenderAddKid.text.toString()
            val currentUser = auth.currentUser
            val mail = currentUser?.email
            val kidPermission = if (permission) "yes" else "no"
            val saveDate = System.currentTimeMillis()

            val userMap = hashMapOf(
                "kidName" to kidName,
                "kidAge" to kidAge.toString(),
                "kidGender" to kidGender,
                "kidPermission" to kidPermission,
                "color" to colorInfo,
                "mail" to mail,
                "saveDate" to saveDate
            )

            firestore.collection("kids").add(userMap)
                .addOnSuccessListener { documentReference ->
                    val action = AddKidDirections.actionAddKidToUser()
                    findNavController().navigate(action)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Kayıt Başarısız.", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun schedulePeriodicAgeUpdateWorker() {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicKidAgeUpdateWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "KidAgeUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }
}

class PeriodicKidAgeUpdateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun doWork(): Result {
        val firestore = FirebaseFirestore.getInstance()
        val oneYearInMillis = TimeUnit.DAYS.toMillis(365)
        val currentTime = System.currentTimeMillis()


        val taskCompletionSource = TaskCompletionSource<Void>()

        firestore.collection("kids").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val saveDate = document.getLong("saveDate") ?: continue
                    val kidAge = document.getString("kidAge")?.toInt() ?: continue
                    val kidId = document.id

                    if (currentTime - saveDate >= oneYearInMillis) {
                        val newAge = kidAge + 1
                        val newSaveDate = saveDate + oneYearInMillis

                        firestore.collection("kids").document(kidId)
                            .update("kidAge", newAge.toString(), "saveDate", newSaveDate)
                            .addOnSuccessListener {
                                // Logging successful update
                                Log.d("UpdateSuccess", "DocumentSnapshot successfully updated!")
                            }
                            .addOnFailureListener { e ->
                                // Handle the failure to update Firestore
                                Log.w("UpdateFailure", "Error updating document", e)
                            }
                    }
                }
                taskCompletionSource.setResult(null)
            }
            .addOnFailureListener { e ->
                // Handle failure to get documents
                Log.w("FetchFailure", "Error getting documents", e)
                taskCompletionSource.setException(e)
            }

        try {
            Tasks.await(taskCompletionSource.task)
        } catch (e: Exception) {
            // Handle the exception
            Log.w("TaskException", "Error awaiting task completion", e)
            return Result.failure()
        }

        return Result.success()
    }


    }


