package com.example.firebasestorage

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var btnGallery: Button
    private lateinit var btnUpload: Button
    private lateinit var btnRetrieve: Button
    private var storageReference = FirebaseStorage.getInstance().reference
    private lateinit var uri: Uri
    private lateinit var selectedFileUri: Uri
    private val PICK_FILE_REQUEST_CODE = 101
    private val REQUEST_PERMISSION_CODE = 102
    private val CHANNEL_ID = "FileUpload"

    private lateinit var btnSelectFile: Button
    private lateinit var btnUploadFile: Button
    private lateinit var btnDownloadFile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        btnGallery = findViewById(R.id.gallery)
        btnUpload = findViewById(R.id.upload)
        btnRetrieve = findViewById(R.id.retrieve)
        btnSelectFile = findViewById(R.id.file)
        storageReference = FirebaseStorage.getInstance().reference

        btnUploadFile = findViewById(R.id.upload_file)
        btnDownloadFile = findViewById(R.id.download_file)

        val galleryImage = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { result ->
            imageView.setImageURI(result)
            if (result != null) {
                uri = result
            }
        }

        btnGallery.setOnClickListener {
            galleryImage.launch("image/*")
        }

        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST_CODE)
        }

        btnRetrieve.setOnClickListener {
            val intent = Intent(this,MainActivity2::class.java)
            startActivity(intent)
        }

        createNotificationChannel()

        btnUpload.setOnClickListener {
            uploadImage()
        }

        btnSelectFile.setOnClickListener {
            checkPermissionAndSelectFile()
        }

        btnUploadFile.setOnClickListener {
            if (!::selectedFileUri.isInitialized) {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
            } else {
                uploadFile()
            }
        }

        btnDownloadFile.setOnClickListener {
                val intent = Intent(this,FileActivity::class.java)
                startActivity(intent)
        }
    }
    private fun uploadImage() {
        if (!::uri.isInitialized) {
            Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
            return
        }

        val fileRef = storageReference.child("images/${System.currentTimeMillis()}")

        fileRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        Toast.makeText(this, "File uploaded successfully. URL: $downloadUri", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to retrieve download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    companion object {
        private const val PICK_PDF_REQUEST_CODE = 101
    }



    //file upload
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "File Upload"
            val descriptionText = "File Uploaded Successfully"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(message: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("File Upload")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
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
        intent.type = "application/pdf"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
        if (intent.type=="applcation/pdf") {

        }else{
            Toast.makeText(this,"select only PDF",Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun uploadFile() {
        val fileRef = storageReference.child("files/${System.currentTimeMillis()}")

        val uploadTask = fileRef.putFile(selectedFileUri)

        uploadTask.addOnSuccessListener {
            showNotification("File uploaded successfully")
            Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to upload file: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("UploadError", "Failed to upload file", e)
        }
    }

    @Deprecated("Deprecated in Java")
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
            Toast.makeText(this, "Permission denied. Cannot select file.", Toast.LENGTH_SHORT).show()
        } else {
            selectFile()
        }
    }
}
