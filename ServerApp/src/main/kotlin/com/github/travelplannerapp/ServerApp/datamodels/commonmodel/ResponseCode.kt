package com.github.travelplannerapp.communication.commonmodel

enum class ResponseCode {
    OK,
    AUTHORIZATION_ERROR,
    EMAIL_TAKEN_ERROR,
    AUTHENTICATION_ERROR,
    ADD_TRAVEL_ERROR,
    UPDATE_TRAVEL_ERROR,
    DELETE_TRAVELS_ERROR,
    ADD_SCAN_ERROR,
    UPLOAD_SCAN_ERROR,
    DELETE_SCANS_ERROR,
    NO_ITEMS_SEARCH_ERROR,
    ADD_PLAN_ELEMENT_ERROR,
    DELETE_PLAN_ELEMENTS_ERROR,
    ADD_FRIEND_ERROR,
    DELETE_FRIENDS_ERROR,
    RATE_PLACE_ERROR,
    OTHER_ERROR;
}
