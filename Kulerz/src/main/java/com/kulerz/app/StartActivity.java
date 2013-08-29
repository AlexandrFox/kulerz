package com.kulerz.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.kulerz.app.helpers.SystemHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StartActivity extends KulerzActivity {

    private Button selectFromGalleryBtn;
    private Button takeAPhotoBtn;
    private Button myPalettesListBtn;
    private File image;

    private static final int SELECT_PHOTO_GALLERY = 100;
    private static final int TAKE_A_PHOTO = 101;
    private static final String FILE_KEY = "image";
    private static final String FILE_PREFIX = "kulerz_";
    private static final String FILE_SUFFIX = ".jpg";
    private static final String ALBUM_NAME = "kulerz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        selectFromGalleryBtn = (Button)findViewById(R.id.selectFromGalleryBtn);
        takeAPhotoBtn = (Button)findViewById(R.id.takeAPhotoBtn);
        myPalettesListBtn = (Button)findViewById(R.id.myPalettesListBtn);
        initializeHandlers();
        if(savedInstanceState != null) {
            image = (File)savedInstanceState.getSerializable(FILE_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(FILE_KEY, image);
    }

    private void initializeHandlers() {
        selectFromGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity.this.onSelectFromGalleryClick(view);
            }
        });
        takeAPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartActivity.this.onTakeAPhotoClick(view);
            }
        });
    }

    private void onSelectFromGalleryClick(View view) {
        if(!SystemHelper.isIntentAvailable(Intent.ACTION_PICK, this)) {
            SystemHelper.showShortToast(R.string.app_not_founded, this);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PHOTO_GALLERY);
    }

    private void onTakeAPhotoClick(View view) {
        if(!SystemHelper.isIntentAvailable(MediaStore.ACTION_IMAGE_CAPTURE, this)) {
            SystemHelper.showShortToast(R.string.app_not_founded, this);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        image = createImageFile();
        if(image != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
            startActivityForResult(intent, TAKE_A_PHOTO);
        } else {
            SystemHelper.showShortToast(R.string.cannot_create_image, this);
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = FILE_PREFIX + timeStamp + "_";
        try {
            return File.createTempFile(imageFileName, FILE_SUFFIX, getAlbumDir());
        } catch (IOException e) {
            Log.e(Kulerz.TAG, e.toString());
            return null;
        }
    }

    private File getAlbumDir() throws IOException {
        File storageDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = getAlbumStorageDir(ALBUM_NAME);
            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()){
                        throw new IOException("Failed to create directory.");
                    }
                }
            }
        } else {
            throw new IOException("External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    public File getAlbumStorageDir(String albumName) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) ,albumName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent content) {
        super.onActivityResult(requestCode, resultCode, content);
        switch (requestCode) {
            case SELECT_PHOTO_GALLERY:
                if(resultCode == RESULT_OK) {
                    Uri selectedImage = content.getData();
                    if(selectedImage != null) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra(MainActivity.IMAGE_URI, selectedImage);
                        startActivity(intent);
                    } else {
                        SystemHelper.showShortToast(R.string.file_not_found, this);
                    }
                }
                break;
            case TAKE_A_PHOTO:
                if(resultCode == RESULT_OK) {
                    Uri photoUri = Uri.fromFile(image);
                    Intent mediaScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScan.setData(photoUri);
                    sendBroadcast(mediaScan);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(MainActivity.IMAGE_URI, photoUri);
                    startActivity(intent);
                }
                break;
        }
    }

}
