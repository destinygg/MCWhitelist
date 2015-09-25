/**
 * 
 */
package org.destinygg.mcwhitelist;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Defines how player should be authenticated on pre-login events
 * 
 * Note @EventHandler annotations must be placed on relevant event handlers in
 * the implementation, since Bukkit does NOT observer inheritance when
 * registering plugins (reflectively)
 * 
 * @author xtphty
 *
 */
public interface PlayerAuthListener extends Listener {
	/**
	 * Initializes the listener with the given config (avoiding static ref to
	 * config)
	 * 
	 * @param authConfig
	 *            the auth config to initialize with
	 */
	public void initializeListener(FileConfiguration authConfig);
	
	/**
	 * Authenticates players trying to log in
	 * 
	 * @param event
	 *            the player's pre login event
	 */
	public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event);

	/**
	 * Announces the player when they have joined the server
	 * 
	 * @param event
	 *            the player's join event
	 */
	public void onPlayerJoinEvent(PlayerJoinEvent event);

	/**
	 * The announcement message to display for players allowed in by
	 * {@link #onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent)}
	 * 
	 * Note: This should be used on the main server thread, only after the
	 * player has logged in (or they wont see the message)
	 * 
	 * @param mcName
	 *            the player's minecraft name
	 * @param authName
	 *            the player's login name
	 * @return A formatted player announcement message. Returns null if no
	 *         message should be displayed
	 */
	public String getAnnounceMessage(String mcName, String authName);
}
