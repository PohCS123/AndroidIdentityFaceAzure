package com.example.androididentityfaceazure;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Main3Activity extends AppCompatActivity {

    ImageView imageView;

    Button button;
    Uri imageUri;
    Uri imageUriPass;
    Button btOpen;
    Button btConfirm;
    String []cameraPermission;
    String []storagePermission;
    boolean bool_image;

    private static final int PICK_IMAGE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        bool_image = false;

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        imageView = findViewById(R.id.imageView_Photo);

        button = findViewById(R.id.button_Gallery);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();

            }
        });

        btOpen = findViewById(R.id.button_TakePhoto);

        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //Open Camera

                if (checkCameraPermission()) {
                    dispatchTakePictureIntent();
                }

            }

        });

        btConfirm = findViewById(R.id.button_Confirm);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Confirm

                if (bool_image == true){
                    Bundle extras = getIntent().getExtras();

                    Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                    intent.putExtra("pic", imageUriPass.toString());
                    intent.putExtra("personName",extras.getString("username"));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),"Please upload your image",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery,PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data ){
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            imageUriPass = imageUri;
            imageView.setImageURI(imageUri);
            bool_image = true;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            imageUriPass = imageUri;
            imageView.setImageURI(imageUri);
            bool_image = true;
        }





    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        if (!result1) {

            requestStoragePermission();

        }
        if (!result) {
            requestCameraPermission();
        }

        return result && result1;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.TITLE, "NewPic");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }


    }


}
