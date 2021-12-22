package com.example.kotlinalbumapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.kotlinalbumapp.databinding.ActivitySelectGalleryBinding
import com.example.kotlinalbumapp.databinding.ActivityTakeFotoBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.util.*

class TakePhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakeFotoBinding
    private val REQUEST_CODE = 13
    private val PERMISSION_CODE = 100
    private lateinit var filePhoto: File
    private var  FILE_NAME = "photo.jpg"
    private var vFilename:String=""
    private var takenPhoto:Bitmap?=null
    private lateinit var dataBase: SQLiteDatabase





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityTakeFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataBase=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        binding.viewImage.setOnClickListener(View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                openCamera()
            } else {
                Toast.makeText(this@TakePhotoActivity,"Sorry you're version android is not support, Min Android 12.0 ",Toast.LENGTH_LONG).show()
            }
        })

        val intent=intent
        val info=intent.getStringExtra("info")
        if(info.equals("new")){
            binding.comment.setText("")
            binding.year.setText("")
            binding.buttonSave.visibility=View.VISIBLE
            binding.viewImage.setImageResource(R.drawable.select_image)

        }else{
            binding.buttonSave.visibility=View.INVISIBLE
            val selectedId=intent.getIntExtra("id",1)
            val cursor=dataBase.rawQuery("SELECT * FROM arts WHERE id=?", arrayOf(selectedId.toString()))
            while(cursor.moveToNext()){
                binding.comment.setText(cursor.getString(cursor.getColumnIndex("comment")))
                binding.year.setText(cursor.getString(cursor.getColumnIndex("year")))

                val byteArray=cursor.getBlob(cursor.getColumnIndex("image"))
                val bitmap= BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.viewImage.setImageBitmap(bitmap)

            }
            cursor.close()


        }
    }

    private fun openCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        filePhoto = getPhotoFile(FILE_NAME)


        val providerFile =FileProvider.getUriForFile(this,"com.example.kotlinalbumapp.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (takePhotoIntent.resolveActivity(this.packageManager) != null){
            startActivityForResult(takePhotoIntent, REQUEST_CODE)
        }else {
            Toast.makeText(this,"Camera could not open", Toast.LENGTH_SHORT).show()
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
                    openCamera()
                }else{
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            takenPhoto = BitmapFactory.decodeFile(filePhoto.absolutePath)
            binding.viewImage.setImageBitmap(takenPhoto)
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            binding.viewImage.setImageURI(data?.data)
        }

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

    private fun getPhotoFile(fileName:String):File{
        val directoryStorage=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if(!directoryStorage!!.exists()) directoryStorage.mkdirs()
        return File.createTempFile(fileName,".jpg",directoryStorage)
    }

    fun buttonSave(view:View){
        val comment=binding.comment.text.toString()
        val year=binding.year.text.toString()
        if(takenPhoto!=null){
            val smallBitmap = smallerBitmap(takenPhoto!!,300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try{

                dataBase.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, comment VARCHAR, year VARCHAR, image BLOB)")
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
        val intent=Intent(this@TakePhotoActivity,MainActivity::class.java)
        startActivity(intent)


    }
}