package com.example.kotlinalbumapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlinalbumapp.databinding.ActivitySelectGalleryBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception


class SelectGalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectGalleryBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? =null
    private lateinit var dataBase:SQLiteDatabase
    private val IMAGE_CHOOSE = 1000;
    private val PERMISSION_CODE = 1001;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivitySelectGalleryBinding.inflate(layoutInflater);
        setContentView(binding.root)
        dataBase=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)



        val intent=intent
        val info=intent.getStringExtra("info")
        if(info.equals("new")){
            binding.commentText.setText("")
            binding.yearText.setText("")
            binding.save.visibility=View.VISIBLE
            binding.imageSelect.setImageResource(R.drawable.select_image)

        }else{
            binding.save.visibility=View.INVISIBLE
            val selectedId=intent.getIntExtra("id",1)
            val cursor=dataBase.rawQuery("SELECT * FROM arts WHERE id=?", arrayOf(selectedId.toString()))
            while(cursor.moveToNext()){
                binding.commentText.setText(cursor.getString(cursor.getColumnIndex("name")))
                binding.yearText.setText(cursor.getString(cursor.getColumnIndex("year")))

                val byteArray=cursor.getBlob(cursor.getColumnIndex("image"))
                val bitmap= BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageSelect.setImageBitmap(bitmap)

            }
            cursor.close()


        }



    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    chooseImageGallery()
                }else{
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)
    }


    private fun smallerBitmap(image:Bitmap,MaxSize:Int):Bitmap{
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            width = MaxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            height = MaxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    fun imageSelectButton(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE)
            } else{
                chooseImageGallery();

            }
        }else{
            chooseImageGallery();

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        if(requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK){
            binding.imageSelect.setImageURI(data?.data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    fun saveButton(view: View) {

        val comment=binding.commentText.text.toString()
        val year=binding.yearText.text.toString()
        if(selectedBitmap!=null){
            val smallBitmap = smallerBitmap(selectedBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try{

                dataBase.execSQL("CREATE TABLE IF NOT EXISTS Arts(id INTEGER PRIMARY KEY AUTOINCREMENT, comment VARCHAR, year VARCHAR, image BLOB)")
                val sqlQuery="INSERT INTO arts (comment, year, image) VALUES (?, ?, ?)"
                val statement=dataBase.compileStatement(sqlQuery)
                statement.bindString(1, comment)
                statement.bindString(2, year)
                statement.bindBlob(3, byteArray)

                statement.execute()




            }catch (e:Exception)
            {
                e.printStackTrace()
            }

        }
        val intent=Intent(this@SelectGalleryActivity,MainActivity::class.java)
        startActivity(intent)


    }

}