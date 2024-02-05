package me.kenvera.chronocore.Database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DataManager {
    private final JavaPlugin plugin;
    private final HashMap<String, DataManager.Config> configs = new HashMap();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public DataManager.Config getConfig(String name) {
        if (!this.configs.containsKey(name)) {
            this.configs.put(name, new DataManager.Config(name));
        }

        return (DataManager.Config)this.configs.get(name);
    }

    public DataManager.Config saveConfig(String name) {
        return this.getConfig(name).save();
    }

    public DataManager.Config reloadConfig(String name) {
        return this.getConfig(name).reload();
    }

    public class Config {
        private final String name;
        private File file;
        private YamlConfiguration config;

        public Config(String name) {
            this.name = name;
        }

        public DataManager.Config save() {
            if (this.config != null && this.file != null) {
                try {
                    if (((ConfigurationSection)Objects.requireNonNull(this.config.getConfigurationSection(""))).getKeys(true).size() != 0) {
                        this.config.save(this.file);
                    }
                } catch (IOException var2) {
                    var2.printStackTrace();
                }

                return this;
            } else {
                return this;
            }
        }

        public YamlConfiguration get() {
            if (this.config == null) {
                this.reload();
            }

            return this.config;
        }

        public DataManager.Config saveDefaultConfig() {
            this.file = new File(DataManager.this.plugin.getDataFolder(), this.name);
            DataManager.this.plugin.saveResource(this.name, false);
            return this;
        }

        public DataManager.Config reload() {
            if (this.file == null) {
                this.file = new File(DataManager.this.plugin.getDataFolder(), this.name);
            }

            this.config = YamlConfiguration.loadConfiguration(this.file);

            try {
                Reader defConfigStream = new InputStreamReader((InputStream)Objects.requireNonNull(DataManager.this.plugin.getResource(this.name)), StandardCharsets.UTF_8);
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                this.config.setDefaults(defConfig);
            } catch (NullPointerException var3) {
            }

            return this;
        }

        public DataManager.Config copyDefaults(boolean force) {
            this.get().options().copyDefaults(force);
            return this;
        }

        public DataManager.Config set(String key, Object value) {
            this.get().set(key, value);
            return this;
        }

        public Object get(String key) {
            return this.get().get(key);
        }
    }
}
