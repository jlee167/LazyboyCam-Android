package com.example.guardiancamera_wifi.presentation.views.app.watch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
import com.example.guardiancamera_wifi.data.configs.Addresses;
import com.example.guardiancamera_wifi.domain.models.ProtectedClientStream;
import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;


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



public class StreamFragment extends Fragment {

    WebView videoView;          // Video Player
    MapView mapView;
    AudioTrack audioPlayer;     // Audio Player

    Thread geoDataFetcher;
    boolean geoDataFetcherOn;

    HttpConnection conn;
    JSONObject sendData;


    public StreamFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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


        /* Initialize audio player */
        audioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                1024,
                AudioTrack.MODE_STREAM);

        audioPlayer.play();

        mapView = new MapView(getActivity());
        ViewGroup mapViewContainer = (ViewGroup) getActivity().findViewById(R.id.mapView);
        mapViewContainer.addView(mapView);


        /*
        try {
            conn = new HttpConnection();
            sendData = new JSONObject();
            sendData.put("token", ProtectedClientStream.getJWT());
            conn.sendHttpRequest(ProtectedClientStream.getGeoSrcUrl(), sendData, "POST", getString(R.string.STREAMING_SERVER_IP));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        */

        geoDataFetcher = new Thread() {
            @Override
            public void run() {
                while (geoDataFetcherOn) {
                    try {
                        JSONObject geoData = new JSONObject(
                                conn.sendHttpRequest(
                                        ProtectedClientStream.getGeoSrcUrl(),
                                        sendData,
                                        "POST",
                                        Addresses.STREAMING_SERVER_IP
                                )
                        );
                        double latitude = geoData.getDouble("latitude");
                        double longitude = geoData.getDouble("longitude");
                        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true);

                        MapPOIItem marker = new MapPOIItem();
                        marker.setItemName("Client Location");
                        marker.setTag(0);
                        marker.setMapPoint(mapView.getMapCenterPoint());
                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                        mapView.addPOIItem(marker);

                        JSONObject audioInput = new JSONObject(
                                conn.sendHttpRequest(
                                        ProtectedClientStream.getAudioSrcUrl(),
                                        sendData,
                                        "POST",
                                        Addresses.STREAMING_SERVER_IP
                                )
                        );
                        byte [] audioData = audioInput.getString("audioData")
                                                    .getBytes(StandardCharsets.UTF_8);
                        audioPlayer.write(audioData, 0, 1024);

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }



    @Override
    public void onStart() {
        geoDataFetcherOn = true;
        geoDataFetcher.start();
        super.onStart();
    }

    @Override
    public void onDetach() {
        geoDataFetcherOn = false;
        audioPlayer.stop();
        audioPlayer = null;
        super.onDetach();
    }


    public void audioTest() {
        getActivity().runOnUiThread(new Thread(){
            @Override
            public void run() {
                super.run();
                while(true) {
                    byte[] audioData = new byte[1024];
                    new Random().nextBytes(audioData);
                    audioPlayer.write(audioData, 0, 1024);
                }
            }
        });
    }
}