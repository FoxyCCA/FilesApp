package com.gmail.etest.foxy.filesapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ListPopupWindow
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import com.gmail.etest.foxy.filesapp.databinding.ActivityMainBinding
import io.ktor.util.toLowerCasePreservingASCIIRules
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var currentDir: File
    private lateinit var adapter: FilesAdapter
    private lateinit var fileList: List<FileDataClass>
    private var selectionMode: Boolean = false
    private val rootDir = Environment.getExternalStorageDirectory()
    private var selectionList: MutableList<FileDataClass> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        setContentView(binding.root)

        currentDir = rootDir
        updateFileAdapter(currentDir, null)

        binding.searchViewFileSearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                updateFileAdapter(currentDir, newText)
                return true
            }

        })

        binding.listViewFiles.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val file = fileList[position]

                if (selectionMode) {
                    file.isSelected = !file.isSelected

                    if (file.isSelected) {
                        selectionList.add(file)
                    } else {
                        selectionList.remove(file)
                        if (selectionList.isEmpty()) {
                            binding.imageViewUnselectButton.visibility = View.INVISIBLE
                            binding.imageButtonSelectionOptions.visibility = View.INVISIBLE
                            binding.textViewSelectCount.visibility = View.INVISIBLE
                            selectionMode = false
                        }
                    }

                    binding.textViewSelectCount.text = selectionList.count().toString()
                    adapter.notifyDataSetChanged()

                } else {
                    selectionList.clear()

                    if (file.fileExtension == "zip") {
                        val unzipIntent = Intent(this, UnarchiveFiles::class.java)
                        unzipIntent.putExtra("zipFilePath", file.filePath)
                        startActivity(unzipIntent)
                    } else if (file.isFileADirectory) {
                        updateFileAdapter(file.file!!, null)
                        currentDir = file.file
                    } else {
                        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file.file!!)
                        val intent = Intent(Intent.ACTION_VIEW)

                        intent.setDataAndType(uri, getMimeType(file.fileExtension.toLowerCasePreservingASCIIRules()))
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    }
                    makeButtonsVisible(View.INVISIBLE)
                }
            }

        binding.listViewFiles.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val file = fileList[position]

            if (!selectionMode){
                selectionMode = true
                makeButtonsVisible(View.VISIBLE)
                selectionList.add(file)
                file.isSelected = !file.isSelected
                binding.textViewSelectCount.text = selectionList.count().toString()
                adapter.notifyDataSetChanged()
            }

            true
        }

        binding.imageViewUnselectButton.setOnClickListener {
            selectionMode = false
            makeButtonsVisible(View.INVISIBLE)
            for(file in fileList){
                file.isSelected = false
            }

            adapter.notifyDataSetChanged()
            selectionList.clear()
        }
        binding.imageButtonSelectionOptions.setOnClickListener {
            val popupWindow = ListPopupWindow(this, null, com.google.android.material.R.attr.listPopupWindowStyle)
            popupWindow.anchorView = binding.imageButtonSelectionOptions
            showMenu(binding.imageButtonSelectionOptions, R.menu.selection_option_menu)
        }


        onBackPressedDispatcher.addCallback(this) {
            if (currentDir == rootDir) {
                finish()
            } else {
                updateFileAdapter(currentDir.parentFile ?: rootDir, null)
                currentDir = currentDir.parentFile ?: rootDir
            }

        }

        taskViewModel.selectedFilePath.observe(this) {
            Toast.makeText(this, "Moving file/s to ${it.replace("/storage/emulated/0", "~")}", Toast.LENGTH_LONG).show()

            if(selectionList.isNotEmpty()){
                for(file in selectionList){
                    if (file.isFileADirectory){
                        val dirFile = File(it + "/" + file.fileName).mkdir()
                        if (dirFile){
                            Files.move(Path(file.filePath), Path(it + "/" + file.fileName), StandardCopyOption.REPLACE_EXISTING)
                            file.file?.deleteRecursively()
                        }
                    } else {
                        Files.move(Path(file.filePath), Path(it + "/" + file.fileName), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
            updateFileAdapter(currentDir, null)
            selectionMode = false
            makeButtonsVisible(View.INVISIBLE)
            for(file in fileList){
                file.isSelected = false
            }

            adapter.notifyDataSetChanged()
            selectionList.clear()
        }
    }

    private fun makeButtonsVisible(visibility: Int){
        binding.imageViewUnselectButton.visibility = visibility
        binding.imageButtonSelectionOptions.visibility = visibility
        binding.textViewSelectCount.visibility = visibility

    }

    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val metrics = DisplayMetrics()
        val popup = PopupMenu(this, v)

        popup.menuInflater.inflate(menuRes, popup.menu)

        if (currentDir != rootDir) {
            popup.menu[1].isVisible = true
            popup.menu[2].isVisible = true
        }

        popup.setOnMenuItemClickListener {
            if (it.itemId == R.id.option_archive) {
                val archiveFilesIntent = Intent(this, ArchiveFiles::class.java)
                val parcelList: ArrayList<FileDataClass> = ArrayList(selectionList)

                archiveFilesIntent.putExtra("path", currentDir.path)
                archiveFilesIntent.putExtra("selectedFilesList", parcelList)
                startActivity(archiveFilesIntent)
            }

            if (it.itemId == R.id.option_moveTo) {
                val bottomSheet = BottomSheetDirectoryPicker()
                bottomSheet.isCancelable = false
                bottomSheet.show(supportFragmentManager, "Select folder")

                updateFileAdapter(currentDir, null)
            }

            if (it.itemId == R.id.option_deleteSelected) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Are you sure you want to delete these file?\n${selectionList.map { it.fileName }}")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { _, _ ->
                        var wasDeleted: Boolean? = true
                        for (file in selectionList) {
                            wasDeleted = file.file?.deleteRecursively()
                            if (wasDeleted == false){
                                break
                            }
                        }

                        if (wasDeleted == true) {
                            Toast.makeText(
                                this,
                                "Files were successfully deleted",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {
                            Toast.makeText(
                                this,
                                "Files could not be removed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        updateFileAdapter(currentDir, null)
                        selectionMode = false
                        makeButtonsVisible(View.INVISIBLE)
                        for(files in fileList){
                            files.isSelected = false
                        }

                        adapter.notifyDataSetChanged()
                        selectionList.clear()
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
                        TypedValue.COMPLEX_UNIT_DIP, 32f, metrics
                    ).toInt()
                if (item.icon != null) {
                    item.icon =
                        InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                }
            }
        }
        popup.show()
    }

    private fun updateFileAdapter(dir: File, query: String?){
        fileList = listFiles(dir, query, false)
        adapter = FilesAdapter(this, fileList)
        binding.listViewFiles.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        updateFileAdapter(currentDir, null)
    }
}