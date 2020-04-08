package com.example.androididentityfaceazure;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        imageView = findViewById(R.id.imageView_PhotoB);
        Bundle extras = getIntent().getExtras();
        String imageUri = extras.getString("pic");
        if (imageView == null) {
            Toast.makeText(getApplicationContext(),"[error]" + "Image is not in format",Toast.LENGTH_LONG).show();
        } else {
            //imageView.setImageURI(Uri.parse(imageUri));
        }
    }
}
