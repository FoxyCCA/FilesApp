package com.gmail.etest.foxy.filesapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gmail.etest.foxy.filesapp.databinding.ActivityUnarchiveFilesBinding
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class UnarchiveFiles : AppCompatActivity() {

    private lateinit var binding: ActivityUnarchiveFilesBinding
    private val zipFileList: MutableList<FileDataClass> = mutableListOf()
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnarchiveFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val filePath  = intent.getStringExtra("zipFilePath")

        println(filePath)

        if (filePath != null){
            val filePathWithoutExtension = filePath.replace(".zip", "")
            val file = File(filePath)
            binding.textViewFileUnzipInfoName.text = file.name

            val zis =  ZipInputStream(FileInputStream(File(filePath)))
            var zipEntry = zis.nextEntry


            while (zipEntry != null){
                zipFileList.add(FileDataClass(null, zipEntry.name, filePathWithoutExtension + "/" + zipEntry.name, zipEntry.name.split(".").last(), zipEntry.isDirectory, false))
                zipEntry = zis.nextEntry
            }
            zis.closeEntry()
            zis.close()

            val zipList = zipFileList.toList()
            binding.listViewZippedFiles.adapter = SimpleFilesAdapter(this, zipList)

            binding.buttonUnzip.setOnClickListener {
                unZip(filePath, filePathWithoutExtension)
                Toast.makeText(this, "File has been unzipped", Toast.LENGTH_LONG).show()
                finish()
            }

            binding.buttonUnzipAs.setOnClickListener {
                val bottomSheet = BottomSheetDirectoryPicker()
                bottomSheet.isCancelable = false
                bottomSheet.show(supportFragmentManager, "Select folder")
            }

            taskViewModel.selectedFilePath.observe(this) {
                unZip(filePath, it + "/" + file.name.replace(".zip", ""))
                Toast.makeText(this, "File has been unzipped", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}