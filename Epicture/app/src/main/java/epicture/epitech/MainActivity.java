package epicture.epitech;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
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
import android.widget.Button;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ImgurAPI imgur;
    public static long expiresIn;
    public static String accessToken;
    public static String refreshToken;
    public static String myUsername;
    public boolean isMyPicture;
    public String uploadedImage;



    private static class Photo {
        String id;
        String title;
        Button addFav;
    }
    public Button setButton()
    {
        Button toRet = new Button(this);
        toRet.setText("Add To Fav");
        return (toRet);
    }

    public class ImgurAPI {

        private OkHttpClient httpClient;
        Request request;
        public List<MainActivity.Photo> photos;
        public String addOrRemove;

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
                    final MainActivity.Photo photo = new MainActivity.Photo();

                    if (!isMyPicture)
                    {
                        if(item.getBoolean("is_album"))
                            photo.id = item.getString("cover");
                        else
                            photo.id = item.getString("id");
                    }
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
                    System.out.println("Error: " + e.toString());
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
        public void getFav()
        {
            String favurl = "https://api.imgur.com/3/account/" + myUsername + "/favorites";//"/gallery_favorites/0/newest";
            request = new Request.Builder()
                    .url(favurl)
//                    .addHeader("Authorization","Client-ID 0d39c4b2e7b8c0a")
                    .addHeader("Authorization", "Bearer " + accessToken)
 //                   .addHeader("User-Agent","epicture")
 //                   .addHeader("Authorization","Client-Secret 21fb50ec36c4c613e1cae8817aa938362c9f6475")
                    .build();
            addOrRemove = "Remove to favorite";
            research();
            refreshList();
        }
        public void setFav(String idp)
        {
            String favurl = "https://api.imgur.com/3/image/" + idp + "/favorite";
            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, "");
            Request nrequest = new Request.Builder()
                    .url(favurl)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("User-Agent","epicture")
                    .post(body)
                    .build();


                    httpClient.newCall(nrequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            System.out.println("Error:" + e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
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
                vh.button = (Button)vh.itemView.findViewById(R.id.but);
                return vh;
            }

            @Override
            public void onBindViewHolder(PhotoVH holder, final int position) {
                Picasso.with(MainActivity.this).load("https://i.imgur.com/" +
                        imgur.photos.get(position).id + ".jpg").into(holder.photo);
                holder.title.setText(imgur.photos.get(position).title);
                holder.button.setText(imgur.addOrRemove);
                imgur.addOrRemove = "Add To favorite";
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imgur.setFav(imgur.photos.get(position).id);

                    }
                });
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
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        InputStream stream = null;
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            try {
                stream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                // Creates Byte Array from picture
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] byteArray = baos.toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                String baseUrl = "https://api.imgur.com/3/";
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", encoded)
                        .addFormDataPart("type", "base64")
                        .build();
                Request request = new Request.Builder()
                        .url(baseUrl + "image")
                        .addHeader("Authorization","Client-ID " + "0d39c4b2e7b8c0a")
                        //.addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("User-Agent","epicture")
                        .addHeader("Accept", "application/json; q=0.5")
                        .post(requestBody)
                        .build();
                try {
                    OkHttpClient httpClient = new OkHttpClient.Builder().build();
                    httpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("MEssage", "An unexpected error has occurred : " + e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            uploadedImage = null;
                            if (response.code() == 200) {
                                try {
                                    String responseString = response.body().string();
                                    JSONObject Jobject = new JSONObject(responseString);
                                    JSONObject jobject = Jobject.getJSONObject("data");
                                    uploadedImage = jobject.getString("link");
                                    Log.d("TAG", Jobject.getString("data"));
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            success_dialog(uploadedImage);
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {

                                }
                            }
                        }
                    });

                }
                finally {
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void success_dialog(String str)
    {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Success")
                .setMessage("Your picture has been successfully uploaded anonymously to url " + str)
                .setView(null)
                .setNegativeButton("OK", null)
                .create();
        dialog.show();
    }

    private void create_dialog_upload()
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Upload an image to Imgur")
                .setMessage("Describe the title and description before browsing your image")
                .setView(null)
                .setPositiveButton("BROWSE",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("image/*");

                                startActivityForResult(intent, 1);
                            }
                        })
                .setNegativeButton("EXIT", null)
                .create();
        dialog.show();
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
                isMyPicture = false;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_connect:
                Intent intent = new Intent(MainActivity.this, LoginDisplay.class);
                startActivity(intent);
                return true;
            case R.id.action_upload:
                create_dialog_upload();
                //if (uploadedImage != null)
                    //success_dialog(uploadedImage);
                return true;
            case R.id.action_myimg:
                imgur.request = new Request.Builder()
                        .url("https://api.imgur.com/3/account/me/images")
                        .header("Authorization","Client-ID 0d39c4b2e7b8c0a")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("User-Agent","epicture")
                        .build();
                isMyPicture = true;
                imgur.research();
                refreshList();
                return true;
            case R.id.action_getFav:
                imgur.getFav();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean loggedIn = !TextUtils.isEmpty(accessToken);
        menu.findItem(R.id.action_connect).setVisible(!loggedIn);
        menu.findItem(R.id.action_upload).setVisible(loggedIn);
        menu.findItem(R.id.action_myimg).setVisible(loggedIn);
        menu.findItem(R.id.action_getFav).setVisible(loggedIn);
        return super.onPrepareOptionsMenu(menu);
    }
}
