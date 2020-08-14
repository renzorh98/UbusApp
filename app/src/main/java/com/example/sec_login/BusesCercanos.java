package com.example.sec_login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class    BusesCercanos extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    boolean control = false;
    private double Lati;
    private double Long;
    private FusedLocationProviderClient mFusedLocationClient;
    LatLng uPos;
    String str="";

    /*CONNEXION FIREBASE*/
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    /*FIN CONNEXION FIREBASE*/
    private  Location locato;
    private GoogleMap goMap;
    private List<Seleccion> ListaCompanyas=new ArrayList<Seleccion>();
    private ArrayList<Marker> tmpMarker = new ArrayList<Marker>();
    private ArrayList<Marker> Markers = new ArrayList<Marker>();

    CountDownTimer Count;
    @Override
    public void onBackPressed() {
        Count.cancel();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        if(control == true) {
            countDownTimer();
            control = false;
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        control = true;
        Count.cancel();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Count.cancel();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buses_cercanos);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();
        ListaCompanyas = bundle.getParcelableArrayList("ListaSeleccion");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                locato=location;
                CalculoDeUbicaciones(location);
            }
        });
        markerMap(mMap);
    }
    private void CalculoDeUbicaciones(Location location){

        if(location != null){
            Lati = location.getLatitude();
            Long = location.getLongitude();
            if (ActivityCompat.checkSelfPermission(BusesCercanos.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(BusesCercanos.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            uPos = new LatLng(Lati, Long);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uPos,15));
            for (Seleccion s:ListaCompanyas) {
                for (BusPosition bp:s.buses) {
                    Double lat = bp.getLatitud();
                    Double lon = bp.getLongitud();
                    bp.DistanciaConCiudadano=distanciaCoord(Lati,Long,lat,lon);
                    if(s.CercanosCiudadano.size()<3){
                        if(s.CercanosCiudadano.size()==1){
                            if(s.CercanosCiudadano.get(0).DistanciaConCiudadano>bp.DistanciaConCiudadano){
                                s.CercanosCiudadano.add(0,bp);
                            }else{
                                s.CercanosCiudadano.add(bp);
                            }
                        }else {
                            s.CercanosCiudadano.add(bp);
                        }
                    }else{
                        if(s.CercanosCiudadano.get(0).DistanciaConCiudadano>bp.DistanciaConCiudadano){
                            s.CercanosCiudadano.add(0,bp);
                            s.CercanosCiudadano.remove(2);
                        }else if(s.CercanosCiudadano.get(1).DistanciaConCiudadano>bp.DistanciaConCiudadano){
                            s.CercanosCiudadano.add(1,bp);
                            s.CercanosCiudadano.remove(2);
                        }
                    }
                }
            }
            for (Seleccion s:ListaCompanyas) {
                int i=0;
                for (BusPosition bp:s.CercanosCiudadano) {
                    Double lat = bp.getLatitud();
                    Double lon = bp.getLongitud();
                    MarkerOptions markeroptions = new MarkerOptions();
                    markeroptions.position(new LatLng(lat, lon)).title(
                            "" + s.Companya).snippet(
                            "Unidad de transporte: " + s.Placas.get(i)).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.bus));
                    tmpMarker.add(goMap.addMarker(markeroptions));

                    Markers.clear();
                    Markers.addAll(tmpMarker);
                    i++;
                }
            }
        }
    }
    private void markerMap(final GoogleMap gMap){
        // CONSULTA A LA BASE DE DATOS DE LA UBICACION DE LAS UNIDADES
        Query mData = mDatabase.child("Users");
        goMap=gMap;
        mData.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                for(Marker marker: Markers){
                    marker.remove();
                }
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    try {
                        if(2 == data.child("tipo").getValue(Integer.class)){
                            boolean bandera=true;
                            for (Seleccion s:ListaCompanyas) {
                                try {
                                    int IdaVuelta=s.Vuelta?1:0;
                                    int DataIdaVuelta=data.child("Ubicacion").child("IdaVuelta").getValue(Integer.class);

                                    if (s.Companya.equalsIgnoreCase(data.child("Companya").getValue().toString())
                                    && IdaVuelta==DataIdaVuelta) {
                                        s.buses.add(data.child("Ubicacion").getValue(BusPosition.class));
                                        s.Placas.add(data.child("user").getValue().toString());
                                        break;
                                    }
                                }catch (Exception e){
                                    Log.e("Mensaje", e.toString());
                                }
                            }

                        }
                    }catch (Exception e){continue;}
                }

                CalculoDeUbicaciones(locato);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        countDownTimer();
    }
    private void countDownTimer(){
        Count = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("seconds remaining: ", ""+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
                markerMap(mMap);
            }
        }.start();
    }

    public static double distanciaCoord(double lat1, double lng1, double lat2, double lng2) {
        //double radioTierra = 3958.75;//en millas
        double radioTierra = 6371;//en kilómetros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va1 = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        double distancia = radioTierra * va2;

        return distancia;
    }
}