package com.example.kampaybusiness.fragments

import android.os.Bundle
import java.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.kampaybusiness.dataClasses.job_details_data_class
import com.example.kampaybusiness.databinding.FragmentAddJobPopupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import java.util.*
import kotlin.properties.Delegates


class AddJobPopupFragment : DialogFragment() {


    private lateinit var binding: FragmentAddJobPopupBinding
    private lateinit var listener: dialogSaveBtnClickListener
    private var per_day by Delegates.notNull<Double>()
    private var jobDetailsDataClass:job_details_data_class ?= null

    fun setListener(listener: dialogSaveBtnClickListener){
        this.listener = listener
    }

    companion object{
        const val TAG = "DialogFragment"

        @JvmStatic
        fun newInstance(jobId: String? = null, jobTitle: String? = null, payPerHour: String? = null, startTime: String? = null,
                        endTime: String? = null, description: String? = null, durationInSeconds: Long? = null,
                        payPerDay: Double? = null, isEditMode: Boolean = false) = AddJobPopupFragment().apply {
                            arguments= Bundle().apply {
                                putString("jobId",jobId)
                                putString("jobTitle",jobTitle)
                                putString("payPerHour",payPerHour)
                                putString("startTime",startTime)
                                putString("endTime",endTime)
                                putString("description",description)
                                putLong("durationInSeconds", durationInSeconds!!)
                                putDouble("payPerDay",payPerDay!!)

                            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddJobPopupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null){
            jobDetailsDataClass = job_details_data_class(
                arguments?.getString("jobId").toString(),
                arguments?.getString("jobTitle").toString(),
                arguments?.getString("payPerHour").toString(),
                arguments?.getString("startTime").toString(),
                arguments?.getString("endTime").toString(),
                arguments?.getString("description").toString(),
                arguments?.getLong("durationInSeconds")!!.toLong(),
                arguments?.getDouble("payPerDay")!!.toDouble(),
                arguments?.getString("userId").toString()
            )

            binding.jobTitleEdittext.setText(jobDetailsDataClass?.jobTitle)
            binding.payPerHourEdittext.setText(jobDetailsDataClass?.payPerHour)
            binding.descriptionEdittext.setText(jobDetailsDataClass?.description)

            val startTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(jobDetailsDataClass?.startTime!!)

            // Set the hour and minute values on the TimePicker
            val calendar = Calendar.getInstance()
            calendar.time = startTime!!
            binding.startTimePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            binding.startTimePicker.minute = calendar.get(Calendar.MINUTE)


            val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(jobDetailsDataClass?.endTime!!)

            val calendar1 = Calendar.getInstance()
            calendar.time = endTime!!
            binding.endTimePicker.hour = calendar1.get(Calendar.HOUR_OF_DAY)
            binding.endTimePicker.minute = calendar1.get(Calendar.MINUTE)


        }

        addJobDetails()
    }

    private fun addJobDetails() {


        binding.savebtn.setOnClickListener {

            val jobTitle = binding.jobTitleEdittext.text.toString()
            val payPerHour = binding.payPerHourEdittext.text.toString()
            val startTime = String.format("%02d:%02d", binding.startTimePicker.hour, binding.startTimePicker.minute)
            val endTime = String.format("%02d:%02d", binding.endTimePicker.hour, binding.endTimePicker.minute)
            val description = binding.descriptionEdittext.text.toString()

            val duration = calculateDuration(startTime, endTime)
            val durationInSeconds = duration.seconds
            per_day= calculatePayPerDay(payPerHour.toDouble(),duration)


            payPerHour.let {
                if (it.isNotBlank()) {

                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val databaseRef = FirebaseDatabase.getInstance().reference.child("user_business")
                        .child(userId!!).child("Jobs")


                    val jobRef = databaseRef.push()
                    val job_details = job_details_data_class(jobId = jobRef.key.toString(),jobTitle,
                        payPerHour, startTime, endTime, description, durationInSeconds, per_day,userId)

                    if (jobTitle.isNotEmpty() && payPerHour.isNotEmpty() && startTime.isNotEmpty()
                        && endTime.isNotEmpty() && description.isNotEmpty() ){

                        val isEditMode = arguments?.getBoolean("isEditMode") ?: false
                        if (isEditMode) {
                            jobDetailsDataClass?.jobTitle = jobTitle
                            jobDetailsDataClass?.description = description
                            jobDetailsDataClass?.payPerHour = payPerHour
                            jobDetailsDataClass?.startTime= startTime
                            jobDetailsDataClass?.endTime = endTime

                            if (per_day>9999){
                                Toast.makeText(context,"Pay Per Day is exceeding Rs. 9999",Toast.LENGTH_SHORT).show()
                            }else if (durationInSeconds>43200){
                                Toast.makeText(context,"Duration is exceeding 12 hours",Toast.LENGTH_SHORT).show()
                            }else{
                            listener.onUpdateJob(job_details,
                                binding.jobTitleEdittext, binding.payPerHourEdittext,binding.startTimePicker,
                                binding.endTimePicker,binding.descriptionEdittext)}
                        }
                        else{
                            if (per_day>9999){
                                Toast.makeText(context,"Pay Per Day is exceeding Rs. 9999",Toast.LENGTH_SHORT).show()
                            }else if (durationInSeconds>43200){
                                Toast.makeText(context,"Duration is exceeding 12 hours",Toast.LENGTH_SHORT).show()
                            }
                            else{
                            listener.onSaveJob(job_details,
                                binding.jobTitleEdittext, binding.payPerHourEdittext,binding.startTimePicker,
                                binding.endTimePicker,binding.descriptionEdittext)}
                        }

                    }else{
                        Toast.makeText(context,"empty fields not allowed",Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(context,"empty fields not allowed",Toast.LENGTH_SHORT).show()
                }
            }



        }
    }

    interface dialogSaveBtnClickListener{
        fun onSaveJob(jobDetailsDataClass: job_details_data_class,
                      jobTitle : EditText,payPerHour: EditText, startTime:TimePicker,endTime: TimePicker,
                      description: EditText)

        fun onUpdateJob(jobDetailsDataClass: job_details_data_class,
                      jobTitle : EditText,payPerHour: EditText, startTime:TimePicker,endTime: TimePicker,
                      description: EditText)

    }

    fun calculateDuration(startTime: String, endTime: String): Duration {
        val start = LocalTime.parse(startTime)
        val end = LocalTime.parse(endTime)
        return Duration.between(start, end)
    }

    fun calculatePayPerDay(payPerHour: Double, duration: Duration): Double {
        val hours = duration.toMinutes() / 60.0
        return payPerHour * hours
    }

}