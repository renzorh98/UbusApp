package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    /*PARTES VISTA*/
    private EditText ETUsuarioL;
    private EditText ETPasswordL;
    private Button BLogin;
    private Button BRegistrar;
    private Button BRecCon;
    private LoginButton BLoginFacebook;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/
    private String Usuario;
    private String Password;
    /*FIN VARIABLES*/
    /*CONNEXION FIREBASE*/
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthState;
    private DatabaseReference mDatabase;
    private CallbackManager mCallbackManager;
    private AccessTokenTracker accessTokenTracker;
    /*FIN CONNEXION FIREBASE*/

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
        protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager=CallbackManager.Factory.create();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        ETUsuarioL=(EditText)findViewById(R.id.UsuarioL);
        ETPasswordL=(EditText)findViewById(R.id.PasswordL);
        BLogin=(Button)findViewById(R.id.Login);
        BRecCon=(Button)findViewById(R.id.Recuperar);
        BRecCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RecContrasena.class);
                startActivity(i);
                finish();
            }
        });

        BRegistrar=(Button)findViewById(R.id.Registrar);
        BLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Usuario=ETUsuarioL.getText().toString().trim();
                Password=ETPasswordL.getText().toString().trim();
                if(!Usuario.isEmpty() && !Password.isEmpty()){
                    LoginUsuario();
                }else{
                    Toast.makeText(MainActivity.this,"Complete los campos necesarios.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        BRegistrar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,RegistroActivity.class));
            }
        });

        mAuthState= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user!=null){
                    Toast.makeText(MainActivity.this,user.getDisplayName(),Toast.LENGTH_SHORT).show();
                }
            }
        };
        accessTokenTracker=new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken==null){
                    mAuth.signOut();
                }
            }
        };


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookToken(AccessToken accessToken) {
        AuthCredential credential= FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String id=mAuth.getCurrentUser().getUid();
                    mDatabase.child("Users").child(id).child("DatosPersonales").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                Map<String, Object> map = new HashMap<>();
                                map.put("DNI", "");
                                map.put("Movil", "");
                                String id=mAuth.getCurrentUser().getUid();
                                DatabaseReference datos = mDatabase.child("Users").child(id).child("DatosPersonales");
                                datos.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task2) {
                                        if (!task2.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "Error al momento de crear Datos.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    startActivity(new Intent(MainActivity.this,DatosActivity.class));
                    finish();
                }
            }
        });
    }

    private void LoginUsuario() {
        mAuth.signInWithEmailAndPassword(Usuario,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    startActivity(new Intent(MainActivity.this,DatosActivity.class));
                    finish();
                }else{
                    Toast.makeText(MainActivity.this,"Error en Email o Contraseña, compruebe los datos.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthState);
        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(MainActivity.this,DatosActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthState!=null){
            mAuth.removeAuthStateListener(mAuthState);
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(this, "This version is not Android 6 or later " + Build.VERSION.SDK_INT, Toast.LENGTH_LONG).show();

        } else {
            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.CAMERA);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA},REQUEST_CODE_ASK_PERMISSIONS);

                Toast.makeText(this, "Requiere permisos", Toast.LENGTH_LONG).show();

            }else if (hasWriteContactsPermission == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permiso de camara habilitado", Toast.LENGTH_LONG).show();
            }
            int permissionCheck = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionCheck != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);

                Toast.makeText(this, "Requiere permisos", Toast.LENGTH_LONG).show();
            }else if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permiso de localizacion habilitado", Toast.LENGTH_LONG).show();
            }

        }

        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(REQUEST_CODE_ASK_PERMISSIONS == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso activado! " + Build.VERSION.SDK_INT, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "No cuenta con permisos! " + Build.VERSION.SDK_INT, Toast.LENGTH_LONG).show();
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}