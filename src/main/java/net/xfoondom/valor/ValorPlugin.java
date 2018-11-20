package net.xfoondom.valor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.xfoondom.valor.command.CommandPvP;

public class ValorPlugin extends JavaPlugin {
	
	private String pluginName = "Valor";
	private String pluginVersion = "1.0";
	
	private ValorUtil util;

	protected ArrayList<UUID> flaggedPlayers;
	protected ArrayList<UUID> enteringPlayers;
	protected ArrayList<UUID> leavingPlayers;
	protected ArrayList<UUID> queuedPlayers;
	protected ArrayList<UUID> fightingPlayers;
	
	protected File rewardsFile = new File(this.getDataFolder()+"/rewards.yml");
	protected FileConfiguration rewards = YamlConfiguration.loadConfiguration(rewardsFile);

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		loadConfig();
		Bukkit.getServer().getPluginManager().registerEvents(new ValorEvent(this), this);
		this.getCommand("pvp").setExecutor(new CommandPvP(this));
		
		util = new ValorUtil(this);
		
		flaggedPlayers = new ArrayList<UUID>();
		enteringPlayers = new ArrayList<UUID>();
		leavingPlayers = new ArrayList<UUID>();
		queuedPlayers = new ArrayList<UUID>();
		fightingPlayers = new ArrayList<UUID>();
		
		
		System.out.println(">> " + pluginName + " v" + pluginVersion + " enabled");
	}
	
	@Override
	public void onDisable() {
		System.out.println(">> " + pluginName + " v" + pluginVersion + " enabled");
	}
	
	public void loadConfig() {
		this.reloadConfig();
		this.saveConfig();
		this.saveRewards();
	}

	public void saveRewards() {
		try {
			rewards.save(rewardsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ValorUtil getUtil() {
		return util;
	}
	
}
