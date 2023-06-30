package com.example.kampaybusiness.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kampaybusiness.R
import com.example.kampaybusiness.adapters.applicantsAdapter
import com.example.kampaybusiness.dataClasses.applicants_data_class
import com.google.firebase.database.*
import kotlin.properties.Delegates

class ApplicantsPopUpFragment : DialogFragment(), applicantsAdapter.ApplicantClickListener
{

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mApplicantAdapter: applicantsAdapter
    private lateinit var applicantsList: MutableList<applicants_data_class>
    private lateinit var mDatabse: DatabaseReference
    private lateinit var uid: String
    private lateinit var jobId: String
    private lateinit var jobTitle: String
    private lateinit var applicantDatabase: DatabaseReference
    private var payPerDay_CurrentJob by Delegates.notNull<Double>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        getDialog()!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val view = inflater.inflate(R.layout.fragment_applicants_pop_up, container, false)
        mRecyclerView = view.findViewById(R.id.recyclerView_Applicants)

        val tv_jobTitle = view.findViewById<TextView>(R.id.tv_popUp_heading_JobTitle)
        val close_btn = view.findViewById<ImageView>(R.id.close_btn_pop_Up)
        val close_btn_relative_layout = view.findViewById<RelativeLayout>(R.id.close_btn_popUp_Relative_layout)

        applicantsList = mutableListOf()
        mApplicantAdapter = applicantsAdapter(applicantsList,requireContext(),this)
        mRecyclerView.adapter = mApplicantAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(context)

        uid = arguments?.getString("uid") ?: ""
        jobId = arguments?.getString("jobId") ?: ""

        jobTitle = arguments?.getString("jobTitle") ?: ""

        payPerDay_CurrentJob = arguments?.getDouble("payPerDay")!!


        tv_jobTitle.text = "Applicants for $jobTitle"

        close_btn.setOnClickListener {
            dismiss()
        }
        close_btn_relative_layout.setOnClickListener {
            dismiss()
        }



        mDatabse = FirebaseDatabase.getInstance().getReference("user_business").child(uid)
            .child("Jobs").child(jobId)

        applicantDatabase = FirebaseDatabase.getInstance().getReference("user_student")

        retrieveApplicants()

        return view
    }

    private fun retrieveApplicants() {
        mDatabse.child("applicants").addValueEventListener(object :ValueEventListener{

            @SuppressLint("NotifyDataSetChanged")

            override fun onDataChange(snapshot: DataSnapshot) {
                applicantsList.clear()
                for (applicantSnapshot in snapshot.children){
                    val applicant = applicantSnapshot.getValue(applicants_data_class::class.java)
                    applicant?.let {
                        applicantsList.add(applicant)
                    }
                }
                mApplicantAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Error in Retrieving applicants data"
                    , Toast.LENGTH_LONG).show()
            }

        })
    }

    override fun onApplicantPhotoClicked(applicants_user_id_uid2: String) {

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Confirmation")
            .setMessage("Are you sure you want to pay this candidate ?")
            .setPositiveButton("Confirm") { _, _ ->
                applicantDatabase.child(applicants_user_id_uid2).child("info").child("earningsPerDay")
                    .setValue(payPerDay_CurrentJob)
                Toast.makeText(context,"Success :)",Toast.LENGTH_SHORT).show()
                dismiss()
            }

            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

    override fun onRejectClicked(applicants_user_id_uid2: String) {
        val MDATABASE = mDatabse.child("applicants").child(applicants_user_id_uid2)

            MDATABASE.removeValue().addOnCompleteListener {
            Toast.makeText(context,"Rejected Success",Toast.LENGTH_SHORT).show()
        }
    }




}