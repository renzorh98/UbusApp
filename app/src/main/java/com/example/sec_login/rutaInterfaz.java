package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
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


public class rutaInterfaz extends FragmentActivity implements OnMapReadyCallback {
    boolean control = false;

    private GoogleMap mMap;
    public double Latitude;
    public double Longitude;
    private FusedLocationProviderClient mFusedLocationClient;
    LatLng uPos;
    TableLayout TListadoCompanyas;
    private int contadorChecks=0;
    private List<Seleccion> ListaCompanyas=new ArrayList<Seleccion>();

    /*CONNEXION FIREBASE*/
    DatabaseReference mDatabase;
    /*FIN CONNEXION FIREBASE*/

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
        setContentView(R.layout.activity_ruta_interfaz);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        TListadoCompanyas=(TableLayout)findViewById(R.id.ListadoCompanias);
        TListadoCompanyas.setStretchAllColumns(true);
        Query mData = mDatabase.child("Users");
        mData.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                int i=0;
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    try {
                        int tipo = data.child("tipo").getValue(Integer.class);
                        if (tipo == 1) {
                            String companya= data.child("user").getValue(String.class);
                            TableRow row=new TableRow(getApplicationContext());
                            CheckBox cb=new CheckBox(getApplicationContext());
                            cb.setText(companya);
                            cb.setId(i);
                            cb.setTextColor(Color.BLACK);
                            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if(contadorChecks<1) {
                                        String companya=buttonView.getText().toString();
                                        if (isChecked){
                                            contadorChecks++;
                                            int ID=buttonView.getId();
                                            Seleccion s=new Seleccion();
                                            s.posicion=ListaCompanyas.size();
                                            s.ID=buttonView.getId();
                                            s.Companya=buttonView.getText().toString();
                                            ListaCompanyas.add(s);
                                            TableRow row= (TableRow) TListadoCompanyas.getChildAt(buttonView.getId());
                                            Switch sw=new Switch(getApplicationContext());
                                            sw.setText("Ida / Vuelta");
                                            sw.setId(buttonView.getId());
                                            sw.setTextColor(Color.BLACK);
                                            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    int ID=buttonView.getId();
                                                    for(int i=0;i<ListaCompanyas.size();i++){
                                                        if(ListaCompanyas.get(i).ID==ID){
                                                            if(isChecked){
                                                                ListaCompanyas.get(i).Vuelta=true;
                                                            }else{
                                                                ListaCompanyas.get(i).Vuelta=false;
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                            row.addView(sw);
                                        }else{
                                            contadorChecks--;
                                            for(int i=0;i<ListaCompanyas.size();i++){
                                                if(ListaCompanyas.get(i).Companya.equalsIgnoreCase(companya)){
                                                    ListaCompanyas.remove(i);
                                                }
                                            }
                                            TableRow row= (TableRow) TListadoCompanyas.getChildAt(buttonView.getId());
                                            row.removeViewAt(1);
                                        }
                                    }else if(contadorChecks==1 && isChecked){
                                        Toast.makeText(rutaInterfaz.this,"Maximo 1 compañia.", Toast.LENGTH_SHORT).show();
                                    }
                                    else if(contadorChecks==1 && !isChecked){
                                        contadorChecks--;
                                        String companya=buttonView.getText().toString();
                                        for(int i=0;i<ListaCompanyas.size();i++){
                                            if(ListaCompanyas.get(i).Companya.equalsIgnoreCase(companya)){
                                                ListaCompanyas.remove(i);
                                            }
                                        }
                                        TableRow row= (TableRow) TListadoCompanyas.getChildAt(buttonView.getId());
                                        row.removeViewAt(1);
                                    }
                                }
                            });

                            row.addView(cb);
                            TListadoCompanyas.addView(row);
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    Latitude = location.getLatitude();
                    Longitude = location.getLongitude();
                    //Log.e("",Latitude+","+Longitude);

                    if (ActivityCompat.checkSelfPermission(rutaInterfaz.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(rutaInterfaz.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    uPos = new LatLng(Latitude, Longitude);
                    //mMap.addMarker(new MarkerOptions().position(uPos).title("Posicion Actual"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uPos,15));
                }
            }
        });

        /*
       GRAFICA TODAS LAS UNIDADES DE TRANSPORTE QUE SE TIENEN EN EL FIREBASE
         */



        //EN UN FOR SE DEBERIA REALIZAR LA INVOCACION DE ESTE METODO EJM
        /*
        Clase seleccion{
            compania
            ruta
            ida_vuelta
        }
        List = {seleccion1, seleccion2, seleccion3}
        for(seleccion e: List):
            polyLineMap(mMap, e.compania, e.ruta, e.ida_vuelta)
         */

        //polyLineMap(mMap, "Cerro_Colorado_SAC", "RUTA_E_06A", "Ida");
        //polyLineMap(mMap, "Cerro_Colorado_SAC", "RUTA_E_06A", "Vuelta");
        polyLineMap(mMap, "DICA", "Ida");
        polyLineMap(mMap, "DICC", "Vuelta");

        markerMap(mMap);
        //Log.e("Msng", "termine");


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

    private void polyLineMap(final GoogleMap gMap, String ruta, final String ida_vuelta){
        mDatabase.child("Ruta").child(ruta).child(ida_vuelta).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<BusPosition> Ruta = new ArrayList<>();
                Ruta.addAll(StringToArray(dataSnapshot.getValue(String.class)));
                PolylineOptions polylineOptions = new PolylineOptions();
                for(int i = 0; i < Ruta.size()-1; i++){
                    if(ida_vuelta.equals("Ida")){
                        polylineOptions.add(new LatLng(Ruta.get(i).getLatitud(),Ruta.get(i).getLongitud()),
                                new LatLng(Ruta.get(i+1).getLatitud(),Ruta.get(i+1).getLongitud())).color(Color.RED).width(7);
                    }
                    else if(ida_vuelta.equals("Vuelta")){
                        polylineOptions.add(new LatLng(Ruta.get(i).getLatitud(),Ruta.get(i).getLongitud()),
                                new LatLng(Ruta.get(i+1).getLatitud(),Ruta.get(i+1).getLongitud())).color(Color.BLUE).width(7);
                    }

                }
                gMap.addPolyline(polylineOptions);
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