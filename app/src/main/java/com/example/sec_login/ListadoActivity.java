package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListadoActivity extends AppCompatActivity {
    /*PARTES VISTA*/
    private TableLayout TListadoCompanyas;
    private Button BBuscarCercanos;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/
    private int contadorChecks=0;
    private ArrayList<Seleccion> ListaCompanyas=new ArrayList<Seleccion>();
    /*FIN VARIABLES*/
    /*CONNEXION FIREBASE*/
    DatabaseReference mDatabase;
    /*FIN CONNEXION FIREBASE*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        TListadoCompanyas=(TableLayout)findViewById(R.id.ListadoCompanyas);
        TListadoCompanyas.setStretchAllColumns(true);
        Query mData = mDatabase.child("Users");
        BBuscarCercanos=(Button) findViewById(R.id.BuscarCercanos);
        BBuscarCercanos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*AQUI CREA A QUE ACTIVITY TE REDIRECCIONARA PARA EL MAPA*/
                try {
                    Intent intent = new Intent(ListadoActivity.this, BusesCercanos.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("ListaSeleccion",ListaCompanyas);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(ListadoActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }





                //startActivity(new Intent(DatosActivity.this,OperacionesActivity.class));
            }
        });
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
                                    if(contadorChecks<3) {
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
                                    }else if(contadorChecks==3 && isChecked){
                                        Toast.makeText(ListadoActivity.this,"Maximo 3 compañias.", Toast.LENGTH_SHORT).show();
                                    }
                                    else if(contadorChecks==3 && !isChecked){
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
    }
}