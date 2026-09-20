package net.kdt.pojavlaunch.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.value.MinecraftAccount;
import net.kdt.pojavlaunch.vartox.VXenoAuthClient;

import java.io.File;

public class SelectAuthFragment extends Fragment {
    public static final String TAG = "AUTH_SELECT_FRAGMENT";
    private static final String PREF_NAME = "vxeno_auth_prefs";
    private static final String KEY_SAVED_LOGIN = "saved_login";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private static final String KEY_AUTO_LOGIN = "auto_login";
    private static final String KEY_SAVE_PASSWORD = "save_password";

    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private CheckBox mSavePasswordCheck;
    private CheckBox mAutoLoginCheck;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    private SharedPreferences mPrefs;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    public SelectAuthFragment(){
        super(R.layout.fragment_select_auth_method);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mUsernameEdit = view.findViewById(R.id.vxeno_username);
        mPasswordEdit = view.findViewById(R.id.vxeno_password);
        mSavePasswordCheck = view.findViewById(R.id.vxeno_save_password);
        mAutoLoginCheck = view.findViewById(R.id.vxeno_auto_login);
        mLoginButton = view.findViewById(R.id.vxeno_login_button);
        mProgressBar = view.findViewById(R.id.vxeno_progress);

        if (getContext() != null) {
            mPrefs = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            boolean savePass = mPrefs.getBoolean(KEY_SAVE_PASSWORD, true);
            boolean autoLogin = mPrefs.getBoolean(KEY_AUTO_LOGIN, true);
            String savedLogin = mPrefs.getString(KEY_SAVED_LOGIN, "");
            String savedPassword = mPrefs.getString(KEY_SAVED_PASSWORD, "");

            mSavePasswordCheck.setChecked(savePass);
            mAutoLoginCheck.setChecked(autoLogin);
            mUsernameEdit.setText(savedLogin);
            if (savePass) {
                mPasswordEdit.setText(savedPassword);
            }

            if (autoLogin && !savedLogin.isEmpty() && !savedPassword.isEmpty()) {
                performLogin(savedLogin, savedPassword);
            }
        }

        mLoginButton.setOnClickListener(v -> {
            String login = mUsernameEdit.getText().toString().trim();
            String password = mPasswordEdit.getText().toString().trim();
            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Введите логин и пароль", Toast.LENGTH_SHORT).show();
                return;
            }
            performLogin(login, password);
        });
    }

    private void performLogin(final String login, final String password) {
        mLoginButton.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            final VXenoAuthClient.AuthResult result = VXenoAuthClient.authenticate(login, password);

            mMainHandler.post(() -> {
                if (getContext() == null) return;
                mProgressBar.setVisibility(View.GONE);
                mLoginButton.setEnabled(true);

                if (result.success) {
                    if (mPrefs != null) {
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putBoolean(KEY_SAVE_PASSWORD, mSavePasswordCheck.isChecked());
                        editor.putBoolean(KEY_AUTO_LOGIN, mAutoLoginCheck.isChecked());
                        editor.putString(KEY_SAVED_LOGIN, login);
                        if (mSavePasswordCheck.isChecked()) {
                            editor.putString(KEY_SAVED_PASSWORD, password);
                        } else {
                            editor.remove(KEY_SAVED_PASSWORD);
                        }
                        editor.apply();
                    }

                    MinecraftAccount account = new MinecraftAccount();
                    account.username = result.username;
                    account.profileId = result.uuid;
                    account.accessToken = result.accessToken;
                    account.clientToken = "vxeno_client";
                    account.selectedVersion = "1.21.1";

                    try {
                        new File(Tools.DIR_ACCOUNT_NEW).mkdirs();
                        account.save(Tools.DIR_ACCOUNT_NEW + "/" + account.username + ".json");
                    } catch (Exception ignored) {}

                    ExtraCore.setValue(ExtraConstants.MOJANG_LOGIN_TODO, new String[]{ account.username, "" });
                    Tools.swapFragment(requireActivity(), MainMenuFragment.class, MainMenuFragment.TAG, null);
                } else {
                    Toast.makeText(getContext(), "Ошибка входа: " + result.errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}
