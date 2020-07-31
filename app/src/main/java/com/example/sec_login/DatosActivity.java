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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class DatosActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    /*PARTES VISTA*/
    private Button BCerrarSesion;
    private Button BGenerarCodigo;
    private Button BAbrirOperaciones;
    private TextView TVUsuario;
    private TextView TVCodigo;
    private LoginButton BLoginFacebook;
    private ImageView qrCode;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/
    private String ID;
    private int tipoUsuario;
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

    private String bdemail;
    private String bdtipo;
    /*Fin QR Scanner*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);
        qrCode=(ImageView)findViewById(R.id.imageView);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        TVUsuario=(TextView)findViewById(R.id.Usuario);
        BAbrirOperaciones=findViewById(R.id.AbrirOperaciones);
        BAbrirOperaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DatosActivity.this, OperacionesActivity.class);
                intent.putExtra("tipoUsuario", tipoUsuario);
                startActivity(intent);
                //startActivity(new Intent(DatosActivity.this,OperacionesActivity.class));
            }
        });
        BLoginFacebook=findViewById(R.id.login_button);
        BLoginFacebook.setReadPermissions("email","public_profile");
        BLoginFacebook.setVisibility(View.INVISIBLE);
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
                String textcode = "user: "+(String)TVUsuario.getText()+"\nemail: "+bdemail+"\ntipo: "+bdtipo;


                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(""+textcode, BarcodeFormat.QR_CODE, 500, 500);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    qrCode.getLayoutParams().height= 500;
                    qrCode.setImageBitmap(bitmap);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
        TVCodigo=findViewById(R.id.MostrarCodigo);
        BGenerarCodigo=findViewById(R.id.GenCodTransportistas);
        BGenerarCodigo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String codigo = UUID.randomUUID().toString();
                Map<String, Object> map = new HashMap<>();
                map.put("Uso", "0");
                mDatabase.child("Users").child(ID).child("Codigos").child(codigo).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task2) {
                        if (task2.isSuccessful()) {
                            Toast.makeText(DatosActivity.this, "Generación Exitosa", Toast.LENGTH_SHORT).show();
                            TVCodigo.setText(codigo);
                        } else {
                            Toast.makeText(DatosActivity.this, "Error al momento de Generar Codigo.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        BscanQR = (Button)findViewById(R.id.scan_QR);
        BscanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
//                Intent intent = new Intent();
//                mScannerView = new ZXingScannerView(DatosActivity.this);
//                setContentView(mScannerView);
//                mScannerView.setResultHandler(DatosActivity.this);
//                mScannerView.startCamera();
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
        getDatos();
    }

    private void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents() != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(result.getContents());
                builder.setTitle("Scanning Result");
                builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Realizar pago
                        scanCode();

                    }
                }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("Pay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("Pago", "Realizar pago Paypal");
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                Toast.makeText(this, "Scan Error", Toast.LENGTH_LONG).show();
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getDatos(){
        BscanQR.setVisibility(View.INVISIBLE);
        BgenerateQR.setVisibility(View.INVISIBLE);
        BGenerarCodigo.setVisibility(View.INVISIBLE);
        ID=mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.child("user").exists()){
                    try {
                        int tipo = dataSnapshot.child("tipo").getValue(Integer.class);
                        tipoUsuario=tipo;
                        String Usuario = dataSnapshot.child("user").getValue().toString();
                        TVUsuario.setText(Usuario);
                        bdemail=dataSnapshot.child("email").getValue().toString();
                        bdtipo=dataSnapshot.child("tipo").getValue().toString();

                        if (tipo == 1) {
                            bdtipo="Compania";
                            BscanQR.setVisibility(View.INVISIBLE);
                            BgenerateQR.setVisibility(View.INVISIBLE);
                            BGenerarCodigo.setVisibility(View.VISIBLE);
                        } else if (tipo == 2) {
                            bdtipo="Transportista";
                            BscanQR.setVisibility(View.INVISIBLE);
                            BgenerateQR.setVisibility(View.VISIBLE);
                            BGenerarCodigo.setVisibility(View.INVISIBLE);
                        } else {
                            bdtipo="Ciudadano";
                            BscanQR.setVisibility(View.VISIBLE);
                            BgenerateQR.setVisibility(View.INVISIBLE);
                            BGenerarCodigo.setVisibility(View.INVISIBLE);
                        }

                    }catch (Exception e){
                        BscanQR.setVisibility(View.INVISIBLE);
                        BgenerateQR.setVisibility(View.VISIBLE);
                        BGenerarCodigo.setVisibility(View.INVISIBLE);
                    }
                }else{
                    FirebaseUser user= mAuth.getCurrentUser();
                    TVUsuario.setText(user.getDisplayName());
                    BscanQR.setVisibility(View.INVISIBLE);
                    BgenerateQR.setVisibility(View.VISIBLE);
                    BGenerarCodigo.setVisibility(View.INVISIBLE);
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