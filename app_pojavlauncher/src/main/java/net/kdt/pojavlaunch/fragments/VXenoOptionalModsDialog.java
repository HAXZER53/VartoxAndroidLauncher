package net.kdt.pojavlaunch.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import net.kdt.pojavlaunch.R;

import java.util.ArrayList;
import java.util.List;

public class VXenoOptionalModsDialog extends DialogFragment {
    public static final String TAG = "VXENO_OPTIONAL_MODS";
    private static final String PREF_MODS = "vxeno_optional_mods_pref";

    public static class OptionalModItem {
        public String name;
        public String description;
        public boolean defaultEnabled;

        public OptionalModItem(String name, String description, boolean defaultEnabled) {
            this.name = name;
            this.description = description;
            this.defaultEnabled = defaultEnabled;
        }
    }

    private static final List<OptionalModItem> MODS = new ArrayList<>();
    static {
        MODS.add(new OptionalModItem("AllTheLeaks", "Исправление утечек памяти", true));
        MODS.add(new OptionalModItem("BadOptimizations", "Оптимизация и увеличение FPS", true));
        MODS.add(new OptionalModItem("BetterModList", "Улучшенный список модов", true));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        SharedPreferences prefs = context.getSharedPreferences(PREF_MODS, Context.MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 20);

        TextView title = new TextView(context);
        title.setText("Опциональные моды (VXeno)");
        title.setTextSize(18f);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        List<CheckBox> checkBoxes = new ArrayList<>();
        for (OptionalModItem mod : MODS) {
            CheckBox cb = new CheckBox(context);
            boolean isChecked = prefs.getBoolean(mod.name, mod.defaultEnabled);
            cb.setText(mod.name + " (" + mod.description + ")");
            cb.setChecked(isChecked);
            layout.addView(cb);
            checkBoxes.add(cb);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            SharedPreferences.Editor editor = prefs.edit();
            for (int i = 0; i < MODS.size(); i++) {
                editor.putBoolean(MODS.get(i).name, checkBoxes.get(i).isChecked());
            }
            editor.apply();
        });
        builder.setNegativeButton("Отмена", null);

        return builder.create();
    }
}
