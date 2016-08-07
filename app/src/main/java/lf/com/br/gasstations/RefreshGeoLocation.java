package lf.com.br.gasstations;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Fernando on 17/04/2016.
 */
public class RefreshGeoLocation implements LocationListener {

    public GoogleApiClient client;
    private MapFragment mapa;
    private Context context;
    public LatLng local;

    public RefreshGeoLocation(Context context, MapFragment mapa) {
        this.context = context;
        this.mapa = mapa;


        Configurador config = new Configurador(this);
        this.client = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(config)
                .build();
        this.client.connect();


    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        local = new LatLng(latitude, longitude);
        this.mapa.centraliza(local);

    }

    public void inicia(LocationRequest request) {

        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
       LocationServices.FusedLocationApi.
                requestLocationUpdates(client, request, this);

    }

    public void cancela(){
        LocationServices.FusedLocationApi.
                removeLocationUpdates(client, this);
        this.client.disconnect();
    }


}
