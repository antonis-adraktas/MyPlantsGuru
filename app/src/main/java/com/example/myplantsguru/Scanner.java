package com.example.myplantsguru;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ErrorCallback;
import com.google.zxing.Result;

public class Scanner extends AppCompatActivity {

    CodeScanner codeScanner;
    CodeScannerView scannerView;
    private TextView resultText;
    public static final int MY_CAMERA_REQUEST_CODE = 100;
    ErrorCallback mErrorCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        scannerView=findViewById(R.id.scannerView);

        resultText=findViewById(R.id.resultsQR);
        codeScanner=new CodeScanner(this,scannerView);

        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultText.setText(result.getText());
                    }
                });
            }
        });

        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeScanner.startPreview();
                resultText.setText(null);
            }
        });

        resultText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!resultText.getText().toString().equals(null)){
                    Log.d(LoginActivity.LOGAPP,"result text clicked!");
                    //create an intent to be opened by your default browser
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(resultText.getText().toString())));
                }

            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LoginActivity.LOGAPP,"permission asked from onCreate");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},MY_CAMERA_REQUEST_CODE);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LoginActivity.LOGAPP,"camera permission granted");
            } else {
                Log.d(LoginActivity.LOGAPP,"camera permission not granted");
                Toast.makeText(this, "Permission is required for this scanner", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED){
            try {
                codeScanner.startPreview();
            } catch (final Exception e) {
                codeScanner.releaseResources();
                final ErrorCallback errorCallback = mErrorCallback;
                if (errorCallback != null) {
                    errorCallback.onError(e);
                } else {
                    throw e;
                }
            }

        }else{
            Toast.makeText(this, "Permission is required for this scanner", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},MY_CAMERA_REQUEST_CODE);
            Log.d(LoginActivity.LOGAPP,"permission asked from onResume later");
        }
        
//        requestPermission();

    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    //alternative way to request permissions with third party library

//    private void requestPermission() {
//        Dexter.withContext(Scanner.this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//                        codeScanner.startPreview();
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
//                        Toast.makeText(Scanner.this, "Permission is required for this scanner", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
//                        permissionToken.continuePermissionRequest();
//                    }
//                });
//    }

    public void onBackPressed() {
        Intent intent=new Intent(Scanner.this,MainActivity.class);
        finish();
        startActivity(intent);
    }
}