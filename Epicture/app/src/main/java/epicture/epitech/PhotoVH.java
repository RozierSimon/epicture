package epicture.epitech;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class PhotoVH  extends RecyclerView.ViewHolder {

    ImageView photo;
    TextView title;
    Button   button;

    public PhotoVH(View itemView)
    {
        super(itemView);
    }
}
