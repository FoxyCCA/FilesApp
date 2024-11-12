package com.gmail.etest.foxy.filesapp

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.gmail.etest.foxy.filesapp.databinding.ActivityArchiveFilesBinding
import kotlinx.coroutines.launch
import java.io.File

class ArchiveFiles : AppCompatActivity() {
    private lateinit var binding: ActivityArchiveFilesBinding
    private lateinit var taskViewModel: TaskViewModel

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val selectedList: ArrayList<FileDataClass>? = intent.getParcelableArrayListExtra("selectedFilesList")
        val path = intent.getStringExtra("path")


        if (selectedList != null){
            val count = selectedList.count().toString() + " Selected Files"
            binding.textViewNumberOfSelectedFiles.text = count
            binding.listViewSelectedFiles.adapter = SimpleFilesAdapter(this, selectedList)


            val fileArray: List<File> = selectedList.map { File(it.filePath) }
            binding.buttonSave.setOnClickListener {
                lifecycleScope.launch {
                    val api = ArchiveAPI()
                    val fileName = binding.editTextArchiveName.text.toString()

                    binding.buttonSave.isEnabled = false
                    binding.buttonSaveAs.isEnabled = false

                    if (path != null) {
                        val response = api.archiveFile(fileArray, path, fileName)
                        if (response){
                            Toast.makeText(this@ArchiveFiles, "File/s successfully archived", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            binding.buttonSave.isEnabled = true
                            binding.buttonSaveAs.isEnabled = true
                        }
                    }
                }
            }

            binding.buttonSaveAs.setOnClickListener {
                val bottomSheet = BottomSheetDirectoryPicker()
                bottomSheet.isCancelable = false
                bottomSheet.show(supportFragmentManager, "Select folder")
            }

            taskViewModel.selectedFilePath.observe(this) {
                lifecycleScope.launch {
                    val api = ArchiveAPI()
                    val fileName = binding.editTextArchiveName.text.toString()

                    binding.buttonSave.isEnabled = false
                    binding.buttonSaveAs.isEnabled = false

                    if (path != null) {
                        val response = api.archiveFile(fileArray, it, fileName)
                        if (response){
                            Toast.makeText(this@ArchiveFiles, "File/s successfully archived", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            binding.buttonSave.isEnabled = true
                            binding.buttonSaveAs.isEnabled = true
                        }
                    }
                }
            }
        }
    }
}