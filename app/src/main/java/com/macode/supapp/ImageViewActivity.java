package com.macode.supapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_PICTURES;

public class ImageViewActivity extends AppCompatActivity {

    public ImageView imageView, downloadImageButton, downloadBackButton;
    public String url;

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
                downloadImage(url);
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

    private void downloadImage(String url) {
//        Toast.makeText(getBaseContext(), "Downloading Image", Toast.LENGTH_LONG).show();
//        Uri uri = Uri.parse(url);
//        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//        DownloadManager.Request request = new DownloadManager.Request(uri);
//        request.setVisibleInDownloadsUi(true);
//        request.setDestinationInExternalFilesDir(ImageViewActivity.this, DIRECTORY_DOWNLOADS, ".jpg");
//        downloadManager.enqueue(request);
    }
}