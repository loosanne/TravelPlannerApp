package com.github.travelplannerapp.ServerApp.datamanagement

import com.github.travelplannerapp.ServerApp.db.dao.Travel
import com.github.travelplannerapp.ServerApp.db.merge
import com.github.travelplannerapp.ServerApp.db.repositories.TravelRepository
import com.github.travelplannerapp.ServerApp.db.transactions.TravelTransaction
import com.github.travelplannerapp.ServerApp.exceptions.AddTravelException
import com.github.travelplannerapp.ServerApp.jsondatamodels.AddTravelRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TravelManagement : ITravelManagement {

    @Autowired
    lateinit var travelTransaction: TravelTransaction
    @Autowired
    lateinit var travelRepository: TravelRepository

    override fun addTravel(request: AddTravelRequest): Travel {
        val addedTravel = travelTransaction.addTravel(request.travelName, request.userId)
        if (addedTravel != null) {
            return addedTravel
        } else {
            throw AddTravelException("Error when adding travel")
        }
    }

    override fun updateTravel(id: Int, changes: MutableMap<String, Any?>): Travel? {
        val travel = travelRepository.get(id)
        val travelChanges = Travel(changes)
        val updatedTravel = travelChanges merge travel!!

        return if (travelRepository.update(updatedTravel)) updatedTravel else null
    }
}