package com.farah.foodapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.GeolocationPermissions;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class MapPickActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request location permission at runtime
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        FrameLayout rootLayout = new FrameLayout(this);
        webView = new WebView(this);
        //LOADING
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);

        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                150, 150, Gravity.CENTER);
        progressBar.setLayoutParams(progressParams);
        progressBar.setIndeterminate(true);
        progressBar.getIndeterminateDrawable().setColorFilter(
                Color.parseColor("#9E090F"), android.graphics.PorterDuff.Mode.SRC_IN);

        rootLayout.addView(webView);
        rootLayout.addView(progressBar);
        setContentView(rootLayout);

      //connect views.MapView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                webView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

           // Receive selected location from WebView using custom URL scheme
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("myapp://pick-location")) {
                    Uri uri = Uri.parse(url);

                    // Extract selected location data from custom URL
                    double lat = Double.parseDouble(uri.getQueryParameter("lat"));
                    double lon = Double.parseDouble(uri.getQueryParameter("lng"));
                    String address = uri.getQueryParameter("address");

                    // Send selected location back to CheckoutActivity
                    Intent result = new Intent();
                    result.putExtra("pickedLat", lat);
                    result.putExtra("pickedLon", lon);
                    result.putExtra("pickedAddress", address);
                    setResult(RESULT_OK, result);
                    finish();
                    return true;
                }
                return false;
            }
        });

        String baseUrl = getString(R.string.map_url);
        String mapUrl = baseUrl + "/select-location/";
        webView.loadUrl(mapUrl);
    }

    // Reload map after user grants location permission

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (webView != null) webView.reload();
        }
    }
}
