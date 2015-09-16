package net.bitjump.bukkit.subwhitelister.commands;

import net.bitjump.bukkit.subwhitelister.SubWhitelister;
import net.bitjump.bukkit.subwhitelister.util.ConfigManager;

public class ToggleCommand extends Subcommand 
{	
	public ToggleCommand()
	{
		this.name = "toggle";
		this.description = "Toggles remote whitelist";
		this.permission = "subwhitelister.toggle";
	}
	
	public void perform()
	{	
		boolean b = SubWhitelister.config.getBoolean("enabled");
		b ^= true;
		
		sender.sendMessage("Remote whitelist " + (b ? "enabled!" : "disabled!"));
		
		SubWhitelister.config.set("enabled", b);
		ConfigManager.saveConfig("config.yml");
	}

}
