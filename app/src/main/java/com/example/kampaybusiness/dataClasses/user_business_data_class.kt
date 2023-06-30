package com.example.kampaybusiness

data class user_business_data_class(

    val businessName :String,
    val Address:String,
    val businessLogo: String,
    val email :String,
    val ratings : Double
)
{

    // Default constructor required for Firebase
    constructor() : this("", "", "","",0.0)
}

