package com.example.androididentityfaceazure;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import edmt.dev.edmtdevcognitiveface.Contract.Face;
import edmt.dev.edmtdevcognitiveface.Contract.VerifyResult;
import edmt.dev.edmtdevcognitiveface.FaceServiceClient;
import edmt.dev.edmtdevcognitiveface.FaceServiceRestClient;

public class Step2Activity extends AppCompatActivity {

    ImageView imageView;
    TextView textView_error;

    Button btConfirm,button,btOpen;
    boolean bool_image;
    String errorMessage;

    private final String API_KEY="298ac06ff3884863928b35b43e7d07a6";
    private final String API_LINK="https://southeastasia.api.cognitive.microsoft.com/face/v1.0/";

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient(API_LINK, API_KEY);

    Uri imageUri;
    Uri imageUriPass;

    private static final int PICK_IMAGE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String []cameraPermission;
    String []storagePermission;





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
            new CheckPhotoTask().execute(imageUri.toString());
            //imageView.setImageURI(imageUri);
            //bool_image = true;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            imageUriPass = imageUri;
            new CheckPhotoTask().execute(imageUri.toString());
            //imageView.setImageURI(imageUri);
            //bool_image = true;
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

    private static class ParamsVerify {
        String imgPath1,imgPath2;

        ParamsVerify(String imgPath1,String imgPath2) {
            this.imgPath1 = imgPath1;
            this.imgPath2 = imgPath2;
        }
    }

    class CheckPhotoTask extends AsyncTask<String,String, Integer> {
        @Override
        protected void onPostExecute(Integer integer) {
            if (integer == 1){
                textView_error.setText("");
                imageView.setImageURI(imageUri);
                bool_image = true;
            } else if (integer==2||integer==3||integer==0) {
                textView_error.setText(errorMessage);
                bool_image = false;
            } else {
                textView_error.setText("unknown error");
                bool_image = false;
            }

        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                Face[] result;
                errorMessage = "";

                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(params[0]));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                InputStream imageInputStream = new ByteArrayInputStream(stream.toByteArray());

                result = faceServiceClient.detect(
                        imageInputStream,
                        true,         // returnFaceId
                        false,        // returnFaceLandmarks
                        null          // returnFaceAttributes:
                );

                if (result.length > 1 ) {
                    errorMessage = "Cannot exceed more than 1 face";
                    return 2;
                } else if (result == null || result.length == 0) {
                    errorMessage = "No face detected";
                    return 3;
                } else {
                    return 1;
                }
            } catch (Exception e) {
                errorMessage = "[Error]" + e;
                return 0;
            }
        }
    }

    class VerifyTask extends AsyncTask<ParamsVerify,String, VerifyResult> {
        @Override
        protected void onPostExecute(VerifyResult result) {
            if (result != null) {
                if (result.isIdentical){
                    Intent intent = new Intent(getApplicationContext(), Step3Activity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getBaseContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    textView_error.setText("The faces are different");
                }
            }
        }

        @Override
        protected VerifyResult doInBackground(ParamsVerify... params) {
            try {
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(params[0].imgPath1));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                InputStream imageInputStream = new ByteArrayInputStream(stream.toByteArray());
                Face[] result,result2;
                result = faceServiceClient.detect(
                        imageInputStream,
                        true,         // returnFaceId
                        false,        // returnFaceLandmarks
                        null          // returnFaceAttributes:
                );

                if (result.length > 1 ) {
                    return null;
                } else if (result == null || result.length == 0) {
                    return null;
                }

                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(params[0].imgPath2));
                stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                imageInputStream = new ByteArrayInputStream(stream.toByteArray());

                result2 = faceServiceClient.detect(
                        imageInputStream,
                        true,         // returnFaceId
                        false,        // returnFaceLandmarks
                        null          // returnFaceAttributes:
                );

                if (result2.length > 1 ) {
                    return null;
                } else if (result2 == null || result2.length == 0) {
                    return null;
                }

                //Verify
                return faceServiceClient.verify(result[0].faceId,result2[0].faceId);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2);


        imageView = findViewById(R.id.imageView_PhotoB);
        Bundle extras = getIntent().getExtras();
        String imageUri = extras.getString("pic");

        bool_image = false;

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        button = findViewById(R.id.button_GalleryB);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();

            }
        });

        btOpen = findViewById(R.id.button_TakePhotoB);

        textView_error = findViewById(R.id.textView_error);

        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //Open Camera

                if (checkCameraPermission()) {
                    dispatchTakePictureIntent();
                }

            }

        });

        btConfirm = findViewById(R.id.button_Confirm2B);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Confirm

                if (bool_image == true){
                    Bundle extras = getIntent().getExtras();
                    String imageUri = extras.getString("pic");
                    ParamsVerify paramsVerify = new ParamsVerify(imageUri,imageUriPass.toString());

                    new VerifyTask().execute(paramsVerify);

                } else {
                    Toast.makeText(getApplicationContext(),"Please upload your image",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
