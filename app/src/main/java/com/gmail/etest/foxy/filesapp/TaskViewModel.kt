package com.gmail.etest.foxy.filesapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskViewModel: ViewModel() {
    var selectedFilePath = MutableLiveData<String>()
}