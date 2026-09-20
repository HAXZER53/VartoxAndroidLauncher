package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.mcVersionSpinner;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;

public class MainMenuFragment extends Fragment {
    public static final String TAG = "MainMenuFragment";

    private mcVersionSpinner mVersionSpinner;
    private TextView mPlayerNameText;
    private View mCardNewEra;
    private View mCardHiTech;
    private TextView mNewEraIndicator;
    private String mSelectedProfileName = "NewEra";

    public MainMenuFragment(){
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mPlayerNameText = view.findViewById(R.id.vxeno_player_name);
        mCardNewEra = view.findViewById(R.id.card_server_newera);
        mCardHiTech = view.findViewById(R.id.card_server_hitech);
        mNewEraIndicator = view.findViewById(R.id.newera_status_indicator);
        mVersionSpinner = view.findViewById(R.id.mc_version_spinner);

        // Display current authenticated user name
        String currentAccount = LauncherPreferences.DEFAULT_PREF.getString("currentAccount", "VXeno Player");
        if (currentAccount != null && !currentAccount.isEmpty()) {
            mPlayerNameText.setText(currentAccount);
        }

        // Logout button: clear auto-login and return to login screen
        Button logoutBtn = view.findViewById(R.id.vxeno_logout_button);
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                Context ctx = getContext();
                if (ctx != null) {
                    SharedPreferences prefs = ctx.getSharedPreferences("vxeno_auth_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("auto_login", false).apply();
                }
                Tools.swapFragment(requireActivity(), SelectAuthFragment.class, SelectAuthFragment.TAG, null);
            });
        }

        // Server selection logic
        mCardNewEra.setOnClickListener(v -> selectServerProfile("NewEra"));
        mCardHiTech.setOnClickListener(v -> selectServerProfile("HiTech"));

        // Settings / Optional Mods Button
        View settingsBtn = view.findViewById(R.id.client_settings_button);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                new VXenoOptionalModsDialog().show(getParentFragmentManager(), VXenoOptionalModsDialog.TAG);
            });
        }

        // PLAY button
        Button playBtn = view.findViewById(R.id.play_button);
        if (playBtn != null) {
            playBtn.setOnClickListener(v -> {
                selectServerProfile(mSelectedProfileName);
                ExtraCore.setValue(ExtraConstants.LAUNCH_GAME, true);
            });
        }
    }

    private void selectServerProfile(String name) {
        mSelectedProfileName = name;
        LauncherPreferences.DEFAULT_PREF.edit().putString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, name).apply();
        if (LauncherProfiles.mainProfileJson != null && LauncherProfiles.mainProfileJson.profiles != null) {
            if (!LauncherProfiles.mainProfileJson.profiles.containsKey(name)) {
                MinecraftProfile prof = new MinecraftProfile();
                prof.name = name;
                prof.lastVersionId = "1.21.1";
                LauncherProfiles.mainProfileJson.profiles.put(name, prof);
                LauncherProfiles.write();
            }
        }
        if (mNewEraIndicator != null) {
            mNewEraIndicator.setText("NewEra".equals(name) ? "● Выбран для запуска" : "Нажмите для выбора");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVersionSpinner != null) {
            mVersionSpinner.reloadProfiles();
        }
    }
}
