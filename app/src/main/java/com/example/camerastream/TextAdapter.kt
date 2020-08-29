package com.example.camerastream
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView

class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val textLabel: EditText = view.findViewById(R.id.textLabelEditText)
    val textText: EditText = view.findViewById(R.id.configureTextTextEditText)
    val textPositionX: EditText = view.findViewById(R.id.textPositionXEditText)
    val textPositionY: EditText = view.findViewById(R.id.textPositionYEditText)
    val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)

    var labelTextWatcher: TextWatcher? = null
    var textTextWatcher: TextWatcher? = null
    var positionXTextWatcher: TextWatcher? = null
    var positionYTextWatcher: TextWatcher? = null
}

class TextAdapter(
    private var texts: ArrayList<Text>,
    private val afterLabelChanged: (Int, String) -> Unit,
    private val afterTextChanged: (Int, String) -> Unit,
    private val afterPositionXChanged: (Int, String) -> Unit,
    private val afterPositionYChanged: (Int, String) -> Unit,
    private val onDeleteClicked: (Int) -> Unit
): RecyclerView.Adapter<TextViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.configure_text, parent, false)
        return TextViewHolder(view)
    }

    override fun getItemCount(): Int {
        return texts.size
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        val text: Text = texts[position]

        holder.textLabel.removeTextChangedListener(holder.labelTextWatcher)
        holder.textLabel.setText(text.label)
        holder.labelTextWatcher = (object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                Log.i("mojtag", position.toString())
                afterLabelChanged(position, s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        holder.textLabel.addTextChangedListener(holder.labelTextWatcher)

        holder.textText.removeTextChangedListener(holder.textTextWatcher)
        holder.textText.setText(text.text)
        holder.textTextWatcher = (object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                afterTextChanged(position, s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        holder.textText.addTextChangedListener(holder.textTextWatcher)

        holder.textPositionX.removeTextChangedListener(holder.positionXTextWatcher)
        holder.textPositionX.setText(text.positionX?.toString())
        holder.positionXTextWatcher = (object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (!s?.toString().isNullOrEmpty() && s!!.toString().toFloat() > 100f){
                    holder.textPositionX.setText("100")
                    afterPositionXChanged(position, "100")
                }else{
                    afterPositionXChanged(position, s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        holder.textPositionX.addTextChangedListener(holder.positionXTextWatcher)

        holder.textPositionY.removeTextChangedListener(holder.positionYTextWatcher)
        holder.textPositionY.setText(text.positionY?.toString())
        holder.positionYTextWatcher = (object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (!s?.toString().isNullOrEmpty() && s!!.toString().toFloat() > 100f){
                    holder.textPositionY.setText("100")
                    afterPositionYChanged(position, "100")
                }else{
                    afterPositionYChanged(position, s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        holder.textPositionY.addTextChangedListener(holder.positionYTextWatcher)

        holder.deleteButton.setOnClickListener { onDeleteClicked(position) }
    }

    fun loadNewData(newTexts: ArrayList<Text>){
        texts = newTexts
        notifyDataSetChanged()
    }
}