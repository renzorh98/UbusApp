package com.example.sec_login;

import androidx.annotation.NonNull;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.location.FusedLocationProviderClient;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class rutaInterfaz extends FragmentActivity implements OnMapReadyCallback {
    boolean control = false;

    private GoogleMap mMap;
    public double Latitude = -16.4040495;
    public double Longitude = -71.5740311;
    private FusedLocationProviderClient mFusedLocationClient;
    LatLng uPos;
    RadioGroup RGlistadoCompanyas;

    private List<Seleccion> ListaCompanyas=new ArrayList<Seleccion>();

    /*CONNEXION FIREBASE*/
    DatabaseReference mDatabase;
    /*FIN CONNEXION FIREBASE*/

    private ArrayList<Marker> tmpMarker = new ArrayList<Marker>();
    private ArrayList<Marker> Markers = new ArrayList<Marker>();

    private ArrayList<Polyline> tmpPolyline = new ArrayList<Polyline>();
    private ArrayList<Polyline> Polylines = new ArrayList<Polyline>();

    Marker ini;
    Marker end;

    CountDownTimer Count;
    private int RBquery = 0;
    private String RouteQuery = "null";
    private Polyline RouteVuelta;
    private Polyline RouteIda;
    ImageView imageView;


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
        setContentView(R.layout.activity_ruta_interfaz);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        RGlistadoCompanyas=(RadioGroup)findViewById(R.id.ListadoCompanias);

        Query mData = mDatabase.child("Users");
        mData.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                int i=0;
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    try {
                        int tipo = data.child("tipo").getValue(Integer.class);
                        if (tipo == 1) {
                            final String companya= data.child("user").getValue(String.class);
                            final RadioButton rb = new RadioButton(getApplicationContext());
                            rb.setText(companya);
                            rb.setId(i);
                            rb.setTextColor(Color.BLACK);
                            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if(isChecked==true){
                                        Log.e("Mensaje","select "+companya);
                                        RBquery = 1;
                                        RouteQuery = companya;
                                        onMapReady(mMap);

                                    }
                                    else{
                                        Log.e("Mensaje","unselect "+companya);

                                    }
                                }
                            });
                            RGlistadoCompanyas.addView(rb);

                            i++;
                        }
                    }catch (Exception e){continue;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        imageView = (ImageView)findViewById(R.id.center);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapCenter(mMap);
            }
        });



    }

    private void MapCenter(GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uPos,15));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(RBquery == 1){
            for(Polyline poly: Polylines){
                poly.remove();
            }
            try {
                RouteIda.remove();
                RouteVuelta.remove();
                ini.remove();
                end.remove();
            }catch (NullPointerException e){

            }

            Count.cancel();
            polyLineMap(mMap,RouteQuery,"Ida");
            polyLineMap(mMap,RouteQuery,"Vuelta");
            Polylines.clear();
            Polylines.addAll(tmpPolyline);

            //tmpPolyline.clear();
        }
        else {
            Log.e("msg2", "latitude "+Latitude+" Longitude "+Longitude);
            uPos = new LatLng(Latitude, Longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uPos,15));
        }
        /*Log.e("msg2", "latitude "+Latitude+" Longitude "+Longitude);
        uPos = new LatLng(Latitude, Longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uPos,15));*/

        markerMap(mMap);
    }

    private void markerMap(final GoogleMap gMap){

        // CONSULTA A LA BASE DE DATOS DE LA UBICACION DE LAS UNIDADES
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(Marker marker: Markers){
                    marker.remove();
                }
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    mDatabase.child("Users").child(snapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                            //Log.e("asdasd",""+dataSnapshot2.child("tipo").getValue(Integer.class));
                            if(2 == dataSnapshot2.child("tipo").getValue(Integer.class)){
                                try {
                                    BusPosition bp = dataSnapshot2.child("Ubicacion").getValue(BusPosition.class);
                                    Double lat = bp.getLatitud();
                                    Double lon = bp.getLongitud();
                                    //Log.e("LATLONG",""+lat+","+lon);
                                    MarkerOptions markeroptions = new MarkerOptions();
                                    markeroptions.position(new LatLng(lat,lon)).title(
                                            ""+dataSnapshot2.child("email").getValue()).snippet(
                                            "Unidad de transporte: "+dataSnapshot2.child("user").getValue()).icon(
                                            BitmapDescriptorFactory.fromResource(R.drawable.bus));
                                    tmpMarker.add(gMap.addMarker(markeroptions));
                                }
                                catch (NullPointerException e){
                                    Log.e("Exception", ""+e.toString());
                                }


                            }
                            Markers.clear();
                            Markers.addAll(tmpMarker);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
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
                //Toast.makeText(rutaInterfaz.this, "Actualizando", Toast.LENGTH_SHORT).show();
            }
        }.start();

    }

    private void polyLineMap(final GoogleMap gMap, final String ruta, final String ida_vuelta){
        mDatabase.child("Ruta").child(ruta).child(ida_vuelta).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<BusPosition> Ruta = new ArrayList<>();

                Ruta.addAll(StringToArray(dataSnapshot.getValue(String.class)));
                Latitude = Ruta.get((int)Ruta.size()/2).getLatitud();
                Longitude = Ruta.get((int)Ruta.size()/2).getLongitud();
                Log.e("msg1", "latitude "+Latitude+" Longitude "+Longitude);
                uPos = new LatLng(Latitude, Longitude);
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uPos,15));

                PolylineOptions polylineOptions = new PolylineOptions();
                for(int i = 0; i < Ruta.size()-1; i++){
                    if(ida_vuelta.equals("Ida")){
                        if(i==0){
                            MarkerOptions markeroptions = new MarkerOptions();
                            markeroptions.position(new LatLng(Ruta.get(0).getLatitud(),Ruta.get(0).getLongitud())).title(
                                    "Inicio Ida").snippet("Ruta: "+ruta);
                            ini = gMap.addMarker(markeroptions);
                        }
                        polylineOptions.add(new LatLng(Ruta.get(i).getLatitud(),Ruta.get(i).getLongitud()),
                                new LatLng(Ruta.get(i+1).getLatitud(),Ruta.get(i+1).getLongitud())).color(Color.RED).width(7);
                    }
                    else if(ida_vuelta.equals("Vuelta")){
                        if(i==Ruta.size()-2){
                            MarkerOptions markeroptions = new MarkerOptions();
                            markeroptions.position(new LatLng(Ruta.get(0).getLatitud(),Ruta.get(0).getLongitud())).title(
                                    "Inicio Vuelta").snippet("Ruta: "+ruta);
                            end = gMap.addMarker(markeroptions);
                        }
                        polylineOptions.add(new LatLng(Ruta.get(i).getLatitud(),Ruta.get(i).getLongitud()),
                                new LatLng(Ruta.get(i+1).getLatitud(),Ruta.get(i+1).getLongitud())).color(Color.BLUE).width(7);
                    }

                }
                if(ida_vuelta.equals("Ida")){
                    RouteIda = gMap.addPolyline(polylineOptions);
                }
                else{
                    RouteVuelta = gMap.addPolyline(polylineOptions);
                }

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



}