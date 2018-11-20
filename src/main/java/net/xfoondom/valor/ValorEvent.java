package net.xfoondom.valor;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;

public class ValorEvent implements Listener {
	
	private ValorPlugin plugin;
	
	public ValorEvent(ValorPlugin plugin) {
		this.plugin = plugin;
	}

	
	@EventHandler
	public void DamageEvent(EntityDamageByEntityEvent event) {
		Entity attacker = null;
		Player victim = null;
		if(event.getEntity() instanceof Player) {
			victim = (Player) event.getEntity();
			if(event.getDamager() instanceof Player) {
				attacker = (Player) event.getDamager();
			} else if(event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
				attacker = (Player) ((Projectile) event.getDamager()).getShooter();
			}
			if(attacker != null) {
				if(!plugin.getUtil().isPlayerFlagged(attacker.getUniqueId()) || !plugin.getUtil().isPlayerFlagged(victim.getUniqueId())) {
					plugin.getUtil().sendMessage((Player) attacker, plugin.getUtil().RESTRICT);
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void BurnEvent(EntityCombustByEntityEvent event) {
		if(event.getCombuster() instanceof Projectile && ((Projectile) event.getCombuster()).getShooter() instanceof Player && event.getEntity() instanceof Player) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void MoveEvent(PlayerMoveEvent event) {
		if(plugin.getUtil().isInRegion(event.getTo(), plugin.getConfig().getConfigurationSection("arenas").getKeys(false))) {
			plugin.getUtil().flagPlayer(event.getPlayer().getUniqueId());
		} else {
			plugin.getUtil().unflagPlayer(event.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler
	public void DeathEvent(PlayerDeathEvent event) {
		if(event.getEntity().getKiller() instanceof Player || plugin.getUtil().isInRegion(event.getEntity().getLocation(), plugin.getConfig().getConfigurationSection("arenas").getKeys(false))) {
			event.setDeathMessage(null);
			if(event.getEntity().getKiller() instanceof Player) {
				plugin.getUtil().sendMessage(event.getEntity(), plugin.getUtil().KILLEDV.replaceAll("<0>", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getEntity().getKiller().getDisplayName()))));
				plugin.getUtil().sendMessage(event.getEntity().getKiller(), plugin.getUtil().KILLEDK.replaceAll("<0>", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getEntity().getDisplayName()))));
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(player != event.getEntity() && player != event.getEntity().getKiller()) {
						plugin.getUtil().sendMessage(player, plugin.getUtil().KILLEDS.replaceAll("<0>", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getEntity().getKiller().getDisplayName()))).replaceAll("<1>", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getEntity().getDisplayName()))));
					}
				}
				ItemStack head = new ItemStack(Material.PLAYER_HEAD);
				SkullMeta meta = (SkullMeta) head.getItemMeta();
				meta.setOwningPlayer(event.getEntity());
				head.setItemMeta(meta);
				plugin.getUtil().giveReward(event.getEntity().getKiller(), head);
				event.getEntity().getKiller().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', plugin.getUtil().KILLEDK.replaceAll("<0>", event.getEntity().getDisplayName()))));
			}
			event.setKeepInventory(true);
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void RespawnEvent(PlayerRespawnEvent event) {
		if(plugin.getUtil().isPlayerFlagged(event.getPlayer().getUniqueId())) {
			plugin.getUtil().forceUnflagPlayer(event.getPlayer().getUniqueId());
			event.setRespawnLocation(plugin.getUtil().getSpawn(plugin.getConfig().getString("current-arena")));
		}
	}
	
	@EventHandler
	public void ChestEvent(PlayerInteractEvent event) {
		if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
			if (plugin.getUtil().isChest(event)) {
				event.setCancelled(true);
				Inventory chest = Bukkit.createInventory(null, 36, ChatColor.BLACK + "PvP Chest");
				
				if(plugin.rewards.get(event.getPlayer().getName()) != null) {
					
					for(String key : plugin.rewards.getConfigurationSection(event.getPlayer().getName()).getKeys(false)) {
						if(Integer.parseInt(key) >= 0) {
							ItemStack item = new ItemStack(Material.getMaterial(plugin.rewards.getString(event.getPlayer().getName() + "." + key + ".id")));
							ItemMeta meta = item.getItemMeta();
							meta.setDisplayName(plugin.rewards.getString(event.getPlayer().getName() + "." + key + ".name"));
							meta.setLore(plugin.rewards.getStringList(event.getPlayer().getName() + "." + key + ".lore"));
							item.setAmount(plugin.rewards.getInt(event.getPlayer().getName() + "." + key + ".amount"));
							((Damageable) meta).setDamage(plugin.rewards.getInt(event.getPlayer().getName() + "." + key + ".durability"));
							for(String enchantment : plugin.rewards.getStringList(event.getPlayer().getName() + "." + key + ".enchantments")) {
								String name = enchantment.split(":")[0];
								Integer level = Integer.parseInt(enchantment.split(":")[1]);
								meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(name)), level, false);
							}
							if(item.getType() == Material.PLAYER_HEAD) {
								SkullMeta headmeta = (SkullMeta) meta;
								headmeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(plugin.rewards.getString(event.getPlayer().getName() + "." + key + ".owner"))));
								item.setItemMeta(headmeta);
							} else {
								item.setItemMeta(meta);
							}
							if(Integer.parseInt(key) < chest.getSize()) {
								chest.setItem(Integer.parseInt(key), item);
							}
						}
					}
				}
				event.getPlayer().openInventory(chest);
			}
			
		}
	}
	
	@EventHandler
	public void CloseEvent(InventoryCloseEvent event) {
		if(event.getInventory().getTitle().equals("PvP Chest")) {
			ArrayList<ItemStack> currentItems = new ArrayList();
			for(int i = 0; i < event.getInventory().getSize(); i++) {
				plugin.rewards.set(event.getPlayer().getName() + "." + i, null);
				if(event.getInventory().getItem(i) != null) {
					currentItems.add(event.getInventory().getItem(i));
				}
			}
			for(int i = 0; i < currentItems.size(); i++) {
				plugin.getUtil().giveReward(Bukkit.getPlayer(event.getPlayer().getName()), currentItems.get(i));
			}
			plugin.getUtil().sortRewards(Bukkit.getPlayer(event.getPlayer().getName()));
		}
	}
	
	@EventHandler()
	public void TeleportEvent(PlayerTeleportEvent event) {
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getPlayer().getGameMode() != GameMode.SPECTATOR && !event.getPlayer().hasPermission("valor.teleport"))
		if(event.getTo().getBlockX() != event.getFrom().getBlockX() || event.getTo().getBlockY() != event.getFrom().getBlockY() || event.getTo().getBlockZ() != event.getFrom().getBlockZ() || event.getTo().getWorld() != event.getFrom().getWorld()) {
			if(plugin.getUtil().isPlayerFlagged(event.getPlayer().getUniqueId())) {
				event.setTo(plugin.getUtil().getSpawn(plugin.getConfig().getString("current-arena")));
			} else {
				if(plugin.getUtil().isInRegion(event.getTo(), plugin.getConfig().getConfigurationSection("arenas").getKeys(false))) {
					event.setTo(plugin.getUtil().getSpawn(plugin.getConfig().getString("current-arena")));
				}
			}
			if(plugin.getUtil().isInRegion(event.getTo(), plugin.getConfig().getConfigurationSection("arenas").getKeys(false))) {
				plugin.getUtil().flagPlayer(event.getPlayer().getUniqueId());
			} else {
				plugin.getUtil().unflagPlayer(event.getPlayer().getUniqueId());
			}
		}
	}
	
//	@EventHandler
//	public void BlockEvent(BlockBreakEvent event) {
//		final Material type = event.getBlock().getType();
//		final Block block = event.getBlock();
//		if(type != Material.GLASS) {
//			new ValorDelayer(plugin, 20 * 5) {
//				public void run() {
//					block.setType(Material.GLASS);
//					new ValorDelayer(plugin, 20 * 5) {
//						public void run() {
//							block.setType(type);
//						}
//					};
//				}
//			};
//		}
//	}
	
}
