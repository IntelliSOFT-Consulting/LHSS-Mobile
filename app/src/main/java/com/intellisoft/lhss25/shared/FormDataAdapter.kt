package com.intellisoft.lhss25.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss25.R

class FormDataAdapter(
    private val formDataList: ArrayList<FormData>,
    private val context: Context
    )
    : RecyclerView.Adapter<FormDataAdapter.FormDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormDataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_form_data, parent, false)
        return FormDataViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormDataViewHolder, position: Int) {
        val formData = formDataList[position]
        holder.bind(formData)
    }

    override fun getItemCount(): Int {
        return formDataList.size
    }

    inner class FormDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val expandIcon: ImageView = itemView.findViewById(R.id.expandIcon)
        private val imgBtn: ImageView = itemView.findViewById(R.id.imgBtn)
        private val contentLayout: LinearLayout = itemView.findViewById(R.id.contentLayout)

        fun bind(formData: FormData) {
//            titleTextView.text = toSentenceCase(formData.title)

            val formatterClass = FormatterClass(context)

            val title = formData.title

            val workflowTitles = formatterClass.getWorkflowTitles(title)

            if (workflowTitles != null){
                titleTextView.text = formatterClass.toSentenceCase(workflowTitles.text)
                imgBtn.setImageResource(workflowTitles.iconId)
            }

            // Set default state
            expandIcon.setImageResource(R.drawable.ic_arrow_down)
            contentLayout.visibility = View.GONE

            toggleContent()

            // Handle expand/collapse
            titleTextView.setOnClickListener {
                toggleContent()
            }

            expandIcon.setOnClickListener {
                toggleContent()
            }
        }

        private fun toSentenceCase(name:String): String {
            return name
                .replace('_', ' ')      // Replace underscores with spaces
                .lowercase()            // Convert to lowercase
                .split(' ')             // Split the string into words
                .joinToString(" ") {    // Join the words with a space, capitalizing the first letter of each word
                    it.replaceFirstChar { char -> char.uppercase() }
                }
        }

        private fun toggleContent() {
            if (contentLayout.visibility == View.GONE) {
                // Expand
                contentLayout.visibility = View.VISIBLE
                expandIcon.setImageResource(R.drawable.ic_arrow_up)
                populateContent(formDataList[adapterPosition].formDataList)
            } else {
                // Collapse
                contentLayout.visibility = View.GONE
                expandIcon.setImageResource(R.drawable.ic_arrow_down)
            }
        }

        private fun populateContent(dbFormDataList: ArrayList<DbFormData>) {
            contentLayout.removeAllViews() // Clear previous content

            for (dbFormData in dbFormDataList) {
                val dbItemView = LayoutInflater.from(contentLayout.context).inflate(R.layout.list_item_db_form_data, contentLayout, false)

                val tagTextView: TextView = dbItemView.findViewById(R.id.tagTextView)
                val textTextView: TextView = dbItemView.findViewById(R.id.textTextView)

                tagTextView.text = dbFormData.tag
                textTextView.text = dbFormData.text

                contentLayout.addView(dbItemView)
            }
        }
    }
}


