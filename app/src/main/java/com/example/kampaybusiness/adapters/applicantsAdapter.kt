package com.example.kampaybusiness.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.kampaybusiness.R
import com.example.kampaybusiness.dataClasses.applicants_data_class

class applicantsAdapter(private val applicants: List<applicants_data_class>,val context: Context,
                        private val listener: ApplicantClickListener) :
    RecyclerView.Adapter<applicantsAdapter.ViewHolder>()  {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): applicantsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.applicants_each_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: applicantsAdapter.ViewHolder, position: Int) {
        val applicant = applicants[position]
        holder.bind(applicant)

    }

    override fun getItemCount(): Int {
        return applicants.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val applicantNameTextView: TextView = itemView.findViewById(R.id.name_applicant)
        private val applicantRatingLinearLayout:LinearLayout = itemView.findViewById(R.id.linear_layout_rating_applicants)
        private val applicantRatingsTextView:TextView = itemView.findViewById(R.id.user_ratings_pOP_uP)
        private val applicantMobileTextView:TextView = itemView.findViewById(R.id.tv_user_MobileNo_popUp)
        private val applicantEmailTextView:TextView = itemView.findViewById(R.id.tv_email_popUp)
        private val applicantPhotoImageView:ImageView = itemView.findViewById(R.id.applicants_profile_photo)

        private val Reject_Btn: ImageView = itemView.findViewById(R.id.reject_btn_applicants)


        fun bind(applicant: applicants_data_class) {

            applicantNameTextView.text = applicant.name
            applicantRatingsTextView.text = applicant.ratings.toString()
            applicantMobileTextView.text = applicant.mobileNo
            applicantEmailTextView.text = applicant.email

            Glide.with(context)
                .load(applicant.profileImage)
                .placeholder(R.drawable.profile_default_photo)
                .into(applicantPhotoImageView)

            applicantRatingLinearLayout.setBackgroundColor(when {
                applicant.ratings >= 4.50 -> Color.parseColor("#318F2F") // light green
                applicant.ratings >= 4.00 -> Color.parseColor("#34B132") // green
                applicant.ratings >= 3.50 -> Color.parseColor("#B1AB16") // yellow
                applicant.ratings >= 3.00 -> Color.parseColor("#E5DE21") // light yellow
                applicant.ratings >= 2.50 -> Color.parseColor("#B18616") // orange
                applicant.ratings >= 2.00 -> Color.parseColor("#FFC225") // light orange
                else -> Color.parseColor("#FF3F25") // red
            })

            val curent_user_uid = applicant.userId


            applicantPhotoImageView.setOnClickListener {
                listener.onApplicantPhotoClicked(curent_user_uid)
            }
            Reject_Btn.setOnClickListener {
                listener.onRejectClicked(curent_user_uid)
            }

            // Bind other applicant data to respective views
        }
    }
    interface ApplicantClickListener {
        fun onApplicantPhotoClicked(userId2:String)
        fun onRejectClicked(userId2: String)
    }

}