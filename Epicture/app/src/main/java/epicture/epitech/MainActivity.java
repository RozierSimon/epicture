package epicture.epitech;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ImgurAPI imgur;

    private static class Photo {
        String id;
        String title;
    }


    public class ImgurAPI {

        private OkHttpClient httpClient;
        Request request;
        public List<MainActivity.Photo> photos;


        ImgurAPI()
        {
            httpClient = new OkHttpClient.Builder().build();
        }

        private void init_request(String query)
        {
            request = new Request.Builder()
                    .url("https://api.imgur.com/3/gallery/search/time/all/1?q=" + query)
                    .header("Authorization","Client-ID d153d804ce4cb01")
                    .header("User-Agent","Epicture2")
                    .build();
        }

        private void render(final List<MainActivity.Photo> photos)
        {
            RecyclerView rv = (RecyclerView)findViewById(R.id.rv_of_photos);
            rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        }

        private void ParseMsg(Response response)
        {
            JSONObject data = null;

            try {
                data = new JSONObject(response.body().string());
                JSONArray items = data.getJSONArray("data");

                photos = new ArrayList<MainActivity.Photo>();

                for(int i=0; i<items.length();i++)
                {
                    JSONObject item = items.getJSONObject(i);
                    MainActivity.Photo photo = new MainActivity.Photo();

                    if(item.getBoolean("is_album"))
                        photo.id = item.getString("cover");
                    else
                        photo.id = item.getString("id");

                    photo.title = item.getString("title");
                    photos.add(photo); // Add photo to list
                }


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void research()
        {
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("zob");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    ParseMsg(response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            render(photos);
                        }
                    });
                }
            });
        }
    }

    private void refreshList()
    {
        RecyclerView.Adapter<PhotoVH> adapter = new RecyclerView.Adapter<PhotoVH>() {
            @Override
            public PhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
                PhotoVH vh = new PhotoVH(getLayoutInflater().inflate(R.layout.item, null));
                vh.photo = (ImageView) vh.itemView.findViewById(R.id.photo);
                vh.title = (TextView) vh.itemView.findViewById(R.id.title);
                return vh;
            }

            @Override
            public void onBindViewHolder(PhotoVH holder, int position) {
                Picasso.with(MainActivity.this).load("https://i.imgur.com/" +
                        imgur.photos.get(position).id + ".jpg").into(holder.photo);
                holder.title.setText(imgur.photos.get(position).title);
            }

            @Override
            public int getItemCount() {
                return imgur.photos.size();
            }
        };
        RecyclerView rv = (RecyclerView)findViewById(R.id.rv_of_photos);
        rv.setAdapter(adapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgur = new ImgurAPI();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view_menu_item, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        final SearchView searchViewAndroidActionBar = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchViewAndroidActionBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchViewAndroidActionBar.clearFocus();
                imgur.init_request(query);
                imgur.research();
                refreshList();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


}
