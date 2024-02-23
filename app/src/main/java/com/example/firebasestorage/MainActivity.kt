package com.example.firebasestorage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var btnGallery: Button
    private lateinit var btnUpload: Button
    private lateinit var btnRetrieve: Button
    private lateinit var btnSelectFile: Button
    private val storageReference = FirebaseStorage.getInstance().reference
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        btnGallery = findViewById(R.id.gallery)
        btnUpload = findViewById(R.id.upload)
        btnRetrieve = findViewById(R.id.retrieve)
        btnSelectFile = findViewById(R.id.file)

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

        btnUpload.setOnClickListener {
            uploadFile()
        }
    }

    private fun uploadFile() {
        if (!::uri.isInitialized) {
            Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
            return
        }

        val fileRef = storageReference.child("files/${System.currentTimeMillis()}")

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
}
