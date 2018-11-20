package net.xfoondom.valor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import net.md_5.bungee.api.ChatColor;

public class ValorUtil {
	
	private ValorPlugin plugin;

	public static String PREFIX = "&f[&8PvP&f] ";
	public static String FLAG = "§cFlagged for PvP!";
	public static String UNFLAG = "§aNo longer flagged for PvP!";
	public static String FLAGGING = "§6Entering PvP in <0>...";
	public static String UNFLAGGING = "§6Leaving PvP in <0>...";
	public static String KILLEDV = "&c<0> &ckilled you";
	public static String KILLEDK = "&aYou killed <0>";
	public static String KILLEDS = "&7<0> &7killed <1>";
	public static String DEAD = "&7<0> &7died of natural causes";
	public static String RESTRICT = "&7You cannot damage players here";
	public static String OPENJOIN = "&7Teleported to <0>";
	public static String DISABLED = "&4Unavailable";
	public static String ARENALIST = "&eList of arenas: <0>";
	public static String RELOAD = "&eAll configuration files were reloaded";
	public static String ENABLE = "&ePvP was enabled";
	public static String DISABLE = "&ePvP was disabled";
	public static String REGISTER = "&e'<0>' was registered";
	public static String DELETE = "&e'<0>' was deleted";
	public static String SETCURRENT = "&e'<0>' was set to the current arena";
	public static String INVALID = "&4'<0>' is not a valid arena";
	public static String PERMISSION = "&4You are not permitted to do that";
	public static String SYNTAXSET = "&4Wrong usage. /pvp set <ID>";
	public static String SYNTAXREGISTER = "&4Wrong usage. /pvp register <ID> <Name>";
	public static String SYNTAXDEL = "&4Wrong usage. /pvp delete <ID>";
	
	public ValorUtil(ValorPlugin plugin) {
		this.plugin = plugin;
	}
	
	public boolean isPlayerFlagged(UUID uuid) {
		boolean flagged = false;
		if(plugin.flaggedPlayers.contains(uuid)) flagged = true;
		return flagged;
	}
	
	public boolean isPlayerQueued(UUID uuid) {
		boolean queued = false;
		if(plugin.queuedPlayers.contains(uuid)) queued = true;
		return queued;
	}
	
	public int getQueuePlace(UUID uuid) {
		int place = 0;
		if(isPlayerQueued(uuid)) {
			for(int i = 0; i < plugin.queuedPlayers.size(); i++) {
				if(plugin.queuedPlayers.get(i) == uuid) {
					place = i;
					break;
				}
			}
		}
		return place;
	}
	
	public void queuePlayer(UUID uuid) {
		if(!isPlayerQueued(uuid)) {
			plugin.queuedPlayers.add(uuid);
		}
	}
	
	public void unqueuePlayer(UUID uuid) {
		if(isPlayerQueued(uuid)) {
			plugin.queuedPlayers.remove(uuid);
		}
	}
	
	public boolean isBattleReady() {
		boolean isReady = false;
		if(plugin.fightingPlayers.isEmpty() && plugin.queuedPlayers.size() > 1) {
			isReady = true;
		}
		return isReady;
	}
	
	public Player[] contestants() {
		Player[] returningPlayers = null;
		if(isBattleReady()) {
			Player[] players = {
					Bukkit.getPlayer(plugin.queuedPlayers.get(0)),
					Bukkit.getPlayer(plugin.queuedPlayers.get(1))
			};
			returningPlayers = players;
		}
		return returningPlayers;
	}
	
	public boolean isContestant(Player player) {
		boolean contestant = false;
		for(Player p : contestants()) {
			if(p == player) {
				contestant = true;
				break;
			}
		}
		return contestant;
	}
	
	public void forceFlagPlayer(UUID uuid) {
		if(!plugin.flaggedPlayers.contains(uuid)) {
			plugin.flaggedPlayers.add(uuid);
			sendMessage(Bukkit.getPlayer(uuid), FLAG);
			Bukkit.getPlayer(uuid).sendTitle(FLAG, "", 10, 40, 10);
		}
	}
	
