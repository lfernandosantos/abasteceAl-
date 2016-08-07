package lf.com.br.gasstations;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import lf.com.br.gasstations.dao.DAOPosto;
import lf.com.br.gasstations.model.DadosPosto;
import lf.com.br.gasstations.model.Data;
import lf.com.br.gasstations.model.Posto;
import lf.com.br.gasstations.model.PostoCatalog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Fernando on 17/04/2016.
 */
public class MapFragment extends SupportMapFragment {


    private List<Posto> listaPostos;
    private ProgressDialog dialog;
    private RadioGroup radioGroup;
    private ImageView btnOtherPlace;
    private EditText edtOtherPlace;
    private TextView txtNomePosto;
    private TextView txtEndPosto;
    private TextView txtBairroPosto;
    private ImageView btnLimpaCampoOther;
    List<Posto> listOtherPlace;
    LatLng otherPlace;
    RefreshGeoLocation refreshGeoLocation;

    private ListView listView ;
    private ImageButton btnSendComments;
    private Button btnNavigationMarker;
    private  EditText edtComments;
    private  ImageView imgBandeiraPosto;
    private  ImageView imgValGAS;
    private  ImageView imgValAlc;
    private  ImageView imgValGNV;
    private Button btnMyLocation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listaPostos = (List<Posto>) getActivity().getIntent().getSerializableExtra("posto");

        radioGroup = (RadioGroup) getActivity().findViewById(R.id.radioGroup);
        btnOtherPlace = (ImageView) getActivity().findViewById(R.id.btnGoOtherPlace);
        edtOtherPlace = (EditText) getActivity().findViewById(R.id.editTextOtherPlace);
        btnLimpaCampoOther = (ImageView) getActivity().findViewById(R.id.btnLimpaCampoOther);
        btnMyLocation = (Button) getActivity().findViewById(R.id.btnMyLocation);

