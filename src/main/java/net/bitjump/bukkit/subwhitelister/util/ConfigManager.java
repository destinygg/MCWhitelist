package net.bitjump.bukkit.subwhitelister.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
	private static JavaPlugin plugin;
	private static final HashMap<String, FileConfiguration> configs = new HashMap<String, FileConfiguration>();

	public static void setup(JavaPlugin plugin) {
		ConfigManager.plugin = plugin;
	}

	public static FileConfiguration setupConfig() {
		return setupConfig("config.yml");
	}

	public static FileConfiguration setupConfig(String s) {
		File f = new File(plugin.getDataFolder(), s);

		if (!f.exists()) {
			f.getParentFile().mkdirs();
			copy(plugin.getResource(s), f);
		}

		FileConfiguration config = YamlConfiguration.loadConfiguration(f);

		configs.put(s, config);
		return config;
	}

	private static void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveConfig(String s) {
		try {
			configs.get(s).save(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}