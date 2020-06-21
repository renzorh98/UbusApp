package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {
    /*PARTES VISTA*/
    private EditText ETUsuarioL;
    private EditText ETEmailL;
    private EditText ETPasswordL;
    private Button BRegistrar;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/
    private String Usuario;
    private String Email;
    private String Password;
    /*FIN VARIABLES*/
    /*CONNEXION FIREBASE*/
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    /*FIN CONNEXION FIREBASE*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ETUsuarioL = (EditText) findViewById(R.id.UsuarioL);
        ETEmailL = (EditText) findViewById(R.id.EmailL);
        ETPasswordL = (EditText) findViewById(R.id.PasswordL);
        BRegistrar = (Button) findViewById(R.id.Registrar);
        BRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Usuario = ETUsuarioL.getText().toString().trim();
                Email = ETEmailL.getText().toString().trim();
                Password = ETPasswordL.getText().toString().trim();
                if (!Usuario.isEmpty() && !Email.isEmpty() && !Password.isEmpty()) {
                    if (Password.length() >= 6) {
                        registrarUsuario();
                    } else {
                        Toast.makeText(RegistroActivity.this, "El password debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegistroActivity.this, "Complete los campos necesarios.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registrarUsuario() {
        mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("user", Usuario);
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
                                                        Toast.makeText(RegistroActivity.this, "Registro Exitoso.", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(RegistroActivity.this, "Error al momento de crear Datos.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                            startActivity(new Intent(RegistroActivity.this, DatosActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(RegistroActivity.this, "Error en Usuario o Contraseña, compruebe los datos.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(RegistroActivity.this, "Error al momento de crear Datos.", Toast.LENGTH_SHORT).show();
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