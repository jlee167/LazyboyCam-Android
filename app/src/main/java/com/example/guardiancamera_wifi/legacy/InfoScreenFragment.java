package com.example.guardiancamera_wifi.legacy;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.guardiancamera_wifi.R;

public class InfoScreenFragment extends Fragment {

    public InfoScreenFragment() {
    }


    public InfoScreenFragment(Activity parentActivity) {

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_stat_view, container, false);
    }
}
