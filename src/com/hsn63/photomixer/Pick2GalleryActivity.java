package com.hsn63.photomixer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class Pick2GalleryActivity extends Activity {
    private AlertDialog.Builder alertDialogA;
    private AlertDialog.Builder alertDialogB;
    private HashMap<String, Uri> pick2UriMH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //メニューバー非表示
        window.requestFeature(Window.FEATURE_NO_TITLE); //タイトルバー非表示
        setContentView(R.layout.activity_pick2_gallery);
        
        final HashMap<Uri, Bitmap> UriBmpHM = loadHashMap();
        final ArrayList<Uri> imgUri = new ArrayList<Uri>(UriBmpHM.keySet());
        
        alertDialogA = new AlertDialog.Builder(Pick2GalleryActivity.this);
        alertDialogB = new AlertDialog.Builder(Pick2GalleryActivity.this);
        pick2UriMH = new HashMap<String, Uri>();
        
        mBitmapAdapter adapter = new mBitmapAdapter(
                getApplicationContext(), R.layout.list_items2, new ArrayList<Bitmap>(UriBmpHM.values()));
        
        GridView gridView = (GridView) findViewById(R.id.imageGridView2);
        gridView.setAdapter(adapter);
        
        gridView.setOnItemClickListener(new OnItemClickListener () {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                
                if (pick2UriMH.containsKey("groundImg")) {
                    selectOverlayImage(imgUri.get(position));
                }else {
                    selectGroundImage(imgUri.get(position));
                }
                return;
                }
        });
    }
    
    public void selectGroundImage(final Uri uri) {
        alertDialogA.setTitle("Select GroundImg");
        alertDialogA.setMessage("Do you set this image as GroundImg?");
        alertDialogA.setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                pick2UriMH.put("groundImg", uri);
                Toast.makeText(Pick2GalleryActivity.this, "GroundImg Setted", Toast.LENGTH_SHORT).show();
                return;
            }
        });
        alertDialogA.setNegativeButton("No", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                return;
            }
        });
        alertDialogA.create().show();
    }
    
    public void selectOverlayImage(final Uri uri) {
        alertDialogB.setTitle("Select OverlayImg");
        alertDialogB.setMessage("Do you set this image as OverlayImg?");
        alertDialogB.setPositiveButton("Yes", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                pick2UriMH.put("overlayImg", uri);
                Toast.makeText(Pick2GalleryActivity.this, "OverlayImg Setted", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(Pick2GalleryActivity.this, ImageProByButtonActivity.class);
                Intent intent = new Intent(Pick2GalleryActivity.this, ImageProByButtonActivity.class);
                intent.putExtra("groundImg", pick2UriMH.get("groundImg"));
                intent.putExtra("overlayImg", pick2UriMH.get("overlayImg"));
                pick2UriMH.clear();
                startActivity(intent);
            }
        });
        alertDialogB.setNeutralButton("No", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                return;
            }
        });
        alertDialogB.setNegativeButton("Cancel GroundImg", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                pick2UriMH.clear();
                Toast.makeText(Pick2GalleryActivity.this, "GroundImg Cancelled", Toast.LENGTH_SHORT).show();
                return;
            }
        });
        alertDialogB.create().show();
    }
    
    private HashMap<Uri, Bitmap> loadHashMap() {
        HashMap<Uri, Bitmap> imgHM = new HashMap<Uri, Bitmap>();
        ContentResolver cr = getContentResolver();
        int mk = MediaStore.Images.Thumbnails.MICRO_KIND;
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = managedQuery(uri, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(
                    cr, id, mk, null);
            Uri bmpUri = ContentUris.withAppendedId(uri, id);
            imgHM.put(bmpUri, bmp);
            cursor.moveToNext();
        }
        return imgHM;
    }
}

class mBitmapAdapter extends ArrayAdapter<Bitmap> {
    private int resourceId;
    
    public mBitmapAdapter(Context context, int resource, List<Bitmap> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resourceId, null);
        }
        ImageView view = (ImageView) convertView;
        view.setImageBitmap(getItem(position));
        
        return view;
    }
}