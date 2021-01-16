package com.example.kurikulumappinstagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.activity_edit_profile.*

class AddPost : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicture: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPicture = FirebaseStorage.getInstance().reference.child("Post Picture")

        btn_add_post.setOnClickListener {
            uploadImagePost()
        }
        CropImage.activity()
            .setAspectRatio(2,1)
            .start(this@AddPost)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null){
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            add_post_image.setImageURI(imageUri)
        }
    }

    private fun uploadImagePost() {
        when{
            imageUri == null -> Toast.makeText(this, "Pilih...", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(edt_desc_post.text.toString()) -> Toast.makeText(this, "HALO", Toast.LENGTH_SHORT).show()

            else -> {
                val progressDialog = ProgressDialog(this@AddPost)
                progressDialog.setTitle("Post")
                progressDialog.setMessage("Tunggu Sebentar...")
                progressDialog.show()

                val fileRef = storagePostPicture!!.child(System.currentTimeMillis().toString() + ".jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{task ->
                    if (!task.isSuccessful){
                        task.exception.let {
                            throw it!!
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> {task ->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = postRef.push().key

                        val postMap = HashMap<String, Any>()
                        postMap["postid"]      = postId!!
                        postMap["description"] = edt_desc_post.text.toString()
                        postMap["publisher"]   = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"]   = myUrl

                        postRef.child(postId).updateChildren(postMap)

                        Toast.makeText(this, "Post Berhasil",  Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }else{
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }
}