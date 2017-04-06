package com.declut.morningstar.declut;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.lorentzos.flingswipe.FlingCardListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import org.w3c.dom.Text;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ImageActivity extends AppCompatActivity {

    public static MyAppAdapter myAppAdapter;
    public static ViewHolder viewHolder;
    private ArrayList<String> array;
    String fName = "";
    private SwipeFlingAdapterView flingContainer;
    TextView folderName;
    Typeface customFont;
    ImageView dlt,skip;
    AlertDialog.Builder deleteDialog;
    DialogInterface.OnClickListener deleteDialogClickListner;
    String path ="";
    String path1 ="";
    String path2 ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        flingContainer = (SwipeFlingAdapterView)findViewById(R.id.frame);
        array = new ArrayList<>();
        array = getIntent().getStringArrayListExtra("ImageList");
        fName = fName + getIntent().getStringExtra("FolderName");
        dlt = (ImageView) this.findViewById(R.id.dlt);
        skip = (ImageView)this.findViewById(R.id.skip);
        dlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pathToFile = array.get(0);
                File file = new File(pathToFile);
                file.delete();
                File file1 = new File(pathToFile);
                Uri contentUri = Uri.fromFile(file1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                }
                else
                {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(pathToFile)));
                }
                Toast.makeText(getApplicationContext(),"Deleted!", 200).show();
                array.remove(0);
                flingContainer.removeAllViewsInLayout();
                myAppAdapter.notifyDataSetChanged();
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Skipped!",200).show();
                array.remove(0);
                flingContainer.removeAllViewsInLayout();
                myAppAdapter.notifyDataSetChanged();
            }
        });
        myAppAdapter = new MyAppAdapter(array, ImageActivity.this);
        path = "/DeClut";
        path1 = path + "/Liked/";
        path2 = path + "/Disliked/";
        try {
            File dir = new File(Environment.getExternalStorageDirectory(),path);
            if(!dir.exists()){
                Log.i("Creating Directory",path);
                dir.mkdir();
            }
            File dir1 = new File(Environment.getExternalStorageDirectory(),path1);
            if(!dir1.exists()){
                Log.i("Creating Directory",path1);
                dir1.mkdir();
            }
            File dir2 = new File(Environment.getExternalStorageDirectory(),path2);
            if(!dir2.exists()){
                Log.i("Creating Directory",path2);
                dir2.mkdir();
            }
        }catch (Exception e){
            Log.i("Exception",e.getMessage());
        }

        flingContainer.setAdapter(myAppAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {

            }


            @Override
            public void onLeftCardExit(Object dataObject) {
                String pathToFile = array.get(0);
                File file = new File(pathToFile);
                String fileName = file.getName();
                File newFile = new File(Environment.getExternalStorageDirectory()+""+path2+fileName);

                file.renameTo(newFile);
                array.remove(0);
                myAppAdapter.notifyDataSetChanged();
                refreshMedia(newFile.getPath(),pathToFile);
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                String pathToFile = array.get(0);
                File file = new File(pathToFile);
                String fileName = file.getName();
                File newFile = new File(Environment.getExternalStorageDirectory()+""+path1+fileName);
                file.renameTo(newFile);
                array.remove(0);
                myAppAdapter.notifyDataSetChanged();
                refreshMedia(newFile.getPath(),pathToFile);
            }

            public void refreshMedia(String newPath,String oldPath){
                File f = new File(newPath);
                File f2 = new File(oldPath);
                Uri contentUri = Uri.fromFile(f);
                Uri contentUri2 = Uri.fromFile(f2);
                Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
                    Log.i("Android Version","Greater than or equal to Kitkat!");
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Intent mediaScanIntent2 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Intent mediaScanIntent3 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                    mediaScanIntent2.setData(contentUri2);
                    sendBroadcast(mediaScanIntent2);
                    mediaScanIntent3.setData(uri);
                    sendBroadcast(mediaScanIntent3);

                }
                else
                {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(newPath)));
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(oldPath)));
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(uri.toString())));
                }
                return;
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                if (itemsInAdapter == 0){
                    Toast.makeText(getApplicationContext(),"All images from folder '" + fName + "' have been arranged",500).show();
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.background).setAlpha(0);
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.background).setAlpha(0);

                myAppAdapter.notifyDataSetChanged();
            }
        });
    }

    public static class ViewHolder {
        public static FrameLayout background;
        public TextView DataText;
        public ImageView cardImage;


    }

    public class MyAppAdapter extends BaseAdapter {


        public List<String> parkingList;
        public Context context;

        private MyAppAdapter(List<String> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;


            if (rowView == null) {

                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.item, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.DataText = (TextView) rowView.findViewById(R.id.folderName);
                viewHolder.background = (FrameLayout) rowView.findViewById(R.id.background);
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.cardImage);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String[] arrPath = parkingList.get(position).split("/");


            customFont = Typeface.createFromAsset(getAssets(), "fonts/font3.ttf");
            viewHolder.DataText.setText(arrPath[arrPath.length-2]);
            viewHolder.DataText.setTypeface(customFont);
            Glide.with(ImageActivity.this).load(parkingList.get(position)).into(viewHolder.cardImage);

            return rowView;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();

    }
}