        btnLimpaCampoOther.setVisibility(View.INVISIBLE);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId){
                    case R.id.radioAll: Log.i("RADIO","ALL");
                        pegaAll();
                        break;
                    case R.id.radioGAS: Log.i("RADIO","GAS");
                        pegaApenasGAS();
                        break;
                    case R.id.radioAlcool: Log.i("RADIO","ALCOOL");
                        pegaApenasAlcool();
                        break;
                    case R.id.radioGNV: Log.i("RADIO","GNV");
                        pegaApenasGNV();
                        break;
                }
            }
        });

        if (edtOtherPlace.getText().toString() != " " || edtOtherPlace.getText().toString() != "" ){
            btnOtherPlace.setEnabled(false);
        }else {
            btnOtherPlace.setEnabled(true);
        }

        edtOtherPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    btnOtherPlace.setEnabled(false);
                    listOtherPlace = null;
                    btnLimpaCampoOther.setVisibility(View.INVISIBLE);

                }else {
                    btnOtherPlace.setEnabled(true);
                    btnLimpaCampoOther.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnOtherPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pegaAll();
                searchOtherPlace();

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtOtherPlace.getWindowToken(), 0);
                edtOtherPlace.clearFocus();
                refreshGeoLocation.cancela();
            }
        });
        btnLimpaCampoOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtOtherPlace.setText("");
            }
        });

        edtOtherPlace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    pegaAll();
                    searchOtherPlace();
                    edtOtherPlace.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtOtherPlace.getWindowToken(), 0);
                }
                return true;
            }
        });

        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResume();
                edtOtherPlace.clearFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtOtherPlace.getWindowToken(), 0);
            }
        });

    }

    private void searchOtherPlace() {
        Localizador localizador = new Localizador(getActivity());
        otherPlace = localizador.getCoordenada(edtOtherPlace.getText().toString());
        centralizaOtherPlace(otherPlace);

        buscaJSONOtherPlace(otherPlace.latitude, otherPlace.longitude);
    }

    private void pegaAll() {
        getMap().clear();
        adcMarker(listaPostos);

        if (listOtherPlace != null) {
            adcMarker(listOtherPlace);
            centralizaOtherPlace(otherPlace);
        }


    }

    private void pegaApenasGNV() {
        List<Posto> listaGNV = new ArrayList<Posto>();
        for (Posto p : listaPostos){
            if (Double.valueOf(p.gnv) > 0.0){
                listaGNV.add(p);
            }
        }
        getMap().clear();
        adcMarker(listaGNV);

        if (listOtherPlace != null) {
            List<Posto> listaGNVO = new ArrayList<Posto>();

            for (Posto p : listOtherPlace) {
                if (Double.valueOf(p.gnv) > 0.0) {
                    listaGNVO.add(p);
                }
            }
            centralizaOtherPlace(otherPlace);
            adcMarker(listaGNVO);
        }

    }

    private void pegaApenasAlcool() {
        List<Posto> listaAlcool = new ArrayList<Posto>();
        for (Posto p : listaPostos){
            if (Double.valueOf(p.alcool) > 0.0){
                listaAlcool.add(p);
            }
        }
        getMap().clear();
        adcMarker(listaAlcool);

        if (listOtherPlace != null) {
            List<Posto> listaAlcoolO = new ArrayList<Posto>();

            for (Posto p : listOtherPlace) {
                if (Double.valueOf(p.alcool) > 0.0) {
                    listaAlcoolO.add(p);
                }
            }
            centralizaOtherPlace(otherPlace);
            adcMarker(listaAlcoolO);

        }
    }

    private void pegaApenasGAS() {
        getMap().clear();
        List<Posto> listaGAS = new ArrayList<Posto>();
        for (Posto p : listaPostos){
            if (Double.valueOf(p.gasolina) > 0.0){
                listaGAS.add(p);
            }
        }if (listOtherPlace != null) {
            List<Posto> listaGASO = new ArrayList<Posto>();

            for (Posto p : listOtherPlace) {
                if (Double.valueOf(p.gasolina) > 0.0) {
                    listaGASO.add(p);
                }
            }
            centralizaOtherPlace(otherPlace);
            adcMarker(listaGASO);
        }

        adcMarker(listaGAS);

    }


    @Override
    public void onResume() {
        super.onResume();

        dialog = ProgressDialog.show(getActivity(),"Aguarde...", "Atualizando localização.");
        adcMarker(listaPostos);
        refreshGeoLocation = new RefreshGeoLocation(getActivity(), this);

    }

    private void adcMarker(List<Posto> lPosto) {
        for (Posto p : lPosto) {

            LatLng coordenada = new LatLng(Double.valueOf(p.latitude), Double.valueOf(p.longitude));

            if (coordenada != null) {
                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(coordenada).title(p.nome).snippet(p.endereco).draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gasstation_dra));
                getMap().addMarker(markerOptions);

            }
        }

        getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

//                DadosPosto dadosPosto = new DadosPosto();
//                DAOPosto  dao = new DAOPosto(getActivity());
//                Integer idPosto = Integer.valueOf(marker.getSnippet());
//                dadosPosto = dao.getPosto(idPosto);
//                dao.close();

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.layout_detail_posto_beta, null);
                dialogBuilder.setView(dialogView);

                txtNomePosto = (TextView) dialogView.findViewById(R.id.txtNomePosto);
                txtEndPosto = (TextView) dialogView.findViewById(R.id.txtEndPosto);
                txtBairroPosto = (TextView) dialogView.findViewById(R.id.txtBairroPosto);
//                listView = (ListView) dialogView.findViewById(R.id.listViewPostoDetail);
//                btnSendComments = (ImageButton) dialogView.findViewById(R.id.btnSendComments);
                btnNavigationMarker = (Button) dialogView.findViewById(R.id.btnGMaps);
//                edtComments = (EditText) dialogView.findViewById(R.id.editTextComments);


                imgBandeiraPosto = (ImageView) dialogView.findViewById(R.id.imgBandeira);
                imgValGAS = (ImageView) dialogView.findViewById(R.id.imgViewPostoGAS);
                imgValAlc = (ImageView) dialogView.findViewById(R.id.imgViewPostoAlc);
                imgValGNV = (ImageView) dialogView.findViewById(R.id.imgViewPostoGNV);


