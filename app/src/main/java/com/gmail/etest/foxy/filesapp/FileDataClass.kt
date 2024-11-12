package com.gmail.etest.foxy.filesapp

import android.os.Parcel
import android.os.Parcelable
import java.io.File

data class FileDataClass(
    val file: File?,
    val fileName: String,
    val filePath: String,
    val fileExtension: String,
    val isFileADirectory: Boolean,
    var isSelected: Boolean,
): Parcelable {
    constructor(parcel: Parcel) : this(
        null,
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fileName)
        parcel.writeString(filePath)
        parcel.writeString(fileExtension)
        parcel.writeByte(if (isFileADirectory) 1 else 0)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FileDataClass> {
        override fun createFromParcel(parcel: Parcel): FileDataClass {
            return FileDataClass(parcel)
        }

        override fun newArray(size: Int): Array<FileDataClass?> {
            return arrayOfNulls(size)
        }
    }
}
