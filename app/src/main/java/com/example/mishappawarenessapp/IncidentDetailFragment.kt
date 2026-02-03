package com.example.mishappawarenessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View

class IncidentDetailFragment : Fragment() {

    private val args: IncidentDetailFragmentArgs by navArgs()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        return inflater.inflate(R.layout.fragment_incident_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTv = view.findViewById<TextView>(R.id.tvTitle)
        val descTv = view.findViewById<TextView>(R.id.tvDescription)
        val typeTv = view.findViewById<TextView>(R.id.tvType)

        firestore.collection("incidents")
            .document(args.incidentId)
            .get()
            .addOnSuccessListener { doc ->
                titleTv.text = doc.getString("title")
                descTv.text = doc.getString("description")
                typeTv.text = doc.getString("type")
            }
    }
}
