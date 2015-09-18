package org.destinygg.mcwhitelist;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.destinygg.mcwhitelist.auth.AuthService;
import org.destinygg.mcwhitelist.auth.DestinyGGAuthServiceImpl;

/**
 * {@link DestinyGGAuthServiceImpl} based {@link PlayerAuthListener}
 * 
 * @author xtphty
 *
 */
public class DestinyGGPlayerAuthListener extends AbstractPlayerAuthListener implements PlayerAuthListener {
	private static Logger LOGGER = Logger.getLogger(AbstractPlayerAuthListener.class.getName());

	@Override
	public AuthService createAuthService(FileConfiguration authConfig) {
		LOGGER.info("DestinyGGAuthServiceImpl service initializing...");
		return new DestinyGGAuthServiceImpl(authConfig.getString("authentication.privateKey"),
				authConfig.getString("authentication.apiUrl"));
	}

	@Override
	public String getAnnounceMessage(String mcName, String authName) {
		if (StringUtils.isBlank(mcName) || StringUtils.isBlank(authName)) {
			return null;
		} else {
			return "destiny.gg sub " + ChatColor.RED + authName + ChatColor.WHITE + " connected as " + ChatColor.BLUE
					+ mcName;
		}
	}
}
