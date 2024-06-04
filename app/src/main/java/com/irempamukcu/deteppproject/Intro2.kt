package com.irempamukcu.deteppproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.irempamukcu.deteppproject.databinding.FragmentIntro2Binding


class Intro2 : Fragment() {

    private lateinit var binding : FragmentIntro2Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentIntro2Binding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nextButton = binding.nextIntro2
        val logoButton = binding.logoIntro2

        logoButton.setOnClickListener {
            val action = Intro2Directions.actionIntro2ToStart2()
            findNavController().navigate(action)
        }

        nextButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Change the image resource when the ImageView is pressed

                    nextButton.setImageResource(R.drawable.nextclick)
                    val action = Intro2Directions.actionIntro2ToStart2()
                    findNavController().navigate(action)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Optionally, revert to the initial image when the press is released or cancelled
                    nextButton.setImageResource(R.drawable.next)
                    true
                }
                else -> false
            }
        }





    }

}