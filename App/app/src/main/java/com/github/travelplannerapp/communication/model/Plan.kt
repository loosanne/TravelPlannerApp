package com.github.travelplannerapp.communication.model

import java.io.Serializable

data class Plan(
        var id: Int,
        var locale: String, // time format that allows time to be correct in every time zone
        var fromDateTimeMs: Long,
        var toDateTimeMs: Long,
        var placeId: Int,
        var travelId: Int,
        var place: Place
) : Serializable
