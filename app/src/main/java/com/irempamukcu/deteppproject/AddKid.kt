package com.irempamukcu.deteppproject

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.irempamukcu.deteppproject.databinding.FragmentAddKidBinding
import java.security.Permission

class AddKid : Fragment() {

    private lateinit var binding : FragmentAddKidBinding
    private lateinit var auth : FirebaseAuth
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
        // Inflate the layout for this fragment
        arguments?.let {
            colorInfo = AddKidArgs.fromBundle(it).color
        }
        auth = Firebase.auth
        firestore = Firebase.firestore
        binding = FragmentAddKidBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkColor(colorInfo)

        val changeProfileButton = binding.changeAddKid

        val saveButton = binding.save

        val backButton = binding.backAddKid

        binding.inputAgeAddKid.setOnClickListener {
            binding.inputAgeAddKid.inputType = InputType.TYPE_CLASS_NUMBER
        }

        var permissionButton = binding.permissionButtonAddKid

        permissionButton.setOnClickListener {
           permissionCheck(permission)
        }


        changeProfileButton.setOnClickListener {
            val action = AddKidDirections.actionAddKidToChangeProfilePicture("add_kid","")
            findNavController().navigate(action)
        }

        saveButton.setOnClickListener {

            saveData()

        }

        backButton.setOnClickListener {
            val action = AddKidDirections.actionAddKidToUser()
            findNavController().navigate(action)
        }
    }

    private fun checkColor(colorInfo : String){

        if(colorInfo.equals("blue")){
            binding.profileAddKid.setImageResource(R.drawable.blue)
        }else if(colorInfo.equals("red")){
            binding.profileAddKid.setImageResource(R.drawable.red)
        }else if(colorInfo.equals("green")){
            binding.profileAddKid.setImageResource(R.drawable.green)
        }else if(colorInfo.equals("yellow")){
            binding.profileAddKid.setImageResource(R.drawable.yellow)
        }else if(colorInfo.equals("darkGreen")){
            binding.profileAddKid.setImageResource(R.drawable.darkgreen)
        }else if(colorInfo.equals("pink")){
            binding.profileAddKid.setImageResource(R.drawable.pink)
        }else if(colorInfo.equals("purple")){
            binding.profileAddKid.setImageResource(R.drawable.purple)
        }else if(colorInfo.equals("orange")){
            binding.profileAddKid.setImageResource(R.drawable.orange)
        }else{
            Toast.makeText(requireContext(),"Bir şeyler ters gitti.",Toast.LENGTH_LONG).show()
        }
    }

    private fun permissionCheck(permission : Boolean){
        if(permission){
            this.permission = !permission
            binding.permissionButtonAddKid.setImageResource(R.drawable.permissionoff)
            println(permission)
        }else{
            this.permission = !permission
            binding.permissionButtonAddKid.setImageResource(R.drawable.permissionon)
            println(permission)
        }
    }

    private fun checkData() : Boolean{
        val kidName = binding.inputNameAddKid.text.toString()
        val kidAge = binding.inputAgeAddKid.text.toString()
        val kidGender = binding.inputGenderAddKid.text.toString()
        val currentUser = auth.currentUser
        val mail = currentUser?.email



        if (kidName.isBlank() || kidAge.isBlank() || kidGender.isBlank()) {
            Toast.makeText(requireContext(), "Lütfen boş bırakmayınız.", Toast.LENGTH_LONG).show()
            return false
        }

        if(mail == null){
            Toast.makeText(requireContext(), "Lütfen mail bilgilerinizi kontrol ediniz.", Toast.LENGTH_LONG).show()
            return false
        }


        try {
            val age = kidAge.toInt()
            if(age !in 19 downTo 4){
                Toast.makeText(requireContext(), "Lütfen geçerli bir yaş girin. Yaş verisi 5 ile 18 arasında olmak zorundadır.", Toast.LENGTH_LONG).show()
                return false
            }
        }catch (e : Exception){
            Toast.makeText(requireContext(), "Lütfen sayı değeri girdiğinizden emin olunuz.", Toast.LENGTH_LONG).show()
            return false

        }


        if(kidGender.equals("Kız", ignoreCase = true) || kidGender.equals("Erkek", ignoreCase = true)){
            //nothing happens
        }else{
            Toast.makeText(requireContext(), "Cinsiyet verisi Kız ya da Erkek şeklinde olmalıdır.", Toast.LENGTH_LONG).show()
            return false
        }

        return true

    }

    private fun saveData() {

        if(checkData()){
            val kidName = binding.inputNameAddKid.text.toString()
            val kidAge = binding.inputAgeAddKid.text.toString()
            val kidGender = binding.inputGenderAddKid.text.toString()
            val currentUser = auth.currentUser
            val mail = currentUser?.email
            val kidPermission = if (permission) "yes" else "no"



            val userMap = hashMapOf(
                "kidName" to kidName,
                "kidAge" to kidAge,
                "kidGender" to kidGender,
                "kidPermission" to kidPermission,
                "color" to colorInfo,
                "mail" to mail
            )

            firestore.collection("kids").add(userMap)
                .addOnSuccessListener {
                    val action = AddKidDirections.actionAddKidToUser()
                    findNavController().navigate(action)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(),"Kayıt Başarısız.",Toast.LENGTH_LONG).show()

                }
        }

    }


}