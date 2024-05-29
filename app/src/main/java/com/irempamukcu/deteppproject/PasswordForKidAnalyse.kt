package com.irempamukcu.deteppproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.irempamukcu.deteppproject.databinding.FragmentPasswordForKidAnalyseBinding


class PasswordForKidAnalyse : Fragment() {
    private lateinit var binding : FragmentPasswordForKidAnalyseBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = Firebase.auth

        binding = FragmentPasswordForKidAnalyseBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextButton = binding.nextPasswordKid
        val backButton = binding.backPasswordKid

        nextButton.setOnClickListener {
            val password = binding.inputPasswordKid.text.toString()
            val currentUser = auth.currentUser


           if(currentUser != null) {
               val mail = currentUser.email

              if(mail != null){
                  checkPassword(mail,password){
                      if(it){
                          val action = PasswordForKidAnalyseDirections.actionPasswordForKidAnalyseToKidAnalyse()
                          findNavController().navigate(action)
                      }else{
                          Toast.makeText(requireContext(),"Şifre bilgilerinizi kontrol edip tekrar deneyiniz.",Toast.LENGTH_LONG).show()
                      }

                  }
              }
            }

        }

        backButton.setOnClickListener {
            val action = PasswordForKidAnalyseDirections.actionPasswordForKidAnalyseToAccount()
            findNavController().navigate(action)
        }
    }

    private fun checkPassword(mail: String, password: String, onComplete : (Boolean) -> Unit){
        auth.signInWithEmailAndPassword(mail,password).addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    }


}