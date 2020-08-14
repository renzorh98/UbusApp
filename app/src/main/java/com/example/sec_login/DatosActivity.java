package com.example.sec_login;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sec_login.Config.Config;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class DatosActivity extends AppCompatActivity{
    /*PARTES VISTA*/
    private Button BCerrarSesion;
    private Button BGenerarCodigo;
    private Button BAbrirOperaciones;
    private TextView TVUsuario;
    private TextView TVCodigo;
    private TextView TVTipo;
    private ImageView qrCode;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/
    private String ID;
    private int tipoUsuario;
    /*FIN VARIABLES*/
    /*CONNEXION FIREBASE*/
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    /*FIN CONNEXION FIREBASE*/
    /*QR Scanner*/
    private Button BgenerateQR;
    private Button BscanQR;

    private String textcode;
    private String bdemail;
    private String bdtipo;
    private String bdcompanya;
    private String bdruta;
    private String monto = "0.30";
    /*Fin QR Scanner*/
    private static final int PAYPAL_REQUEST_CODE = 7171;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);
    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);
        qrCode=(ImageView)findViewById(R.id.imageView);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        TVUsuario=(TextView)findViewById(R.id.Usuario);
        TVTipo=(TextView)findViewById(R.id.TextViewTipo);
        BAbrirOperaciones=findViewById(R.id.AbrirOperaciones);
        BAbrirOperaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DatosActivity.this, OperacionesActivity.class);
                intent.putExtra("tipoUsuario", tipoUsuario);
                startActivity(intent);
            }
        });

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
                if(qrCode.getLayoutParams().height != 0){
                    qrCode.setImageBitmap(null);
                    qrCode.getLayoutParams().height= 0;
                }
                else {
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    textcode = "Compania: "+bdcompanya+"\nUser: "+(String)TVUsuario.getText()+"\nTipo: "+bdtipo+"\nEmail: "+bdemail+"\nRuta: "+bdruta;


                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(""+textcode, BarcodeFormat.QR_CODE, 500, 500);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        qrCode.getLayoutParams().height= 500;
                        qrCode.setImageBitmap(bitmap);

                    } catch (WriterException e) {
                        Log.e("Mensajegenqr", e.toString());

                    }
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
            }
        });


        getDatos();
        Intent intent = new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
    }

    private void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }
    private void procesarPago(){
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(String.valueOf(monto)),
                "USD", "Pago viaje", PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(intent,PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                PaymentConfirmation confirmation = null;
                try {
                    confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                }
                catch (NullPointerException e){
                    Log.e("Mensajenullexe", ""+e.toString());
                }
                if (confirmation != null){
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        Log.d("Activity",""+paymentDetails);
                        startActivity(new Intent(this,Confirmacion.class).putExtra("PaymentDetails",paymentDetails).putExtra("PaymentAmount", monto).putExtra("DetallesTrans",textcode).putExtra("User", TVUsuario.getText()));

                    }catch (JSONException e){
                        Log.e("Mensajejsonexec", e.toString());

                    }
                }
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Cancelada",Toast.LENGTH_SHORT).show();

            }
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toast.makeText(this, "Invalida", Toast.LENGTH_SHORT).show();
        }

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents() != null){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                textcode = result.getContents();
                builder.setMessage(result.getContents());
                builder.setTitle("Scanning Result");
                builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scanCode();

                    }
                }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                if (tipoUsuario == 3){
                    builder.setNeutralButton("Pay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            procesarPago();
                        }
                    });
                }
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
                        try {
                            bdcompanya = dataSnapshot.child("Companya").getValue().toString();

                            if(dataSnapshot.child("Ubicacion").child("IdaVuelta").getValue(Integer.class) == 0){
                                bdruta = "Ida";
                            }else{
                                bdruta = "Vuelta";
                            }
                        }
                        catch(Exception e){
                            Log.e("Mensaje", e.toString());
                        }

                        if (tipo == 1) {
                            bdtipo="Compania";
                            BscanQR.setVisibility(View.INVISIBLE);
                            BgenerateQR.setVisibility(View.INVISIBLE);
                            BGenerarCodigo.setVisibility(View.VISIBLE);
                        } else if (tipo == 2) {
                            bdtipo="Transportista";
                            BscanQR.setVisibility(View.VISIBLE);
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
                TVTipo.setText(bdtipo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}