package net.bitjump.bukkit.subwhitelister.commands;

import org.apache.commons.lang.StringUtils;

import net.bitjump.bukkit.subwhitelister.util.WhitelistManager;

public class ListCommand extends Subcommand 
{
	public ListCommand()
	{
		this.name = "list";
		this.description = "Lists whitelisted users.";
		this.permission = "subwhitelister.list";
	}
	
	public void perform()
	{	
		String mess = "Users: ";
		mess += StringUtils.join(WhitelistManager.getUsers(), ", ");
		
		sender.sendMessage(mess);
	}

}
