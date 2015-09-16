package net.bitjump.bukkit.subwhitelister;

import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.destinygg.mcwhitelist.DestinyGGAuthPlayerListenerImpl;

import net.bitjump.bukkit.subwhitelister.commands.CommandManager;
import net.bitjump.bukkit.subwhitelister.commands.ExportCommand;
import net.bitjump.bukkit.subwhitelister.commands.ListCommand;
import net.bitjump.bukkit.subwhitelister.commands.ReloadCommand;
import net.bitjump.bukkit.subwhitelister.commands.ToggleCommand;
import net.bitjump.bukkit.subwhitelister.util.ConfigManager;
import net.bitjump.bukkit.subwhitelister.util.WhitelistManager;

public class SubWhitelister extends JavaPlugin
{
	public static JavaPlugin instance;
	
	public static Logger LOGGER;

	public static FileConfiguration config;
	public static PluginDescriptionFile pdf;

	public static String name;
	public static String version;
	public static String author;
	
	public static CommandManager cm;
	
	public void onEnable()
	{
		LOGGER = getLogger();
		
		LOGGER.info("Plugin initializing...");
		
		pdf = getDescription();

		name = pdf.getName();
		version = pdf.getVersion();
		author = pdf.getAuthors().get(0);
		
		ConfigManager.setup(this);
		config = ConfigManager.setupConfig();
		
		LOGGER.info("Setting up commands...");
		cm = new CommandManager(this);
		cm.setCommandPrefix("sw");
		cm.registerCommand(new ListCommand());
		cm.registerCommand(new ReloadCommand());
		cm.registerCommand(new ToggleCommand());
		cm.registerCommand(new ExportCommand());
				
		LOGGER.info("Setting up listeners...");
		getServer().getPluginManager().registerEvents(new DestinyGGAuthPlayerListenerImpl(), this);
		
		instance = this;
		
		WhitelistManager.initialize();
	}
	
	public void onDisable()
	{
		
	}
}
