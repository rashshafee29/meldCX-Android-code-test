package com.example.medlcx_android_code_test.model

import java.util.*

/**
 * Model class
 * url -> Entered URL
 * date -> Capture Date
 * imageName -> Saved Image Name
 */
data class URLImageInfo(
    var url: String,
    var date: Date,
    var imageName: String
)
