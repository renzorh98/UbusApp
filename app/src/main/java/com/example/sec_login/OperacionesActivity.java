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

public class OperacionesActivity extends AppCompatActivity {
    /*PARTES VISTA*/
    private Button BActivarGPS;
    private Button BSeleccionRuta;
    private Button BVerRuta;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/
    private LocationManager locationManager;
    private int tipoUsuario;
    /*FIN VARIABLES*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operaciones);
        tipoUsuario = this.getIntent().getExtras().getInt("tipoUsuario");
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);

        BActivarGPS=findViewById(R.id.AbrirOperaciones);
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
    }
}