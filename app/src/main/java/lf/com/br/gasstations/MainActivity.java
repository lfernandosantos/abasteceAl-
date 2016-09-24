package lf.com.br.gasstations;


import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.fitness.data.RawDataPoint;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import lf.com.br.gasstations.model.Data;

import lf.com.br.gasstations.model.NoGPSInternet;
import lf.com.br.gasstations.model.Posto;
import lf.com.br.gasstations.model.PostoCatalog;
import me.drakeet.materialdialog.MaterialDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity implements LocationListener, GoogleApiClient.ConnectionCallbacks {


    public static final int REQUEST_PERMISSIONS_CODE = 128;
    private TextView textView;
    private List<Posto> postoList;
    private GoogleApiClient client;
    private Boolean conectado;
    private Boolean gpsConectado;
    private ImageView imageView;
    private ImageView imageViewScreen;
    private ImageView imageViewLogo;
    MaterialDialog mMaterialDialog;
    Thread timerThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsConectado = getStatusGPS();
        conectado = getStatusInternet();

        findViews();



        if (conectado == false){

            Glide.with(MainActivity.this).load(R.drawable.ic_signal_wifi_off).into(imageViewScreen);
            textView.setText("Sem Conexão!");
            imageViewScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
//            new AlertDialog.Builder(this).setTitle("Sem Conexão!")
//                    .setMessage("Você precisa ter acesso a internet para utilizar o aplicativo.")
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//                    }).setCancelable(false).show();

        }else {

            if (gpsConectado == false) {

                Glide.with(MainActivity.this).load(R.drawable.ic_location_off).into(imageViewScreen);
                textView.setText("GPS Desabilitado!");
                imageViewScreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

//                new AlertDialog.Builder(this).setTitle("GPS Desabilitado!")
//                        .setMessage("Você precisa estar com o GPS ativo para utilizar o aplicativo.")
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                finish();
//                            }
//                        }).setCancelable(false).show();
            }else {

                callAccessLocation();

            }
        }
    }

    private void rodaSplash() {

        postoList = new ArrayList<>();

        //Glide.with(MainActivity.this).load(R.drawable.screen).into(imageViewScreen);

        Glide.with(MainActivity.this).
                load(R.drawable.ic_new_launcher).into(imageView);

        Glide.with(MainActivity.this)
                .load(R.drawable.logo_abastece_ai).into(imageViewLogo);


        timerThread = new Thread(){
            public void run(){
                try{
                    sleep(3000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("posto", (Serializable) postoList);
                    startActivity(intent);
                }
            }
        };

        callConnection();
    }

    public void callAccessLocation() {
        Log.i("PER", "callAccessLocation()");

        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){

            if( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.ACCESS_FINE_LOCATION ) ){
                callDialog( "É preciso habilitar as permissões para utilizar o App.", new String[] {Manifest.permission.ACCESS_FINE_LOCATION} );
            }
            else{
                ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE );
            }
        }
        else{
            rodaSplash();
        }
    }

    private void callDialog( String message, final String[] permissions ){
        mMaterialDialog = new MaterialDialog(this)
                .setTitle("Permission")
                .setMessage( message )
                .setPositiveButton(" Ok ", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSIONS_CODE);
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton(" Cancel ", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                        finish();
                    }
                });
        mMaterialDialog.show();
    }

    private Boolean getStatusInternet() {
        Boolean aBoolean;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected()){

            aBoolean = true;

        }else {
            aBoolean = false;
        }
        return aBoolean;
    }

    private Boolean getStatusGPS() {
        Boolean aBoolean;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            aBoolean = true;
        }else {
            aBoolean = false;
        }
        return aBoolean;
    }

    private void buscaJSON(Double lat, Double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PostoService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();


        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(lon);

        PostoService jsonPostos = retrofit.create(PostoService.class);

        Call<PostoCatalog> listaRequest = jsonPostos.listaPostos(latitude, longitude);
        listaRequest.enqueue(new Callback<PostoCatalog>() {
            @Override
            public void onResponse(Call<PostoCatalog> call, Response<PostoCatalog> response) {
                if (!response.isSuccessful()) {
                    Log.i("TAG", "ERRO: " + response.code());

                } else {
                    PostoCatalog catalog = response.body();

                    Data data = catalog.data;

                    for (Posto getPosto : data.postos) {

                        postoList.add(getPosto);
                    }

                    timerThread.start();
                }
            }

            @Override
            public void onFailure(Call<PostoCatalog> call, Throwable t) {

            }
        });
    }

    public List<Posto> buscaJSONOtherPlace(Double lat, Double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PostoService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();

        final List<Posto> listOtherPlace = new ArrayList<>();
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(lon);

        PostoService jsonPostos = retrofit.create(PostoService.class);

        Call<PostoCatalog> listaRequest = jsonPostos.listaPostos(latitude, longitude);
        listaRequest.enqueue(new Callback<PostoCatalog>() {
            @Override
            public void onResponse(Call<PostoCatalog> call, Response<PostoCatalog> response) {
                if (!response.isSuccessful()) {
                    Log.i("TAG", "ERRO: " + response.code());

                } else {
                    PostoCatalog catalog = response.body();

                    Data data = catalog.data;

                    for (Posto getPosto : data.postos) {
                        listOtherPlace.add(getPosto);
                    }
                }
            }

            @Override
            public void onFailure(Call<PostoCatalog> call, Throwable t) {

            }
        });

        Log.i("OTHER", String.valueOf(listOtherPlace.size()));
        return listOtherPlace;
    }

    private void findViews() {
        textView = (TextView) findViewById(R.id.textViewSplash);
        imageView = (ImageView) findViewById(R.id.imageViewSplash);
        imageViewScreen = (ImageView) findViewById(R.id.screen);
        imageViewLogo = (ImageView) findViewById(R.id.imageViewLogo);
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    public void iniciar(LocationRequest request) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    public void cancela() {
        LocationServices.FusedLocationApi.
                removeLocationUpdates(client, this);
        this.client.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location l = LocationServices.FusedLocationApi.getLastLocation(client);

        if (l != null) {
            buscaJSON(l.getLatitude(), l.getLongitude());
        }else {

            Intent intentNoGpsInternet = new Intent(MainActivity.this, NoGPSInternet.class);
            startActivity(intentNoGpsInternet);


        }
        client.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    private synchronized void callConnection(){
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }


}
