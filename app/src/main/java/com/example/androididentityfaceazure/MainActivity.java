package com.example.androididentityfaceazure;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import edmt.dev.edmtdevcognitiveface.Contract.AddPersistedFaceResult;
import edmt.dev.edmtdevcognitiveface.Contract.CreatePersonResult;
import edmt.dev.edmtdevcognitiveface.Contract.Face;
import edmt.dev.edmtdevcognitiveface.Contract.FaceRectangle;
import edmt.dev.edmtdevcognitiveface.Contract.IdentifyResult;
import edmt.dev.edmtdevcognitiveface.Contract.Person;
import edmt.dev.edmtdevcognitiveface.Contract.TrainingStatus;
import edmt.dev.edmtdevcognitiveface.Contract.VerifyResult;
import edmt.dev.edmtdevcognitiveface.FaceServiceClient;
import edmt.dev.edmtdevcognitiveface.FaceServiceRestClient;
import edmt.dev.edmtdevcognitiveface.Rest.ClientException;
import edmt.dev.edmtdevcognitiveface.Rest.Utils;


public class MainActivity extends AppCompatActivity {

    private final String API_KEY="298ac06ff3884863928b35b43e7d07a6";
    private final String API_LINK="https://southeastasia.api.cognitive.microsoft.com/face/v1.0/";

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient(API_LINK, API_KEY);

    private final String personGroupID = "grouptest";
    private final String personGroupName = "Group Test";


    ImageView img_view;
    TextView textview_status;
    Bitmap bitmap;
    Face[] faceDetected;



    Button btn_detect,btn_identify;


    private static class ParamsVerify {
        String imgPath1,imgPath2;

        ParamsVerify(String imgPath1,String imgPath2) {
            this.imgPath1 = imgPath1;
            this.imgPath2 = imgPath2;
        }
    }

    class detectTask extends AsyncTask<InputStream,String,Face[]>{
        android.app.AlertDialog alertDialog = new  SpotsDialog.Builder()
                .setContext(MainActivity.this)
                .setCancelable(false)
                .build();


        @Override
        protected void onPreExecute() {
            alertDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            alertDialog.setMessage(values[0]);
        }

        @Override
        protected Face[] doInBackground(InputStream... inputStreams) {

            try {
                publishProgress( "Detecting..." );
                Face [] result = faceServiceClient.detect(inputStreams[0],true,false,null);
                if (result == null) {
                    return null;
                }
                else {
                    return result;
                }
            } catch (ClientException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Face[] faces) {

            alertDialog.dismiss();

            if (faces == null){
                Toast.makeText(MainActivity.this,"No faces detected",Toast.LENGTH_SHORT).show();
            }
            else
            {
                img_view.setImageBitmap(Utils.drawFaceRectangleOnBitmap(bitmap,faces, Color.YELLOW));
                faceDetected = faces;

                btn_identify.setEnabled(true);
            }
        }
    }

    class IdentificationTask extends AsyncTask<UUID,String, IdentifyResult[]>{
        android.app.AlertDialog alertDialog = new  SpotsDialog.Builder()
                .setContext(MainActivity.this)
                .setCancelable(false)
                .build();

        @Override
        protected void onPreExecute() {
            alertDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            alertDialog.setMessage(values[0]);
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... uuids) {
            try {
                publishProgress("Getting person group status...");
                TrainingStatus trainingStatus = faceServiceClient.getPersonGroupTrainingStatus(personGroupID);

                if (trainingStatus.status != TrainingStatus.Status.Succeeded){
                    Log.d("Error","Person Group Training status is" + trainingStatus.status);
                    return null;
                }

                publishProgress("Identifying");
                IdentifyResult[] result = faceServiceClient.identity(personGroupID,uuids,1);

                return result;
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(IdentifyResult[] identifyResults) {
            alertDialog.dismiss();
            if (identifyResults != null){
                for(IdentifyResult identifyResult:identifyResults)
                    new PersonDetectionTask().execute(identifyResult.candidates.get(0).personId);

            }
        }


    }

    class PersonDetectionTask extends AsyncTask<UUID,String, Person>{
        android.app.AlertDialog alertDialog = new  SpotsDialog.Builder()
                .setContext(MainActivity.this)
                .setCancelable(false)
                .build();

        @Override
        protected void onPreExecute() {
            alertDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            alertDialog.setMessage(values[0]);
        }

        @Override
        protected Person doInBackground(UUID... uuids) {
            try {
                return faceServiceClient.getPerson(personGroupID,uuids[0]);
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Person person) {
            alertDialog.dismiss();

            img_view.setImageBitmap(Utils.drawFaceRectangleWithTextOnBitmap(bitmap,faceDetected,person.name,Color.YELLOW,100));
        }
    }

    //--------------------------------------------------------------------


    class VerifyTask extends AsyncTask<ParamsVerify,String, VerifyResult>{
        @Override
        protected void onPostExecute(VerifyResult result) {
            if (result != null) {
                if (result.isIdentical){
                    textview_status.setText("Same");
                } else {
                    textview_status.setText("Different");
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

                //mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(params[0].imgPath2));
                mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.jackma);
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
        setContentView(R.layout.activity_main);

        //Set bitmap for ImageView
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.jackma);
        img_view = (ImageView)findViewById(R.id.img_view);
        img_view.setImageBitmap(bitmap);



        btn_detect = (Button)findViewById(R.id.btn_detect);
        btn_identify = (Button)findViewById(R.id.btn_identify);
        textview_status = (TextView)findViewById(R.id.textView_status);
        textview_status = (TextView)findViewById(R.id.textView_status2);

        Bundle extras = getIntent().getExtras();
        String personName = extras.getString("personName");
        String imageUri = extras.getString("pic");




        ParamsVerify paramsVerify = new ParamsVerify(imageUri,"");

        new VerifyTask().execute(paramsVerify);



        //
        btn_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                new detectTask().execute(inputStream);
            }
        });

        btn_identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (faceDetected.length >0 )
                {
                    final UUID[] faceIds = new UUID[faceDetected.length];
                    for (int i = 0;i < faceDetected.length;i++)
                        faceIds[i] = faceDetected[i].faceId;

                    new IdentificationTask().execute(faceIds);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "No face to detect", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
