package com.example.sec_login;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

public class Seleccion implements Parcelable {
    public int posicion;
    public int ID;
    public String Companya;
    public boolean Vuelta=false;
    public Seleccion(){

    }
    protected Seleccion(Parcel in) {
        posicion = in.readInt();
        ID = in.readInt();
        Companya = in.readString();
        Vuelta = in.readByte() != 0;
    }

    public static final Creator<Seleccion> CREATOR = new Creator<Seleccion>() {
        @Override
        public Seleccion createFromParcel(Parcel in) {
            return new Seleccion(in);
        }

        @Override
        public Seleccion[] newArray(int size) {
            return new Seleccion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(posicion);
        dest.writeInt(ID);
        dest.writeString(Companya);
        dest.writeBoolean(Vuelta);
    }
}
