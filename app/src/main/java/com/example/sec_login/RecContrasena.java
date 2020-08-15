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
import com.google.firebase.auth.FirebaseAuth;

public class RecContrasena extends AppCompatActivity {
    private EditText ETemail;
    private Button BTrecCon;
    private Button BTexitr;
    FirebaseAuth auth;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_contrasena);
        ETemail = findViewById(R.id.EmailRC);
        BTrecCon = findViewById(R.id.EnviarEmail);
        BTexitr = findViewById(R.id.Volver);

        auth = FirebaseAuth.getInstance();
        getRecuperar();
        BTexitr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecContrasena.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void getRecuperar() {
        BTrecCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = ETemail.getText().toString().trim();
                if(!email.isEmpty()){
                    getEnviarCorreo();

                }
                else {
                    Toast.makeText(RecContrasena.this, "Llene la casilla Email", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getEnviarCorreo() {
        auth.setLanguageCode("es");
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RecContrasena.this, "Revise su correo para restaurar contraseña", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(RecContrasena.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(RecContrasena.this, "No se pudo enviar el correo", Toast.LENGTH_LONG).show();

                }
            }
        });

    }
}