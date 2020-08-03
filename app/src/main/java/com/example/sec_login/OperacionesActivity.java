package com.example.sec_login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OperacionesActivity extends AppCompatActivity {
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
        try {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                BActivarGPS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
            } else {
                BActivarGPS.setVisibility(View.INVISIBLE);
            }
        }catch (Exception e){}
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
}