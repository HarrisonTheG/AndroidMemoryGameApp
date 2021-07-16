package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
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
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private EditText imgUrl;

    private Button fetchButton;

    private GridView gridView;

    private ProgressBar bar;

    private Boolean IS_MUSIC_ON;
    Thread bgThread;
    static int count=0;
    private TextView valText;
    static boolean exit=false;

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
        valText= (TextView) findViewById(R.id.validationText);

        fetchButton=(Button) findViewById(R.id.Fetch);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(count>0){
                        exit=true;
                    }
                displayImg();
                    count++;
                System.out.println("done downloading");

                }
                catch(Exception e){

                }
            }
        });

        bar=(ProgressBar) findViewById(R.id.bar);

    }

    //populate empty list or replace list back to original image after second search
    public void populateImageDefault(){

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.default_image);

        if(images.isEmpty()){
        for(int i=0; i< 20 ; i++){
            Image image = new Image(bitmap, i, false);
            images.add(image);
        }} else {
            for(int i=0; i< 20 ; i++){
                Image image = new Image(bitmap, i, false);
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
            populateImageDefault();
        }

        String url=imgUrl.getText().toString();

        bgThread = new Thread(new Runnable() {
            int bgCount = 0;
            @Override
            public void run() {
                if (Thread.currentThread().interrupted()) {
                    populateImageDefault();
                    return;
                }
                //check if url is valid before downloading images
                if (!url.isEmpty() && url != null && url != "" && URLUtil.isValidUrl(url)) {
                    trustEveryone();
                    ArrayList<String> imgUrls = ImageFetchManager.getImageSrc(url);

                    if (imgUrls == null) {
                        return;
                    }

                    for (String img : imgUrls) {

                        if (!"".equals(img) && (img.startsWith("http://") || img.startsWith("https://"))) {

                            if(exit==true){
                                Thread.currentThread().interrupt();
                                exit=false;
                                return;
                            }
                            //if fully loaded, it will be as if its a fresh "fetch"
                            if(bgCount==20){
                                count=0;
                                return;
                            }

                            //create file and directory to save
                            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            File destFile = new File(dir, bgCount + ".jpg");

                            //download image one by one
                            if (ImageFetchManager.downloadImage(img, destFile)) {
                                Image pic = new Image(BitmapFactory.decodeFile(destFile.getAbsolutePath()), bgCount,true);
                                images.set(bgCount, pic);
                                imageAdapter = new SearchImageAdapterV2(SearchImageActivity.this, images);

                                //run main UI thread activity
                                runOnUiThread(new Runnable() { //access the UI element to set images for example

                                    @Override
                                    public void run() {

                                        //redisplay the gridview with updated data
                                        imageAdapter.notifyDataSetChanged();
                                        gridView.setAdapter(imageAdapter);
                                        //set the progress bar
                                        TextView downloadingStatus=(TextView)findViewById(R.id.downloadingStatus);
                                        downloadingStatus.setText("Downloading "+ bgCount +" of 20 images...");
                                    }
                                });
                            }
                            bgCount++;
                            bar.setProgress(bgCount);
                            try{Thread.sleep(500);}
                            catch(Exception e){}

                            //if its the 2nd time you click fetch, thread interrupt.
                            //as long as not fully loaded, the count will always be >0
                            //which means exit = true, which means interrupt thread and return

                        }

                    }
                }
                else {
                    //validation of download url failed
                    valText.setText("Invalid URL");
                    Thread.currentThread().interrupt();
                }
                if (Thread.currentThread().interrupted()) {
                    return;
                }
            }
        } );
        exit=false;
        count=0;
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