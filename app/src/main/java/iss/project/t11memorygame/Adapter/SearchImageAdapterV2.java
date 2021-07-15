package iss.project.t11memorygame.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.ArrayList;

import iss.project.t11memorygame.IClickGridItem;
import iss.project.t11memorygame.R;
import iss.project.t11memorygame.activity.GameActivity;
import iss.project.t11memorygame.activity.SearchImageActivity;
import iss.project.t11memorygame.model.Image;

public class SearchImageAdapterV2 extends BaseAdapter {
    private ArrayList<Image> images;
    //private ArrayList<ImageView> seleted_view = new ArrayList<>();
    private Context context;
    private LayoutInflater layoutInflater;
    private IClickGridItem iClickGridItem;

    public SearchImageAdapterV2(Context context, ArrayList<Image> images) {
        this.context = context;
        this.images = images;
        this.layoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        iClickGridItem = (IClickGridItem) context;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {

        //if accessing adapter on SearchImageActivity
        if(context instanceof SearchImageActivity) {
            final Image image = images.get(pos);
            image.setPosID(pos);

            if (view == null) {

                view = layoutInflater.inflate(R.layout.gridviewimages_nonflip, viewGroup, false);

            }

            ImageView imageView = (ImageView) view.findViewById(R.id.grid_image_nonflip);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(250,250));
//            imageView.setLayoutParams(new LinearLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.MATCH_PARENT,
//                    RelativeLayout.LayoutParams.MATCH_PARENT
//            ));

            imageView.setImageBitmap(image.getBitmap());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageView.setColorFilter(ContextCompat.getColor(
                            context,
                            R.color.purple_200),
                            PorterDuff.Mode.SRC_OVER
                    );
                    imageView.setClickable(false);
                    //Delegate method to SearchImageActivity to send onClick data over
                    iClickGridItem.onClickItem(pos);
                }
            });
            imageView.setClickable(image.isFetched());

            return imageView;
        }
        else if(context instanceof GameActivity){



           // return ;
        }

        return view;
    }




    public void updateItemList(ArrayList<Image> newItemList) {
        //images.clear();
        this.images = newItemList;
        notifyDataSetChanged();
    }
}
