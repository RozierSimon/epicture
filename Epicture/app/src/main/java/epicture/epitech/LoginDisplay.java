package epicture.epitech;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginDisplay extends Activity {

    private WebView mWebView;
    public static final Pattern accessTokenPattern = Pattern.compile("access_token=([^&]*)");
    public static final Pattern refreshTokenPattern = Pattern.compile("refresh_token=([^&]*)");
    public static final Pattern expiresInPattern = Pattern.compile("expires_in=(\\d+)");
    public static final Pattern usernamePattern = Pattern.compile("account_username=([^&]*)");

    private void setupWebView() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean tokensURL = false;
                if (url.startsWith("epicture://callback")) {
                    tokensURL = true;
                    Matcher m;

                    m = refreshTokenPattern.matcher(url);
                    m.find();
                    MainActivity.refreshToken = m.group(1);

                    m = accessTokenPattern.matcher(url);
                    m.find();
                    MainActivity.accessToken = m.group(1);

                    m = expiresInPattern.matcher(url);
                    m.find();
                    long expiresIn = Long.valueOf(m.group(1));

                    m = usernamePattern.matcher(url);
                    m.find();
                    MainActivity.myUsername = m.group(1);
                    finish();
                }
                return tokensURL;
            }
        });
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        mWebView = new WebView(this);
        root.addView(mWebView);
        setContentView(root);

        setupWebView();

        mWebView.loadUrl("https://api.imgur.com/oauth2/authorize?client_id=" + "0d39c4b2e7b8c0a" + "&response_type=token");
    }
}
