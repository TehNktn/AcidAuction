package ru.nokton.acidauction;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Files {
    private JavaPlugin plugin;
    private HashMap<String, File> files;
    private HashMap<String, YamlConfiguration> yamls;

    public Files(JavaPlugin plugin) {
        this.plugin = plugin;
        this.files = new HashMap();
        this.yamls = new HashMap();
    }

    public YamlConfiguration registerNewFile(String name) {
        if (this.files.containsKey(name)) {
            return (YamlConfiguration)this.yamls.get(name);
        } else {
            File file = new File(this.plugin.getDataFolder() + File.separator + name + ".yml");
            if (!file.exists()) {
                this.plugin.saveResource(name + ".yml", false);
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            this.yamls.put(name, yaml);
            this.files.put(name, file);
            return yaml;
        }
    }

    public File getFile(String name) {
        return (File)this.files.get(name);
    }

    public YamlConfiguration getYaml(String name) {
        return (YamlConfiguration)this.yamls.get(name);
    }

    public void save(String name, YamlConfiguration yaml) {
        try {
            yaml.save(this.getFile(name));
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public void reload(String fileName) {
        File file = new File(this.plugin.getDataFolder() + File.separator + fileName + ".yml");
        if (!file.exists()) {
            this.plugin.saveResource(fileName + ".yml", false);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        this.yamls.put(fileName, yaml);
        this.files.put(fileName, file);
    }

    public void reload() {
        Iterator var1 = this.files.keySet().iterator();

        while(var1.hasNext()) {
            String fileName = (String)var1.next();
            File file = new File(this.plugin.getDataFolder() + File.separator + fileName + ".yml");
            if (!file.exists()) {
                this.plugin.saveResource(fileName + ".yml", false);
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            this.yamls.put(fileName, yaml);
            this.files.put(fileName, file);
        }

    }
}
