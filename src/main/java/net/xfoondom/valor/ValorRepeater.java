package net.xfoondom.valor;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class ValorRepeater implements Runnable {

	private int id;
	private JavaPlugin plugin;
	
	public ValorRepeater(JavaPlugin plugin, int arg1, int arg2) {
		this.plugin = plugin;
		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, arg1, arg2);
	}
	
	public void stop() {
		plugin.getServer().getScheduler().cancelTask(id);
	}
}
