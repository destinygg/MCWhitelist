package org.destinygg.mcwhitelist;

import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import net.bitjump.bukkit.subwhitelister.SubWhitelister;
import net.bitjump.bukkit.subwhitelister.util.ConfigManager;

/**
 * Base test class that provides a mock plugin with necessary
 * configurations / initializations
 * 
 * @author xtphty
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SubWhitelister.class)
public abstract class AbstractMCPluginTest {
	private SubWhitelister mockPlugin = null;

	public SubWhitelister getMockPlugin() throws URISyntaxException {
		if (this.mockPlugin == null) {
			// Create mock plugin and initalilize static fields / methods
			// required
			this.mockPlugin = PowerMockito.mock(SubWhitelister.class);
			Whitebox.setInternalState(SubWhitelister.class, "LOGGER", Logger.getLogger(SubWhitelister.class.getName()));
			when(mockPlugin.getDataFolder()).thenReturn(new File(ClassLoader.getSystemResource(".").toURI()));
			ConfigManager.setup(mockPlugin);
			Whitebox.setInternalState(SubWhitelister.class, "config", ConfigManager.setupConfig());
		}
		return this.mockPlugin;
	}

}
