package com.example.guardiancamera_wifi.presentation.views.app.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.guardiancamera_wifi.R;
import com.example.guardiancamera_wifi.domain.broadcasts.ServiceMsgBroadcast;


public class HomeFragment extends Fragment {

    Activity activity;
    HomePresenter presenter;
    ServiceMsgBroadcast serviceMsgBroadcast;

    TextView userStatusView;
    TextView cameraStatusView;
    TextView streamStatusView;


    public HomeFragment() {
    }


    public void handleStreamStart() {
        streamStatusView.setText(R.string.STREAM_STATUS_ACTIVE);
    }


    public void handleStreamStop() {
        streamStatusView.setText(R.string.STREAM_STATUS_INACTIVE);
    }


    public void handleEmergencyStart() {
        userStatusView.setText(R.string.USER_STATUS_EMERGENCY);
    }


    public void handleEmergencyStop() {
        userStatusView.setText(R.string.USER_STATUS_FINE);
    }


    public void handleCameraConnection() {
        cameraStatusView.setText(R.string.CAMERA_STATUS_CONNECTED);
    }


    public void handleCameraDisconnection() {
        cameraStatusView.setText(R.string.CAMERA_STATUS_DISCONNECTED);
    }


    public void openLogs() {
        activity.findViewById(R.id.generalInfoView).setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.logView).setVisibility(View.VISIBLE);
    }


    public void closeLogs() {
        activity.findViewById(R.id.generalInfoView).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.logView).setVisibility(View.INVISIBLE);
    }


    public boolean isLogOpen() {
        return activity.findViewById(R.id.logView).getVisibility() == View.VISIBLE;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        activity = getActivity();
        assert activity != null;
        presenter = new HomePresenter();

        userStatusView = activity.findViewById(R.id.userStatusView);
        cameraStatusView = activity.findViewById(R.id.cameraStatusView);
        streamStatusView = activity.findViewById(R.id.streamStatusView);

        HomeFragment self = this;
        serviceMsgBroadcast = new ServiceMsgBroadcast() {
            @Override
            public void onStreamStart() {
                presenter.handleStreamStart();
                self.handleStreamStart();
            }

            @Override
            public void onStreamStop() {
                presenter.handleStreamStop();
                self.handleStreamStop();
            }

            @Override
            public void onEmergencyStart() {
                presenter.handleEmergencyStart();
                self.handleEmergencyStart();
            }

            @Override
            public void onEmergencyStop() {
                presenter.handleEmergencyStop();
                self.handleEmergencyStop();
            }

            @Override
            public void onCameraConnected() {
                presenter.handleCameraConnection();
                self.handleCameraConnection();
            }

            @Override
            public void onCameraDisconnected() {
                presenter.handleCameraDisconnection();
                self.handleCameraDisconnection();
            }
        };

        userStatusView.setText(R.string.USER_STATUS_FINE);
        cameraStatusView.setText(R.string.CAMERA_STATUS_DISCONNECTED);
        streamStatusView.setText(R.string.STREAM_STATUS_INACTIVE);

        activity.findViewById(R.id.logToggleBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogOpen())
                    closeLogs();
                else
                    openLogs();
            }
        });
    }
}
