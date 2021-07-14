package iss.project.t11memorygame.utility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ImageFetchManager {

    //scrape web for the 20 img src urls
    public static ArrayList<String> getImageSrc(String url){
        ArrayList<String> imgSrc = new ArrayList<String>();
        try{
            Document doc = Jsoup.connect(url).get();
            for(int i=0; i< 20; i++){
                imgSrc.add(doc.select("img[src^=https]").eq(i).attr("src"));
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return imgSrc;
    }

    //download img to app external storage
    public static boolean downloadImage (String imgUrl, File destFile){
        try {
            URL url = new URL(imgUrl);
            URLConnection conn = url.openConnection(); //connect to remote server
            InputStream in = conn.getInputStream(); //receive stream of bytes from server
            FileOutputStream out = new FileOutputStream(destFile); //for storing bytes
            byte[] buf = new byte[1024];
            int bytesRead = -1;
            while ((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead); }  //persist chunk of byte data to output until no more
            out.close();
            in.close();
            return true; }
        catch (Exception e) { return false; }

    }
}
