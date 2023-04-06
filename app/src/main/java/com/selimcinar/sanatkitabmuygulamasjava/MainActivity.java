package com.selimcinar.sanatkitabmuygulamasjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.selimcinar.sanatkitabmuygulamasjava.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //View binding ekleme
    private ActivityMainBinding binding;
    //Art clasından bir Array list oluştur.
    ArrayList <Art> artArrayList;
    ArtAdapter artAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);



        artArrayList = new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager((this)));
        artAdapter = new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);

        getData();

    }


    public  void  getData(){
        //Verileri çekme işlemi
        try {
            //Databaseyi aç
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            //Veritabanından sorgu başlat
            Cursor cursor = sqLiteDatabase.rawQuery("Select * from arts",null);
            //Verileri veritabanından al
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");

            //Veriler üzerinde dolaşımyap
            while (cursor.moveToNext()){
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                //Art clasından obje oluştur ve constructora verileri ekle
                Art art = new Art(name,id);
                //artArrayListe işlemleri ekle
                artArrayList.add(art);
            }
            artAdapter.notifyDataSetChanged(); // verileri gösteri
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Seçenekli menu oluştuğunda ne olsun
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.artbook_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Seçili menü elemanına tıklanınca ne olacak
        if (item.getItemId() == R.id.add_art){
            Intent intent = new Intent(this,Artactivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}