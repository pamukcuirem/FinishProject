package com.irempamukcu.deteppproject

import android.content.Context
import android.content.SharedPreferences
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.irempamukcu.deteppproject.databinding.FragmentLoggedInBinding


class LoggedIn : Fragment() {

    private lateinit var binding : FragmentLoggedInBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var storage : FirebaseStorage
    private var kidName = ""
    private var kidColor = ""
    private var currentKidAge : Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        arguments?.let {
           kidName = LoggedInArgs.fromBundle(it).kidName
            kidColor = LoggedInArgs.fromBundle(it).kidColor
        }
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("mySharedPrefs",Context.MODE_PRIVATE)
        currentKidAge = sharedPreferences.getInt("currentKidAge",0)


        getData()

        binding = FragmentLoggedInBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val goToUsers = binding.profilesLoggedin
        val backButton = binding.backLoggedin

        setColor()

        goToUsers.setOnClickListener {
            val action = LoggedInDirections.actionLoggedInToUser()
            findNavController().navigate(action)

        }

        backButton.setOnClickListener {
            val action = LoggedInDirections.actionLoggedInToUser()
            findNavController().navigate(action)
        }
    }

    private fun getData(){


        firestore.collection("videos")
            .get()
            .addOnSuccessListener {documents->
                val contentList = mutableListOf<ContentData>()
                for(document in documents){
                    val videoName = document.getString("videoName")
                    val videoMinAge = document.getLong("videoMinAge")?.toInt()
                    val videoMaxAge = document.getLong("videoMaxAge")?.toInt()
                    val videoTopic = document.getString("videoTopic")
                    val videoSubTopic = document.getString("videoSubTopic")
                    val videoUrl = document.id

                   {

                    }

                    if(videoName != null && videoMinAge != null && videoMaxAge != null && videoTopic != null && videoSubTopic != null){
                        if(currentKidAge in (videoMinAge..videoMaxAge)){
                            val myContentData = ContentData(videoName, videoMinAge, videoMaxAge, videoTopic, videoSubTopic, videoUrl)
                            contentList.add(myContentData)

                            handleRecyclerView(contentList)
                        }

                        }


                }


            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Veri çekme işlemi başarısız.",Toast.LENGTH_LONG).show()
            }
    }


    private fun handleRecyclerView(contentList : List<ContentData>){
        val recyclerView = view?.findViewById<RecyclerView>(R.id.loggedInRecyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = ContentAdapter(contentList,kidName,kidColor)
    }

    private fun setColor(){
        when (kidColor) {
            "blue" -> {
                binding.profilesLoggedin.setImageResource(R.drawable.blue)
            }
            "red" -> {
                binding.profilesLoggedin.setImageResource(R.drawable.red)
            }
            "green" -> {
                binding.profilesLoggedin.setImageResource(R.drawable.green)
            }
            "yellow" -> {
                binding.profilesLoggedin.setImageResource(R.drawable.yellow)
            }
            "darkGreen" -> {
                binding.profilesLoggedin.setImageResource(R.drawable.darkgreen)
            }
            "purple" -> {
                binding.profilesLoggedin.setImageResource(R.drawable.purple)
            }
            "pink" -> {
                binding.profilesLoggedin.setImageResource(R.drawable.pink)
            }
            "orange" -> {
                binding.profilesLoggedin.setImageResource(R.drawable.orange)
            }
            else -> {
                binding.profilesLoggedin.setImageResource(R.drawable.applogo)
            }
        }
    }

}