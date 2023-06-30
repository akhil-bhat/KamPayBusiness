package com.example.kampaybusiness.dataClasses

data class job_details_data_class(
    var jobId : String,
    var jobTitle: String,
    var payPerHour: String,
    var startTime: String,
    var endTime: String,
    var description: String,
    var durationInSeconds: Long,
    var payPerDay : Double,
    var userId : String
) {
    constructor() : this( "","", "",
        "", "","", 0L,0.0,"")
}