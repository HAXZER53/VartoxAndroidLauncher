package net.kdt.pojavlaunch.vartox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.CustomControlsActivity;
import net.kdt.pojavlaunch.MainActivity;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.authenticator.accounts.MinecraftAccount;
import net.kdt.pojavlaunch.fragments.VXenoOptionalModsDialog;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VXenoMainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "vxeno_prefs";
    private static final String KEY_USER = "saved_username";
    private static final String KEY_PASS = "saved_password";

    private View topBar;
    private Button tabAccount, tabServers, tabSettings;
    private View downloaderLayout;
    private TextView downloadTitle, downloadPercent;
    private ProgressBar downloadProgress;
    private Button downloadCancel;

    private GravitWebSocketClient wsClient;

    public static class ServerCard {
        public String title;
        public String profileName;
        public String version;
        public String desc;
        public int online;
        public int maxOnline;

        public ServerCard(String title, String profileName, String version, String desc, int online, int maxOnline) {
            this.title = title;
            this.profileName = profileName;
            this.version = version;
            this.desc = desc;
            this.online = online;
            this.maxOnline = maxOnline;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vxeno_main);

        topBar = findViewById(R.id.vxeno_top_bar);
        tabAccount = findViewById(R.id.vxeno_tab_account);
        tabServers = findViewById(R.id.vxeno_tab_servers);
        tabSettings = findViewById(R.id.vxeno_tab_settings);

        downloaderLayout = findViewById(R.id.vxeno_downloader_layout);
        downloadTitle = findViewById(R.id.vxeno_download_title);
        downloadPercent = findViewById(R.id.vxeno_download_percent);
        downloadProgress = findViewById(R.id.vxeno_download_progress);
        downloadCancel = findViewById(R.id.vxeno_download_cancel);

        setupTabs();

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedUser = prefs.getString(KEY_USER, null);
        String savedPass = prefs.getString(KEY_PASS, null);

        if (savedUser != null && savedPass != null) {
            openMainTabs();
        } else {
            showWelcomeScreen();
        }
    }

    private void setupTabs() {
        tabAccount.setOnClickListener(v -> {
            selectTab(0);
            showFragment(new AccountFragment());
        });
        tabServers.setOnClickListener(v -> {
            selectTab(1);
            showFragment(new ServerListFragment());
        });
        tabSettings.setOnClickListener(v -> {
            selectTab(2);
            showFragment(new SettingsFragment());
        });
    }

    private void selectTab(int index) {
        tabAccount.setBackgroundResource(index == 0 ? R.drawable.vxeno_top_bar_selected : R.drawable.vxeno_top_bar_unselected);
        tabServers.setBackgroundResource(index == 1 ? R.drawable.vxeno_top_bar_selected : R.drawable.vxeno_top_bar_unselected);
        tabSettings.setBackgroundResource(index == 2 ? R.drawable.vxeno_top_bar_selected : R.drawable.vxeno_top_bar_unselected);
    }

    public void openMainTabs() {
        topBar.setVisibility(View.VISIBLE);
        selectTab(1);
        showFragment(new ServerListFragment());
    }

    public void showWelcomeScreen() {
        topBar.setVisibility(View.GONE);
        showFragment(new WelcomeFragment());
    }

    public void showLoginScreen() {
        topBar.setVisibility(View.GONE);
        showFragment(new LoginFragment());
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.vxeno_content_container, fragment)
                .commitAllowingStateLoss();
    }

    public void launchGame(String profileName, String versionId) {
        downloaderLayout.setVisibility(View.VISIBLE);
        downloadTitle.setText("Синхронизация " + profileName + "...");
        downloadPercent.setText("0%");
        downloadProgress.setProgress(0);

        downloadCancel.setOnClickListener(v -> {
            downloaderLayout.setVisibility(View.GONE);
            Toast.makeText(this, "Запуск отменен", Toast.LENGTH_SHORT).show();
        });

        File clientDir = new File(getExternalFilesDir(null), "profiles/" + profileName);
        if (!clientDir.exists()) clientDir.mkdirs();

        GravitUpdateService.syncProfile(this, profileName, clientDir, new GravitUpdateService.SyncCallback() {
            @Override
            public void onProgress(String currentFile, int percent) {
                runOnUiThread(() -> {
                    downloadTitle.setText(currentFile);
                    downloadPercent.setText(percent + "%");
                    downloadProgress.setProgress(percent);
                });
            }

            @Override
            public void onCompleted() {
                runOnUiThread(() -> {
                    downloaderLayout.setVisibility(View.GONE);
                    Toast.makeText(VXenoMainActivity.this, "Файлы проверены! Запуск игры...", Toast.LENGTH_SHORT).show();
                    Intent gameIntent = new Intent(VXenoMainActivity.this, MainActivity.class);
                    gameIntent.putExtra(MainActivity.INTENT_MINECRAFT_VERSION, versionId != null ? versionId : "1.20.1");
                    startActivity(gameIntent);
                });
            }

            @Override
            public void onError(Exception error) {
                runOnUiThread(() -> {
                    downloaderLayout.setVisibility(View.GONE);
                    Toast.makeText(VXenoMainActivity.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // --- Фрагмент приветствия (в стиле CubixWorld) ---
    public static class WelcomeFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_vxeno_welcome, container, false);
            v.findViewById(R.id.vxeno_btn_open_login).setOnClickListener(btn -> {
                ((VXenoMainActivity) requireActivity()).showLoginScreen();
            });
            v.findViewById(R.id.vxeno_btn_open_register).setOnClickListener(btn -> {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://haxzer.online:9274/"));
                startActivity(browser);
            });
            return v;
        }
    }

    // --- Фрагмент авторизации (в стиле CubixWorld) ---
    public static class LoginFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_vxeno_login, container, false);
            EditText inputUser = v.findViewById(R.id.vxeno_input_login);
            EditText inputPass = v.findViewById(R.id.vxeno_input_password);
            CheckBox chkSave = v.findViewById(R.id.vxeno_chk_save_password);
            TextView txtStatus = v.findViewById(R.id.vxeno_login_status);
            Button btnSubmit = v.findViewById(R.id.vxeno_btn_submit_login);

            btnSubmit.setOnClickListener(b -> {
                String login = inputUser.getText().toString().trim();
                String pass = inputPass.getText().toString();

                if (login.isEmpty() || pass.isEmpty()) {
                    txtStatus.setVisibility(View.VISIBLE);
                    txtStatus.setText("Заполните все поля");
                    return;
                }

                txtStatus.setVisibility(View.VISIBLE);
                txtStatus.setText("Авторизация через LaunchServer...");

                GravitWebSocketClient client = new GravitWebSocketClient("ws://haxzer.online:9274/api", new GravitWebSocketClient.Callback() {
                    @Override
                    public void onConnected() {}

                    @Override
                    public void onAuthSuccess(String username, String accessToken) {
                        requireActivity().runOnUiThread(() -> {
                            if (chkSave.isChecked()) {
                                SharedPreferences.Editor ed = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
                                ed.putString(KEY_USER, username);
                                ed.putString(KEY_PASS, pass);
                                ed.apply();
                            }
                            try {
                                MinecraftAccount account = new MinecraftAccount();
                                account.username = username;
                                account.accessToken = accessToken != null ? accessToken : "0";
                                account.save();
                            } catch (Exception ignored) {}

                            ((VXenoMainActivity) requireActivity()).openMainTabs();
                        });
                    }

                    @Override
                    public void onAuthError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            txtStatus.setVisibility(View.VISIBLE);
                            txtStatus.setText("Ошибка: " + error);
                        });
                    }

                    @Override
                    public void onProfilesReceived(List<String> profiles) {}
                });

                client.connectAndAuth(login, pass);
            });

            return v;
        }
    }

    // --- Фрагмент серверов ---
    public static class ServerListFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_vxeno_servers, container, false);
            RecyclerView rv = v.findViewById(R.id.vxeno_servers_recycler);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));

            List<ServerCard> list = new ArrayList<>();
            list.add(new ServerCard("NewEra", "NewEra", "1.20.1 Fabric", "Высокотехнологичный сервер с индустриальной магией", 18, 100));
            list.add(new ServerCard("HiTech", "HiTech", "1.12.2 Forge", "Классический индустриальный мир с модами", 32, 100));
            list.add(new ServerCard("TechnoMagic", "TechnoMagic", "1.12.2 Forge", "Слияние передовых машин и древней магии", 25, 100));

            rv.setAdapter(new ServerAdapter(list, (VXenoMainActivity) requireActivity()));
            return v;
        }
    }

    public static class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.VH> {
        private final List<ServerCard> list;
        private final VXenoMainActivity activity;

        public ServerAdapter(List<ServerCard> list, VXenoMainActivity activity) {
            this.list = list;
            this.activity = activity;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vxeno_server, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ServerCard item = list.get(position);
            holder.txtName.setText(item.title);
            holder.txtOnline.setText("Онлайн: " + item.online + "/" + item.maxOnline);
            holder.txtDesc.setText(item.desc + " (" + item.version + ")");

            holder.btnMods.setOnClickListener(v -> {
                VXenoOptionalModsDialog dialog = new VXenoOptionalModsDialog(item.profileName, null);
                dialog.show(activity.getSupportFragmentManager(), "vxeno_mods");
            });

            holder.btnPlay.setOnClickListener(v -> {
                activity.launchGame(item.profileName, item.version.split(" ")[0]);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView txtName, txtOnline, txtDesc;
            Button btnMods, btnPlay;

            public VH(@NonNull View itemView) {
                super(itemView);
                txtName = itemView.findViewById(R.id.vxeno_server_name);
                txtOnline = itemView.findViewById(R.id.vxeno_server_online);
                txtDesc = itemView.findViewById(R.id.vxeno_server_desc);
                btnMods = itemView.findViewById(R.id.vxeno_btn_optional_mods);
                btnPlay = itemView.findViewById(R.id.vxeno_btn_play_server);
            }
        }
    }

    // --- Фрагмент профиля игрока ---
    public static class AccountFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_vxeno_account, container, false);
            TextView txtUser = v.findViewById(R.id.vxeno_profile_username);
            SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            txtUser.setText(prefs.getString(KEY_USER, "Игрок"));

            v.findViewById(R.id.vxeno_btn_logout).setOnClickListener(b -> {
                prefs.edit().clear().apply();
                ((VXenoMainActivity) requireActivity()).showWelcomeScreen();
            });
            return v;
        }
    }

    // --- Фрагмент настроек (ОЗУ и кнопки) ---
    public static class SettingsFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_vxeno_settings, container, false);
            SeekBar seek = v.findViewById(R.id.vxeno_ram_seekbar);
            TextView lbl = v.findViewById(R.id.vxeno_ram_label);

            int currentRam = LauncherPreferences.PREF_RAM_ALLOCATION;
            seek.setProgress(currentRam);
            lbl.setText("Выделение оперативной памяти (ОЗУ): " + currentRam + " МБ");

            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress < 512) progress = 512;
                    lbl.setText("Выделение оперативной памяти (ОЗУ): " + progress + " МБ");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int val = seekBar.getProgress();
                    if (val < 512) val = 512;
                    LauncherPreferences.PREF_RAM_ALLOCATION = val;
                    LauncherPreferences.computeRamAllocation(requireContext());
                }
            });

            v.findViewById(R.id.vxeno_btn_open_controls).setOnClickListener(b -> {
                startActivity(new Intent(requireContext(), CustomControlsActivity.class));
            });

            return v;
        }
    }
}