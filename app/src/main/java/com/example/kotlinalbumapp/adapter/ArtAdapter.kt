package com.example.kotlinalbumapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinalbumapp.SelectGalleryActivity
import com.example.kotlinalbumapp.TakePhotoActivity
import com.example.kotlinalbumapp.databinding.RecyclerRowBinding
import com.example.kotlinalbumapp.model.Art

class ArtAdapter(val ArtList:ArrayList<Art>): RecyclerView.Adapter<ArtAdapter.ArtHolder>() {


    class ArtHolder(var binding:RecyclerRowBinding): RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }



    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.commentView.text=ArtList.get(position).comment
        holder.itemView.setOnClickListener{
            val intent= Intent(holder.itemView.context, SelectGalleryActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",ArtList[position].id)
            holder.itemView.context.startActivity(intent)
            val intentTake= Intent(holder.itemView.context, TakePhotoActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",ArtList[position].id)
            holder.itemView.context.startActivity(intentTake)

        }
    }

    override fun getItemCount(): Int {
        return ArtList.size
    }
}