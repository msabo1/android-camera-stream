package com.example.camerastream

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val textLabel: EditText = view.findViewById(R.id.textLabel)
    val textText: EditText = view.findViewById(R.id.textText)
    val textPositionX: EditText = view.findViewById(R.id.textPositionX)
    val textPositionY: EditText = view.findViewById(R.id.textPositionY)
}

class TextAdapter(private var texts: ArrayList<Text>): RecyclerView.Adapter<TextViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.configure_text, parent, false)
        return TextViewHolder(view)
    }

    override fun getItemCount(): Int {
        return texts.size
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        val text: Text = texts[position]

        holder.textLabel.setText(text.label)
        holder.textText.setText(text.text)
        holder.textPositionX.setText(text.positionX.toString())
        holder.textPositionY.setText(text.positionY.toString())

    }

    fun loadNewData(newTexts: ArrayList<Text>){
        texts = newTexts
        notifyDataSetChanged()
    }
}