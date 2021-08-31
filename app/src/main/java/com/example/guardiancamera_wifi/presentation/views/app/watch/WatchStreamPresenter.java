package com.example.guardiancamera_wifi.presentation.views.app.watch;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class WatchStreamPresenter {

    /* Initialize audio player */
    private AudioTrack audioPlayer;     // Audio Player
    int audioSampleRateHz;
    int audioBufferSize;

    WatchStreamPresenter() {
        audioSampleRateHz = 44100;
        audioBufferSize = 1024;
        initAudio();
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

    public void writeAudioData(byte [] audioData, int sizeInBytes) {
        audioPlayer.write(audioData, 0, sizeInBytes);
    }
}
