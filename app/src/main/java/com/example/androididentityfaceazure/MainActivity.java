package com.example.androididentityfaceazure;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import edmt.dev.edmtdevcognitiveface.Contract.AddPersistedFaceResult;
import edmt.dev.edmtdevcognitiveface.Contract.CreatePersonResult;
import edmt.dev.edmtdevcognitiveface.Contract.Face;
import edmt.dev.edmtdevcognitiveface.Contract.FaceRectangle;
import edmt.dev.edmtdevcognitiveface.Contract.IdentifyResult;
import edmt.dev.edmtdevcognitiveface.Contract.Person;
import edmt.dev.edmtdevcognitiveface.Contract.TrainingStatus;
import edmt.dev.edmtdevcognitiveface.FaceServiceClient;
import edmt.dev.edmtdevcognitiveface.FaceServiceRestClient;
import edmt.dev.edmtdevcognitiveface.Rest.ClientException;
import edmt.dev.edmtdevcognitiveface.Rest.Utils;


public class MainActivity extends AppCompatActivity {

    private final String API_KEY="298ac06ff3884863928b35b43e7d07a6";
    private final String API_LINK="https://southeastasia.api.cognitive.microsoft.com/face/v1.0/";

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient(API_LINK, API_KEY);

    private final String personGroupID = "celebritiesactor";


    ImageView img_view;
    Bitmap bitmap;
    Face[] faceDetected;

    Face[] mFaceIndices;
    GridView mFaceGridViewAdapter;

    Button btn_detect,btn_identify;

    private static class Params1 {
        String personGroupId,personGroupName;

        Params1(String personGroupId, String personGroupName) {
            this.personGroupId = personGroupId;
            this.personGroupName = personGroupName;
        }
    }

    private static class Params2 {
        String personGroupId,personGroupName,imgPath;

        Params2(String personGroupId, String personGroupName, String imgPath) {
            this.personGroupId = personGroupId;
            this.personGroupName = personGroupName;
            this.imgPath = imgPath;
        }
    }

    private static class Params3 {
        String personGroupId,imgPath;
        CreatePersonResult createPersonResult;

        Params3(String personGroupId, CreatePersonResult createPersonResult, String imgPath) {
            this.personGroupId = personGroupId;
            this.createPersonResult = createPersonResult;
            this.imgPath = imgPath;
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

    class CreatePersonGroupTask extends AsyncTask<Params1,String,String>{
        @Override
        protected String doInBackground(Params1... params) {

            try{
                //publishProgress("Syncing with server to add person...");

                //addLog("Request: Creating Person in person group" + params[0]);

                // Start the request to creating person.
                faceServiceClient.createPersonGroup(
                        params[0].personGroupId,
                        params[0].personGroupName,
                        "");

                return params[0].personGroupId;

            } catch (Exception e) {
                publishProgress(e.getMessage());
                //addLog(e.getMessage());
                return null;
            }
        }
    }

    class AddPersonToGroupTask extends AsyncTask<Params2,String,Params3>{
        @Override
        protected void onPostExecute(Params3 params3) {
            new DetectFaceAndRegisterTask().execute(params3);
        }

        @Override

        protected Params3 doInBackground(Params2... params) {
            try{
                publishProgress("Syncing with server to add person...");

                // Start the request to creating person.
                faceServiceClient.getPersonGroup("");

                CreatePersonResult createPersonResult = faceServiceClient.createPerson(
                        params[0].personGroupId,
                        params[0].personGroupName,
                        "");


                Params3 params3 = new Params3(params[0].personGroupId,createPersonResult,params[0].imgPath);
                return params3;
            } catch (Exception e) {
                return null;
            }

        }
    }

    class DetectFaceAndRegisterTask extends AsyncTask<Params3,String,Boolean>{
        @Override
        protected Boolean doInBackground(Params3... params) {
            try{
                Bitmap mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(params[0].imgPath));

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                InputStream imageInputStream = new ByteArrayInputStream(stream.toByteArray());

                Face[] result = faceServiceClient.detect(
                        imageInputStream,
                        true,         // returnFaceId
                        false,        // returnFaceLandmarks
                        null          // returnFaceAttributes:
                );

                if (result != null) {
                    for (Face face : result) {
                        FaceRectangle faceRectangle = face.faceRectangle;
                        AddPersistedFaceResult result2 = faceServiceClient.addPersonFace(
                                params[0].personGroupId,
                                params[0].createPersonResult.personId,
                                imageInputStream,
                                "User data",
                                faceRectangle);

                    }
                    return true;
                }


            } catch (Exception e) {
                return false;
            }
            return null;
        }
    }

    class TrainingAITask extends AsyncTask<String ,String ,String >{
        @Override
        protected String doInBackground(String... params) {

            // Get an instance of face service client.
            try{
                publishProgress("Training person group...");

                faceServiceClient.trainPersonGroup(params[0]);
                return params[0];
            } catch (Exception e) {
                publishProgress(e.getMessage());
                return null;
            }
        }
    }






    //---------------------------------------------------------------------------------------------------------




    //--------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set bitmap for ImageView
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.jay);
        img_view = (ImageView)findViewById(R.id.img_view);
        img_view.setImageBitmap(bitmap);

        btn_detect = (Button)findViewById(R.id.btn_detect);
        btn_identify = (Button)findViewById(R.id.btn_identify);

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
