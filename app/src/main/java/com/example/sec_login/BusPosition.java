package com.example.sec_login;

public class BusPosition {
    private double Latitud;
    private double Longitud;
    private double DistanciaConCiudadano=Double.POSITIVE_INFINITY;
    private double DistanciaConLaRuta=Double.POSITIVE_INFINITY;

    BusPosition(){

    }

    public double getLatitud() {
        return Latitud;
    }

    public void setLatitud(double latitud) {
        this.Latitud = latitud;
    }

    public double getLongitud() {
        return Longitud;
    }

    public void setLongitud(double longitud) {
        this.Longitud = longitud;
    }
}
