package com.irempamukcu.deteppproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.irempamukcu.deteppproject.databinding.FragmentUserBinding

class User : Fragment() {

    private lateinit var binding: FragmentUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var isCamera: Boolean = false
    private var lastFragment = ""
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Save data to hide permission button or perform action that requires the permission
            savePermissionStatus(true)
        } else {
            Toast.makeText(requireContext(), "Kamera İzinlerini Vermediğiniz İçin Çocuğunuzun Analiz Bilgilerine Ulaşamazsınız. Eğer izin vermek istiyorsanız uygulamayı baştan yükleyebilirsiniz ya da telefon ayarlarınızdan değiştirebilirsiniz.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        auth = Firebase.auth
        firestore = Firebase.firestore
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addKid = binding.addButtonUser
        val goToAccount = binding.accountUser
        val currentUser = auth.currentUser
        val mail = currentUser?.email

        if (mail != null) {
            firestore.collection("kids").whereEqualTo("mail", mail).get().addOnSuccessListener { documents ->
                val kidList = mutableListOf<Kid>()
                for (document in documents) {
                    val name = document.getString("kidName") ?: ""
                    val age = document.getString("kidAge") ?: ""
                    val gender = document.getString("kidGender") ?: ""
                    val permission = document.getString("kidPermission") ?: ""
                    val color = document.getString("color") ?: ""
                    val kid = Kid(name, age, gender, permission, color)

                    kidList.add(kid)
                }

                handleRecyclerView(kidList)

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Verilere ulaşmakta sıkıntı oluştu. Bize ulaşabilirsiniz...", Toast.LENGTH_LONG).show()
            }
        }
        goToAccount.setOnClickListener {
            val action = UserDirections.actionUserToAccount()
            findNavController().navigate(action)
        }

        addKid.setOnClickListener {
            val action = UserDirections.actionUserToAddKid("blue")
            findNavController().navigate(action)
        }

        // Check if camera exists
        isCameraExists()

        // Check for camera permissions
        checkSelfPerm()

        if(lastFragment.equals("loggedin")){
            restartFragment(User())
        }
    }


    private fun handleRecyclerView(kidList: MutableList<Kid>) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewUser)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = KidAdapter(kidList)
    }

    // Check whether your app is running on a device that has a front-facing camera.
    private fun isCameraExists() {
        if (requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            isCamera = true
        } else {
            Toast.makeText(requireContext(), "Telefon Kamerası Bulunmamakta.", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkSelfPerm() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted, save status in SharedPreferences
                savePermissionStatus(true)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun savePermissionStatus(granted: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("phonePermission", if (granted) "yes" else "no")
            apply()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Handle other permissions if needed
    }


    private fun Fragment.restartFragment(fragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Get the tag of the fragment
        val tag = fragment.tag

        // Remove the fragment
        fragmentTransaction.remove(fragment)
        fragmentTransaction.commit()

        // This is needed to complete the transaction before adding the fragment again
        fragmentManager.executePendingTransactions()

        // Add the fragment back
        fragmentTransaction.add(R.id.fragmentContainerView, fragment, tag)
        fragmentTransaction.commit()
    }


}