//                if (dadosPosto.getComments()!=null){
//
//                    String listaTeste[] = {dadosPosto.getComments()};
//                    ArrayAdapter<String> adapter =
//                            new ArrayAdapter<String>(getActivity(),
//                                    android.R.layout.simple_list_item_1, listaTeste);
//                    listView.setAdapter(adapter);
//                }else {
//                    String listaTeste[] = {"Sem comentários...", "vai"};
//
//                    ArrayAdapter<String> adapter =
//                            new ArrayAdapter<String>(getActivity(),
//                                    android.R.layout.simple_list_item_1, listaTeste);
//                    listView.setAdapter(adapter);
//                }


                final AlertDialog alertDialog = dialogBuilder.create();


                buscaJSONForName(marker.getTitle(), alertDialog);

                alertDialog.show();

                btnNavigationMarker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LatLng markerLocation = marker.getPosition();

                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+markerLocation.latitude+","+markerLocation.longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                });

 /*               btnSendComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        DadosPosto dadosPostoAddComment = new DadosPosto();
//                        DAOPosto  dao = new DAOPosto(getActivity());
//
//                        dadosPostoAddComment.setId(Integer.valueOf(marker.getSnippet()));
//                        dadosPostoAddComment.setComments(edtComments.getText().toString());
//                        dao.addComments(dadosPostoAddComment);
//                        dao.close();
//
//                        alertDialog.dismiss();

                        Toast.makeText(getActivity(), "Comentário enviado!", Toast.LENGTH_SHORT).show();
                    }
                });
                */

                return false;
            }
        });
    }


    public void centraliza(LatLng local) {

        GoogleMap mapa = this.getMap();
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 15));
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


            return;
        }
        mapa.setMyLocationEnabled(true);

        if (local != null) {
            dialog.dismiss();
        }

    }

    public void centralizaOtherPlace(LatLng local) {

        GoogleMap mapa = this.getMap();
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 15));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(local);
        mapa.addMarker(markerOptions);

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
    }


    public void buscaJSONOtherPlace(Double lat, Double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PostoService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();

        listOtherPlace = new ArrayList<>();
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
                    adcMarker(listOtherPlace);
                }
            }

            @Override
            public void onFailure(Call<PostoCatalog> call, Throwable t) {

            }
        });

    }
    public void buscaJSONForName(String nomePosto, final AlertDialog alertDialog ) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PostoService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build();

        final List<Posto> itemPostoPorNome = new ArrayList<>();

        PostoService jsonPostos = retrofit.create(PostoService.class);

        Call<PostoCatalog> listaRequest = jsonPostos.listaPostosPorNome(nomePosto);
        listaRequest.enqueue(new Callback<PostoCatalog>() {
            @Override
            public void onResponse(Call<PostoCatalog> call, Response<PostoCatalog> response) {
                if (!response.isSuccessful()) {
                    Log.i("TAG", "ERRO: " + response.code());

                } else {
                    PostoCatalog catalog = response.body();

                    Data data = catalog.data;

                    for (Posto getPosto : data.postos) {
                        itemPostoPorNome.add(getPosto);
                    }

                    txtNomePosto.setText(itemPostoPorNome.get(0).nome);
                    txtEndPosto.setText("Endereço: " + itemPostoPorNome.get(0).endereco);
                    txtBairroPosto.setText(itemPostoPorNome.get(0).bairro);

                    if (itemPostoPorNome.get(0).icone != "") {
                        Glide.with(getActivity()).load(itemPostoPorNome.get(0).icone).into(imgBandeiraPosto);
                    }
                    if (Double.valueOf(itemPostoPorNome.get(0).gasolina) > 0.0){
                        Glide.with(getActivity()).load(R.drawable.ic_img_gas_on).into(imgValGAS);
                    }

                    if (Double.valueOf(itemPostoPorNome.get(0).alcool) > 0.0){
                        Glide.with(getActivity()).load(R.drawable.ic_ethanol_on).into(imgValAlc);
                    }

                    if (Double.valueOf(itemPostoPorNome.get(0).gnv) > 0.0){
                        Glide.with(getActivity()).load(R.drawable.ic_img_gnv_on).into(imgValGNV);
                    }
                }
            }

            @Override
            public void onFailure(Call<PostoCatalog> call, Throwable t) {

            }
        });

    }
}
