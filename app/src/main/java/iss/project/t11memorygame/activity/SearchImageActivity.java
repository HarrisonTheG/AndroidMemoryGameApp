package iss.project.t11memorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import iss.project.t11memorygame.Adapter.SearchImageAdapter;
import iss.project.t11memorygame.R;

import iss.project.t11memorygame.service.BGMusicService;

import iss.project.t11memorygame.model.ChosenImage;
public class SearchImageActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    ArrayList<Integer> chosen = new ArrayList<>();

    private BGMusicService bgMusicService;

    EditText imgUrl;

    Button fetchButton;

    GridView gridView;

    private Boolean IS_MUSIC_ON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_image);


        //get from home activity whether music is on
        Intent intent = getIntent();
        IS_MUSIC_ON = intent.getBooleanExtra("isMusicOn", false);
        bindMusicService(IS_MUSIC_ON);

        int[] drawables = {
                R.drawable.r15,
                R.drawable.r3,
                R.drawable.r1,
                R.drawable.monster,
                R.drawable.v2,
                R.drawable.s1000rr,
                R.drawable.cbr1000rr,
                R.drawable.gs1200
        };


        //initialise the gridview images
        gridView = (GridView) findViewById(R.id.gridViewImagesToChoose);
        SearchImageAdapter imageAdapter = new SearchImageAdapter(this, drawables);
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Tapped", Toast.LENGTH_SHORT).show();
                chosen.add(drawables[position]);
                view.setOnClickListener(null);
                view.setBackgroundColor(Color.GRAY);

                TextView matchestext = findViewById(R.id.matches);
                matchestext.setText(chosen.stream().count() + " Out of 6 images");
                if (chosen.stream().count() == 6) {
                    Toast.makeText(getApplicationContext(), "You have Chosen 6 Images", Toast.LENGTH_SHORT).show();
                    onClick(view);
                }
            }
        });

        imgUrl=(EditText) findViewById(R.id.ImgUrl);

        fetchButton=(Button) findViewById(R.id.Fetch);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(runnable).start();
            }
        });

    }


    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        ChosenImage chosenones = new ChosenImage();
        ArrayList<Integer> chosenimages = chosenones.getChoices();
        for (Integer i : chosen) {
            chosenimages.add(i);
        }

        intent.putExtra("images", chosenimages);
        intent.putExtra("isMusicOn", IS_MUSIC_ON);
        startActivity(intent);
    }


    //after android 9, requesting HTTP is not allowed in the main thread, so need to create a sub-thread to request for image resources
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            try {
                fetch();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

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

    protected void fetch() throws IOException {
        String url=imgUrl.getText().toString();
        trustEveryone();
        Document htmlResource = Jsoup.connect(url).get();
        Elements Images = htmlResource.getElementsByTag("img");
        int count=0;
        for(Element img : Images){
            String imgSrc = img.attr("src");
            if (!"".equals(imgSrc) && (imgSrc.startsWith("http://") || imgSrc.startsWith("https://"))) {
                System.out.println("正在下载的图片的地址：" + imgSrc);
                URL imgPath = new URL(imgSrc);
                HttpURLConnection conn = (HttpURLConnection) imgPath.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                conn.addRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
                if (conn.getResponseCode() == 200) {
                    System.out.println("Connection succeed");
                    InputStream inputStream = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ImageView imageView=(ImageView) gridView.getAdapter().getView(0,null,null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                    saveBitmap(bitmap,"Image"+count);
                    count++;
                }
            }
            if (count==8){
                break;
            }
        }
    }

    public void saveBitmap(Bitmap bm,String fileName){
        String fn = fileName;
        String prefix= Environment.DIRECTORY_PICTURES;
        System.out.println(prefix);
        String path = this.getFilesDir() + File.separator + fn+".png";
        System.out.println(path);
        try{
            OutputStream os = new FileOutputStream(path);
            bm.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        }catch(Exception e){
            e.printStackTrace();
        }
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
    }
}