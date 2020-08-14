package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {
    /*PARTES VISTA*/
    private EditText ETUsuarioL;
    private EditText ETEmailL;
    private EditText ETPasswordL;
    private Button BRegistrar;
    private Switch SProveedor;
    private Switch SCompania;
    private EditText ETCompaniaL;
    private LinearLayout LLProveedores;
    private Spinner SPCompanias;
    private EditText ETContactoL;
    private EditText ETPlacaL;
    private EditText ETCodigoRegistroL;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/
    private String Usuario;
    private String Email;
    private String Password;
    private String NumContacto;
    private String Compania;
    private String Placa;
    private String Codigo;
    private boolean ES_Compania;
    private boolean ES_Proveedor;
    private String UsuarioCompanya;
    /*FIN VARIABLES*/
    /*CONNEXION FIREBASE*/
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    /*FIN CONNEXION FIREBASE*/
    private ArrayList<String> datosCompania = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        LLenar_Componentes_INCIALIZAR_CARACTERISTICAS();
        funciones_NO_BUTON();
        /*LLENAR DE DATA*/
        Query mData = mDatabase.child("Users");
        final List<String> companias = new ArrayList<String>();
        companias.add("Seleccione");
        mData.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    try {
                        int tipo = data.child("tipo").getValue(Integer.class);
                        if (tipo == 1) {
                            companias.add(data.child("user").getValue(String.class));
                        }
                    }catch (Exception e){continue;}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        SPCompanias.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,companias));
        /*END LLENAR DE DATA*/
        funciones_BUTON();
    }

    private void LLenar_Componentes_INCIALIZAR_CARACTERISTICAS(){
        ETUsuarioL = (EditText) findViewById(R.id.UsuarioL);
        ETEmailL = (EditText) findViewById(R.id.EmailL);
        ETPasswordL = (EditText) findViewById(R.id.PasswordL);
        ETCompaniaL=(EditText)findViewById(R.id.CompaniaL);
        ETContactoL = (EditText) findViewById(R.id.ContactoL);
        ETPlacaL = (EditText) findViewById(R.id.PlacaL);
        ETCodigoRegistroL = (EditText) findViewById(R.id.CodRegistroL);
        LLProveedores=(LinearLayout) findViewById(R.id.llProveedores);
        SProveedor=(Switch)findViewById(R.id.swProveedor);
        SCompania=(Switch)findViewById(R.id.swCompania);
        SPCompanias=(Spinner) findViewById(R.id.SpCompania);
        BRegistrar = (Button) findViewById(R.id.Registrar);
        /*CARACTERISTICAS*/
        LLProveedores.setVisibility(View.INVISIBLE);
        SPCompanias.setVisibility(View.INVISIBLE);
        ETCompaniaL.setVisibility(View.VISIBLE);
        ETPlacaL.setVisibility(View.INVISIBLE);
        ETContactoL.setVisibility(View.VISIBLE);
        ETCodigoRegistroL.setVisibility(View.INVISIBLE);
        ES_Compania=true;
        ES_Proveedor=false;
        /*END CARACTERISTICAS*/
    }
    private void funciones_NO_BUTON(){
        SCompania.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ETCompaniaL.setVisibility(View.INVISIBLE);
                    SPCompanias.setVisibility(View.VISIBLE);
                    ETContactoL.setVisibility(View.INVISIBLE);
                    ETPlacaL.setVisibility(View.VISIBLE);
                    ETCodigoRegistroL.setVisibility(View.VISIBLE);
                    ES_Compania=false;
                }else{
                    SPCompanias.setVisibility(View.INVISIBLE);
                    ETCompaniaL.setVisibility(View.VISIBLE);
                    ETPlacaL.setVisibility(View.INVISIBLE);
                    ETContactoL.setVisibility(View.VISIBLE);
                    ETCodigoRegistroL.setVisibility(View.INVISIBLE);
                    ES_Compania=true;
                }
            }
        });
        SProveedor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ETUsuarioL.setVisibility(View.INVISIBLE);
                    LLProveedores.setVisibility(View.VISIBLE);
                    ES_Proveedor=true;
                }else{
                    LLProveedores.setVisibility(View.INVISIBLE);
                    ETUsuarioL.setVisibility(View.VISIBLE);
                    ES_Proveedor=false;
                }
            }
        });
    }
    private void funciones_BUTON(){
        BRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Usuario = ETUsuarioL.getText().toString().trim();
                Email = ETEmailL.getText().toString().trim();
                Password = ETPasswordL.getText().toString().trim();
                NumContacto= ETContactoL.getText().toString().trim();
                Compania= ETCompaniaL.getText().toString().trim();
                Placa= ETPlacaL.getText().toString().trim();
                Codigo= ETCodigoRegistroL.getText().toString().trim();
                if ((   (!ES_Proveedor && !Usuario.isEmpty()) ||
                        (ES_Proveedor && ES_Compania && !Compania.isEmpty() && !NumContacto.isEmpty() ) ||
                        (ES_Proveedor && !ES_Compania && !Placa.isEmpty() && !(SPCompanias.getSelectedItem().toString().equals("Seleccione")) )
                    )
                    && !Email.isEmpty() && !Password.isEmpty()) {
                    if (Password.length() >= 6) {
                        if(!ES_Proveedor) {
                            registrarUsuario();
                        }else if(ES_Proveedor && ES_Compania){
                            if(NumContacto.length()==9)
                                Previo_Para_Companias();
                            else
                                Toast.makeText(RegistroActivity.this, "El Numero de Contacto debe contener 9 digitos.", Toast.LENGTH_SHORT).show();
                        }else{
                            Previo_Para_Placas();
                        }
                    } else {
                        Toast.makeText(RegistroActivity.this, "El password debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegistroActivity.this, "Complete los campos necesarios.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void Previo_Para_Companias(){
        Query mData = mDatabase.child("Users");
        mData.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                boolean bandera=false;
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    try {
                        int tipo = data.child("tipo").getValue(Integer.class);
                        String user=data.child("user").getValue(String.class);
                        if (tipo == 1 && user.equalsIgnoreCase(Compania)) {
                            bandera=true;
                            break;
                        }
                    }catch (Exception e){continue;}
                }
                if(!bandera){
                    registrarUsuario();
                }else{
                    Toast.makeText(RegistroActivity.this, "Ya existe esta Compañia.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void Previo_Para_Placas(){
        Query mData = mDatabase.child("Users");
        mData.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                boolean bandera=false;
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    try {
                        int tipo = data.child("tipo").getValue(Integer.class);
                        String user=data.child("user").getValue(String.class);
                        if (tipo == 2 && user.equalsIgnoreCase(Placa.toUpperCase())) {break;}
                    }catch (Exception e){continue;}
                }
                if(!bandera){
                    Query mData = mDatabase.child("Users");
                    mData.addListenerForSingleValueEvent(new ValueEventListener(){
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot){
                            boolean bandera=false;
                            for(DataSnapshot data: dataSnapshot.getChildren()){
                                try {
                                    int tipo = data.child("tipo").getValue(Integer.class);
                                    String user=data.child("user").getValue(String.class);
                                    if (tipo == 1 && user.equalsIgnoreCase(SPCompanias.getSelectedItem().toString())) {
                                        String userGuid = data.getKey();
                                        String uso = data.child("Codigos").child(Codigo).child("Uso").getValue(String.class);
                                        if (uso.equalsIgnoreCase("0")) {
                                            UsuarioCompanya=userGuid;
                                            registrarUsuario();
                                            bandera=true;
                                            break;
                                        }
                                    }
                                }catch (Exception e){continue;}
                            }
                            if(!bandera) Toast.makeText(RegistroActivity.this, "Codigo Invalido.", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }else{Toast.makeText(RegistroActivity.this, "Esta Placa ya esta Registrada.", Toast.LENGTH_SHORT).show();}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
    private void registrarUsuario() {
        mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> map = new HashMap<>();
                    if(!ES_Proveedor) {
                        map.put("user", Usuario);
                        map.put("tipo", 3);
                    }
                    if(ES_Proveedor && !ES_Compania) {
                        map.put("user", Placa);
                        map.put("tipo", 2);
                        map.put("Companya",SPCompanias.getSelectedItem().toString());
                    }
                    if(ES_Proveedor && ES_Compania) {
                        map.put("user", Compania);
                        map.put("tipo", 1);
                        map.put("NumContacto", NumContacto);
                    }
                    map.put("email", Email);
                    map.put("password", Password);
                    String id = mAuth.getCurrentUser().getUid();
                    mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if (task2.isSuccessful()) {
                                mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            String id = mAuth.getCurrentUser().getUid();
                                            DatabaseReference datos = mDatabase.child("Users").child(id).child("DatosPersonales");
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("DNI", "");
                                            map.put("Movil", "");
                                            datos.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task2) {
                                                    if (task2.isSuccessful()) {

                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("Uso", "1");
                                                        mDatabase.child("Users").child(UsuarioCompanya).child("Codigos").child(Codigo).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task2) {
                                                                Toast.makeText(RegistroActivity.this, "Registro Exitoso.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(RegistroActivity.this, "Error al momento de crear Datos.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                            startActivity(new Intent(RegistroActivity.this, DatosActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(RegistroActivity.this, "Error en Email o Contraseña, compruebe los datos.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(RegistroActivity.this, "Error al momento de crear el Registro.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegistroActivity.this, "No se pudo registrar este Usuario.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}