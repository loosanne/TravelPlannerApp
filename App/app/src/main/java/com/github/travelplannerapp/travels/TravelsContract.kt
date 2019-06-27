package com.github.travelplannerapp.travels

//TODO [Dorota] Display message when list of travels is empty
interface TravelsContract {
    interface View {
        fun showAddTravel()
        //TODO [Dorota] Change to int (travel id) after database is implemented
        fun showTravelDetails(travel: String)

        fun showTravels()

        fun showNoTravels()
    }

    interface TravelItemView {
        fun setName(name: String)
    }

    interface Presenter{

        fun loadTravels()

        fun getTravelsCount() : Int

        fun onBindTravelsAtPosition(position: Int, itemView: TravelItemView)

        fun openTravelDetails(position: Int)
    }
}