	public void forceUnflagPlayer(UUID uuid) {
		if(plugin.flaggedPlayers.contains(uuid)) {
			plugin.flaggedPlayers.remove(uuid);
			sendMessage(Bukkit.getPlayer(uuid), UNFLAG);
			Bukkit.getPlayer(uuid).sendTitle(UNFLAG, "", 10, 40, 10);
		}
	}
	
	public void flagPlayer(final UUID uuid) {
		if(!plugin.flaggedPlayers.contains(uuid) && !plugin.enteringPlayers.contains(uuid)) {
			plugin.enteringPlayers.add(uuid);
			new ValorRepeater(plugin, 0, 20) {
				int remainingSeconds = 5;
				public void run() {
					if(Bukkit.getServer().getPlayer(uuid) == null) {
						stop();
					}
					Bukkit.getPlayer(uuid).sendTitle("", FLAGGING.replaceAll("<0>", String.valueOf(remainingSeconds)), 0, 20, 0);
					remainingSeconds--;
					if(remainingSeconds < 0) {
						plugin.enteringPlayers.remove(uuid);
						forceFlagPlayer(uuid);
						stop();
					}
					else if(!isInRegion(Bukkit.getPlayer(uuid).getLocation(), plugin.getConfig().getConfigurationSection("arenas").getKeys(false))) {
						plugin.enteringPlayers.remove(uuid);
						stop();
					}
				}
			};
		}
	}
	
	public void unflagPlayer(final UUID uuid) {
		if(plugin.flaggedPlayers.contains(uuid) && !plugin.leavingPlayers.contains(uuid)) {
			plugin.leavingPlayers.add(uuid);
			new ValorRepeater(plugin, 0, 20) {
				int remainingSeconds = 5;
				public void run() {
					if(Bukkit.getServer().getPlayer(uuid) == null) {
						stop();
					}
					Bukkit.getPlayer(uuid).sendTitle("", UNFLAGGING.replaceAll("<0>", String.valueOf(remainingSeconds)), 0, 20, 0);
					remainingSeconds--;
					if(remainingSeconds < 0) {
						plugin.leavingPlayers.remove(uuid);
						forceUnflagPlayer(uuid);
						stop();
					}
					else if(isInRegion(Bukkit.getPlayer(uuid).getLocation(), plugin.getConfig().getConfigurationSection("arenas").getKeys(false))) {
						plugin.leavingPlayers.remove(uuid);
						stop();
					}
				}
			};
		}
	}
	
	public boolean inRegion(Location location, String regionName) {
		RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
		boolean inRegion = false;
		for(ProtectedRegion region : set) {
			if(region.getId().equalsIgnoreCase(regionName)) {
				inRegion = true;
				break;
			}
		}
		return inRegion;
	}
	
	public boolean isInRegion(Location location, Set<String> set) {
		boolean withinRegion = false;
		for(String region : set) {
			if(inRegion(location, region)) {
				withinRegion = true;
				break;
			}
		}
		return withinRegion;
	}
	
	public void giveReward(Player player, ItemStack item) {
		int offspring = 0;
		while(plugin.rewards.get(player.getName() + "." + offspring) != null) {
			offspring++;
		}
		plugin.rewards.set(player.getName() + "." + offspring + ".id", "" + item.getType());
		plugin.rewards.set(player.getName() + "." + offspring + ".name", item.getItemMeta().getDisplayName());
		plugin.rewards.set(player.getName() + "." + offspring + ".lore", item.getItemMeta().getLore());
		plugin.rewards.set(player.getName() + "." + offspring + ".amount", item.getAmount());
		plugin.rewards.set(player.getName() + "." + offspring + ".durability", ((Damageable) item.getItemMeta()).getDamage());
		ArrayList<String> enchantments = new ArrayList<String>();
		for(Enchantment enchantment : item.getItemMeta().getEnchants().keySet()) {
			String name = enchantment.getKey().getKey();
			int level = item.getEnchantmentLevel(enchantment);
			enchantments.add(name + ":" + level);
		}
		plugin.rewards.set(player.getName() + "." + offspring + ".enchantments", enchantments);
		if(item.getType() == Material.PLAYER_HEAD) {
			plugin.rewards.set(player.getName() + "." + offspring + ".owner", ((SkullMeta) item.getItemMeta()).getOwningPlayer().getUniqueId().toString());
		}
		plugin.saveRewards();
	}
	
