package com.declut.morningstar.declut;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    int flag = 0;
    ArrayList<String> imageList;
    RelativeLayout mainLayout;
    Intent imageActivityIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        char[] arr = {'a','b','c','d','f','g','h','i','j'};
        Random random = new Random();
        String imgSrc = arr[random.nextInt(9)]+"";
        Resources r = getResources();
        int picId = r.getIdentifier(imgSrc,"mipmap","com.declut.morningstar.declut");

        mainLayout = (RelativeLayout) findViewById(R.id.activity_main);
        mainLayout.setBackgroundResource(picId);


        if(ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){

            flag = 1;
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
        if(ActivityCompat.checkSelfPermission(
                this,Manifest.permission.WRITE_EXTERNAL_STORAGE )!=
                PackageManager.PERMISSION_GRANTED){

            flag = 1;
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        if (flag == 0){
            new ReadImages().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    flag = 0;
                    new ReadImages().execute();
                }
                else {
                    Log.i("Permission","Not Granted");
                    Toast.makeText(this, "Permission is required!", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    private class ReadImages extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {

            imageList = new ArrayList<>();
            Toast.makeText(getApplicationContext(),"Getting your pictures!",Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            Uri uri;
            Cursor cursor;
            int count=0;
            int column_index_data;
            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

            cursor = getApplicationContext().getContentResolver().query(uri, projection, null,
                    null, null);
            column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            int emptycount = 0;
            while (cursor.moveToNext()) {
                File ftemp = new File(cursor.getString(column_index_data));

                if (ftemp.length()==0){
                    Log.i("File","Empty"+(++emptycount)+" " + ftemp.getPath());
                    getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + "=?", new String[]{ ftemp.getPath() } );
                }else {
                    imageList.add((String) cursor.getString(column_index_data));
                    count++;
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    imageActivityIntent = new Intent(MainActivity.this,FoldersActivity.class);
                    imageActivityIntent.putStringArrayListExtra("imageList",imageList);
                    startActivity(imageActivityIntent);
                    finish();
                }
            },2000);
            super.onPostExecute(s);
        }
    }
}
