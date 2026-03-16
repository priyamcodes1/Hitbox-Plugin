package itzglecni.customhitboxes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "customhitboxes.json");
    private static ModConfig instance = new ModConfig();

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                instance = GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                System.err.println("[CustomHitboxes] Critical failure reading configuration JSON.");
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            System.err.println("[CustomHitboxes] Critical failure writing configuration JSON.");
            e.printStackTrace();
        }
    }

    public static ModConfig getConfig() {
        return instance;
    }
}