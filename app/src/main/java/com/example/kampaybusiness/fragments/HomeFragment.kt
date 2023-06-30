package com.example.kampaybusiness.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kampaybusiness.R
import com.example.kampaybusiness.adapters.addedJobListAdapter
import com.example.kampaybusiness.dataClasses.job_details_data_class
import com.example.kampaybusiness.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class HomeFragment : Fragment(), AddJobPopupFragment.dialogSaveBtnClickListener,
    addedJobListAdapter.addedJobListAdapterClickListener
     {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var getDatabaseRef: DatabaseReference
    private var popupFragment: AddJobPopupFragment ?= null
    private lateinit var adapter: addedJobListAdapter
    private lateinit var mList : MutableList<job_details_data_class>
    private lateinit var dialog: Dialog
    private lateinit var Current_JobId:String
    private lateinit var progress_bar_home_frag:ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        showProgressBar()
        progress_bar_home_frag = binding.homeProgressBar
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        getDataFromFirebase()
        addJob()
        }

    private fun getDataFromFirebase() {

        hideProgressBar()
        databaseRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                mList.clear()
                for (jobSnapshot in snapshot.children){
                    val JobTask = jobSnapshot.getValue(job_details_data_class::class.java)
                    Log.d(TAG, "JobTask value: $JobTask")

                    Current_JobId = JobTask!!.jobId

                    if (JobTask != null) {

                        mList.add(JobTask)
                        if (mList.isEmpty()){
                            binding.jobListRecyclerView.visibility = View.GONE
                            binding.defaultTVHome.visibility = View.VISIBLE

                        }else{

                            binding.jobListRecyclerView.visibility = View.VISIBLE
                            binding.defaultTVHome.visibility = View.GONE
                        }


                    }
                }
                binding.defaultTVHome.visibility = View.VISIBLE
                adapter.notifyDataSetChanged()
                progress_bar_home_frag.visibility= View.GONE

            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressBar()
                Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }


    private fun addJob() {
        hideProgressBar()
        binding.addJobBtn.setOnClickListener{
            if (popupFragment!= null)
                childFragmentManager.beginTransaction().remove(popupFragment!!).commit()

            popupFragment = AddJobPopupFragment()
            popupFragment!!.setListener(this)
            popupFragment!!.show(
                childFragmentManager,
                AddJobPopupFragment.TAG
            )

        }
    }

    private fun init() {
        hideProgressBar()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val uid1 = FirebaseAuth.getInstance().uid
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().reference.child("user_business")
                   .child(userId!!).child("Jobs")

        getDatabaseRef = FirebaseDatabase.getInstance().reference.child("user_business").child(uid1!!)

        binding.jobListRecyclerView.setHasFixedSize(true)
        binding.jobListRecyclerView.layoutManager = LinearLayoutManager(context)

        mList = mutableListOf()
        adapter = addedJobListAdapter(mList)
        adapter.setListener(this)
        binding.jobListRecyclerView.adapter= adapter

    }

    override fun onSaveJob(
        jobDetailsDataClass: job_details_data_class,
        jobTitle: EditText,
        payPerHour: EditText,
        startTime: TimePicker,
        endTime: TimePicker,
        description: EditText,
    ) {
        showProgressBar()
        databaseRef.child(jobDetailsDataClass.jobId).setValue(jobDetailsDataClass).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(activity, "Job added successfully", Toast.LENGTH_SHORT).show()
                hideProgressBar()
                jobTitle.text = null
                payPerHour.text = null
                description.text = null

                popupFragment!!.dismiss()
            } else {
                hideProgressBar()
                Toast.makeText(activity, "Job added successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

         override fun onUpdateJob(
             jobDetailsDataClass: job_details_data_class,
             jobTitle: EditText,
             payPerHour: EditText,
             startTime: TimePicker,
             endTime: TimePicker,
             description: EditText,
         )
         {
             showProgressBar()
             val newJobTitle = jobTitle.text.toString() // get the new job title entered in the UI
             val newPayPerHour = payPerHour.text.toString()
             val newDescription= description.text.toString()
             val newStartTime = startTime.hour.toString() + ":" + startTime.minute.toString()
             val newEndTime = endTime.hour.toString() + ":" + endTime.minute.toString()


             jobDetailsDataClass.jobTitle = newJobTitle
             jobDetailsDataClass.payPerHour = newPayPerHour
             jobDetailsDataClass.description = newDescription
             jobDetailsDataClass.startTime = newStartTime
             jobDetailsDataClass.endTime = newEndTime

             val map = HashMap<String, Any>()
             map["jobId"] = jobDetailsDataClass.jobId
             map["jobTitle"] = jobDetailsDataClass.jobTitle
             map["payPerHour"] = jobDetailsDataClass.payPerHour
             map["startTime"] = jobDetailsDataClass.startTime
             map["endTime"] = jobDetailsDataClass.endTime
             map["description"] = jobDetailsDataClass.description
             map["durationInSeconds"] = jobDetailsDataClass.durationInSeconds
             map["payPerDay"] = jobDetailsDataClass.payPerDay
             map["userId"] = jobDetailsDataClass.userId

             databaseRef.child(jobDetailsDataClass.jobId).updateChildren(map).addOnCompleteListener {
                 if (it.isSuccessful) {
                     hideProgressBar()
                     Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()

                 } else {
                     hideProgressBar()
                     Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
                 }
                 jobTitle.text = null
                 payPerHour.text = null
                 description.text = null
                 popupFragment!!.dismiss()
             }
         }


         private fun showProgressBar() {
             dialog = Dialog(requireActivity())
             dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
             dialog.setContentView(R.layout.dialog_wait)
             dialog.setCanceledOnTouchOutside(false)
             dialog.show()
         }

         private fun hideProgressBar() {
             dialog.dismiss()
         }

         override fun onDeleteButtonClicked(
             jobDetailsDataClass: job_details_data_class,
             position: Int,
             jobId :String
         ) {
             databaseRef.child(jobId).removeValue().addOnCompleteListener {
                 if (it.isSuccessful){
                     Toast.makeText(activity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                 }else{
                     Toast.makeText(activity,it.exception?.message, Toast.LENGTH_SHORT).show()
                 }
             }
         }

         override fun onEditButtonClicked(
             jobDetailsDataClass: job_details_data_class,
             position: Int,
             jobId :String
         ) {
             if (popupFragment != null)
                 childFragmentManager.beginTransaction().remove(popupFragment!!).commit()
             popupFragment = AddJobPopupFragment.newInstance(

                 jobId = jobId,
                 jobTitle = jobDetailsDataClass.jobTitle,
                 payPerHour = jobDetailsDataClass.payPerHour,
                 startTime = jobDetailsDataClass.startTime,
                 endTime = jobDetailsDataClass.endTime,
                 description = jobDetailsDataClass.description,
                 durationInSeconds = jobDetailsDataClass.durationInSeconds,
                 payPerDay = jobDetailsDataClass.payPerDay,
                 isEditMode = true
             )
             popupFragment!!.setListener(this)
             popupFragment!!.show(
                 childFragmentManager,AddJobPopupFragment.TAG
             )
         }

         override fun onShowApplicants(jobDetailsDataClass: job_details_data_class, position: Int,
                                       jobTitle1:String,payPerDay1:Double, jobId :String) {
             val popUpFragment = ApplicantsPopUpFragment()

             val userId = FirebaseAuth.getInstance().currentUser?.uid
             // Pass necessary data to the pop-up fragment using arguments
             val bundle = Bundle()

             bundle.putString("uid", userId)
             bundle.putString("jobId", jobId)
             bundle.putString("jobTitle",jobTitle1)
             bundle.putDouble("payPerDay", payPerDay1)

             popUpFragment.arguments = bundle

             // Show the pop-up fragment
             val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
             val transaction: FragmentTransaction = fragmentManager.beginTransaction()
             transaction.addToBackStack(null)
             popUpFragment.show(transaction, "popUpFragment")
         }

     }