	public void sortRewards(Player player) {
		if(plugin.rewards.get(player.getName()) != null) {
			ArrayList<ItemStack> items = new ArrayList<ItemStack>();
			for(String key : plugin.rewards.getConfigurationSection(player.getName()).getKeys(false)) {
				ItemStack item = new ItemStack(Material.getMaterial(plugin.rewards.getString(player.getName() + "." + key + ".id")));
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(plugin.rewards.getString(player.getName() + "." + key + ".name"));
				meta.setLore(plugin.rewards.getStringList(player.getName() + "." + key + ".lore"));
				item.setAmount(plugin.rewards.getInt(player.getName() + "." + key + ".amount"));
				((Damageable) meta).setDamage(plugin.rewards.getInt(player.getName() + "." + key + ".durability"));
				for(String enchantment : plugin.rewards.getStringList(player.getName() + "." + key + ".enchantments")) {
					String name = enchantment.split(":")[0];
					Integer level = Integer.parseInt(enchantment.split(":")[1]);
					meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(name)), level, false);
				}
				if(item.getType() == Material.PLAYER_HEAD) {
					SkullMeta headmeta = (SkullMeta) meta;
					headmeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(plugin.rewards.getString(player.getName() + "." + key + ".owner"))));
					item.setItemMeta(headmeta);
				} else {
					item.setItemMeta(meta);
				}
				items.add(item);
			}
			plugin.rewards.set(player.getName(), null);
			plugin.saveRewards();
			for(int i = 0; i < items.size(); i++) {
				giveReward(player, items.get(i));
			}
		}
		
	}
	
	public void sendMessage(Player player, String str) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + str));
	}
	
	public Location getSpawn(String id) {
		World world = Bukkit.getWorld(plugin.getConfig().getString("arenas." + id + ".spawn.world"));
		double x = plugin.getConfig().getDouble("arenas." + id + ".spawn.x");
		double y = plugin.getConfig().getDouble("arenas." + id + ".spawn.y");
		double z = plugin.getConfig().getDouble("arenas." + id + ".spawn.z");
		float  yaw = (float) plugin.getConfig().getDouble("arenas." + id + ".spawn.yaw");
		float pitch = (float) plugin.getConfig().getDouble("arenas." + id + ".spawn.pitch");
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	public boolean validArena(String id) {
		boolean valid = false;
		if(plugin.getConfig().getString("arenas." + id) != null) {
			valid = true;
		}
		return valid;
	}
	
	public ItemStack getModule() {
		ItemStack item = new ItemStack(Material.BARRIER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bPvP Chest Module"));
		item.setItemMeta(meta);
		return item;
	}
	
	public boolean isChest(PlayerInteractEvent event) {
		boolean isChest = false;
		Chest chest = (Chest) event.getClickedBlock().getState();
		for(ItemStack item : chest.getBlockInventory().getContents()) {
			if(item != null && item.getType() == Material.BARRIER && item.getItemMeta().getDisplayName().equals(getModule().getItemMeta().getDisplayName())) {
				if(!event.getPlayer().hasPermission("valor.chest") || !event.getPlayer().isSneaking()) {
					isChest = true;
				}
			}
		}
		return isChest;
	}
	
	public void showHelp(CommandSender sender) {
		sender.sendMessage("-- " + PREFIX + " --");
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "-- " + PREFIX + " --"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp &8(Warps to the spawn of the current arena)"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp help &8(Displays this message)"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp list &8(Displays a list of arenas)"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp set <ID> &8(Sets the current arena)"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp register <ID> <name> &8(Registers an arena)"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp delete <ID> &8(Deletes an arena)"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp module &8(Gives you the chest-module)"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp <enable/disable> &8(Toggles the availability of /pvp)"));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8 - &7/pvp reload &8(Reloads the configuration files of Valor)"));
	}
	
}
