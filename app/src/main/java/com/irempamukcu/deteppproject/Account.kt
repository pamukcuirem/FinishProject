package com.irempamukcu.deteppproject

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.irempamukcu.deteppproject.databinding.FragmentAccountBinding


class Account : Fragment() {

    private lateinit var binding : FragmentAccountBinding
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
        showInformation()

        binding = FragmentAccountBinding.inflate(inflater,container,false)
        return binding.root
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = binding.backAccount
        val kidButton = binding.kidAccount
        val exitButton = binding.exitAccount
        val changeButton = binding.changeAccount
        val contactButton = binding.contactAccount
        val deleteButton = binding.deleteAccount

        val mostDetectedFace = MostDetectedFace(firestore)

        mostDetectedFace.analyzeEmotions()


        backButton.setOnClickListener {
            val action = AccountDirections.actionAccountToUser()
            findNavController().navigate(action)

        }

        kidButton.setOnClickListener {
            val action = AccountDirections.actionAccountToPasswordForKidAnalyse()
            findNavController().navigate(action)
        }

        exitButton.setOnClickListener {
            auth.signOut()
            val action = AccountDirections.actionAccountToStart22()
            findNavController().navigate(action)

        }

        changeButton.setOnClickListener {
            val action = AccountDirections.actionAccountToChangeAccountInformation()
            findNavController().navigate(action)

        }



        contactButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Change the image resource when the ImageView is pressed

                    contactButton.setImageResource(R.drawable.contactusclick)
                    goToGmail()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Optionally, revert to the initial image when the press is released or cancelled
                    contactButton.setImageResource(R.drawable.contactus)
                    true
                }
                else -> false
            }
        }

        deleteButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Change the image resource when the ImageView is pressed

                    deleteButton.setImageResource(R.drawable.deleteaccountclick)
                    val action = AccountDirections.actionAccountToDeleteAccountVerify()
                    findNavController().navigate(action)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Optionally, revert to the initial image when the press is released or cancelled
                    deleteButton.setImageResource(R.drawable.deleteaccount)
                    true
                }
                else -> false
            }
        }

    }

    private fun showInformation(){

        val currentUser = auth.currentUser

        if(currentUser!= null){
          val uid = currentUser.uid
            val docRef = FirebaseFirestore.getInstance().collection("users").document(uid)

            docRef.get().addOnSuccessListener {
                if (it.exists()){
                    val name = it.getString("name")?.uppercase()
                    val surname = it.getString("surname")?.uppercase()
                    val sonText = "$name $surname"
                    binding.infoAccount.text= sonText
                }else{
                    Toast.makeText(requireContext(),"Bilgilerinize ulaşılamadı.",Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Bilgilerinize ulaşılamadı.",Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun goToGmail(){
        val currentUser = auth.currentUser
        val currentMail = currentUser?.email
        val emailData = hashMapOf(
            "email" to currentMail
        )

        firestore.collection("mailInput")
            .add(emailData)
            .addOnSuccessListener {
                Toast.makeText(context, "Size kısa süre içinde mail göndereceğiz", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Mail hatası", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }


    }



