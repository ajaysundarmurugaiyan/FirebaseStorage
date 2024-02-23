package com.example.firebasestorage

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class FileActivity : AppCompatActivity() {

    private lateinit var storageReference: StorageReference
    private lateinit var selectedFileUri: Uri
    private val PICK_FILE_REQUEST_CODE = 101
    private val REQUEST_PERMISSION_CODE = 102

    private lateinit var btnSelectFile: Button
    private lateinit var btnUploadFile: Button
    private lateinit var btnDownloadFile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storageReference = FirebaseStorage.getInstance().reference

        btnSelectFile = findViewById(R.id.file)
        btnUploadFile = findViewById(R.id.upload_file)
        btnDownloadFile = findViewById(R.id.download_file)

        btnSelectFile.setOnClickListener {
            checkPermissionAndSelectFile()
        }

        btnUploadFile.setOnClickListener {
            selectedFileUri.let { uri ->
                uploadFile(uri)
            }
        }

        btnDownloadFile.setOnClickListener {
            downloadFile()
        }
    }

    private fun checkPermissionAndSelectFile() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE
            )
        } else {
            selectFile()
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // Allow all file types
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    private fun uploadFile(uri: Uri) {
        val fileRef = storageReference.child("files/${System.currentTimeMillis()}")

        fileRef.putFile(uri)
            .addOnSuccessListener {
                Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadFile() {
        // Replace "fileUrl" with the actual URL of the file in Firebase Storage
        val fileUrl = "your_file_url_here"
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle("File Download")
            request.setDescription("Downloading file...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloaded_file")
            downloadManager.enqueue(request)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to download file: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        storageRef.getFile(selectedFileUri).addOnSuccessListener {
            Toast.makeText(this@FileActivity,"File downloaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {exception ->
            Toast.makeText(this@FileActivity,"Failed to download file: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun downloadImage(url: String) {
//
//        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
//        val localFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "${System.currentTimeMillis()}.jpg")
//
//        val request = DownloadManager.Request(Uri.parse(url))
//            .setTitle("Image Download")
//            .setDescription("Downloading..")
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            .setDestinationInExternalPublicDir(
//                Environment.DIRECTORY_DOWNLOADS,
//                "${System.currentTimeMillis()}.jpg"
//            )
//            .setAllowedOverMetered(true)
//            .setAllowedOverRoaming(true)
//
//        storageRef.getFile(localFile).addOnSuccessListener {
//            Toast.makeText(this@MainActivity2,"Image downloaded successfully", Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener { exception ->
//            Toast.makeText(this@MainActivity2,"Failed to download image: ${exception.message}", Toast.LENGTH_SHORT).show()
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.data!!
            Toast.makeText(this, "File selected: ${selectedFileUri.path}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectFile()
        } else {
            Toast.makeText(this, "Permission denied. Cannot select file.", Toast.LENGTH_SHORT).show()
        }
    }
}



