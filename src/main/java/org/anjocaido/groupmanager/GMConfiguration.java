/*
 *  GroupManager - A plug-in for Spigot/Bukkit based Minecraft servers.
 *  Copyright (C) 2020  ElgarL
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.anjocaido.groupmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.World;
import org.anjocaido.groupmanager.localization.Messages;
import org.anjocaido.groupmanager.utils.Tasks;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

/**
 * 
 * @author gabrielcouto
 */
public class GMConfiguration {
	
	private String language;
	private boolean allowCommandBlocks;
	private boolean opOverride;
	private boolean toggleValidate;
	private boolean tabValidate;
	private Integer saveInterval;
	private Integer backupDuration;
	private String loggerLevel; //$NON-NLS-1$
	private Map<String, Object> mirrorsMap;
	private Map<String, Object> GMconfig;
	

	private final GroupManager plugin;

	public GMConfiguration(GroupManager plugin) {

		this.plugin = plugin;
		
		/*
		 * Set defaults
		 */
		language = "english"; //$NON-NLS-1$
		allowCommandBlocks = false;
		opOverride = true;
		toggleValidate = true;
		tabValidate = true;
		saveInterval = 10;
		backupDuration = 24;
		loggerLevel = "OFF"; //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	public void load() {

		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}

		File configFile = new File(plugin.getDataFolder(), "config.yml"); //$NON-NLS-1$
		boolean firstRun = !configFile.exists();

		if (!configFile.exists()) {
			try {
				Tasks.copy(plugin.getResource("config.yml"), configFile); //$NON-NLS-1$
			} catch (IOException ex) {
				GroupManager.logger.log(Level.SEVERE, Messages.getString("GMConfiguration.ERROR_CREATING_CONFIG"), ex); //$NON-NLS-1$
			}
		}

		Yaml configYAML = new Yaml();

		try {
			FileInputStream configInputStream = new FileInputStream(configFile);
			GMconfig = configYAML.load(new UnicodeReader(configInputStream));
			configInputStream.close();

		} catch (Exception ex) {
			throw new IllegalArgumentException(String.format(Messages.getString("GroupManager.FILE_CORRUPT"), configFile.getPath()), ex); //$NON-NLS-1$
		}

