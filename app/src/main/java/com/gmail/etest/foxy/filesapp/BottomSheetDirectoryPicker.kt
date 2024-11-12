package com.gmail.etest.foxy.filesapp

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.gmail.etest.foxy.filesapp.databinding.FragmentBottomSheetDirectoryPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDirectoryPicker : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetDirectoryPickerBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        taskViewModel = ViewModelProvider(activity)[TaskViewModel::class.java]
        binding.listViewSaveAsList.adapter = SimpleFilesAdapter(requireContext(), listFiles(Environment.getExternalStorageDirectory(), null, true))

        binding.listViewSaveAsList.setOnItemClickListener { _, _, position, _ ->
            val files = binding.listViewSaveAsList.adapter.getItem(position) as FileDataClass
            binding.listViewSaveAsList.adapter = SimpleFilesAdapter(requireContext(), listFiles(files.file!!, null, true))
        }

        binding.listViewSaveAsList.setOnItemLongClickListener { _, _, position, _ ->
            val file = binding.listViewSaveAsList.adapter.getItem(position) as FileDataClass
            if (file.fileName != " .."){
                taskViewModel.selectedFilePath.value = file.filePath
                dismiss()
            }
            true
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        binding = FragmentBottomSheetDirectoryPickerBinding.inflate(inflater, container, false)
        return binding.root
    }
}