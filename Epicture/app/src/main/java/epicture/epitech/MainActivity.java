package epicture.epitech;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ImgurAPI imgur;
    private WebView mWebView;
    private static final Pattern accessTokenPattern = Pattern.compile("access_token=([^&]*)");
    private static final Pattern refreshTokenPattern = Pattern.compile("refresh_token=([^&]*)");
    private static final Pattern expiresInPattern = Pattern.compile("expires_in=(\\d+)");




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
                    .header("Authorization","Client-ID 0d39c4b2e7b8c0a")
                    .header("User-Agent","epicture")
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


    private void setupWebView() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // intercept the tokens
                // http://example.com#access_token=ACCESS_TOKEN&token_type=Bearer&expires_in=3600
                boolean tokensURL = false;
                if (url.startsWith("epicture://callback")) {
                    tokensURL = true;
                    Matcher m;

                    m = refreshTokenPattern.matcher(url);
                    m.find();
                    String refreshToken = m.group(1);

                    m = accessTokenPattern.matcher(url);
                    m.find();
                    String accessToken = m.group(1);

                    m = expiresInPattern.matcher(url);
                    m.find();
                    long expiresIn = Long.valueOf(m.group(1));
                    System.out.println(accessToken);
                    System.out.println("COUCOU");

                }

                return tokensURL;
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //imgur = new ImgurAPI();
        FrameLayout root = new FrameLayout(this);
        mWebView = new WebView(this);
        root.addView(mWebView);
        setContentView(root);

        setupWebView();

        mWebView.loadUrl("https://api.imgur.com/oauth2/authorize?client_id=" + "0d39c4b2e7b8c0a" + "&response_type=token");
        setContentView(R.layout.activity_main);
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
