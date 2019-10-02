package com.github.travelplannerapp.ServerApp.exceptions

class SearchNoItemsException(
    override val message: String,
    override val code: ResponseCode = ResponseCode.NO_ITEMS_SEARCH_ERROR
) : Exception(message), ApiException
