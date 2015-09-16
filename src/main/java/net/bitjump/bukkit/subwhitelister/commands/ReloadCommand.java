package net.bitjump.bukkit.subwhitelister.commands;

import net.bitjump.bukkit.subwhitelister.util.WhitelistManager;

public class ReloadCommand extends Subcommand 
{
	public ReloadCommand()
	{
		this.name = "reload";
		this.description = "Reloads remote whitelist";
		this.permission = "subwhitelister.reload";
	}
	
	public void perform()
	{	
		WhitelistManager.updateRemoteWhitelists();
		sender.sendMessage("Remote whitelist reloaded.");
	}

}
