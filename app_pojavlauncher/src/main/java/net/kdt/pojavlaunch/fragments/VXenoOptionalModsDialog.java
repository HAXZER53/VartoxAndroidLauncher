package net.kdt.pojavlaunch.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.vartox.GravitLauncherService;

import java.util.ArrayList;
import java.util.List;

public class VXenoOptionalModsDialog extends DialogFragment {
    public static final String TAG = "VXENO_OPTIONAL_MODS";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        String currentProfile = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "NewEra");
        SharedPreferences prefs = context.getSharedPreferences("vxeno_mods_" + currentProfile, Context.MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 20);

        TextView title = new TextView(context);
        title.setText("Опциональные моды: " + currentProfile);
        title.setTextSize(18f);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        List<GravitLauncherService.ServerProfile> allProfiles = GravitLauncherService.fetchProfiles();
        GravitLauncherService.ServerProfile selected = null;
        for (GravitLauncherService.ServerProfile p : allProfiles) {
            if (p.title.equalsIgnoreCase(currentProfile)) {
                selected = p;
                break;
            }
        }

        if (selected == null && !allProfiles.isEmpty()) {
            selected = allProfiles.get(0);
        }

        final List<GravitLauncherService.OptionalMod> mods = (selected != null) ? selected.optionalMods : new ArrayList<>();
        final List<CheckBox> checkBoxes = new ArrayList<>();

        if (mods.isEmpty()) {
            TextView emptyText = new TextView(context);
            emptyText.setText("Для данного сервера нет опциональных модов");
            layout.addView(emptyText);
        } else {
            for (GravitLauncherService.OptionalMod mod : mods) {
                CheckBox cb = new CheckBox(context);
                boolean isChecked = prefs.getBoolean(mod.name, mod.defaultEnabled);
                cb.setText(mod.name + "\n" + mod.info);
                cb.setChecked(isChecked);
                cb.setPadding(0, 8, 0, 8);
                layout.addView(cb);
                checkBoxes.add(cb);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        builder.setPositiveButton("Применить", (dialog, which) -> {
            SharedPreferences.Editor editor = prefs.edit();
            for (int i = 0; i < mods.size(); i++) {
                editor.putBoolean(mods.get(i).name, checkBoxes.get(i).isChecked());
            }
            editor.apply();
        });
        builder.setNegativeButton("Закрыть", null);

        return builder.create();
    }
}
