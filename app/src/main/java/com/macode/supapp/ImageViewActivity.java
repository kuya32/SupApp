package com.macode.supapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_PICTURES;

public class ImageViewActivity extends AppCompatActivity {

    public ImageView imageView, downloadImageButton, downloadBackButton;
    public String url;
    public OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imageView = findViewById(R.id.imageView);
        downloadImageButton = findViewById(R.id.downloadImageButton);
        downloadBackButton = findViewById(R.id.downloadBackButton);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        url = getIntent().getStringExtra("url");
        Picasso.get().load(url).into(imageView);

        downloadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage(getBitmapFromURL(url));
            }
        });

        downloadBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageViewActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void downloadImage(Bitmap bitmap) {

        String filePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).toString();
        File file = new File(filePath, System.currentTimeMillis() + ".jpg");
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            System.out.println(myBitmap.toString());
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}