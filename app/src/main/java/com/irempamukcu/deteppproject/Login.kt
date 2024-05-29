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
import com.irempamukcu.deteppproject.databinding.FragmentLoginBinding


class Login : Fragment() {

    private lateinit var binding : FragmentLoginBinding
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


        val currentUser = auth.currentUser


        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = binding.backLogin
        val loginButton = binding.loginLogin



        backButton.setOnClickListener {
            val action = LoginDirections.actionLoginToStart2()
            findNavController().navigate(action)
        }

        loginButton.setOnClickListener {

            val mail = binding.inputMailLogin.text.toString()
            val password = binding.inputPasswordLogin.text.toString()

            if(mail.isNotEmpty() && password.isNotEmpty()){
                auth.signInWithEmailAndPassword(mail,password).addOnSuccessListener {
                    val action = LoginDirections.actionLoginToUser()
                    findNavController().navigate(action)
                }.addOnFailureListener{
                    Toast.makeText(requireContext(),"Yanlış e-posta ya da şifre. bilgilerinizi kontrol edip tekrar deneyin.",Toast.LENGTH_LONG).show()
                }
            }else{
               Toast.makeText(requireContext(),"Bir şeyler ters gitti. Gerekli alanları boş bırakmadığınızdan emin olun.",Toast.LENGTH_LONG).show()
            }

        }
    }

}