package iss.project.t11memorygame.activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import iss.project.t11memorygame.R;

public class GameImageAdapter extends BaseAdapter {
    private Context context;

    public GameImageAdapter(Context context)
    {
        this.context=context;
    }
    @Override
    public int getCount() {
        return 12;
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
            imageView=new ImageView(this.context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(350,350));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        else{
            imageView=(ImageView) convertView;
        }
        imageView.setImageResource(R.drawable.hidden);
        return imageView;
    }

}