		/*
		 * Read our config settings and store them for reading later.
		 */
		try {
			Map<String, Object> config = getElement("config", getElement("settings", GMconfig)); //$NON-NLS-1$ //$NON-NLS-2$

			try {
				language = (String) config.get("language"); //$NON-NLS-1$
			} catch (Exception ex) {
				GroupManager.logger.log(Level.SEVERE, nodeError("language"), ex); //$NON-NLS-1$
			}
			if (language == null || language.isEmpty()) language = "english";
			Messages.setLanguage();
			
			try {
				allowCommandBlocks = (Boolean) config.get("allow_commandblocks"); //$NON-NLS-1$
			} catch (Exception ex) {
				GroupManager.logger.log(Level.SEVERE, nodeError("allow_commandblocks"), ex); //$NON-NLS-1$
				allowCommandBlocks = false;
			}
			
			try {
				opOverride = (Boolean) config.get("opOverrides"); //$NON-NLS-1$
			} catch (Exception ex) {
				GroupManager.logger.log(Level.SEVERE, nodeError("opOverrides"), ex); //$NON-NLS-1$
				opOverride = true;
			}
			
			try {
				toggleValidate = (Boolean) config.get("validate_toggle"); //$NON-NLS-1$
			} catch (Exception ex) {
				GroupManager.logger.log(Level.SEVERE, nodeError("validate_toggle"), ex); //$NON-NLS-1$
				toggleValidate = true;
			}
			
			try {
				tabValidate = (Boolean) config.get("tab_validate"); //$NON-NLS-1$
			} catch (Exception ex) {
				GroupManager.logger.log(Level.SEVERE, nodeError("tab_validate"), ex); //$NON-NLS-1$
				tabValidate = true;
			}

			/*
			 * data node for save/backup timers.
			 */
			try {
				Map<String, Object> save = getElement("save", getElement("data", getElement("settings", GMconfig))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				try {
					saveInterval = (Integer) save.get("minutes"); //$NON-NLS-1$
				} catch (Exception ex) {
					GroupManager.logger.log(Level.SEVERE, nodeError("minutes"), ex); //$NON-NLS-1$
					saveInterval = 10;
				}
				
				try {
					backupDuration = (Integer) save.get("hours"); //$NON-NLS-1$
				} catch (Exception ex) {
					GroupManager.logger.log(Level.SEVERE, nodeError("hours"), ex); //$NON-NLS-1$
					backupDuration = 24;
				}
				
			} catch (Exception ex) {
				GroupManager.logger.log(Level.SEVERE, nodeError("data"), ex); //$NON-NLS-1$
			}



			String level = ((Map<String, String>) getElement("settings", GMconfig).get("logging")).get("level"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (level != null)
				loggerLevel = level;

			/*
			 * Store our mirrors map for parsing later.
			 */
			mirrorsMap = (Map<String, Object>) ((Map<String, Object>) GMconfig.get("settings")).get("mirrors"); //$NON-NLS-1$ //$NON-NLS-2$
			
			if (mirrorsMap == null)
				throw new Exception();

		} catch (Exception ex) {
			/*
			 * Flag the error and use defaults
			 */
			GroupManager.logger.log(Level.SEVERE, Messages.getString("GMConfiguration.ERRORS_IN_CONFIG"), ex); //$NON-NLS-1$
			
			mirrorsMap = new HashMap<>();
		}
		if (firstRun)
			regenerateMirrors();

		// Setup defaults
		adjustLoggerLevel();
	}
	
	private String nodeError(String node) {
		
		return String.format(Messages.getString("GMConfiguration.CORRUPT_NODE"), node); //$NON-NLS-1$
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getElement(String element, Map<String, Object> map) {
		
		if (!map.containsKey(element)) {
			throw new IllegalArgumentException(String.format(Messages.getString("GMConfiguration.MISSING_NODE"), element)); //$NON-NLS-1$
		}
		
		return (Map<String, Object>) map.get(element);
		
	}
	
	public String getLanguage() {

		return language;
	}
	
	public boolean isAllowCommandBlocks() {

		return allowCommandBlocks;
	}

	public boolean isOpOverride() {

		return opOverride;
	}

	public boolean isToggleValidate() {
		
		return toggleValidate;
	}
	
	public void setToggleValidate(Boolean value) {
		
		this.toggleValidate = value;
	}
	
	public boolean isTabValidate() {
		
		return tabValidate;
	}

	public Integer getSaveInterval() {

		return saveInterval;
	}

	public Integer getBackupDuration() {

		return backupDuration;
	}

	public void adjustLoggerLevel() {

		try {
			GroupManager.logger.setLevel(Level.parse(loggerLevel));
			return;
		} catch (Exception ignored) {
		}

		GroupManager.logger.setLevel(Level.INFO);
	}
	
	public Map<String, Object> getMirrorsMap() {

		if (!mirrorsMap.isEmpty()) {
			return mirrorsMap;
		}
		return null;

	}


	/*
	 * First run only: rebuild the mirrors section from the worlds this server
	 * actually loaded, so permissions follow the real world names instead of
	 * the shipped defaults. The mirrors stay a regular config option and can
	 * be edited by hand afterwards.
	 */
	@SuppressWarnings("unchecked")
	private void regenerateMirrors() {

		try {
			List<World> worlds = plugin.getServer().getWorlds();
			if (worlds.isEmpty())
				return;

			// Same default world the WorldsHolder uses (level-name).
			// Bukkit loads the server default world first.
			World defaultWorld = worlds.get(0);

			// parent world -> (child world -> [users, groups])
			Map<String, Map<String, List<String>>> generated = new LinkedHashMap<>();

			for (World world : worlds) {
				if (world.equals(defaultWorld) || world.getEnvironment() == World.Environment.NORMAL)
					continue;

				// Dimensions belong to the normal world sharing their name prefix,
				// otherwise they follow the server default world.
				String primary = defaultWorld.getName();
				String lower = world.getName().toLowerCase();
				if (lower.endsWith("_nether") || lower.endsWith("_the_end")) { //$NON-NLS-1$ //$NON-NLS-2$
					String candidate = lower.endsWith("_nether") //$NON-NLS-1$
							? world.getName().substring(0, world.getName().length() - 7)
							: world.getName().substring(0, world.getName().length() - 8);
					for (World w : worlds) {
						if (w.getEnvironment() == World.Environment.NORMAL && w.getName().equalsIgnoreCase(candidate)) {
							primary = w.getName();
							break;
						}
					}
				}

				generated.computeIfAbsent(primary, k -> new LinkedHashMap<>())
						.put(world.getName(), new ArrayList<>(List.of("users", "groups"))); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Keep the all_unnamed_worlds option: anything unlisted follows the default world.
			Map<String, List<String>> defaultChildren = generated.computeIfAbsent(defaultWorld.getName(), k -> new LinkedHashMap<>());
			defaultChildren.putIfAbsent("all_unnamed_worlds", new ArrayList<>(List.of("users", "groups"))); //$NON-NLS-1$ //$NON-NLS-2$

			// Apply in memory so this first enable already uses them.
			Map<String, Object> settings = (Map<String, Object>) GMconfig.get("settings"); //$NON-NLS-1$
			if (settings == null) {
				settings = new LinkedHashMap<>();
				GMconfig.put("settings", settings); //$NON-NLS-1$
			}
			settings.put("mirrors", (Map<String, Object>) (Map<?, ?>) generated);
			mirrorsMap = (Map<String, Object>) (Map<?, ?>) generated;

			// Rewrite only the mirrors section in config.yml; keep everything above it.
			File configFile = new File(plugin.getDataFolder(), "config.yml"); //$NON-NLS-1$
			List<String> lines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
			List<String> out = new ArrayList<>();
			boolean cut = false;
			for (String line : lines) {
				if (!cut && line.startsWith("  ") && line.trim().equals("mirrors:")) { //$NON-NLS-1$ //$NON-NLS-2$
					cut = true;
					break;
				}
				out.add(line);
			}
			if (!cut)
				return; // unexpected config shape, leave the file alone

			out.add("  mirrors:"); //$NON-NLS-1$
			out.add("        # Worlds listed here use the same users/groups files as their parent world."); //$NON-NLS-1$
			out.add("        # Auto-generated from the worlds this server loaded on first start."); //$NON-NLS-1$
			out.add("        # Edit freely (delete all_unnamed_worlds to give unlisted worlds their own data),"); //$NON-NLS-1$
			out.add("        # then restart the server or run /manload to apply."); //$NON-NLS-1$
			for (Map.Entry<String, Map<String, List<String>>> parent : generated.entrySet()) {
				out.add("        " + quote(parent.getKey()) + ":"); //$NON-NLS-1$
				for (Map.Entry<String, List<String>> child : parent.getValue().entrySet()) {
					out.add("          " + quote(child.getKey()) + ":"); //$NON-NLS-1$
					for (String type : child.getValue())
						out.add("          - " + type); //$NON-NLS-1$
				}
			}
			out.add("    #   world2:      (World2 would have it's own set of user and groups files)"); //$NON-NLS-1$
			out.add("    #     world3:"); //$NON-NLS-1$
			out.add("    #     - users    (World3 would use the users.yml from world2, but it's own groups.yml)"); //$NON-NLS-1$
			out.add("    #     world4:"); //$NON-NLS-1$
			out.add("    #     - groups   (World4 would use the groups.yml from world2, but it's own users.yml)"); //$NON-NLS-1$
			out.add("    #   world5:"); //$NON-NLS-1$
			out.add("    #     - world6   (this would cause world6 to mirror both files from world5)"); //$NON-NLS-1$
			Files.write(configFile.toPath(), out, StandardCharsets.UTF_8);
			GroupManager.logger.info("Generated mirrors for the loaded worlds: " + generated.keySet()); //$NON-NLS-1$
		} catch (Exception ex) {
			GroupManager.logger.log(Level.WARNING, "Could not auto-generate mirrors, keeping the shipped defaults", ex); //$NON-NLS-1$
		}
	}

	private String quote(String value) {

		return new Yaml().dump(value).trim();
	}
}