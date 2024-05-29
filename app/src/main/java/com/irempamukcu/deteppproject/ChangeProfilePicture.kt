package com.irempamukcu.deteppproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.irempamukcu.deteppproject.databinding.FragmentChangeProfilePictureBinding


class ChangeProfilePicture : Fragment() {
    private lateinit var binding : FragmentChangeProfilePictureBinding
    private var fragmentInfo = ""
    private var currentName = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChangeProfilePictureBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.let {
            fragmentInfo = ChangeProfilePictureArgs.fromBundle(it).fragmentInfo
            currentName = ChangeProfilePictureArgs.fromBundle(it).currentName

        }


        val yellowButton = binding.yellow
        val greenButton = binding.gren
        val blueButton = binding.blue
        val darkGreenButton = binding.darkgreen
        val pinkButton = binding.pink
        val purpleButton = binding.purple
        val redButton = binding.red
        val orangeButton = binding.orange
        val backButton = binding.back


        yellowButton.setOnClickListener {
            check_fragment(fragmentInfo,"yellow")
        }
        redButton.setOnClickListener {
           check_fragment(fragmentInfo,"red")
        }
        greenButton.setOnClickListener {
            check_fragment(fragmentInfo,"green")
        }
        darkGreenButton.setOnClickListener {
            check_fragment(fragmentInfo,"darkGreen")
        }
        blueButton.setOnClickListener {
            check_fragment(fragmentInfo,"blue")
        }
        pinkButton.setOnClickListener {
            check_fragment(fragmentInfo,"pink")
        }
        purpleButton.setOnClickListener {
            check_fragment(fragmentInfo,"purple")
        }
        orangeButton.setOnClickListener {
            check_fragment(fragmentInfo,"orange")
        }
        backButton.setOnClickListener {
            check_fragment(fragmentInfo,"current")
        }
    }

    private fun check_fragment(fragmentInfo : String, color : String){
        if(fragmentInfo.equals("add_kid")){
            val action = ChangeProfilePictureDirections.actionChangeProfilePictureToAddKid(color)
            findNavController().navigate(action)
        }else if (fragmentInfo.equals("change_kid")){
            val action = ChangeProfilePictureDirections.actionChangeProfilePictureToChangeKidInformation(color,currentName)
            findNavController().navigate(action)
        }else{
            Toast.makeText(requireContext(),"Bir şeyler ters gitti.", Toast.LENGTH_LONG).show()
        }
    }



}