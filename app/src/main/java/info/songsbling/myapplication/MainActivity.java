package info.songsbling.myapplication;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {
    WebView webView ;



    String active;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    String urlToLoad="https://songsbling.info/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);

        mInterstitialAd.setAdUnitId("ca-app-pub-2135153768458971/9415000458");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        initWebView(urlToLoad);


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();

                }
                handler.postDelayed(this, 60000); //now is every 2 minutes
            }
        }, 60000);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

    }

    public void initWebView(String url){

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        registerForContextMenu(webView);
        webView.loadUrl(url);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return false;
            }
        });




    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult result = webView.getHitTestResult();

        /*
            WebView.HitTestResult

                IMAGE_TYPE
                    HitTestResult for hitting an HTML::img tag.

                SRC_IMAGE_ANCHOR_TYPE
                    HitTestResult for hitting a HTML::a tag with src=http + HTML::img.
        */

        // If user long press on an image
        if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE ) {

            // Set the title for context menu
            menu.setHeaderTitle("Menu");

            // Add an item to the menu
            menu.add(0, 1, 0, "Download song")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            // Get the image url
                            String imgUrl = result.getExtra();

                            // If this is an image url then download it
                            if(URLUtil.isValidUrl(imgUrl)){
//                                 Initialize a new download request
                                if (mInterstitialAd.isLoaded()) {
                                    mInterstitialAd.show();

                                }
                                Uri uri = Uri.parse(imgUrl);
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,uri.getLastPathSegment());
                                downloadManager.enqueue(request);

//                                Toast.makeText(MainActivity.this,"Downloaded.",Toast.LENGTH_SHORT).show();
                            }else {
//                                Toast.makeText(MainActivity.this,"Invalid url.",Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    });
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();

                    }
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();

                        }
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}

