package net.bitjump.bukkit.subwhitelister.commands;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registers a user with their privateKey
 * @author xtphty
 *
 */
public class RegisterCommand implements CommandExecutor {
	private static final String USAGE = "Invalid command, use: /register <private key>";
	private JavaPlugin plugin;

	public RegisterCommand(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO Auto-generated method stub
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only Players can register!");
			return false;
		}

		plugin.getLogger().info("Command: " + command.toString());

		if (args.length == 0) {
			sender.sendMessage(USAGE);
			return false;
		}

		Player player = (Player) sender;
		UUID uuid = player.getUniqueId();
		String username = player.getName();
		String authToken = "auth-token"; // not sure why this is needed, it
											// should be automated too probably

		// TODO: validate key length and characters before request
		String privateKey = args[0];

		// TODO: call the API and confirm Minecraft name and API key
		plugin.getLogger().log(Level.INFO, "Register: username: " + username + " , uuid: " + uuid.toString()
				+ ", authToken: " + authToken + ", privateKey: " + privateKey);

		return true;
	}

}
