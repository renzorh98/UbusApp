package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.database.ValueEventListener;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class DatosActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    /*PARTES VISTA*/
    private Button BCerrarSesion;
    private TextView TVUsuario;
    private LoginButton BLoginFacebook;
    private ImageView qrCode;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/

    /*FIN VARIABLES*/
    /*CONNEXION FIREBASE*/
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthState;
    private CallbackManager mCallbackManager;
    private AccessTokenTracker accessTokenTracker;
    /*FIN CONNEXION FIREBASE*/
    /*QR Scanner*/
    private ZXingScannerView mScannerView;
    private Button BgenerateQR;
    private Button BscanQR;

    /*Fin QR Scanner*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);
        qrCode = (ImageView)findViewById(R.id.imageView);
        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        TVUsuario=(TextView)findViewById(R.id.Usuario);
        BLoginFacebook=findViewById(R.id.login_button);
        BLoginFacebook.setReadPermissions("email","public_profile");
        BLoginFacebook.setVisibility(View.INVISIBLE);
        getDatos();
        BCerrarSesion=(Button)findViewById(R.id.CerrarSesion);
        BCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                com.facebook.login.LoginManager.getInstance().logOut();
                startActivity(new Intent(DatosActivity.this,MainActivity.class));
                finish();

            }
        });
        BgenerateQR=(Button)findViewById(R.id.generate_QR);
        BgenerateQR.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(""+(String)TVUsuario.getText(), BarcodeFormat.QR_CODE, 500, 500);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    qrCode.getLayoutParams().height= 500;
                    qrCode.setImageBitmap(bitmap);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });

        BscanQR = (Button)findViewById(R.id.scan_QR);
        BscanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                mScannerView = new ZXingScannerView(DatosActivity.this);
                setContentView(mScannerView);
                mScannerView.setResultHandler(DatosActivity.this);
                mScannerView.startCamera();
            }
        });

        mAuthState= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
            }
        };
        accessTokenTracker=new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken==null){
                    mAuth.signOut();
                    startActivity(new Intent(DatosActivity.this,MainActivity.class));
                    finish();
                }
            }
        };
    }
    private void getDatos(){
        final String ID=mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.child("user").exists()){

                    String Usuario=dataSnapshot.child("user").getValue().toString();
                    TVUsuario.setText(Usuario);
                    
                }else{
                    FirebaseUser user= mAuth.getCurrentUser();
                    TVUsuario.setText(user.getDisplayName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void handleResult(Result result) {
        Log.v("HandleResult", result.getText());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resultado del scan");
        builder.setMessage(result.getText());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //mScannerView.resumeCameraPreview(this);


    }
    public void link_PayPal(View v){
        Toast.makeText(this, "Por implementar! ", Toast.LENGTH_LONG).show();
    }
}