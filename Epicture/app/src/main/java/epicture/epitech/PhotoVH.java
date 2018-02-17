package epicture.epitech;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class PhotoVH  extends RecyclerView.ViewHolder {

    ImageView photo;
    TextView title;

    public PhotoVH(View itemView)
    {
        super(itemView);
    }
}
