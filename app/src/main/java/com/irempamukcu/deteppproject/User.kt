package com.irempamukcu.deteppproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private lateinit var binding : FragmentUserBinding
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
        binding = FragmentUserBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addKid = binding.addButtonUser
        val goToAccount = binding.accountUser
        val currentUser = auth.currentUser
        val mail = currentUser?.email

        if(mail != null){
            firestore.collection("kids").whereEqualTo("mail",mail).get().addOnSuccessListener {documents ->
                val kidList = mutableListOf<KidData>()
                for(document in documents){
                    val name = document.getString("kidName") ?: ""
                    val age = document.getString("kidAge") ?: ""
                    val gender = document.getString("kidGender") ?: ""
                    val permission = document.getString("kidPermission") ?: ""
                    val color = document.getString("color") ?: ""
                    val kid = KidData(name, age,gender,permission,color)


                    kidList.add(kid)
                }

                handleRecyclerView(kidList)

            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Verilere ulaşmakta sıkıntı oluştu. Bize ulaşabilirsiniz...",Toast.LENGTH_LONG).show()
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
    }

    private fun handleRecyclerView(kidList : List<KidData>){
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewUser)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = KidAdapter(kidList)
    }
}