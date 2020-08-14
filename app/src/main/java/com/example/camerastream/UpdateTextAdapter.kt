package com.example.camerastream

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UpdateTextViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val textInfo: TextView = view.findViewById(R.id.textInfoTextView)
    val textText: EditText = view.findViewById(R.id.textTextEditText)
    val applyButton: Button = view.findViewById(R.id.applyTextButton)

}

class UpdateTextAdapter(
    private var texts: ArrayList<Text>,
    private val onApplyClicked: (Int, String) -> Unit
): RecyclerView.Adapter<UpdateTextViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateTextViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.update_text, parent, false)
        return UpdateTextViewHolder(view)
    }

    override fun getItemCount(): Int {
        return texts.size
    }

    override fun onBindViewHolder(holder: UpdateTextViewHolder, position: Int) {
        val text: Text = texts[position]

        holder.textInfo.text = """${text.label}(${text.positionX}, ${text.positionY})"""
        holder.textText.setText(text.text)
        holder.applyButton.setOnClickListener{v: View? ->  onApplyClicked(position, holder.textText.text.toString())}
    }

    fun loadNewData(newTexts: ArrayList<Text>){
        texts = newTexts
        notifyDataSetChanged()
    }
}