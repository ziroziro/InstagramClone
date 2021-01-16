package com.example.kurikulumappinstagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.kurikulumappinstagram.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*

class EditProfile : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var cekInfoProfile = ""
    private var myUrl = ""
    private var imageUri : Uri? = null
    private var storageProfilePicture : StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicture = FirebaseStorage.getInstance().reference.child("Profile Picture")

        logout_btn_profile.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val pergi = Intent(this@EditProfile,LoginActivity::class.java)
            pergi.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(pergi)
            finish()
        }
        txt_ganti_image_profile.setOnClickListener {
            cekInfoProfile = "Clicked"

            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@EditProfile)
        }

        btn_save_info_profile.setOnClickListener {
            if (cekInfoProfile == "Clicked"){
                uploadImageProfileAndUpdateInfoProfile()
            }else{
                updateUserInfoOnly()
            }
        }
        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK
            && data != null){
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            set_profileimage.setImageURI(imageUri)
        }
    }

    private fun uploadImageProfileAndUpdateInfoProfile() {
        when {
            imageUri == null -> Toast.makeText(this, "Select Image....", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(set_fullname_profile_edittext.text.toString()) -> {
                Toast.makeText(this, "Jangan Kosong", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(set_username_profile_edittext.text.toString()) -> {
                Toast.makeText(this, "Jangan Kosong", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(set_bio_profile_edittext.text.toString()) -> {
                Toast.makeText(this, "Jangan Kosong", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this@EditProfile)
                progressDialog.setTitle("Update Profile")
                progressDialog.setMessage("Please Wait...")
                progressDialog.show()

                val fileRef = storageProfilePicture!!.child(firebaseUser.uid + ".jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful){
                        task.exception.let {
                            throw it!!
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri>{task ->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = set_fullname_profile_edittext.text.toString()
                        userMap["username"] = set_username_profile_edittext.text.toString()
                        userMap["bio"]      = set_bio_profile_edittext.text.toString()
                        userMap["image"]    = myUrl

                        userRef.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(this,"Info Sudah Di Update",Toast.LENGTH_SHORT).show()
                        val pergi = Intent(this,MainActivity::class.java)
                        startActivity(pergi)
                        finish()
                        progressDialog.dismiss()
                    }else{
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }
    private fun updateUserInfoOnly() {

        when{
            TextUtils.isEmpty(set_fullname_profile_edittext.text.toString()) -> {
                Toast.makeText(this,"Jangan Kosong",Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(set_username_profile_edittext.text.toString()) -> {
                Toast.makeText(this,"Jangan Kosong",Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(set_bio_profile_edittext.text.toString()) -> {
                Toast.makeText(this,"Jangan Kosong",Toast.LENGTH_SHORT).show()
            }
            else -> {
                val userRef = FirebaseDatabase.getInstance().reference
                 .child("Users")

                val userMap = HashMap<String,Any>()
                userMap["fullname"] = set_fullname_profile_edittext.text.toString()
                userMap["username"] = set_username_profile_edittext.text.toString()
                userMap["bio"]      = set_bio_profile_edittext.text.toString()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this,"Info Sudah Di Update",Toast.LENGTH_SHORT).show()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    set_fullname_profile_edittext.setText(user?.getFullname())
                    set_username_profile_edittext.setText(user?.getUsername())
                    set_bio_profile_edittext.setText(user?.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}