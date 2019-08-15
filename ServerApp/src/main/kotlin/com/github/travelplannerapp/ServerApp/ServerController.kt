package com.github.travelplannerapp.ServerApp

import com.github.travelplannerapp.ServerApp.HereCharger.HereLoader
import com.github.travelplannerapp.ServerApp.datamanagement.UserManagement
import com.github.travelplannerapp.ServerApp.db.dao.User
import com.github.travelplannerapp.ServerApp.db.repositories.TravelRepository
import com.github.travelplannerapp.ServerApp.db.repositories.UserRepository
import com.github.travelplannerapp.ServerApp.jsondatamodels.JsonLoginRequest
import com.github.travelplannerapp.ServerApp.jsondatamodels.JsonScanUploadResponse
import com.github.travelplannerapp.ServerApp.jsondatamodels.LoginResponse
import com.github.travelplannerapp.ServerApp.jsondatamodels.ScanUploadResponse
import com.github.travelplannerapp.ServerApp.service.FileStorageService
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PostMapping


@RestController
class ServerController {

    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var travelRepository: TravelRepository
    @Autowired
    lateinit var userManagement: UserManagement
    @Autowired
    lateinit var fileStorageService: FileStorageService

    @GetMapping("/here")
    fun getExampleDataFromHere() {
        val connector = HereLoader()
        connector.findPlaceByText("chrysler", "40.74917", "-73.98529")
        connector.findBestWay("40.74917", "-73.98529", "45.74917",
                "-72.98529", "fastest", "car", "disabled")
    }
    @GetMapping("/travels")
    fun travels(@RequestParam(value = "email") email: String, @RequestParam(value = "auth") auth: String): List<String> {
        if (userManagement.verifyUser(email, auth)) {
            return travelRepository.getAllTravelsByUserEmail(email, auth).map { travel -> travel.name }
        }
        return listOf("Gdańsk", "Elbląg", "Toruń", "Olsztyn", "Szczecin")
    }

    @GetMapping("/db")
    fun getTravel(): User?  {
            return userRepository.getUserByEmail("jan.kowalski@gmail.com")
    }

    @PostMapping("/authenticate")
    fun authenticate(@RequestBody request: String): String {
        val loginRequest = Gson().fromJson(request, JsonLoginRequest::class.java)

        val jsonLoginAnswer = userManagement.authenticateUser(loginRequest)
        if (jsonLoginAnswer.result != LoginResponse.OK) {
            return Gson().toJson(jsonLoginAnswer)
        }

        val authToken = userManagement.updateAuthorizationToken(loginRequest)
        jsonLoginAnswer.authorizationToken = authToken

        return Gson().toJson(jsonLoginAnswer)
    }

    @PostMapping("/register")
    fun register(@RequestBody request: String): String {
        val loginRequest = Gson().fromJson(request, JsonLoginRequest::class.java)
        val jsonLoginAnswer = userManagement.addUser(loginRequest)

        return Gson().toJson(jsonLoginAnswer)
    }

    @PostMapping("/uploadScan")
    fun uploadFile(
            @RequestHeader("authorization") auth: String,
            @RequestParam("email") email: String,
            @RequestParam("file") file: MultipartFile): String {
        return if (userManagement.verifyUser(email, auth)) {
            val response = fileStorageService.storeFile(file, email)
            Gson().toJson(response)
        } else Gson().toJson(JsonScanUploadResponse(ScanUploadResponse.ERROR))
    }

}