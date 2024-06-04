package com.irempamukcu.deteppproject

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class KidAdapter(private val kidList: MutableList<Kid>): RecyclerView.Adapter<KidAdapter.KidViewHolder>() {

    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore


    class KidViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameKidView: TextView = itemView.findViewById(R.id.textViewRecyclerViewUser)
        val colorKidView : ImageView = itemView.findViewById(R.id.profilePictureRecyclerViewUser)
        val changeKid : ImageView = itemView.findViewById(R.id.changeRecyclerViewUser)
        val binView : ImageView = itemView.findViewById(R.id.binRecyclerViewUser)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KidViewHolder {
        auth = Firebase.auth
        firestore = Firebase.firestore

        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_user,parent,false)
        return KidViewHolder(view)
    }

    override fun getItemCount(): Int {
        return kidList.size
    }

    override fun onBindViewHolder(holder: KidViewHolder, position: Int) {
        val kid = kidList[position]
        val kidNameUp = kid.name.uppercase()
        holder.nameKidView.text = kidNameUp

        val kidAge = kid.age.toInt()
        val sharedPreferences = holder.itemView.context.getSharedPreferences("mySharedPrefs",Context.MODE_PRIVATE)



        when (kid.color) {
            "purple" -> holder.colorKidView.setImageResource(R.drawable.purple)
            "pink" -> holder.colorKidView.setImageResource(R.drawable.pink)
            "blue" -> holder.colorKidView.setImageResource(R.drawable.blue)
            "green" -> holder.colorKidView.setImageResource(R.drawable.green)
            "darkGreen" -> holder.colorKidView.setImageResource(R.drawable.darkgreen)
            "yellow" -> holder.colorKidView.setImageResource(R.drawable.yellow)
            "orange" -> holder.colorKidView.setImageResource(R.drawable.orange)
            "red" -> holder.colorKidView.setImageResource(R.drawable.red)
            else -> holder.colorKidView.setImageResource(R.drawable.blue)
        }


        holder.colorKidView.setOnClickListener {
            with(sharedPreferences.edit()){
                putInt("currentKidAge",kidAge)
                apply()
            }
            val action = UserDirections.actionUserToLoggedIn(kid.name,kid.color)
            holder.colorKidView.findNavController().navigate(action)
        }

        holder.changeKid.setOnClickListener {

            val action = UserDirections.actionUserToChangeKidInformation(kid.color,kid.name)
            holder.changeKid.findNavController().navigate(action)
        }

        holder.binView.setOnClickListener {
            deleteKid(kid.name,holder)
        }



    }

    private fun deleteKid(name : String,holder: KidViewHolder){
        val currentUser = auth.currentUser
        val currentMail = currentUser?.email

        val kidCollection= firestore.collection("kids")

        if(currentMail != null){
            kidCollection.
            whereEqualTo("mail" , currentMail)
                .whereEqualTo("kidName", name)
                .get()
                .addOnSuccessListener {documents ->
                    for(document in documents){
                        kidCollection.document(document.id)
                            .delete().addOnSuccessListener {
                                //User again
                                val action = UserDirections.actionUserSelf()
                                findNavController(holder.itemView).navigate(action)

                            }.addOnFailureListener {
                                Toast.makeText(holder.itemView.context,"Silme işlemi başarısız.",Toast.LENGTH_LONG).show()
                            }
                    }

                }.addOnFailureListener {
                    Toast.makeText(holder.itemView.context,"Firestore'a erişim başarısız.",Toast.LENGTH_LONG).show()
                }
        }


    }


}