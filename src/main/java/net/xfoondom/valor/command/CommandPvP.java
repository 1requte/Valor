package net.xfoondom.valor.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.xfoondom.valor.ValorPlugin;
import net.xfoondom.valor.ValorUtil;

public class CommandPvP implements CommandExecutor {
	
	private ValorPlugin plugin;
	
	public CommandPvP(ValorPlugin plugin) {
		this.plugin = plugin;
	}
//	Pls ikke lav på det her klokken pis om natten
//	gør det når du ikke er ristet
//	tak på forhånd,
//	
//	- fortidige emil <3
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		Player player = (Player) sender;
		if(arguments.length == 0) {
			if(player.hasPermission("valor.goto")) {
				if(plugin.getConfig().getBoolean("enabled")) {
					player.teleport(plugin.getUtil().getSpawn(plugin.getConfig().getString("current-arena")));
					plugin.getUtil().sendMessage(player, ValorUtil.OPENJOIN.replaceAll("<0>", plugin.getConfig().getString("arenas." + plugin.getConfig().getString("current-arena") + ".name")));
				}
				else {
					plugin.getUtil().sendMessage(player, ValorUtil.DISABLED);
				}
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}
		
		else if(arguments[0].equalsIgnoreCase("help")) {
			if(player.hasPermission("valor.help")) {
				plugin.getUtil().showHelp(sender);
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}
		
		else if(arguments[0].equalsIgnoreCase("list")) {
			if(player.hasPermission("valor.list")) {
				plugin.getUtil().sendMessage(player, ValorUtil.ARENALIST.replaceAll("<0>", plugin.getConfig().getConfigurationSection("arenas").getKeys(false).toString()));
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}

		else if(arguments[0].equalsIgnoreCase("register")) {
			if(player.hasPermission("valor.register")) {
				if(arguments.length > 2) {
					String id = arguments[1];
					String name = "";
					for(int i = 2; i < arguments.length; i++) {
						name = name + arguments[i] + " ";
					}
					name = name.substring(0, name.length()-1);
					plugin.getConfig().set("arenas." + id + ".name", name);
					plugin.getConfig().set("arenas." + id + ".spawn.world", player.getLocation().getWorld().getName());
					plugin.getConfig().set("arenas." + id + ".spawn.x", player.getLocation().getX());
					plugin.getConfig().set("arenas." + id + ".spawn.y", player.getLocation().getY());
					plugin.getConfig().set("arenas." + id + ".spawn.z", player.getLocation().getZ());
					plugin.getConfig().set("arenas." + id + ".spawn.yaw", player.getLocation().getYaw());
					plugin.getConfig().set("arenas." + id + ".spawn.pitch", player.getLocation().getPitch());
					plugin.getConfig().set("current-arena", id);
					plugin.getUtil().sendMessage(player, ValorUtil.REGISTER.replaceAll("<0>", name));
					plugin.saveConfig();
				}
				else {
					plugin.getUtil().sendMessage(player, ValorUtil.SYNTAXREGISTER);
				}
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}

		else if(arguments[0].equalsIgnoreCase("delete")) {
			if(player.hasPermission("valor.delete")) {
				if(arguments.length > 1) {
					String id = arguments[1];
					if(plugin.getConfig().getString("arenas." + id) != null) {
						plugin.getUtil().sendMessage(player, ValorUtil.DELETE.replaceAll("<0>", plugin.getConfig().getString("arenas." + id + ".name")));
						plugin.getConfig().set("arenas." + id, null);
						plugin.saveConfig();
					} else {
						plugin.getUtil().sendMessage(player, ValorUtil.INVALID.replaceAll("<0>", id));
					}
				}
				else {
					plugin.getUtil().sendMessage(player, ValorUtil.SYNTAXDEL);
				}
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}

		else if(arguments[0].equalsIgnoreCase("set")) {
			if(player.hasPermission("valor.set")) {
				if(arguments.length > 1) {
					String id = arguments[1];
					if(plugin.getConfig().getString("arenas." + id) != null) {
						plugin.getConfig().set("current-arena", id);
						plugin.getUtil().sendMessage(player, ValorUtil.SETCURRENT.replaceAll("<0>", plugin.getConfig().getString("arenas." + id + ".name")));
						plugin.saveConfig();
					} else {
						plugin.getUtil().sendMessage(player, ValorUtil.INVALID.replaceAll("<0>", id));
					}
				}
				else {
					plugin.getUtil().sendMessage(player, ValorUtil.SYNTAXSET);
				}
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}

		else if(arguments[0].equalsIgnoreCase("module")) {
			if(player.hasPermission("valor.chest")) {
				player.getInventory().addItem(plugin.getUtil().getModule());
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}

		else if(arguments[0].equalsIgnoreCase("enable")) {
			if(player.hasPermission("valor.toggle")) {
				plugin.getConfig().set("enabled", true);
				plugin.getUtil().sendMessage(player, ValorUtil.ENABLE);
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}
		
		else if(arguments[0].equalsIgnoreCase("disable")) {
			if(player.hasPermission("valor.toggle")) {
				plugin.getConfig().set("enabled", false);
				plugin.getUtil().sendMessage(player, ValorUtil.DISABLE);
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}
		
		else if(arguments[0].equalsIgnoreCase("reload")) {
			if(player.hasPermission("valor.reload")) {
				plugin.getUtil().sendMessage(player, ValorUtil.RELOAD);
				plugin.loadConfig();
			} else {
				plugin.getUtil().sendMessage(player, ValorUtil.PERMISSION);
			}
		}


//		if(arguments[0].equals("queue")) {
//			if(!this.plugin.getUtil().isPlayerQueued(player.getUniqueId())) {
//				plugin.getUtil().queuePlayer(player.getUniqueId());
//				plugin.getUtil().sendMessage(player, "&aYou are now in queue for a 1v1 Battle.");
//				plugin.getUtil().sendMessage(player, "&aYou are number " + (plugin.getUtil().getQueuePlace(player.getUniqueId()) + 1) + " in line.");
//				
//				if(plugin.getUtil().isBattleReady()) {
//					for(Player contestant : plugin.getUtil().contestants()) {
//						plugin.getUtil().sendMessage(contestant, "&a&lYour 1v1 Battle is now ready!");
//						for(Player opponent : plugin.getUtil().contestants()) {
//							if(opponent != contestant) {
//								plugin.getUtil().sendMessage(contestant, "You are playing against: &c" + opponent.getName() + "&f.");
//							}
//						}
//					}
//				}
//			}
//			else {
//				plugin.getUtil().sendMessage(player, "Error - You are already in queue.");
//			}
//		}
//
//		else if(arguments[0].equalsIgnoreCase("qaccept")) {
//			if(plugin.getUtil().isBattleReady() && plugin.getUtil().isContestant(player)) {
//				plugin.getUtil().sendMessage(player, "&aYou are now marked as ready.");
//				for(Player opponent : plugin.getUtil().contestants()) {
//					if(opponent != player) {
//						plugin.getUtil().sendMessage(player, "&a" + opponent.getName() + "&f is ready.");
//					}
//				}
//			}
//		}
//
//		else if(arguments[0].equalsIgnoreCase("qcancel")) {
//			if(plugin.getUtil().isBattleReady() && plugin.getUtil().isContestant(player)) {
//				plugin.getUtil().sendMessage(player, "&aYou have left the battle.");
//				plugin.getUtil().unqueuePlayer(player.getUniqueId());
//				for(Player opponent : plugin.getUtil().contestants()) {
//					if(opponent != player) {
//						plugin.getUtil().sendMessage(player, "&c" + opponent.getName() + "&f has left the battle.");
//					}
//				}
//			}
//			else if(plugin.getUtil().isPlayerQueued(player.getUniqueId())) {
//				plugin.getUtil().sendMessage(player, "&aYou have left the battle.");
//				plugin.getUtil().unqueuePlayer(player.getUniqueId());
//			}
//			else {
//				plugin.getUtil().sendMessage(player, "&cYou are not in a queue.");
//			}
//		}
		
		return false;
	}

}
