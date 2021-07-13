package iss.project.t11memorygame.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import iss.project.t11memorygame.R;

public class SearchImageAdapter extends BaseAdapter {

    Context context;
    int[] image;
    private LayoutInflater layoutInflater;

    public SearchImageAdapter(Context context) {
        this.context = context;
    }

    public SearchImageAdapter(Context context, int[] image) {
        this.context = context;
        this.image = image;
        this.layoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return image.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView==null){
            //convertView=layoutInflater.inflate(R.layout.image_list,parent,false);
            imageView=new ImageView(this.context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(250,250));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        else{
            imageView=(ImageView) convertView;
        }
        imageView.findViewById(R.id.grid_image);
        imageView.setImageResource(image[position]);

        //imageView.setImageBitmap(image[position]);
        return imageView;
    }
}