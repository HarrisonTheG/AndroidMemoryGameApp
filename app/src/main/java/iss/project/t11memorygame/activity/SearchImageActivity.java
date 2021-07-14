package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import iss.project.t11memorygame.Adapter.SearchImageAdapter;
import iss.project.t11memorygame.Adapter.SearchImageAdapterV2;
import iss.project.t11memorygame.IClickGridItem;
import iss.project.t11memorygame.R;

import iss.project.t11memorygame.model.Image;
import iss.project.t11memorygame.service.BGMusicService;

import iss.project.t11memorygame.model.ChosenImage;
import iss.project.t11memorygame.utility.ImageFetchManager;

public class SearchImageActivity extends AppCompatActivity implements ServiceConnection, IClickGridItem {

    ArrayList<Integer> chosen = new ArrayList<>();
    ArrayList<Image> images = new ArrayList<>();;
    SearchImageAdapterV2 imageAdapter;
    private BGMusicService bgMusicService;

    EditText imgUrl;

    Button fetchButton;

    GridView gridView;

    private Boolean IS_MUSIC_ON;
    Thread bgThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_image);

        //get from home activity whether music is on
        Intent intent = getIntent();
        IS_MUSIC_ON = intent.getBooleanExtra("isMusicOn", false);
        bindMusicService(IS_MUSIC_ON);

        //clear images and populate
        gridView = findViewById(R.id.gridViewImagesToChoose);
        images.clear();
        populateImageDefault();

        imgUrl=(EditText) findViewById(R.id.ImgUrl);

        fetchButton=(Button) findViewById(R.id.Fetch);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                displayImg();
                System.out.println("done downloading");
                }
                catch(Exception e){

                }
            }
        });

    }

    //populate empty list or replace list back to original image after second search
    public void populateImageDefault(){

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.r15);

        if(images.isEmpty()){
        for(int i=0; i< 8 ; i++){
            Image image = new Image(bitmap, i);
            images.add(image);
        }} else {
            for(int i=0; i< 8 ; i++){
                Image image = new Image(bitmap, i);
                images.set(i, image);
            }
        }
        imageAdapter  = new SearchImageAdapterV2(this, images);
        gridView.setAdapter(imageAdapter);
    }

    //this method is to disable the SSL, in case of the SSLHandShakeException
    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    protected void displayImg() throws IOException {
        if(bgThread != null){
            bgThread.interrupt();
        }
        populateImageDefault();
        String url=imgUrl.getText().toString();

        bgThread = new Thread(new Runnable() {
            int bgCount = 0;
            @Override
            public void run() {
                if (Thread.interrupted()) {
                    return;
                }
                if (url != "" || url != null) {
                    trustEveryone();
                    ArrayList<String> imgUrls = ImageFetchManager.getImageSrc(url);

                    if (imgUrls == null) {
                        return;
                    }

                    for (String img : imgUrls) {

                        if (!"".equals(img) && (img.startsWith("http://") || img.startsWith("https://"))) {

                            //create file and directory to save
                            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            File destFile = new File(dir, bgCount + ".jpg");

                            //download image one by one
                            if (ImageFetchManager.downloadImage(img, destFile)) {
                                Image pic = new Image(BitmapFactory.decodeFile(destFile.getAbsolutePath()), bgCount);
                                images.set(bgCount, pic);
                                imageAdapter = new SearchImageAdapterV2(SearchImageActivity.this, images);


                                //run main UI thread activity
                                runOnUiThread(new Runnable() { //access the UI element to set images for example

                                    @Override
                                    public void run() {

                                        //imageAdapter.updateItemList(images);
                                        //gridView.invalidateViews();
                                        imageAdapter.notifyDataSetChanged();
                                        //gridView.setAdapter(imageAdapter);
                                        gridView.setAdapter(imageAdapter);

                                    }
                                });
                            }
                            bgCount++;
                            try{Thread.sleep(500);}
                            catch(Exception e){}
                        }

                    }

                }
                if (Thread.interrupted()) {
                    return;
                }
            }
        } );

            bgThread.start();
        }

    //receive onClick event from SearchImageAdapterV2
    @Override
    public void onClickItem(int positionClick) {
        chosen.add(positionClick);
        TextView matchestext = findViewById(R.id.matches);
        matchestext.setText(chosen.stream().count() + " Out of 6 images");

        if(chosen.stream().count() == 6){
            navigateToGame();
        }

        Toast.makeText(getApplicationContext(), "Tapped", Toast.LENGTH_SHORT).show();
    }

    //navigate to game activity
    public void navigateToGame() {
        Intent intent = new Intent(this, GameActivity.class);
        ArrayList<Integer> chosenImages = new ArrayList<Integer>();

        for (Integer i : chosen) {
            chosenImages.add(images.get(i).getPosID());
        }
        //send array of objects to next activity using bundle
//        Bundle args = new Bundle();
//        args.putSerializable("chosenImages",(Serializable) chosenImages);
//        intent.putExtra("bundle", args);
            intent.putExtra("chosenImages", chosenImages);


        intent.putExtra("isMusicOn", IS_MUSIC_ON);
        startActivity(intent);
    }



//-- Background Music Task

    protected void bindMusicService(boolean isMusicOn) {
        if (isMusicOn) {
            Intent music = new Intent(this, BGMusicService.class);
            bindService(music, this, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        BGMusicService.LocalBinder musicBinder = (BGMusicService.LocalBinder) binder;
        if (binder != null) {
            bgMusicService = musicBinder.getService();
            bgMusicService.resume();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    //Background music lifecycle and binding
    //life cycles
    @Override
    public void onPause(){
        super.onPause();
        if(bgMusicService!=null)
            bgMusicService.pause();
    }

    @Override
    public void onResume(){
        super.onResume();
        // restore
        if(bgMusicService!=null)
            bgMusicService.resume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(bgMusicService!=null)
            unbindService(this);
        images.clear();
    }

}