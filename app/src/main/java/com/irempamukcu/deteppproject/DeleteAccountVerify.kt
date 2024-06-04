package com.irempamukcu.deteppproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.irempamukcu.deteppproject.databinding.FragmentDeleteAccountVerifyBinding

class DeleteAccountVerify : Fragment() {
    private lateinit var binding : FragmentDeleteAccountVerifyBinding
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

        binding = FragmentDeleteAccountVerifyBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val deleteButton = binding.nextDeleteAccount
        val backButton = binding.backDeleteAccount

        backButton.setOnClickListener {
            val action = DeleteAccountVerifyDirections.actionDeleteAccountVerifyToAccount()
            findNavController().navigate(action)
        }


        deleteButton.setOnClickListener {
            val passwordInput = binding.inputDeleteAccount.text.toString()

            val currentUser = auth.currentUser

            if(currentUser != null){
                val currentMail = currentUser.email

                if (currentMail != null) {
                    deleteAnalyzes()
                    deleteKids()
                    checkPassword(currentMail, passwordInput){
                        if(it){

                            deleteAccount()

                        }else{
                            Toast.makeText(requireContext(),"Bir şeyler ters gitti, İşlem başarısız.",Toast.LENGTH_LONG).show()
                        }
                    }


                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }


    private fun deleteAccount(){


        val currentUser = auth.currentUser
        val currentMail = currentUser?.email


        if(currentUser != null){

            val uid = currentUser.uid
            val userDoc = firestore.collection("users").document(uid)


            userDoc.delete().addOnSuccessListener {
                currentUser.delete().addOnSuccessListener {
                    val action = DeleteAccountVerifyDirections.actionDeleteAccountVerifyToStart2()
                    findNavController().navigate(action)
                }.addOnFailureListener {
                    Toast.makeText(requireContext(),"Veri Silme İşlemi Başarısız.", Toast.LENGTH_LONG).show()
                }


            }
        }
    }

    private fun deleteKids(){

        val currentUser = auth.currentUser
        val currentMail = currentUser?.email

        val kidDoc = firestore.collection("kids")

        kidDoc.whereEqualTo("mail",currentMail).get().addOnSuccessListener {documents->
            for(document in documents){
                kidDoc.document(document.id).delete().addOnSuccessListener {

                }.addOnFailureListener {
                    Toast.makeText(requireContext(),"Çocuk bilgisi silme sorunu",Toast.LENGTH_LONG).show()
                }
            }

        }

    }

    private fun deleteAnalyzes(){
        val currentUser = auth.currentUser
        val currentMail = currentUser?.email


        val analyseDoc = firestore.collection("analyzes")



        analyseDoc.whereEqualTo("mail",currentMail).get().addOnSuccessListener {documents->
            for(document in documents){
                if(document.exists()){
                    analyseDoc.document(document.id).delete().addOnSuccessListener {

                    }.addOnFailureListener {
                        Toast.makeText(requireContext(),"Analiz silme hatası",Toast.LENGTH_LONG).show()
                    }
                }

            }

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

