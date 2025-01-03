package com.intellisoft.lhss25.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss25.R


class NotificationAdapter(
    private val context: Context,
    private val dbCommunicationDataList: ArrayList<DbCommunicationData>,
    private val fragment: Fragment,
    ) : RecyclerView.Adapter<NotificationAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgBtnUnread: ImageButton = itemView.findViewById(R.id.imgBtnUnread)
        val tvNotificationTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        val tvNotificationDateTime: TextView = itemView.findViewById(R.id.tvNotificationDateTime)
        val tvNotificationContent: TextView = itemView.findViewById(R.id.tvNotificationContent)
        val tvNotificationViewForm: TextView = itemView.findViewById(R.id.tvNotificationViewForm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_list_view, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parentItem = dbCommunicationDataList[position]

        val formatterClass = FormatterClass(context)
        val communicationId = parentItem.id
        val navigationId = parentItem.navigationId
        val status = parentItem.status
        val basedOn = parentItem.basedOn.first()
        val date = parentItem.dateTime
        val dateTime = formatterClass.convertDateFormat(date) ?: ""
        val content = parentItem.content.replace("_", " ")
        val title = parentItem.title

        holder.tvNotificationTitle.text = title
        holder.tvNotificationDateTime.text = dateTime
        holder.tvNotificationContent.text = content

        if (status == "COMPLETED"){
            holder.imgBtnUnread.visibility = View.GONE
        } else {
            holder.imgBtnUnread.visibility = View.VISIBLE

            holder.tvNotificationTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.tvNotificationDateTime.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.tvNotificationContent.setTypeface(null, android.graphics.Typeface.BOLD)
        }

        holder.tvNotificationViewForm.setOnClickListener {
            formatterClass.saveSharedPref("","notificationBasedOn",basedOn)
            formatterClass.saveSharedPref("","communicationId",communicationId)
            findNavController(fragment).navigate(R.id.action_notificationFragment_to_viewFormFragment)
        }

    }

    override fun getItemCount(): Int = dbCommunicationDataList.size
}