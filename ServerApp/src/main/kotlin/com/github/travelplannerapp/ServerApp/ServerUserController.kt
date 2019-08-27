package com.github.travelplannerapp.ServerApp

import com.github.travelplannerapp.ServerApp.datamanagement.UserManagement
import com.github.travelplannerapp.ServerApp.jsondatamodels.Response
import com.github.travelplannerapp.ServerApp.jsondatamodels.SignInRequest
import com.github.travelplannerapp.ServerApp.jsondatamodels.SignInResponse
import com.github.travelplannerapp.ServerApp.jsondatamodels.SignUpRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class ServerUserController {

    @Autowired
    lateinit var userManagement: UserManagement


    @PostMapping("/authorize")
    fun authorize(@RequestHeader("authorization") token: String, userId: Int): Response<Unit> {
        userManagement.verifyUser(userId, token)
        return Response(200, Unit)
    }

    @PostMapping("/authenticate")
    fun authenticate(@RequestBody request: SignInRequest): Response<SignInResponse> {
        val userId = userManagement.authenticateUser(request)
        val token = userManagement.updateAuthorizationToken(request)
        return Response(200, SignInResponse(token, userId))
    }

    @PostMapping("/register")
    fun register(@RequestBody request: SignUpRequest): Response<Unit> {
        userManagement.addUser(request)
        return Response(200, Unit)
    }
}