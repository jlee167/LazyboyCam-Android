package com.example.guardiancamera_wifi.presentation.views.app.watch;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.example.guardiancamera_wifi.data.api.http.base.HttpConnection;
import com.example.guardiancamera_wifi.data.configs.IpTable;
import com.example.guardiancamera_wifi.domain.models.ClientStreamInfo;
import com.example.guardiancamera_wifi.domain.models.HttpResponse;
import com.example.guardiancamera_wifi.domain.models.PeerStreamData;
import com.example.guardiancamera_wifi.domain.models.base.VideoDescriptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class WatchStreamPresenter {

    WatchStreamFragment fragment;
    Context applicationContext;

    /* Initialize audio player */
    private AudioTrack audioPlayer;
    private int audioSampleRateHz;
    private int audioBufferSize;

    private Thread geoLocationThread;
    private Thread audioThread;
    private boolean streamActive;
    private VideoDescriptor videoDescriptor;
    private ClientStreamInfo clientStreamInfo;

    HttpConnection conn;
    JSONObject sendData;

    WatchStreamPresenter(Context applicationContext, WatchStreamFragment fragment) {
        this.fragment = fragment;
        this.applicationContext = applicationContext;

        audioSampleRateHz = 44100;
        audioBufferSize = 1024;
        initAudio();
        playAudio();
        deactivateStream();

        videoDescriptor = new VideoDescriptor();
        clientStreamInfo = new ClientStreamInfo();

        geoLocationThread = new Thread() {
            @Override
            public void run() {
                while (streamActive) {
                    try {
                        HttpResponse geoDataQueryResult = conn.sendHttpRequest(
                                IpTable.STREAMING_SERVER_IP +PeerStreamData.getGeoSrcUrl(),
                                null,
                                "GET"
                        );
                        JSONObject geoData = new JSONObject(Arrays.toString(geoDataQueryResult.getBody()));
                        double latitude = geoData.getDouble("latitude");
                        double longitude = geoData.getDouble("longitude");
                        fragment.updateGeoLocationUi(latitude, longitude);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        audioThread = new Thread() {
            @Override
            public void run() {
                while (streamActive & !isAudioPlayedOnWebView()) {
                    try {
                       byte [] audioInput =
                            conn.sendHttpRequest(
                                    IpTable.STREAMING_SERVER_IP +PeerStreamData.getAudioSrcUrl(),
                                    null,
                                    "GET"
                            ).getBody();
                        writeAudioData(audioInput, 1024);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public boolean isAudioPlayedOnWebView() {
        return (videoDescriptor.getFormat() == VideoDescriptor.Format.MJPEG)
                    || (videoDescriptor.getFormat() == VideoDescriptor.Format.RGB565);
    }

    public void initAudio() {
        audioPlayer = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                audioSampleRateHz,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBufferSize,
                AudioTrack.MODE_STREAM
        );
    }


    public void playAudio() {
        audioPlayer.play();
    }


    public void writeAudioData(byte[] audioData, int sizeInBytes) {
        audioPlayer.write(audioData, 0, sizeInBytes);
    }


    public void activateStream() {
        streamActive = true;
    }


    public void deactivateStream() {
        streamActive = false;
    }

    public void changeClient() {

    }


    public void destroy() {
        deactivateStream();
        audioThread.interrupt();
        geoLocationThread.interrupt();
    }
}
