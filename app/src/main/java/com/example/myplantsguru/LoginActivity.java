package com.example.myplantsguru;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    public static final String LOGAPP="MyPlantsGuru";

    private FirebaseAuth auth;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private TextView mForgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.login_email);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        mForgotPassword=findViewById(R.id.forget_pass);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        auth= FirebaseAuth.getInstance();
    }

    // Executed when Sign in button pressed
    public void signInExistingUser(View v)   {
        attemptLogin();

    }

    // Executed when Register button pressed
    public void registerNewUser(View v) {
        Intent intent = new Intent(this,RegisterActivity.class);
        finish();
        startActivity(intent);
    }

    //Executed when Forgot Password is pressed
    public void passwordReset(View v){
        String email=mEmailView.getText().toString();
        if (email!=null && isEmailValid(email)){
            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {     //Sends password reset email if email field is not empty
                @Override                                                                                   //or doesn't contain @
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(LOGAPP, "Email sent.");
                        emailSentDialog(LoginActivity.this.getString(R.string.password_reset));
                    }else {
                       emailSentDialog(LoginActivity.this.getString(R.string.password_reset_failed));    //alert the user that password reset process failed
                    }
                }
            });
        }else{
            Toast.makeText(this,R.string.invalid_email,Toast.LENGTH_LONG).show();
        }

    }

    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        return email.contains("@");
    }

    private void attemptLogin() {
        String email=mEmailView.getText().toString();
        String password=mPasswordView.getText().toString();
        if (email.equals("") || password.equals("")){
            return;
        }
        Toast.makeText(this,"Login in progress...",Toast.LENGTH_SHORT).show();

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(LOGAPP,"signInWithEmailAndPassword() onComplete "+task.isSuccessful());

                if (!task.isSuccessful()){
                    Log.d(LOGAPP,"Problem signing in: "+task.getException());
                    showErrorDialog("There was a problem signing in. Please check your email and password");
                }else{
                    Intent intent=new Intent(com.example.myplantsguru.LoginActivity.this,MainActivity.class);
//                    Log.d(MainActivity.LOGAPP,auth.getCurrentUser().getUid()+"\n"+auth.getCurrentUser().getEmail());
                    finish();
                    startActivity(intent);
                }
            }
        });


    }


    private void showErrorDialog(String message){
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void emailSentDialog(String message){
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

}
