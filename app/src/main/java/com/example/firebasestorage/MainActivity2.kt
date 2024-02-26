package com.example.firebasestorage

import android.Manifest
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MainActivity2 : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var imagesList: ArrayList<String>
    private lateinit var storageReference: FirebaseStorage
    private val REQUEST_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        imagesList = arrayListOf()

        storageReference = FirebaseStorage.getInstance()

        if (!checkPermission()) {
            Log.d("Permission", "Permission not granted. Requesting...")
            requestPermission()
        } else {
            Log.d("Permission", "Permission already granted")
        }

        storageReference.reference.child("images").listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imagesList.add(uri.toString())
                    recyclerView.adapter = ImageAdapter(imagesList) { imageUrl ->
                        downloadImage(imageUrl)
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this@MainActivity2, "Failed to retrieve image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this@MainActivity2, "Failed to list files: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Storage permission granted. You can now download images.", Toast.LENGTH_SHORT).show()
            } else {

            }
        }
    }
private fun downloadImage(url: String) {

    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
    val localFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "${System.currentTimeMillis()}.jpg")

    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("Image Download")
        .setDescription("Downloading..")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "${System.currentTimeMillis()}.jpg"
        )
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)

    storageRef.getFile(localFile).addOnSuccessListener {
            Toast.makeText(this@MainActivity2,"Image downloaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Toast.makeText(this@MainActivity2,"Failed to download image: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}
}