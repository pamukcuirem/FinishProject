package com.irempamukcu.deteppproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.irempamukcu.deteppproject.databinding.FragmentKidAnalyseBinding

class KidAnalyse : Fragment() {
    private lateinit var binding : FragmentKidAnalyseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentKidAnalyseBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = binding.backKidAnalyse

        backButton.setOnClickListener {
            val action = KidAnalyseDirections.actionKidAnalyseToAccount()
            findNavController().navigate(action)
        }
    }


}