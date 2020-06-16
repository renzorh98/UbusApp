package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatosActivity extends AppCompatActivity {
    /*PARTES VISTA*/
    private Button BCerrarSesion;
    private TextView TVUsuario;
    private LoginButton BLoginFacebook;
    /*FIN PARTES VISTA*/
    /*VARIABLES*/

    /*FIN VARIABLES*/
    /*CONNEXION FIREBASE*/
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthState;
    private CallbackManager mCallbackManager;
    private AccessTokenTracker accessTokenTracker;
    /*FIN CONNEXION FIREBASE*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);

        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        TVUsuario=(TextView)findViewById(R.id.Usuario);
        BLoginFacebook=findViewById(R.id.login_button);
        BLoginFacebook.setReadPermissions("email","public_profile");
        BLoginFacebook.setVisibility(View.INVISIBLE);
        getDatos();
        BCerrarSesion=(Button)findViewById(R.id.CerrarSesion);
        BCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                com.facebook.login.LoginManager.getInstance().logOut();
                startActivity(new Intent(DatosActivity.this,MainActivity.class));
                finish();

            }
        });
        mAuthState= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
            }
        };
        accessTokenTracker=new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken==null){
                    mAuth.signOut();
                    startActivity(new Intent(DatosActivity.this,MainActivity.class));
                    finish();
                }
            }
        };
    }
    private void getDatos(){
        final String ID=mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.child("user").exists()){

                    String Usuario=dataSnapshot.child("user").getValue().toString();
                    TVUsuario.setText(Usuario);
                    
                }else{
                    FirebaseUser user= mAuth.getCurrentUser();
                    TVUsuario.setText(user.getDisplayName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}