package com.github.travelplannerapp.ServerApp.datamanagement

import com.github.travelplannerapp.ServerApp.jsondatamodels.SignInRequest
import com.github.travelplannerapp.ServerApp.jsondatamodels.SignUpRequest

interface IUserManagement {
  
    fun getUserId(token: String): Int

    fun getUsersEmails():MutableList<String>
  
    fun verifyUser(token: String)
  
    fun authenticateUser(request: SignInRequest): Int
  
    fun updateAuthorizationToken(id: Int, request: SignInRequest): String
  
    fun addUser(request: SignUpRequest)

    fun updateUser(id: Int, changes: MutableMap<String, Any?>)
}