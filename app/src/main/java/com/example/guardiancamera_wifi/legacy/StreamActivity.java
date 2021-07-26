//package com.example.guardiancamera_wifi;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//import android.webkit.WebView;
//
//public class VideoViewActivity extends AppCompatActivity {
//
//    WebView videoView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_video_view);
//
//        videoView = (WebView) findViewById(R.id.broadcastVideo);
//        videoView.getSettings().setJavaScriptEnabled(true);
//        //Todo: Change URL to MJPEG stream HTML page
//        videoView.loadUrl("https://www.google.com");
//
//        //UserInterfaceHandler.initButtonsUI(this);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//
//    }
//
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        super.onDestroy();
//    }
//}
