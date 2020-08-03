package com.example.sec_login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OperacionesActivity extends AppCompatActivity {
    private int WORK = 0;
    private String ID;
    private int tipoUsuario;
    /*PARTES VISTA*/
    private Button BActivarGPS;
    private Button BSeleccionRuta;
    private Button BVerRuta;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/
    private LocationManager locationManager;

    /*FIN VARIABLES*/
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    FusedLocationProviderClient mFusedLocationClient;
    private CountDownTimer Count;

    @Override
    protected void onResume() {
        if(WORK == 1){
            BActivarGPS.setText("GPS ON");
            BActivarGPS.setBackgroundColor(Color.GREEN);
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operaciones);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ID=mAuth.getCurrentUser().getUid();

        tipoUsuario = this.getIntent().getExtras().getInt("tipoUsuario");
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);

        BActivarGPS=findViewById(R.id.ActivarGPS);
        BSeleccionRuta=findViewById(R.id.SeleccionRuta);
        BActivarGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tipoUsuario == 2){
                    if(WORK == 0) {
                        BActivarGPS.setText("GPS ON");
                        BActivarGPS.setBackgroundColor(Color.GREEN);
                        WORK = 1;
                        startWorkGps(ID);
                    }
                    else if(WORK == 1){
                        BActivarGPS.setText("GPS OFF");
                        BActivarGPS.setBackgroundColor(Color.RED);
                        Count.cancel();
                        WORK = 0;
                    }

                }
                else if(tipoUsuario != 2){
                    Toast.makeText(OperacionesActivity.this, "Funcion solo para transportistas.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        BSeleccionRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OperacionesActivity.this,ListadoActivity.class);
                startActivity(intent);
            }
        });

        BVerRuta=findViewById(R.id.VerRuta);
        BVerRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OperacionesActivity.this, rutaInterfaz.class);
                startActivity(intent);
            }
        });

        if(tipoUsuario==1){//Compañia
            BActivarGPS.setVisibility(View.VISIBLE);
            BSeleccionRuta.setVisibility(View.INVISIBLE);
            BVerRuta.setVisibility(View.INVISIBLE);
        }else if(tipoUsuario==2){//Transportista
            BActivarGPS.setVisibility(View.VISIBLE);
            BSeleccionRuta.setVisibility(View.INVISIBLE);
            BVerRuta.setVisibility(View.INVISIBLE);
        }else{//Ciudadano
            BActivarGPS.setVisibility(View.VISIBLE);
            BSeleccionRuta.setVisibility(View.VISIBLE);
            BVerRuta.setVisibility(View.VISIBLE);
        }
    }

    private void startWorkGps(final String id) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    Log.e("",location.getLatitude()+","+location.getLongitude());
                    mDatabase.child("Users").child(id).child("Ubicacion").child("Latitud").setValue(location.getLatitude());
                    mDatabase.child("Users").child(id).child("Ubicacion").child("Longitud").setValue(location.getLongitude());


                    if (ActivityCompat.checkSelfPermission(OperacionesActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(OperacionesActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                }
            }
        });
        countDownTimer();
    }
    private void countDownTimer(){
        Count = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("seconds remaining: ", ""+millisUntilFinished/1000);

            }

            @Override
            public void onFinish() {
                startWorkGps(ID);
            }
        }.start();

    }
}