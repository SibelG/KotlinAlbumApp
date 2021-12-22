package com.example.kotlinalbumapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlinalbumapp.adapter.ArtAdapter
import com.example.kotlinalbumapp.databinding.ActivityMainBinding
import com.example.kotlinalbumapp.model.Art

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artList:ArrayList<Art>
    private lateinit var artAdapter:ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);

        artList=ArrayList<Art>()
        artAdapter= ArtAdapter(artList)
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        binding.recyclerView.adapter=artAdapter




        try {
            val data=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
            val cursor=data.rawQuery("SELECT * FROM arts",null)
            while(cursor.moveToNext()){
                val comment=cursor.getString(cursor.getColumnIndex("comment"));
                val id=cursor.getInt(cursor.getColumnIndex("id"))
                val art=Art(comment,id)
                artList.add(art)
            }

            artAdapter.notifyDataSetChanged()
            cursor.close()


        }catch (e:Exception){
            e.printStackTrace()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.selectoption, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.selectFromGallery) {
            val intent = Intent(this@MainActivity, SelectGalleryActivity::class.java);
            intent.putExtra("info","new")
            startActivity(intent);
        } else if (item.itemId == R.id.takeAFoto) {
            val intent = Intent(this@MainActivity, TakePhotoActivity::class.java);
            intent.putExtra("info","new")
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item)
    }


}