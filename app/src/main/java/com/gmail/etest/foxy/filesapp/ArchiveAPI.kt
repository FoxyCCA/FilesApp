package com.gmail.etest.foxy.filesapp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ArchiveAPI {
    private val key = "M3HkDwm3jCUJ"

    private val client = HttpClient(CIO)

    suspend fun archiveFile(fileList: List<File>, path: String, fileName: String?): Boolean{

        val response = client.post("https://api.archiveapi.com/zip"){
            setBody(MultiPartFormDataContent(
                formData{
                    append("secret", key)
                    append("Password", "")
                    append("CompressionLevel", "9")
                    append("ArchiveName", "newarchive.zip")
                    for (file in fileList){
                        append("File", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                        })
                    }
                },
                boundary = "----FpbVSjGhtld6EDEy"
            ))

            onUpload{ bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
        }

        if(response.status.value in 200..299){

            println("Successful response!")

            withContext(Dispatchers.IO) {
                if (fileName != null) {
                    writeFile(path, fileName, ".zip", response.body())
                } else {
                    writeFile(path, "New Archive", ".zip", response.body())
                }
            }
            return true
        } else {
            println("Something went horribly wrong, stupid")
            return false
        }
    }
}