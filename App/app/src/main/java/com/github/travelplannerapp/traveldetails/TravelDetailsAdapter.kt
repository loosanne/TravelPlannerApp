package com.github.travelplannerapp.traveldetails

import android.content.DialogInterface
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.travelplannerapp.R
import com.github.travelplannerapp.R.layout
import com.github.travelplannerapp.deleteactionmode.DeleteActionModeToolbar
import com.github.travelplannerapp.utils.DateTimeUtils
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_plan_date_separator.*
import kotlinx.android.synthetic.main.item_plan_element.*

class TravelDetailsAdapter(val presenter: TravelDetailsContract.Presenter) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var actionMode: ActionMode? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {

            TravelDetailsContract.DayPlanItem.TYPE_PLAN -> PlanElementViewHolder(presenter, LayoutInflater.from(parent.context)
                    .inflate(layout.item_plan_element, parent, false))

            TravelDetailsContract.DayPlanItem.TYPE_DATE -> DateSeparatorViewHolder(presenter, LayoutInflater.from(parent.context)
                    .inflate(layout.item_plan_date_separator, parent, false))

            else -> throw Exception("There is no ViewHolder that matches the type $viewType")
        }
    }

    override fun getItemCount(): Int {
        return presenter.getDayPlanItemsCount()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            TravelDetailsContract.DayPlanItem.TYPE_PLAN -> presenter.onBindDayPlanItemAtPosition(position, holder as PlanElementViewHolder)
            TravelDetailsContract.DayPlanItem.TYPE_DATE -> presenter.onBindDayPlanItemAtPosition(position, holder as DateSeparatorViewHolder)
            else -> throw Exception("There is no method that would bind ViewHolder with type ${holder.itemViewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return presenter.getDayPlanItemType(position)
    }

    fun leaveActionMode() {
        actionMode = null
    }

    inner class DateSeparatorViewHolder(val presenter: TravelDetailsContract.Presenter, override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer, TravelDetailsContract.DateSeparatorItemView {

        override fun setDate(date: String) {
            textViewPlanDateSeparator.text = date
        }

    }

    inner class PlanElementViewHolder(val presenter: TravelDetailsContract.Presenter, override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer, TravelDetailsContract.PlanElementItemView, View.OnClickListener, View.OnLongClickListener {

        init {
            containerView.setOnClickListener(this)
            containerView.setOnLongClickListener(this)
            checkboxItemPlanElement.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener(
                    fun(_: CompoundButton, isChecked: Boolean) {
                        if (isChecked) {
                            presenter.addPlanElementIdToDelete(adapterPosition)
                        } else {
                            presenter.removePlanElementIdToDelete(adapterPosition)
                        }
                    }
            ))
        }

        override fun onClick(v: View?) {
            presenter.onPlanElementClicked(adapterPosition, textViewItemPlanName.text.toString())
        }

        override fun onLongClick(v: View?): Boolean {
            val context = v!!.context
            val completeOption = context.getString(R.string.mark_as_complete)
            val deleteOption = context.getString(R.string.menu_delete)
            val options = arrayOf(completeOption, deleteOption)
            val builder = AlertDialog.Builder(v!!.context)
            builder.setTitle(R.string.choose_option)
            builder.setItems(options, DialogInterface.OnClickListener { _, which ->
                if (options[which] == completeOption) {
                    presenter.markPlanElement(adapterPosition, true)
                }
                if (options[which] == deleteOption) {
                    actionMode = (containerView.context as AppCompatActivity)
                            .startSupportActionMode(DeleteActionModeToolbar(presenter))
                }
            })
            builder.show()
            return true
        }

        override fun setCompleted(completed: Boolean) {
            if (completed) {
                layoutPlanElementItem.alpha = 0.5F
            } else {
                layoutPlanElementItem.alpha = 1.0F
            }
        }

        override fun setName(name: String) {
            textViewItemPlanName.text = name
        }

        override fun setFromTime(time: String) {
            textViewItemPlanFromTime.text = DateTimeUtils.addLeadingZeroToTime(DateFormat.is24HourFormat(containerView.context), time)
        }

        override fun setIcon(icon: Int) {
            imageViewItemPlan.setImageDrawable(ContextCompat.getDrawable(containerView.context, icon))
        }

        override fun setLocation(location: String) {
            textViewItemPlanLocation.text = location
        }

        override fun showLine() {
            lineItemPlan.visibility = View.VISIBLE
        }

        override fun hideLine() {
            lineItemPlan.visibility = View.INVISIBLE
        }

        override fun setCheckbox() {
            if (actionMode != null) checkboxItemPlanElement.visibility = View.VISIBLE
            else checkboxItemPlanElement.visibility = View.INVISIBLE

            checkboxItemPlanElement.isChecked = false
        }
    }
}
