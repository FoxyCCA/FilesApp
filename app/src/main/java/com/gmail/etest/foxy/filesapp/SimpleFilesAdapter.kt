package com.gmail.etest.foxy.filesapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView

class SimpleFilesAdapter(private val context: Context, private val filesList: List<FileDataClass>)
    : ArrayAdapter<FileDataClass>(context, R.layout.simple_file_list, filesList) {

    private class ViewHolder {
        lateinit var fileName: TextView
        lateinit var fileType: ImageView
        lateinit var cardView: CardView
    }

    override fun getItem(position: Int): FileDataClass {
        return filesList[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cv: View?
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null){
            cv = LayoutInflater.from(context).inflate(R.layout.simple_file_list, null)
            viewHolder = ViewHolder()

            viewHolder.fileName = cv.findViewById(R.id.textViewSimpleFileName)
            viewHolder.fileType = cv.findViewById(R.id.imageViewSimpleFileType)
            viewHolder.cardView = cv.findViewById(R.id.cardViewSimpleFileListItem)
            cv.tag = viewHolder
            view = cv
        } else {
            viewHolder = convertView.tag as ViewHolder
            view = convertView
        }

        val file = getItem(position)

        if(file.isFileADirectory){
            viewHolder.fileType.setImageResource(R.drawable.ic_folder)
        } else {
            viewHolder.fileType.setImageResource(getExtensionImage(file.fileExtension))
        }

        viewHolder.fileName.text = file.fileName
        return view
    }
}