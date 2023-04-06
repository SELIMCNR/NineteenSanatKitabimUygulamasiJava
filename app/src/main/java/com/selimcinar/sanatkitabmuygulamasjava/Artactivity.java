package com.selimcinar.sanatkitabmuygulamasjava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.selimcinar.sanatkitabmuygulamasjava.databinding.ActivityArtactivityBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Artactivity extends AppCompatActivity {
    //Global kapsamlar
    Bitmap  selectedImage ; // Bitmap den selectedImage değişkeni oluştu
    ActivityResultLauncher <Intent> activityResultLauncher; // Galeriye gitmek için galeriye gidince ne olsun
    ActivityResultLauncher <String> permissionLauncher; // İzin almak için  izin alınca ne olsun
    private ActivityArtactivityBinding binding;

     SQLiteDatabase database; // SqlliteDatabase den database değişkeni  oluşturuldu


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artactivity);
        binding = ActivityArtactivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();

        //Database oluştur veya aç
        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        // intent üzerinden bilgileri getir
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        // new art bilgisi intenden gelen buysa şunları yap
        if (info.equals("new")){
            //Textlerin içini sil , resime varsayılan resmi koy
            binding.artnameText.setText("");
            binding.artistNameText.setText("");
            binding.yearText.setText("");
            //Buttonu göster
            binding.button.setVisibility(View.VISIBLE);

            //Seçili resmi gösterme
            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.seciliresim);
            binding.imageView.setImageBitmap(selectImage);
        }
        else {
            // artId yi intentle al
            int artId = intent.getIntExtra("artId",0);
            //Buttonu gizle
            binding.button.setVisibility(View.INVISIBLE);
            try {
                //Sorguyu çalıştır ve id değerini artId dizisinden al stringe dönüştür.
                Cursor cursor = database.rawQuery("SElECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artId)});
                //artname adlı kolumdan değerleri getir
                int artNameIx = cursor.getColumnIndex("artname");
                //painter adlı indexten değerleri getir.
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                // Verilerde dolaş
                while (cursor.moveToNext()){
                    // verilere değişkenlerdeki değerleri ekle örnek artNameIx
                    binding.artnameText.setText(cursor.getString(artNameIx));
                    binding.artistNameText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));
                    //bytes dizisinde  resim dosyasının heksadecimal kodlarını tut
                    byte [] bytes = cursor.getBlob(imageIx);
                    //Heksadecimal kodları bitmape dönüştür resme
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    //imageView alanında göster
                    binding.imageView.setImageBitmap(bitmap
                    );

                }
                //cursoru kapat
                cursor.close();
            }
            catch (Exception e){
                //loga yaz hataları
                e.printStackTrace();
            }
        }
    }

    public void save(View view){
        // Dışardan girilen değerler değişkenlerde tutuldu.
        String name = binding.artnameText.getText().toString();
        String artistName = binding.artistNameText.getText().toString();
        String year = binding.yearText.getText().toString();
        Bitmap smallImage= makeSmallerImage(selectedImage,300);

        //Image resimleri veriye 1,0 ' a byta döndürme
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte [] byteArray = outputStream.toByteArray();

        try {
            //Veritabanına SqlLite'a kaydetme işlemleri

            //Veritabanı oluştur
            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            //Veritabanına tablo oluştur
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY,artname VARCHAR,paintername VARCHAR,year VARCHAR,image BLOB)");
            //Tabloya eklemeler yap , ? işaretleri yerine sqlitestatement 'deki bağlanan yerler örnek 1 name gelecek.
            String sqlString = "INSERT INTO arts (artname,paintername,year,image) VALUES (?,?,?,?)";
            //SQLiteStatement Bağlma binding işlemlerini kolaylaştırır.
            SQLiteStatement  sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            //İşlemleri execute et çalıştır.
            sqLiteStatement.execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(Artactivity.this, MainActivity.class);
        //addFlags ile öncesini sil yerine getir gibi işlemler yapılır.
        //Flag_Actıvıty_Clear_Top  tüm aktiviteleri kapat yeni açacağımı aç
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public  Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        //Küçük görsel oluşturmak

        int width = image.getWidth();
        int height = image.getHeight();

        //Resimlerin oranı ayarlama işlemi bitmap ratio
        float bitmapRatio = (float) width / (float) height;

        //Oran birden büyükse işlemler
        if (bitmapRatio > 1) {
            //landscape image  yatay görsel
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }
        else {
            // portrait image  dikey resim
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }



        return  image.createScaledBitmap(image,width,height,true);

    }

    public  void selectImage(View view){
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            // Android 33+ -> READ_MEDİA_IMAGES
            //Api seviyesine bakmadan izin sorgula(activitesi this,Manifest içinde izinlerde Dosya okuma iznini) != eşitdeğil ise Paketyöneticisinde.izin verilmişse
            //Granted izin verilmiş Denied izin verilmemiş
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
                //ActivityCompate kullanıcı izin vermedi ona mantığı göstereyim mi izin vermesi için
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    //Mantığı göstermek için alert Diaologa benzeyern snack bar yapısı
                    //Snackabar.yap(görünüm,İçindeki yazı ,Kullanıcı bir buttona tıklayana kadar bekle),buttonaksiyonu("button adı",tıklanınca ne olacağına dair listener){
                    // Tıklanınca şu olsun {
                    // }}
                    Snackbar.make(view,"Permisson needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Request permission izin sorgulama
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show(); //snackbarı göster.
                }
                else {
                    //Request permission izin sorgulama
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            }
            else {
                //gallery'e git
                /*Intent intentToGallery objesi = new ıntent(Intente tıklanınca galeriye git ve resmi al resmiUrı urlden konumundan al  )*/
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
        else {
            // android 33 ve altı --> READ_EXTERNAL_STORAGE
            //Api seviyesine bakmadan izin sorgula(activitesi this,Manifest içinde izinlerde Dosya okuma iznini) != eşitdeğil ise Paketyöneticisinde.izin verilmişse
            //Granted izin verilmiş Denied izin verilmemiş
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                //ActivityCompate kullanıcı izin vermedi ona mantığı göstereyim mi izin vermesi için
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //Mantığı göstermek için alert Diaologa benzeyern snack bar yapısı
                    //Snackabar.yap(görünüm,İçindeki yazı ,Kullanıcı bir buttona tıklayana kadar bekle),buttonaksiyonu("button adı",tıklanınca ne olacağına dair listener){
                    // Tıklanınca şu olsun {
                    // }}
                    Snackbar.make(view,"Permisson needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Request permission izin sorgulama
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show(); //snackbarı göster.
                }
                else {
                    //Request permission izin sorgulama
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
            else {
                //gallery'e git
                /*Intent intentToGallery objesi = new ıntent(Intente tıklanınca galeriye git ve resmi al resmiUrı urlden konumundan al  )*/
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }

    }

    private  void  registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                    //Aktiviye gidince cevap verme
                if (result.getResultCode()==RESULT_OK) {
                    Intent intentFromResult = result.getData();
                if (intentFromResult != null){
                    Uri imageData = intentFromResult.getData();
                    //binding.imageView.setImageURI(imageData);

                    try {
                        if(Build.VERSION.SDK_INT >= 28) {
                            //ImageDecoder görsel dönüştürücü kodlayıcı apı 28 ve üstünde gçerli
                            ImageDecoder.Source source = ImageDecoder.createSource(Artactivity.this.getContentResolver(),imageData);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                            binding.imageView.setImageBitmap(selectedImage);
                        }
                        else {
                            //Resimi dönüştürme işlemi apı 28 ve altında
                            selectedImage = MediaStore.Images.Media.getBitmap(Artactivity.this.getContentResolver(),imageData);
                            binding.imageView.setImageBitmap(selectedImage);
                        }

                    }
                    catch (Exception e){
                        e.printStackTrace(); // hataları loglara yaz
                    }
                }
                }
            }
        });

        //İzin almak için yapılan bazı işlemler.
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                // result doğruysa izin verildi yada verilmedi
                if (result){
                    //permission granted izin verildi
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else {
                    //permission denied.
                    Toast.makeText(Artactivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}