package com.example.kampaybusiness.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kampaybusiness.dataClasses.job_details_data_class
import com.example.kampaybusiness.databinding.JobDescriptionCardBinding

class addedJobListAdapter(private val list: MutableList<job_details_data_class>):
RecyclerView.Adapter<addedJobListAdapter.addedJobListViewHolder>(){

    private var listener: addedJobListAdapterClickListener? = null

    fun setListener(listener: addedJobListAdapterClickListener){
        this.listener=listener
    }


      inner class addedJobListViewHolder( val binding: JobDescriptionCardBinding): RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): addedJobListViewHolder {
        val binding = JobDescriptionCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return addedJobListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: addedJobListViewHolder, position: Int) {

        with(holder) {
            with(list[position]) {
                val maxLength = 9 // Maximum length of the job title before applying the ellipsis
                val displayedTitle = if (jobTitle.length > maxLength) {
                    jobTitle.substring(0, maxLength) + "..." // Append "..." to the truncated title
                } else {
                    jobTitle // Use the original title if it doesn't exceed the maximum length
                }

                binding.jobName.text = displayedTitle

                binding.startTime.text = startTime
                binding.endTime.text = endTime
                binding.rates.text = payPerDay.toInt().toString()
                binding.description.text = description

                binding.deleteBtn.setOnClickListener {
                    listener?.onDeleteButtonClicked(this,position,jobId)

                }
                binding.editButton.setOnClickListener {
                    listener?.onEditButtonClicked(this,position,jobId)
                }

                binding.applicantsButton.setOnClickListener {
                    listener?.onShowApplicants(this,position, jobTitle,payPerDay,jobId)
                }

        }}}


        interface addedJobListAdapterClickListener {

            fun onDeleteButtonClicked(jobDetailsDataClass: job_details_data_class, position: Int,jobId:String)

            fun onEditButtonClicked(jobDetailsDataClass: job_details_data_class,position: Int,jobId :String)

            fun onShowApplicants(jobDetailsDataClass: job_details_data_class,position: Int,
                                 jobTitle1:String,payPerDay1:Double, jobId: String)

    }
}