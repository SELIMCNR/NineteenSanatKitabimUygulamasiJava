package com.selimcinar.sanatkitabmuygulamasjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.selimcinar.sanatkitabmuygulamasjava.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {
    //Art clasından arraylist oluştu
    ArrayList<Art> artArrayList;
    //ArtAdapter'ı arrayliste bağladık
    public  ArtAdapter(ArrayList<Art> artArrayList){

        this.artArrayList = artArrayList;
    }


    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Açılınca ne görünecek
        //recycler_rowa bağladık
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        //Bağlanan görüntüyü gösterdik
        return new ArtHolder(recyclerRowBinding);
    }



    @Override
    public void onBindViewHolder(ArtAdapter.ArtHolder holder,  int position) {
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);
        // onclick işlemleri yapılırı tıklanınca şu olsun
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Tıklanınca intentle resim al
                Intent intent = new Intent(holder.itemView.getContext(),Artactivity.class);
                //tıklanınca intentle veri yolla
                intent.putExtra("artId",artArrayList.get(position).id);
                intent.putExtra("info","old");
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {

        //arrayListe eleman boyutu
        return  artArrayList.size();
    }

    public  class  ArtHolder extends  RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        //Görünümden constructor oluştu
        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
