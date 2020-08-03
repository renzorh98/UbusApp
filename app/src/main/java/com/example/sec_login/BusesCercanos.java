package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
    public double Latitude;
    public double Longitude;
    private FusedLocationProviderClient mFusedLocationClient;
    LatLng uPos;
    String str="";

    /*CONNEXION FIREBASE*/
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthState;
    private CallbackManager mCallbackManager;
    private AccessTokenTracker accessTokenTracker;
    /*FIN CONNEXION FIREBASE*/

    private List<Seleccion> ListaCompanyas=new ArrayList<Seleccion>();
    private ArrayList<Marker> tmpMarker = new ArrayList<Marker>();
    private ArrayList<Marker> Markers = new ArrayList<Marker>();

    private ArrayList<Polyline> tmpPolyline = new ArrayList<Polyline>();
    private ArrayList<Polyline> Polynes = new ArrayList<Polyline>();

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
                if(location != null){
                    Latitude = -16.387612;// location.getLatitude();
                    Longitude = -71.532984;// location.getLongitude();
                    if (ActivityCompat.checkSelfPermission(BusesCercanos.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(BusesCercanos.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    uPos = new LatLng(Latitude, Longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uPos,15));
                }
            }
        });
        markerMap(mMap);
    }

    private void markerMap(final GoogleMap gMap){
        // CONSULTA A LA BASE DE DATOS DE LA UBICACION DE LAS UNIDADES
        Query mData = mDatabase.child("Users");
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
                                    String placa=data.child("user").getValue().toString();
                                    if (s.Companya.equalsIgnoreCase(data.child("Companya").getValue().toString())
                                    && IdaVuelta==DataIdaVuelta) {
                                        s.buses.add(data.child("Ubicacion").getValue(BusPosition.class));
                                        s.Placas.add(data.child("user").getValue().toString());
                                        break;
                                    }
                                }catch (Exception e){}
                            }

                        }
                    }catch (Exception e){continue;}
                }
                for (Seleccion s:ListaCompanyas) {
                    polyLineMap(mMap,s.Companya,(s.Vuelta?"Vuelta":"Ida"));
                    //str+=s.ID+" "+s.Companya+" "+(s.Vuelta?"Vuelta":"Ida");
                }
                for (Seleccion s:ListaCompanyas) {
                    int i=0;
                    for (BusPosition bp:s.buses) {
                        Double lat = bp.getLatitud();
                        Double lon = bp.getLongitud();
                        MarkerOptions markeroptions = new MarkerOptions();
                        markeroptions.position(new LatLng(lat, lon)).title(
                                "" + s.Companya).snippet(
                                "Unidad de transporte: " + s.Placas.get(i)).icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.bus));
                        tmpMarker.add(gMap.addMarker(markeroptions));

                        Markers.clear();
                        Markers.addAll(tmpMarker);
                        i++;
                    }
                }
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
    private void polyLineMap(final GoogleMap gMap, String ruta, final String ida_vuelta){
        mDatabase.child("Ruta").child(ruta).child(ida_vuelta).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<BusPosition> Ruta = new ArrayList<>();
                Ruta.addAll(StringToArray(dataSnapshot.getValue(String.class)));
                PolylineOptions polylineOptions = new PolylineOptions();
                for(int i = 0; i < Ruta.size()-1; i++){

                    if(ida_vuelta.equals("Ida")){
                        //polylineOptions.add(new LatLng(Ruta.get(i).getLatitud(),Ruta.get(i).getLongitud()),
                        //        new LatLng(Ruta.get(i+1).getLatitud(),Ruta.get(i+1).getLongitud())).color(Color.RED).width(7);
                    }
                    else if(ida_vuelta.equals("Vuelta")){
                        //polylineOptions.add(new LatLng(Ruta.get(i).getLatitud(),Ruta.get(i).getLongitud()),
                        //        new LatLng(Ruta.get(i+1).getLatitud(),Ruta.get(i+1).getLongitud())).color(Color.BLUE).width(7);
                    }

                }
                //gMap.addPolyline(polylineOptions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<BusPosition> StringToArray(String Sruta){
        String str[] = Sruta.split(";");
        ArrayList<BusPosition> ret = new ArrayList<>();
        ret.clear();
        for(String s: str){
            BusPosition bp = new BusPosition();
            String aux[] = s.split(",");
            bp.setLatitud(Double.parseDouble(aux[0]));
            bp.setLongitud(Double.parseDouble(aux[1]));
            ret.add(bp);
        }
        return ret;
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