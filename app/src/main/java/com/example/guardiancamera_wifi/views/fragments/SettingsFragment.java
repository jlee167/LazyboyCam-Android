package com.example.guardiancamera_wifi.views.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.guardiancamera_wifi.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, null);
    }


/*
    GuardianCamConfigs cameraConfigs;


    public SettingsFragment(){
        addPreferencesFromResource(R.xml.root_preferences);

    };


    @SuppressLint("ValidFragment")
    public SettingsFragment(GuardianCamConfigs camConfigs_inst) {
        cameraConfigs = camConfigs_inst;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> adapter;

        super.onViewCreated(view, savedInstanceState);

        Spinner formatSpinner = Objects.requireNonNull(getView()).findViewById(R.id.formatSpinner);
        adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(this.getContext()),
                R.array.WIFI_VIDEO_FORMATS, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);


        Spinner deviceSpinner = getView().findViewById(R.id.deviceSpinner);
        adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(this.getContext()),
                R.array.WIFI_VIDEO_SOURCES, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(adapter);


        Spinner resolutionSpinner = getView().findViewById(R.id.resolutionSpinner);
        adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(this.getContext()),
                R.array.WIFI_VIDEO_RESOLUTIONS, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resolutionSpinner.setAdapter(adapter);

        resolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        cameraConfigs.setInputSize(GuardianCamConfigs.FRAME_SIZE_QCIF);
                        break;
                    case 1:
                        cameraConfigs.setInputSize(GuardianCamConfigs.FRAME_SIZE_VGA);
                        break;
                    case 2:
                        cameraConfigs.setInputSize(GuardianCamConfigs.FRMAE_SIZE_SVGA);
                        break;
                    case 3:
                        cameraConfigs.setInputSize(GuardianCamConfigs.FRMAE_SIZE_HD);
                        break;
                    case 4:
                        cameraConfigs.setInputSize(GuardianCamConfigs.FRAME_SIZE_FHD);
                        Log.i("Camera Config Changed", "FHD Selected");
                        BufferedWriter consoleOutput = new BufferedWriter(new OutputStreamWriter(System.out));
                        try {
                            consoleOutput.write("Full HD clicked");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }
    */

}
