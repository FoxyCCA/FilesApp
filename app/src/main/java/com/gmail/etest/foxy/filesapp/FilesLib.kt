package com.gmail.etest.foxy.filesapp

import android.os.Environment
import io.ktor.util.toLowerCasePreservingASCIIRules
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.Locale
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.io.path.Path

const val KILOBYTES: Int = 1024
const val MEGABYTES: Int = KILOBYTES * 1024
const val GIGABYTES: Int = MEGABYTES * 1024

fun getExtensionImage(extension: String): Int {
    val ex = extension.toLowerCasePreservingASCIIRules()
    return when (ex) {
        "apk" -> R.drawable.ic_apk
        "iso" -> R.drawable.ic_iso
        "html" -> R.drawable.ic_web
        "txt" -> R.drawable.ic_txt
        "exe" -> R.drawable.ic_exe
        "pdf" -> R.drawable.ic_pdf
        "docx", "doc" -> R.drawable.ic_docx
        "xlxs", "xls" -> R.drawable.ic_xlxs
        "pptx", "ppt" -> R.drawable.ic_pptx
        "zip", "rar", "7z" -> R.drawable.ic_zip
        "png", "jpg", "gif", "jpeg" -> R.drawable.ic_image
        "mp3", "flac", "wav", "midi", "aif" -> R.drawable.ic_music
        "mov", "mp4", "avi", "flv", "prores", "webm" -> R.drawable.ic_video

        else -> R.drawable.ic_other
    }
}

fun getMimeType(extension: String): String {
    val ex = extension.toLowerCasePreservingASCIIRules()
    return when (ex) {
        "html" -> "text/html"
        "txt" -> "text/plain"
        "pdf" -> "application/pdf"
        "docx", "doc" -> "application/msword"
        "xlxs", "xls" -> "application/vnd.ms-excel"
        "pptx", "ppt" -> "application/vnd.ms-powerpoint"
        "png", "jpg", "gif", "jpeg" -> "image/*"
        "mp3", "flac", "wav", "midi", "aif" -> "audio/*"
        "mov", "mp4", "avi", "flv", "prores", "webm" -> "video/*"

        else -> "*/*"
    }
}

fun getFileSizeString(bytes: Long): String{
    return when {
        bytes > GIGABYTES -> String.format(Locale.getDefault(),"%.2f GB", bytes.toFloat() / GIGABYTES)
        bytes > MEGABYTES -> String.format(Locale.getDefault(), "%.2f MB", bytes.toFloat() / MEGABYTES)
        bytes > KILOBYTES -> String.format(Locale.getDefault(), "%.2f KB", bytes.toFloat() / KILOBYTES)
        else -> String.format(Locale.getDefault(), "%d B", bytes.toInt())
    }
}

fun listFiles(directory: File, query: String?, onlyFolders: Boolean): List<FileDataClass>{
    val allFilesInDir = directory.listFiles()
    var fileList: List<FileDataClass>
    val parentDir = directory.parentFile


    fileList = allFilesInDir!!.map {
        FileDataClass(it, it.name, it.absolutePath, it.extension, it.isDirectory, false)
    }
    if(parentDir != null && directory != Environment.getExternalStorageDirectory()){
        fileList = fileList.plus(FileDataClass(parentDir, " ..", parentDir.path, "", true, false))
    }

    if (query!= null){
        fileList = fileList.filter { it.fileName.toLowerCasePreservingASCIIRules().contains(query.toLowerCasePreservingASCIIRules()) }
    }

    if(onlyFolders){
        fileList = fileList.filter { it.isFileADirectory }
    }

    val sortedFileList = fileList.sortedBy { it.fileName }
    return sortedFileList
}

fun writeFile(path: String, fileName: String, extension: String, bytes: ByteArray){
    var newPath = "$path/$fileName$extension"
    if (File(newPath).exists()) {
        var counter = 1
        while (File(newPath).exists()) {
            newPath = "$path/$fileName $counter$extension"
            counter++
        }
    }
    File(newPath).writeBytes(bytes)
}

// unzip code from https://oguzhanaslann.medium.com/zipping-and-unzipping-files-in-android-9041326a9479
// the code has been modified a bit to work with creating folders
fun unZip(zipFilePath: String, unzipDirectoryPath: String ) {
    val unzipDir = File(unzipDirectoryPath)
    if (!unzipDir.exists()) {
        unzipDir.mkdir()
    }

    val zipFile = File(zipFilePath)
    ZipFile(zipFile).unzip(unzipDir)
}

fun ZipFile.unzip(unzipDir: File ) {
    val enum = entries()
    while (enum.hasMoreElements()) {
        val entry = enum.nextElement()
        val entryName = entry.name
        val fis = FileInputStream(this.name)
        val zis = ZipInputStream(fis)

        while (true) {
            val nextEntry = zis.nextEntry ?: break
            if (nextEntry.isDirectory){
                val targetDir = Path(unzipDir.path).resolve(nextEntry.name)
                val normalizedPath = targetDir.normalize()
                if (!normalizedPath.startsWith(unzipDir.path)){
                    throw IOException("Bad zip entry: " + nextEntry.name)
                }
                Files.createDirectories(normalizedPath)
            } else {
                if (nextEntry.name == entryName) {

                    val fileOut = FileOutputStream(File(unzipDir, nextEntry.name))
                    var byte = zis.read()
                    while (byte != -1) {
                        fileOut.write(byte)
                        byte = zis.read()
                    }
                    zis.closeEntry()
                    fileOut.close()
                }
            }
        }
        zis.close()
    }
}
