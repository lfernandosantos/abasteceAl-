package lf.com.br.gasstations.model;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import lf.com.br.gasstations.R;

/**
 * Created by Fernando on 13/08/2016.
 */
public class NoGPSInternet extends Activity {

    private ImageView noGPS;
    private ImageView noInternet;
    Thread timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_nogps_nointernet);

        noGPS = (ImageView) findViewById(R.id.imageViewNoGPS);
        noInternet = (ImageView) findViewById(R.id.imageViewNoInternet);

        noGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        noInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

       timer = new Thread(){
            public void run(){
                try{
                    sleep(6000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    finish();
                }
            }
        };

        timer.start();
    }
}
