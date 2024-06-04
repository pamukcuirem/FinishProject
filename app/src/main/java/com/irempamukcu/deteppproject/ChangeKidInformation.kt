package com.irempamukcu.deteppproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.irempamukcu.deteppproject.databinding.FragmentChangeKidInformationBinding


class ChangeKidInformation : Fragment() {
    private lateinit var binding : FragmentChangeKidInformationBinding
    private var colorInfo = ""
    private var nameInfo = ""
    private var permission : Boolean = true
    private lateinit var kidInfo : Map<String,String>
    private var currentPermission : String = "no"
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore

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


        arguments?.let {
            colorInfo = ChangeKidInformationArgs.fromBundle(it).color
            nameInfo = ChangeKidInformationArgs.fromBundle(it).name
        }


        binding = FragmentChangeKidInformationBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getData(nameInfo)
        checkColor(colorInfo)

        val backButton = binding.backChangeKid
        val profileChangeButton = binding.changeChangeKid
        val saveButton = binding.saveChange
        val permissionButton = binding.permissionButtonChangeKid

        permissionButton.setOnClickListener {
            permissionCheck(permission)
        }

        backButton.setOnClickListener {
            val action = ChangeKidInformationDirections.actionChangeKidInformationToUser()
            findNavController().navigate(action)
        }

        profileChangeButton.setOnClickListener {
            val action = ChangeKidInformationDirections.actionChangeKidInformationToChangeProfilePicture("change_kid",nameInfo)
            findNavController().navigate(action)
        }

        saveButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Change the image resource when the ImageView is pressed

                    saveButton.setImageResource(R.drawable.saveclick)

                    val name = nameInfo
                    val currentName = binding.inputNameChangeKid.text.toString()
                    val currentUser = auth.currentUser
                    val currentMail = currentUser?.email
                    val currentAge = binding.inputAgeChangeKid.text.toString()
                    val currentGender = binding.inputGenderChangeKid.text.toString()



                    kidInfo = mapOf(
                        "color" to colorInfo,
                        "kidAge" to currentAge,
                        "kidGender" to currentGender,
                        "kidName" to currentName,
                        "kidPermission" to currentPermission

                    )

                    if(currentMail != null){
                        changeKidData(name,currentMail,kidInfo)
                    }

                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Optionally, revert to the initial image when the press is released or cancelled
                    saveButton.setImageResource(R.drawable.save)
                    true
                }
                else -> false
            }
        }


    }


    private fun checkColor(colorInfo : String){

        if(colorInfo.equals("blue")){
            binding.profileChangeKid.setImageResource(R.drawable.blue)
        }else if(colorInfo.equals("red")){
            binding.profileChangeKid.setImageResource(R.drawable.red)
        }else if(colorInfo.equals("green")){
            binding.profileChangeKid.setImageResource(R.drawable.green)
        }else if(colorInfo.equals("yellow")){
            binding.profileChangeKid.setImageResource(R.drawable.yellow)
        }else if(colorInfo.equals("darkGreen")){
            binding.profileChangeKid.setImageResource(R.drawable.darkgreen)
        }else if(colorInfo.equals("pink")){
            binding.profileChangeKid.setImageResource(R.drawable.pink)
        }else if(colorInfo.equals("purple")){
            binding.profileChangeKid.setImageResource(R.drawable.purple)
        }else if(colorInfo.equals("orange")){
            binding.profileChangeKid.setImageResource(R.drawable.orange)
        }else{
            Toast.makeText(requireContext(),"Bir şeyler ters gitti.", Toast.LENGTH_LONG).show()
        }
    }

    private fun permissionCheck(permission : Boolean){
        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val phonePermission = sharedPreferences.getString("phonePermission", "no")

        if(phonePermission.equals("yes")){
            if(permission){
                this.permission = !permission
                currentPermission = "no"
                binding.permissionButtonChangeKid.setImageResource(R.drawable.permissionoff)
            }else{
                this.permission = !permission
                currentPermission = "yes"
                binding.permissionButtonChangeKid.setImageResource(R.drawable.permissionon)
            }
        }else{
            binding.permissionButtonChangeKid.setImageResource(R.drawable.permissionoff)
            Toast.makeText(requireContext(),"Kamera İzni Verilmelidir.",Toast.LENGTH_LONG).show()
        }

    }

    private fun getData(name : String){

        val currentUser = auth.currentUser
        val currentMail = currentUser?.email

        if(currentUser != null && currentMail != null){
            firestore.collection("kids")
                .whereEqualTo("mail",currentMail)
                .whereEqualTo("kidName",name).get()
                .addOnSuccessListener {documents ->
                    for(document in documents){
                        if(document.exists()){
                            val kidAge = document.getString("kidAge")
                            val kidGender = document.getString("kidGender")
                            val kidPermission = document.getString("kidPermission")
                            val kidName = document.getString("kidName")

                            binding.inputNameChangeKid.setText(kidName)

                            binding.inputAgeChangeKid.setText(kidAge)

                            binding.inputGenderChangeKid.setText(kidGender)

                            val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                            val phonePermission = sharedPreferences.getString("phonePermission", "no")

                            if(phonePermission.equals("yes")){

                                if(kidPermission.equals("yes")){
                                    binding.permissionButtonChangeKid.setImageResource(R.drawable.permissionon)
                                    permission = true
                                }else if(kidPermission.equals("no")){
                                    binding.permissionButtonChangeKid.setImageResource(R.drawable.permissionoff)
                                    permission = false
                                }else{
                                    Toast.makeText(requireContext(),"Bir şeyler ters gitti. Bilgilere ulaşılamıyor.",Toast.LENGTH_LONG).show()
                                }
                            }else{
                                binding.permissionButtonChangeKid.setImageResource(R.drawable.permissionoff)

                            }


                        }


                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),"Bir şeyler ters gitti. Bilgilere ulaşılamıyor.",Toast.LENGTH_LONG).show()
                }
        }


    }

    private fun changeKidData(name : String, mail : String, newKidInfo : Map<String,String>){
        val kidCollection = firestore.collection("kids")

        kidCollection
            .whereEqualTo("mail", mail)
            .whereEqualTo("kidName",name)
            .get()
            .addOnSuccessListener {documents ->
                for(document in documents){
                    kidCollection.document(document.id).set(newKidInfo, SetOptions.merge()).addOnSuccessListener {
                        val action = ChangeKidInformationDirections.actionChangeKidInformationToUser()
                        findNavController().navigate(action)
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(),"Bilgi güncelleme başarısız. Lütfen daha sonra tekrar deneyiniz.",Toast.LENGTH_LONG).show()
                    }
                }

            }.addOnFailureListener {

                Toast.makeText(requireContext(),"Bilgilerinize ulaşılamadı. Lütfen daha sonra tekrar deneyiniz.",Toast.LENGTH_LONG).show()
            }
    }


}