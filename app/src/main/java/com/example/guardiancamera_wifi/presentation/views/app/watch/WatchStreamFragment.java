package com.example.guardiancamera_wifi.presentation.views.app.watch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.guardiancamera_wifi.R;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;


class WebAppInterface {
    Context mContext;

    WebAppInterface(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
}



public class WatchStreamFragment extends Fragment {

    WebView videoView;
    MapView mapView;
    WatchStreamPresenter presenter;


    public WatchStreamFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new WatchStreamPresenter(getActivity().getApplicationContext(), this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Initialize video player */
        videoView = (WebView) requireActivity().findViewById(R.id.broadcastVideo);
        videoView.setPadding(0, 0, 0, 0);
        //videoView.setInitialScale(1);
        videoView.getSettings().setJavaScriptEnabled(true);
        videoView.getSettings().setDefaultTextEncodingName("UTF-8");
        videoView.getSettings().setDomStorageEnabled(true);
        videoView.getSettings().setLoadsImagesAutomatically(true);
        videoView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        videoView.addJavascriptInterface(new WebAppInterface(getContext()), "Android");
        WebViewClient mWebViewClient = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
            }
        };
        videoView.setWebViewClient(mWebViewClient);
        //Todo: Change URL to MJPEG stream HTML page
        videoView.loadUrl("file:///android_asset/MJPEG_Viewer.html");

        mapView = new MapView(getActivity());
        ViewGroup mapViewContainer = (ViewGroup) getActivity().findViewById(R.id.mapView);
        mapViewContainer.addView(mapView);
    }

    void onClientChange() {
        presenter.changeClient();
    }

    void updateGeoLocationUi(double latitude, double longitude) {
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true);
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Client Location");
        marker.setTag(0);
        marker.setMapPoint(mapView.getMapCenterPoint());
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(marker);
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        presenter.deactivateStream();
    }
}