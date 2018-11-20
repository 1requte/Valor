package net.xfoondom.valor;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class ValorDelayer implements Runnable {

	private int id;
	private JavaPlugin plugin;
	
	public ValorDelayer(JavaPlugin plugin, int arg1) {
		this.plugin = plugin;
		id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, arg1);
	}
	
	public void stop() {
		plugin.getServer().getScheduler().cancelTask(id);
	}
}
