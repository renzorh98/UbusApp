package com.example.sec_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Confirmacion extends AppCompatActivity {

    private TextView tvID, tvStatus, tvMonto, tvComp, tvTrans, tvPag;
    private ImageView qrCode;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacion);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        tvID = (TextView) findViewById(R.id.TextViewID);
        tvStatus = (TextView) findViewById(R.id.TextViewStatus);
        tvMonto = (TextView) findViewById(R.id.TextViewMonto);
        tvComp = (TextView) findViewById(R.id.TextViewCompania);
        tvTrans = (TextView) findViewById(R.id.TextViewTransportista);
        tvPag = (TextView) findViewById(R.id.TextViewPagado);
        qrCode = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        try {
            JSONObject jsonObject = new JSONObject(intent.getStringExtra("PaymentDetails"));
            verDetalles(jsonObject.getJSONObject("response"), intent.getStringExtra("PaymentAmount"), intent.getStringExtra("DetallesTrans"), intent.getStringExtra("User"));
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    private void verDetalles(JSONObject response, String paymentAmount, String detallesTrans, String User) {
        try {
            tvID.setText(response.getString("id"));
            tvStatus.setText(response.getString("state"));
            tvStatus.setTextColor(Color.GREEN);
        }catch (JSONException e){
            Toast.makeText(Confirmacion.this, "Error en los datos devueltos por Paypal.", Toast.LENGTH_SHORT).show();
            finish();
        }
        ArrayList<String> detString = new ArrayList<>();
        detString.addAll(StringToArray(detallesTrans));
        tvMonto.setText(paymentAmount);
        tvComp.setText(detString.get(0)+"-"+detString.get(4));
        tvTrans.setText(detString.get(1));
        tvPag.setText("Usuario "+User);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            String textcode = "ID: "+tvID.getText()+"\nStatus: "+tvStatus.getText()+"\nMonto: "+tvMonto.getText()+"\nCompañia: "+tvComp.getText()+"\nTransportista: "+detString.get(1)+"\nPagado por:"+tvPag.getText();
            BitMatrix bitMatrix = multiFormatWriter.encode(""+textcode, BarcodeFormat.QR_CODE, 800, 800);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            qrCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("Status", tvStatus.getText());
        map.put("Monto", tvMonto.getText());
        map.put("Companya", detString.get(0));
        map.put("IdaVuelta", detString.get(4));
        map.put("User_Trans", detString.get(1));
        map.put("User", User);

        DatabaseReference datos = mDatabase.child("Pagos").child((String) tvID.getText());
        datos.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task2) {
                if (!task2.isSuccessful()) {
                    Toast.makeText(Confirmacion.this, "Error al momento de crear Datos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private ArrayList<String> StringToArray(String detalles){
        String str[] = detalles.split("\n");
        ArrayList<String> ret = new ArrayList<>();
        ret.clear();
        for(String s: str){
            String aux[] = s.split(": ");
            ret.add(aux[1]);
        }
        return ret;
    }

}