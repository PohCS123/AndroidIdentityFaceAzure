package com.example.androididentityfaceazure;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class camera extends AppCompatActivity {
    ImageView imageView;
    Button btOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Assign Variable
        imageView = findViewById(R.id.image_viewCamera);
        btOpen = findViewById(R.id.bt_open);

        //Request camera permission
        if (ContextCompat.checkSelfPermission(camera.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(camera.this,
                    new String[] {
                            Manifest.permission.CAMERA
                    },
                    100);

        }
        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //Open Camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode == 100 ){
            //Get capture Image
            Bitmap captureImage = (Bitmap) data.getExtras().get("data");
            //Set Capture Image to ImageView
            imageView.setImageBitmap(captureImage);
        }
    }

}
