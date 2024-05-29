package com.irempamukcu.deteppproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.irempamukcu.deteppproject.databinding.FragmentRegisteringBinding

class Registering : Fragment() {

    private lateinit var binding : FragmentRegisteringBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

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

        binding = FragmentRegisteringBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = binding.backRegistering
        val registerButton = binding.registerButtonRegistering



        backButton.setOnClickListener {
            val action = RegisteringDirections.actionRegisteringToStart2()
            findNavController().navigate(action)
        }

        registerButton.setOnClickListener {
            val name = binding.inputNameRegistering.text.toString()
            val surname = binding.inputSurnameRegistering.text.toString()
            val mail = binding.inputMailRegistering.text.toString()
            val password = binding.inputPasswordRegistering.text.toString()

            if(name.isNotEmpty() && surname.isNotEmpty() && mail.isNotEmpty() && password.isNotEmpty()){

                register(name, surname, mail, password)


            }else{
                Toast.makeText(requireContext(),"Bilgilerinizi kontrol edip tekrar deneyin.",Toast.LENGTH_LONG).show()
            }

        }
    }



    private fun register(name: String, surname : String, mail : String, password : String){
            auth.createUserWithEmailAndPassword(mail, password)
                .addOnSuccessListener { authResult ->
                    val currentUser = authResult.user

                    if (currentUser != null) {
                        val userData = hashMapOf("name" to name, "surname" to surname, "mail" to mail)

                        firestore.collection("users").document(currentUser.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                val action = RegisteringDirections.actionRegisteringToUser()
                                findNavController().navigate(action)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Veri kaydı başarısız", Toast.LENGTH_LONG).show()

                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Kayıt başarısız", Toast.LENGTH_LONG).show()

                }



    }



    }

