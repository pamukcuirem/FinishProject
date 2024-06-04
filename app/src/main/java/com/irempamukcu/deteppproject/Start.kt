package com.irempamukcu.deteppproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.irempamukcu.deteppproject.databinding.FragmentStartBinding


class Start : Fragment() {

    private lateinit var binding : FragmentStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStartBinding.inflate(inflater,container,false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerButton = binding.registerStart
        val loginButton = binding.loginStart


        registerButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Change the image resource when the ImageView is pressed

                    registerButton.setImageResource(R.drawable.registeringclick)
                    val action = StartDirections.actionStart2ToRegistering()
                    findNavController().navigate(action)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Optionally, revert to the initial image when the press is released or cancelled
                    registerButton.setImageResource(R.drawable.registering)
                    true
                }
                else -> false
            }
        }

        loginButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Change the image resource when the ImageView is pressed
                    val action = StartDirections.actionStart2ToLogin()
                    findNavController().navigate(action)
                    loginButton.setImageResource(R.drawable.loginclick)

                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Optionally, revert to the initial image when the press is released or cancelled
                    loginButton.setImageResource(R.drawable.login)
                    true
                }
                else -> false
            }
        }


    }


}