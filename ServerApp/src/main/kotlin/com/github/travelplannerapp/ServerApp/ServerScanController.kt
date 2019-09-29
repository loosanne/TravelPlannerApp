package com.github.travelplannerapp.ServerApp

import com.github.travelplannerapp.ServerApp.datamanagement.ScanManagement
import com.github.travelplannerapp.ServerApp.datamanagement.UserManagement
import com.github.travelplannerapp.ServerApp.db.dao.Scan
import com.github.travelplannerapp.ServerApp.exceptions.ResponseCode
import com.github.travelplannerapp.ServerApp.exceptions.UploadScanException
import com.github.travelplannerapp.ServerApp.datamodels.Response
import com.github.travelplannerapp.ServerApp.services.FileStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest

@RestController
class ServerScanController {

    @Autowired
    lateinit var userManagement: UserManagement
    @Autowired
    lateinit var fileStorageService: FileStorageService
    @Autowired
    lateinit var scanManagement: ScanManagement

    @PostMapping("/users/{user-id}/scans")
    fun uploadFile(
        @RequestHeader("authorization") token: String,
        @PathVariable("user-id") userId: Int,
        @RequestParam("travelId") travelId: Int,
        @RequestParam("file") file: MultipartFile
    ): Response<Scan> {
        userManagement.verifyUser(token)
        var fileName: String? = null
        try {
            fileName = fileStorageService.storeFile(file)
            val scan = scanManagement.addScan(userId, travelId, fileName)
            return Response(ResponseCode.OK, scan)
        } catch (ex: Exception) {
            fileName?.let { fileStorageService.deleteFile(it) }
            throw UploadScanException(ex.localizedMessage)
        }
    }

    @GetMapping("/users/{user-id}/scans")
    fun getScans(
        @RequestHeader("authorization") token: String,
        @PathVariable("user-id") userId: Int,
        @RequestParam travelId: Int
    ): Response<List<Scan>> {
        userManagement.verifyUser(token)
        val scans = scanManagement.getScans(userId, travelId)
        return Response(ResponseCode.OK, scans)
    }

    @GetMapping("/scans/{fileName:.+}")
    fun downloadFile(@PathVariable fileName: String, request: HttpServletRequest): ResponseEntity<Resource> {
        // Load file as Resource
        val resource = fileStorageService.loadFileAsResource(fileName)

        // Try to determine file's content type
        var contentType = request.servletContext.getMimeType(resource.file.absolutePath)

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream"
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.filename + "\"")
                .body(resource)
    }

    @DeleteMapping("/users/{user-id}/scans")
    fun deleteScans(
        @RequestHeader("authorization") token: String,
        @PathVariable("user-id") userId: Int,
        @RequestBody scans: MutableSet<Scan>
    ): Response<Unit> {
        userManagement.verifyUser(token)

        println("aaaaa")
        print(scans.toString())
        for (scan in scans) {
            println("a")
            scanManagement.deleteScan(scan)
            scan.name?.let { fileStorageService.deleteFile(it) }
        }
        return Response(ResponseCode.OK, Unit)
    }
}
