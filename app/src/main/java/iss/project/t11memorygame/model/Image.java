package iss.project.t11memorygame.model;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Image implements Serializable {
    private Bitmap bitmap ;

    private int posID;

    public Image(Bitmap bitmap, int id) {
        this.bitmap = bitmap;
        this.posID = id;
    }

    public Image(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getPosID() {
        return posID;
    }

    public void setPosID(int posID) {
        this.posID = posID;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}
