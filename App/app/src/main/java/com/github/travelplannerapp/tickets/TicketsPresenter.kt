package com.github.travelplannerapp.tickets

import com.github.travelplannerapp.BasePresenter
import com.github.travelplannerapp.R
import com.github.travelplannerapp.communication.ApiException
import com.github.travelplannerapp.communication.CommunicationService
import com.github.travelplannerapp.communication.model.ResponseCode
import com.github.travelplannerapp.communication.model.Scan
import com.github.travelplannerapp.utils.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable

class TicketsPresenter(view: TicketsContract.View, private val travelId: Int) : BasePresenter<TicketsContract.View>(view), TicketsContract.Presenter {

    private val compositeDisposable = CompositeDisposable()
    private var tickets = ArrayList<Scan>()
    private var ticketsChecks = BooleanArray(tickets.size)

    override fun onAddScanClick() {
        if (view.verifyPermissions()) {
            view.openCamera()
        } else {
            view.requestPermissions()
        }
    }

    override fun onPhotoTaken() {
        view.showScanner(travelId)
    }

    override fun loadScans() {
        view.setLoadingIndicatorVisibility(true)

        compositeDisposable.add(CommunicationService.serverApi.getScans(travelId)
                .observeOn(SchedulerProvider.ui())
                .subscribeOn(SchedulerProvider.io())
                .map { if (it.responseCode == ResponseCode.OK) it.data!! else throw ApiException(it.responseCode) }
                .subscribe(
                        { scans -> handleLoadScansResponse(scans) },
                        { error -> handleErrorResponse(error) }
                ))
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }

    override fun getTicketsCount(): Int {
        return tickets.size
    }

    override fun onBindTicketsAtPosition(position: Int, itemView: TicketsContract.TicketItemView) {
        val ticket = tickets[position]
        itemView.setImage(CommunicationService.getScanUrl(ticket.name))
        itemView.setCheckbox(ticketsChecks[position])
    }

    override fun onAddedScan(scan: Scan) {
        tickets.add(scan)
        ticketsChecks = BooleanArray(tickets.size) { false }
        view.showTickets()
        view.onDataSetChanged()
    }

    override fun onScanClicked(position: Int) {
        val ticket = tickets[position]
        view.showFullScan((CommunicationService.getScanUrl(ticket.name)))
    }

    override fun onDeleteClicked() {
        if (getTicketsToDelete().size > 0) {
            view.showConfirmationDialog()
        }
    }

    override fun deleteTickets() {
        val ticketsToDelete = getTicketsToDelete()
        compositeDisposable.add(CommunicationService.serverApi.deleteScans(ticketsToDelete)
                .observeOn(SchedulerProvider.ui())
                .subscribeOn(SchedulerProvider.io())
                .map { if (it.responseCode == ResponseCode.OK) it.data!! else throw ApiException(it.responseCode) }
                .subscribe(
                        { handleDeleteTicketsResponse() },
                        { error -> handleErrorResponse(error) }
                ))
    }

    override fun enterActionMode() {
        view.onDataSetChanged()
        view.showActionMode()
    }

    override fun leaveActionMode() {
        ticketsChecks = BooleanArray(tickets.size) { false }
        view.onDataSetChanged()
        view.showNoActionMode()
    }

    override fun setTicketCheck(position: Int, checked: Boolean) {
        ticketsChecks[position] = checked
    }

    private fun getTicketsToDelete(): ArrayList<Scan> {
        val ticketsToDelete = ArrayList<Scan>()
        ticketsChecks.mapIndexed { index, isChecked -> if (isChecked) ticketsToDelete.add(tickets[index]) }
        return ticketsToDelete
    }

    private fun handleLoadScansResponse(scans: List<Scan>) {
        tickets = ArrayList(scans)
        ticketsChecks = BooleanArray(tickets.size) { false }
        view.onDataSetChanged()
        view.setLoadingIndicatorVisibility(false)

        if (this.tickets.isEmpty()) view.showNoTickets() else view.showTickets()
    }

    private fun handleDeleteTicketsResponse() {
        loadScans()
        view.showSnackbar(R.string.delete_tickets_ok)
    }

    private fun handleErrorResponse(error: Throwable) {
        loadScans()
        if (error is ApiException) view.showSnackbar(error.getErrorMessageCode())
        else view.showSnackbar(R.string.server_connection_error)
    }

}
