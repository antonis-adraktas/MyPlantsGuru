package com.example.myplantsguru;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myplantsguru.data.ImageData;
import com.example.myplantsguru.data.MyLatLng;
import com.example.myplantsguru.data.WeatherData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class UploadPhoto extends AppCompatActivity implements SensorEventListener {

    public static final String DB_DATA="Data";
    public static final String STORAGE_CHILD="images";
    //code for camera request
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView imageView;
    private AutoCompleteTextView mDescription;
    private AutoCompleteTextView mQuestion;
    private Button mUpload;

    private String currentPhotoPath;

    LocationManager locationManager;
    LocationListener locationListener;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private float mAltitudePressure;
    private double mAltitudeLocation;
    private MyLatLng myLatLng;
    String city;


    //firebase
    private  DatabaseReference databaseReference;
    private StorageReference storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        mDescription=findViewById(R.id.upload_description);
        mQuestion=findViewById(R.id.upload_question);
        mUpload=findViewById(R.id.send);
        imageView=findViewById(R.id.uploadPic);

        databaseReference= FirebaseDatabase.getInstance().getReference();

        storage=FirebaseStorage.getInstance().getReference().child(STORAGE_CHILD);

        //sensor initialization
        mSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
        myLatLng=getLocation();

        if (WeatherRecommendations.getWeather()!=null){
            city=WeatherRecommendations.getWeather().getCity();
            Log.d(LoginActivity.LOGAPP,"get City for existing weather object from WeatherRecommendations");
        }else{
            if (myLatLng!=null){
                RequestParams params=new RequestParams();
                params.put("lat",myLatLng.getLatitude());
                params.put("lon",myLatLng.getLongitude());
                params.put("appid",WeatherRecommendations.APP_ID);
                params.put("units","metric");
                params.put("cnt", WeatherRecommendations.CNT);
                Log.d(LoginActivity.LOGAPP,"get City for current location for UploadPhoto");
                letsDoSomeNetworking(params);
            }

        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LoginActivity.LOGAPP,"permission asked from onCreate");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},Scanner.MY_CAMERA_REQUEST_CODE);
            return;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float pressure=event.values[0];
        Log.d(LoginActivity.LOGAPP,"Pressure measured: "+pressure+"hPa");
        mAltitudePressure =SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE,pressure);
        Log.d(LoginActivity.LOGAPP,"Altitude measured: "+ mAltitudePressure);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(LoginActivity.LOGAPP,"Accuracy changed: "+accuracy);
    }

    @Override
    //this will be executed once application receives the result from the intent called in dispatchTakePictureIntent()
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            setPic();
        }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED){
            imageView.setImageResource(R.drawable.camera);
            currentPhotoPath="empty";
            Log.d(LoginActivity.LOGAPP,"photo path: "+currentPhotoPath);
        }
    }

    //Create a file for the photo to be saved named using timestamp
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);   //photos are saved in app's storage and are only accessible through the app
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d(LoginActivity.LOGAPP,"photo path: "+currentPhotoPath);
        return image;
    }

    //This function calls the intent to use the camera of the device
    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File

                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.myplantsguru",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }else{
            Toast.makeText(this, "Permission is required to use the camera", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},Scanner.MY_CAMERA_REQUEST_CODE);
            Log.d(LoginActivity.LOGAPP,"permission asked for camera after button was clicked");
        }

    }

    // Function to resize the saved file to fit the imageView space. It also rotates the image to appear properly.
    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        //code to rotate pic
        try {
            ExifInterface exif = new ExifInterface(currentPhotoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {
            Log.d(LoginActivity.LOGAPP,"Could not rotate picture due to: \n"+e.getMessage());
        }
        imageView.setImageBitmap(bitmap);
        Log.d(LoginActivity.LOGAPP,"pic loaded in imageView resized and rotated");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Scanner.MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LoginActivity.LOGAPP,"camera permission granted");
            } else {
                Log.d(LoginActivity.LOGAPP,"camera permission not granted");
                Toast.makeText(this, "Permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void upload(){
        String desc=mDescription.getText().toString();
        String question=mQuestion.getText().toString();
        if (myLatLng==null){
            myLatLng=getLocation();
        }
        if (city==null){
            city="Unknown";
        }


        StorageReference userStorage=storage.child(MainActivity.CURRENT_USER).child(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        //here we upload the image file to firebase storage
        if (currentPhotoPath.equals("empty")){
            Toast.makeText(this, "File path is empty. Please retake the picture and upload it", Toast.LENGTH_LONG).show();
        }else {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            userStorage.putFile(Uri.fromFile(new File(currentPhotoPath))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {      //change path to a uri to upload the image
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image uploaded successfully
                    // Dismiss dialog
                    progressDialog.dismiss();
                    Toast.makeText(UploadPhoto.this, "Image Uploaded!!",Toast.LENGTH_SHORT).show();
                    //this part will need editing to get the correct reference
                    //when the pic is successfully uploaded we create an object with all the necessary data(download url, user input data) and it's added in realtime db
                    ImageData picData;
                    // Location and altitude is filled depending on existence. If location doesn't exist it will go to (0,0)
                    if (myLatLng==null&&mSensor==null){
                        picData=new ImageData(MainActivity.CURRENT_USER,taskSnapshot.getMetadata().getPath(),desc,question,new MyLatLng(0.0,0.0),0,city);
                    }else if (myLatLng==null&&mSensor!=null){
                        picData=new ImageData(MainActivity.CURRENT_USER,taskSnapshot.getMetadata().getPath(),desc,question,new MyLatLng(0.0,0.0),Math.round(mAltitudePressure),city);
                    }else{
                        if (mSensor == null) {
                            picData=new ImageData(MainActivity.CURRENT_USER,taskSnapshot.getMetadata().getPath(),desc,question,myLatLng,(int) Math.round(mAltitudeLocation),city);
                        }else{
                            picData=new ImageData(MainActivity.CURRENT_USER,taskSnapshot.getMetadata().getPath(),desc,question,myLatLng,Math.round(mAltitudePressure),city);
                        }
                    }
                    Log.d(LoginActivity.LOGAPP,taskSnapshot.getStorage().getPath());
                    Log.d(LoginActivity.LOGAPP,taskSnapshot.getUploadSessionUri().toString());
                    databaseReference.child(DB_DATA).child(replaceDotsWithUnderscore(MainActivity.CURRENT_USER)).push().setValue(picData);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Error, Image not uploaded
                    progressDialog.dismiss();
                    Toast.makeText(UploadPhoto.this,"Failed " + e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 *snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage(
                            "Uploaded "+ (int)progress + "%");
                }
            });
        }

    }

    private MyLatLng getLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d(LoginActivity.LOGAPP, "onlocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d(LoginActivity.LOGAPP, "longitude is: " + longitude);
                Log.d(LoginActivity.LOGAPP, "latitude is: " + latitude);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Log.d(LoginActivity.LOGAPP, "onProviderEnabled() callback received");
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d(LoginActivity.LOGAPP, "onProviderDisabled() callback received");
            }
        };


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MainActivity.MIN_TIME, MainActivity.MIN_DISTANCE, locationListener);
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (myLocation==null){
                myLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (myLocation==null){
                    Toast.makeText(UploadPhoto.this,"myLocation is still null, couldn't retrieve last known location from GPS or Network provider",Toast.LENGTH_SHORT).show();
                    return null;
                }
            }

            mAltitudeLocation=myLocation.getAltitude();
            Log.d(LoginActivity.LOGAPP,"Altitude from location retrieved: "+mAltitudeLocation);
            return  new MyLatLng(myLocation.getLatitude(),myLocation.getLongitude());

        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.REQUEST_CODE);
            return null;
        }
    }

    public void letsDoSomeNetworking(RequestParams params){
        AsyncHttpClient client=new AsyncHttpClient();
        Log.d(LoginActivity.LOGAPP,"inside networking");
        client.get(WeatherRecommendations.WEATHER_URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(LoginActivity.LOGAPP,"Success! JSON: "+response.toString());
                WeatherData weather= new WeatherData().parseJson(response);
                if (weather!=null){
//                    Log.d(LoginActivity.LOGAPP,"weather data object from upload is "+weather.toString());
                    WeatherRecommendations.setWeather(weather);
                    city=weather.getCity();
                    Log.d(LoginActivity.LOGAPP,"city is: "+city);
                }else{
                    Log.d(LoginActivity.LOGAPP,"weather data object is null");
                    Toast.makeText(UploadPhoto.this,"Couldn't parse weather data. Something unexpected happened!",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d(LoginActivity.LOGAPP,"Fail "+throwable.toString());
                Log.d(LoginActivity.LOGAPP,"Status code "+statusCode);
                Toast.makeText(UploadPhoto.this,"Request failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String replaceDotsWithUnderscore(String s){
        return s.replace('.','_');
    }

    public void onBackPressed() {
        Intent intent=new Intent(UploadPhoto.this,MainActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        Log.d(LoginActivity.LOGAPP,"listener for sensor unregistered in onPause");
        if (locationManager!=null) locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mSensor!=null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            Log.d(LoginActivity.LOGAPP,"Phone doesn't have pressure sensor");
        }
    }
}
