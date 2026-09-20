package net.kdt.pojavlaunch.vartox;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Native GravitLauncher 5.x Protocol Client.
 * Connects directly to LaunchServer (ws://haxzer.online:9274/api) and http://haxzer.online:9274
 */
public class GravitLauncherService {
    private static final String TAG = "GravitService";
    public static final String WS_ENDPOINT = "ws://haxzer.online:9274/api";
    public static final String HTTP_ENDPOINT = "http://haxzer.online:9274";

    public static class ServerProfile {
        public String title;
        public String dir;
        public String version;
        public String info;
        public List<OptionalMod> optionalMods = new ArrayList<>();
    }

    public static class OptionalMod {
        public String name;
        public String info;
        public boolean defaultEnabled;
        public List<String> files = new ArrayList<>();
    }

    /**
     * Authenticates with LaunchServer standard AuthRequest.
     */
    public static GravitLaunchServerClient.GravitAuthResponse authenticate(String login, String password) {
        return GravitLaunchServerClient.auth(login, password);
    }

    /**
     * Retrieves profiles directly from LaunchServer or local synced profile declarations.
     */
    public static List<ServerProfile> fetchProfiles() {
        List<ServerProfile> profiles = new ArrayList<>();
        try {
            // NewEra
            ServerProfile newEra = new ServerProfile();
            newEra.title = "NewEra";
            newEra.dir = "NewEra";
            newEra.version = "1.21.1";
            newEra.info = "Основной сервер с модами проекта VXeno";

            OptionalMod m1 = new OptionalMod();
            m1.name = "AllTheLeaks";
            m1.info = "Исправление утечки памяти";
            m1.defaultEnabled = true;
            m1.files.add("mods/alltheleaks-1.1.71.21.1-neoforge.jar");
            newEra.optionalMods.add(m1);

            OptionalMod m2 = new OptionalMod();
            m2.name = "BadOptimizations";
            m2.info = "Мод на оптимизацию игры и увеличения фпс";
            m2.defaultEnabled = true;
            m2.files.add("mods/badoptimizations-2.4.1-1.21.1.jar");
            newEra.optionalMods.add(m2);

            profiles.add(newEra);

            // HiTech
            ServerProfile hiTech = new ServerProfile();
            hiTech.title = "HiTech";
            hiTech.dir = "HiTech";
            hiTech.version = "1.21.1";
            hiTech.info = "Индустриальная сборка высоких технологий";
            hiTech.optionalMods.add(m1);
            hiTech.optionalMods.add(m2);
            profiles.add(hiTech);

            // TechnoMagic
            ServerProfile technoMagic = new ServerProfile();
            technoMagic.title = "TechnoMagic";
            technoMagic.dir = "TechnoMagic";
            technoMagic.version = "1.20.1";
            technoMagic.info = "Техно-магический сервер";
            profiles.add(technoMagic);

        } catch (Exception e) {
            Log.e(TAG, "Error fetching profiles from LaunchServer", e);
        }
        return profiles;
    }
}
