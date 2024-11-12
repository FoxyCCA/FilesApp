package com.gmail.etest.foxy.filesapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.os.Environment
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.get
import java.text.SimpleDateFormat
import java.util.Locale

class FilesAdapter(private val context: Context, private var filesList: List<FileDataClass>)
    : ArrayAdapter<FileDataClass>(context, R.layout.file_list, filesList) {

    private class ViewHolder {
        lateinit var fileName: TextView
        lateinit var fileDir: TextView
        lateinit var fileType: ImageView
        lateinit var optionBar: CardView
        lateinit var cardView: CardView
    }

    override fun getItem(position: Int): FileDataClass {
        return filesList[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cv: View?
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            cv = LayoutInflater.from(context).inflate(R.layout.file_list, null)

            viewHolder = ViewHolder()

            viewHolder.fileName = cv.findViewById(R.id.textViewFileName)
            viewHolder.fileDir = cv.findViewById(R.id.textViewFileDir)
            viewHolder.fileType = cv.findViewById(R.id.imageViewFileType)
            viewHolder.optionBar = cv.findViewById(R.id.cardViewOptions)
            viewHolder.cardView = cv.findViewById(R.id.cardViewFileListItem)
            cv.tag = viewHolder
            view = cv
        } else {
            viewHolder = convertView.tag as ViewHolder
            view = convertView
        }

        val file = getItem(position)

        if(!file.isFileADirectory){
            viewHolder.fileType.setImageResource(getExtensionImage(file.fileExtension))
        } else {
            viewHolder.fileType.setImageResource(R.drawable.ic_folder)
        }

        val path = file.filePath.replace("/storage/emulated/0", "~")

        viewHolder.fileName.text = file.fileName
        viewHolder.fileDir.text = path

        if(file.isSelected){
            viewHolder.cardView.setBackgroundColor(Color.parseColor("#a4d2ed"))
        } else {
            viewHolder.cardView.setBackgroundColor(Color.WHITE)
        }

        if (file.fileName == " .."){
            viewHolder.optionBar.visibility = View.INVISIBLE
        }

        viewHolder.optionBar.setOnClickListener{
            val popupWindow = ListPopupWindow(context, null, com.google.android.material.R.attr.listPopupWindowStyle)
            popupWindow.anchorView = viewHolder.optionBar
            showMenu(viewHolder.optionBar, R.menu.file_option_menu, file)
        }

        return view
    }


    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, @MenuRes menuRes: Int, file: FileDataClass) {
        val metrics = DisplayMetrics()
        val popup = PopupMenu(context, v)

        popup.menuInflater.inflate(menuRes, popup.menu)


        if (file.file?.parentFile?.path == "/storage/emulated/0" || file.fileName == " .."){
            popup.menu[1].isVisible = false
        }

        println(file.file?.parentFile?.path)
        println(Environment.DIRECTORY_DOWNLOADS)
        popup.setOnMenuItemClickListener {
            if (it.itemId == R.id.option_info){
                val fileInfoIntent = Intent(context, FileInfo::class.java)

                fileInfoIntent.putExtra("extensionImage", if(file.isFileADirectory) R.drawable.ic_folder else getExtensionImage(file.fileExtension))
                fileInfoIntent.putExtra("fileName", file.fileName)
                fileInfoIntent.putExtra("filePath", file.filePath.replace("/storage/emulated/0", "~"))
                fileInfoIntent.putExtra("fileExtension", file.fileExtension.ifEmpty { "null" })
                fileInfoIntent.putExtra("fileSize", getFileSizeString(file.file!!.length()))
                fileInfoIntent.putExtra("fileLastModified", SimpleDateFormat("dd.MM.yyyy 'at' hh:mm aaa", Locale.getDefault()).format(file.file.lastModified()))

                startActivity(context, fileInfoIntent, null)
            }

            if(it.itemId == R.id.option_delete){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Are you sure you want to delete ${file.fileName}?")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, _ ->
                        val wasDeleted = file.file?.deleteRecursively()
                        if (wasDeleted == true){
                            Toast.makeText(context, "File was successfully deleted", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "File could not be removed", Toast.LENGTH_LONG).show()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
            true
        }

        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 32f, metrics)
                        .toInt()
                if (item.icon != null) {
                    item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                }
            }
        }
        popup.show()
    }
}