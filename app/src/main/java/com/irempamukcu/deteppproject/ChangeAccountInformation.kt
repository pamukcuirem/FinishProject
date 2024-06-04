package com.irempamukcu.deteppproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.irempamukcu.deteppproject.databinding.FragmentChangeAccountInformationBinding


class ChangeAccountInformation : Fragment() {

    private lateinit var binding : FragmentChangeAccountInformationBinding
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
        showInformation()
        binding = FragmentChangeAccountInformationBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = binding.backChangeAccount
        val saveButton = binding.saveButtonChangeAccount

        backButton.setOnClickListener {
            val action = ChangeAccountInformationDirections.actionChangeAccountInformationToAccount()
            findNavController().navigate(action)
        }

        saveButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Change the image resource when the ImageView is pressed

                    saveButton.setImageResource(R.drawable.saveclick)
                    val name = binding.inputNameChangeAccount.text.toString()
                    val surname = binding.inputSurnameChangeAccount.text.toString()
                    editInformation(name,surname)

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

    private fun showInformation() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val docRef = firestore.collection("users").document(uid)

            docRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val nameData = document.getString("name")
                    val surnameData = document.getString("surname")

                    binding.inputNameChangeAccount.setText(nameData)
                    binding.inputSurnameChangeAccount.setText(surnameData)


                } else {
                    Toast.makeText(requireContext(), "Bilgilerinize ulaşılamadı.", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Bilgilerinize ulaşılamadı.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun editInformation(newName: String, newSurname: String) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            val docRef = firestore.collection("users").document(uid)
            val userData = hashMapOf("name" to newName, "surname" to newSurname)

            docRef.set(userData, SetOptions.merge()).addOnSuccessListener {
                Toast.makeText(requireContext(),"Güncelleme başarılı.",Toast.LENGTH_LONG).show()
                val action = ChangeAccountInformationDirections.actionChangeAccountInformationToAccount()
                findNavController().navigate(action)
            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Güncelleme başarısız.",Toast.LENGTH_LONG).show()
            }


        }
    }

}





