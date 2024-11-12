package com.gmail.etest.foxy.filesapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gmail.etest.foxy.filesapp.databinding.ActivityFileInfoBinding

class FileInfo : AppCompatActivity() {

    private lateinit var binding: ActivityFileInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileImageType = intent.getIntExtra("extensionImage", R.drawable.ic_other)
        val fileName = intent.getStringExtra("fileName")
        val filePath = intent.getStringExtra("filePath")
        val fileExtension = intent.getStringExtra("fileExtension")
        val fileSize = intent.getStringExtra("fileSize")
        val fileLastModified = intent.getStringExtra("fileLastModified")

        binding.imageViewFileInfoType.setImageResource(fileImageType)
        binding.textViewFileInfoName.text = fileName
        binding.textViewPath.text = filePath
        binding.textViewExtension.text = fileExtension
        binding.textViewSize.text = fileSize
        binding.textViewDateModified.text = fileLastModified

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}