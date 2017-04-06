package com.declut.morningstar.declut;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoldersActivity extends AppCompatActivity {

    private ArrayList<String> imageList, tempList;

    private Map<String,List<String>> imagesMap;
    private TableLayout folders;
    ProgressBar loading;
    Typeface customFont;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable());
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.custom_actionbar_layout,null);
        actionBar.setCustomView(v);
        customFont = Typeface.createFromAsset(getAssets(), "fonts/font4.otf");

        loading = (ProgressBar)findViewById(R.id.loading);
        folders = (TableLayout)findViewById(R.id.folders);
        imageList = new ArrayList<>();
        imageList = getIntent().getStringArrayListExtra("imageList");
        imagesMap = new HashMap<String, List<String>>();
        assignFolders();
    }

    public void assignFolders(){
        Log.i("Assigning Folders","Called");
        int count = 1;
        int row = 0;
        trimCache(getApplicationContext());
        TableRow tableRow  = new TableRow(this);
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams rowChildParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.MATCH_PARENT);
        //rowChildParams.weight = 1;

        layoutParams.weight=1;
        for (String imagePath : imageList){
            String[] arrPath = imagePath.split("/");
            final String folderName = arrPath[arrPath.length-2];

            if(imagesMap.get(folderName) == null){
                imagesMap.put(folderName,new ArrayList<String>());
                RelativeLayout box = new RelativeLayout(this);
                box.setLayoutParams(rowChildParams);
                box.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
                ImageView img = new ImageView(this);
                img.setLayoutParams(params);
                img.setImageResource(getResources().getIdentifier("folder","mipmap","com.declut.morningstar.declut"));
                img.setPadding(50,20,50,20);


                TextView folder_name = new TextView(this);
                folder_name.setLayoutParams(params);
                folder_name.setPadding(0,10,0,10);
                folder_name.setGravity(Gravity.CENTER_HORIZONTAL);
                folder_name.setText(folderName);
                folder_name.setTextColor(Color.WHITE);
                folder_name.setTypeface(customFont);
                folder_name.setTextSize(20);
                folder_name.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                box.addView(img);
                box.addView(folder_name);
                box.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(FoldersActivity.this,ImageActivity.class);
                        ArrayList<String> temp = (ArrayList<String>) imagesMap.get(folderName);
                        i.putStringArrayListExtra("ImageList",temp);
                        i.putExtra("FolderName",folderName);
                        startActivityForResult(i,1);
                    }
                });

                if (count == 1){
                    count = 2;
                    row++;
                    tableRow = new TableRow(this);
                    tableRow.setLayoutParams(rowParams);
                    tableRow.setId(row);
                    tableRow.addView(box);
                    folders.addView(tableRow);
                }
                else{
                    count = 1;
                    tableRow.addView(box);
                }
            }

            imagesMap.get(folderName).add(imagePath);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            trimCache(getApplicationContext());
            new ReadImages().execute();
        }
    }

    private class ReadImages extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {

            tempList = new ArrayList<>();
            Toast.makeText(getApplicationContext(),"Refreshing Media Libraries!",Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.VISIBLE);
            for (int i = 0; i < folders.getChildCount(); i++){
                folders.removeView(folders.getChildAt(i));
            }
            folders.removeAllViews();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            Uri uri;
            Cursor cursor;
            int count=0;
            int column_index_data;
            File f = new File(Environment.getExternalStorageDirectory()+"");

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
                    tempList.add((String) cursor.getString(column_index_data));
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
                    loading.setVisibility(View.INVISIBLE);
                }
            },2000);
            imageList.clear();
            imagesMap.clear();
            for (String p : tempList){
                imageList.add(p);
            }
            assignFolders();
            super.onPostExecute(s);
            return;
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
                return;
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.i("Cache Exception","1111");
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
