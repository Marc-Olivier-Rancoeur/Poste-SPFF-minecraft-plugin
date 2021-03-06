package fr.bigbosses07.laposte;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ListenerPoste implements Listener {
	
	private class nameaddr {
		String name = "";
		String address = "";
	}
	private laposte main;
	public ListenerPoste(laposte laposte) {
		this.main = laposte;
	}
	private boolean isop(Player player) {
		if(main.getConfig().getInt("ops."+player.getName()) == 1) {
			return true;
		}else {
			return false;
		}
	}
	
	private int getNbAddresses(String name){
		return main.getConfig().getInt("players." + name + ".nbaddresses");
	}
	private void modifyMsgNb(String name, String address, int nb) {
		setMsgNb(name, address, getNbMsg(name, address)+nb);
	}
	
	private void setMsgNb(String name, String address, int nb) {
		main.getConfig().set("players." + name + ".addresses." + address, nb);
		if(nb == -1) {
			main.getConfig().set("players." + name + ".nbaddresses", getNbAddresses(name)-1);
			main.getConfig().set("players." + name + ".locations." + address, 0);
		}
		main.saveConf();
		if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(name))) {
			Bukkit.getPlayer(name).sendMessage("Vous avez recu un courrier chez vous : " + address);
		}
	}
	private void updateLight(Player player, Block block) {
		Location location = block.getLocation();
		Directional directional = (Directional)block.getBlockData();
		BlockFace blockface = directional.getFacing();
		int lampx = 0;
		int lampz = 0;
		if(blockface == BlockFace.NORTH) {
			lampz = 1;
		}else if(blockface == BlockFace.WEST){
			lampx = 1;
		}else if(blockface == BlockFace.SOUTH){
			lampz = -1;
		}else if(blockface == BlockFace.EAST){
			lampx = -1;
		}
		Block lamp = Bukkit.getServer().getWorld(player.getWorld().getName()).getBlockAt((int)location.getX()+lampx, (int)location.getY(), (int)location.getZ()+lampz);
		lamp.setType(Material.REDSTONE_LAMP);
		Lightable lightable = (Lightable)lamp.getBlockData();
		lightable.setLit(true);
		lamp.setBlockData(lightable);
	}
	private int getNbMsg(String name, String address) {
		return main.getConfig().getInt("players." + name + ".addresses." + address);
	}
	private int getNbObj(Inventory inventory) {
		int objs = 0;
		for(int i = 0 ; i < 27 ; i++) {
			if(inventory.getItem(i) != null) {
				objs+=1;
			}
		}
		return objs;
	}
	
	private nameaddr getchestloc(Location loc) {
		nameaddr infos = new nameaddr();
		infos.name = "";
		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if(getNbAddresses(player.getName()) > 0) {
				for(String addresses : main.getConfig().getConfigurationSection("players."+player.getName()+".addresses").getKeys(false)) {
					if(main.getConfig().getInt("players."+player.getName()+".locations." + addresses + ".x") == (int)loc.getX()) {
						if(main.getConfig().getInt("players."+player.getName()+".locations." + addresses + ".y") == (int)loc.getY()) {
							if(main.getConfig().getInt("players."+player.getName()+".locations." + addresses + ".z") == (int)loc.getZ()) {
								infos.address = addresses;
								infos.name = player.getName();
								return infos;
							}
						}
					}
				}
			}
		}
		return infos;
	}
	private void destroyboite(Block block, Player player, nameaddr infos) {
		Location location = block.getLocation();
		main.getConfig().set("players."+player.getName()+".locations." + infos.address + ".x", 1000000000);
		main.saveConf();
		Directional direction = (Directional)block.getBlockData();
		BlockFace blockface = direction.getFacing();
		int lampx = 0;
		int lampz = 0;
		if(blockface == BlockFace.NORTH) {
			lampz = 1;
		}else if(blockface == BlockFace.WEST){
			lampx = 1;
		}else if(blockface == BlockFace.SOUTH){
			lampz = -1;
		}else if(blockface == BlockFace.EAST){
			lampx = -1;
		}
		Block lamp = Bukkit.getServer().getWorld(player.getWorld().getName()).getBlockAt((int)location.getX()+lampx, (int)location.getY(), (int)location.getZ()+lampz);
		lamp.setType(Material.AIR);
	}
	private int testinventory(Inventory inventory, ItemStack item){
		for(int i = 0 ; i < 27 ; i++) {
			if(inventory.getItem(i) == null) {
				return i;
			}else if(inventory.getItem(i).equals(item)){
				return -1;
			}
		}
		return -1;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		if(getNbAddresses(name) > 0){
			int colis = 0;
			player.sendMessage("�7SPFF -- Poste");
			for(String addresses : main.getConfig().getConfigurationSection("players."+name+".addresses").getKeys(false)) {
				int nbcolis = main.getConfig().getInt("players."+name+".addresses." + addresses);
				if(nbcolis > 0) {
					player.sendMessage(" Vous avez " + nbcolis + " colis chez vous : �7" + addresses);
					colis+=nbcolis;
				}
			}
			if(colis == 0) {
				player.sendMessage("�7 Vous n'avez recu aucun coli.");
			}else {
				player.sendMessage("�c Vous avez recu " + colis + " colis en tout.");
			}
		}
	}
	
	
	@EventHandler
	public void clic(PlayerInteractEvent event) {
		Action action = event.getAction();
		if(action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK){
			Block block = event.getClickedBlock();
			if(block.getType() == Material.CHEST){
				nameaddr infos = getchestloc(block.getLocation());
				if(!infos.name.equals("")) {
					Player player = event.getPlayer();
					if(infos.name.equals(player.getName())) {
						if(action == Action.RIGHT_CLICK_BLOCK) {
							player.sendMessage("Bienvenue dans votre boite aux lettres");
							if(getNbMsg(infos.name, infos.address) > 0) {
								setMsgNb(infos.name, infos.address, 0);
							}
						}else {
							destroyboite(block, player, infos);
							player.sendMessage("Suppression de votre boite aux lettre. En cas de mauvaise manipulation contactez le staff.");
						}
					}else {
						if(action == Action.RIGHT_CLICK_BLOCK) {
							ItemStack item = event.getItem();
							if(item != null && item.getType() == Material.WRITTEN_BOOK) {
								Chest chest = (Chest) block.getState();
								Inventory inventory = chest.getBlockInventory();
								ItemStack item2 = item.clone();
								item2.setAmount(1);
								int pos = testinventory(inventory, item2);
								event.setCancelled(true);
								if(pos == -1 || pos > 26) {
									player.sendMessage("Impossible de poster cette lettre, elle doit deja avoir ete postee, ou la boite est pleine.");
								}else {
									item.setAmount(item.getAmount()-1);
									inventory.setItem(pos, item2);
									player.sendMessage("Votre courrier a bien ete poste !");
									modifyMsgNb(infos.name, infos.address, 1);
									updateLight(player, block);
								}
							}else if(!isop(player)) {
								event.setCancelled(true);
								player.sendMessage("Vous n'avez pas acces a cette boite aux lettres");
							}else {
								player.sendMessage("Boite aux lettres de : " + infos.name);
							}
						}else {
							if(!isop(player)) {
								event.setCancelled(true);
								player.sendMessage("Vous n'avez pas acces a cette boite aux lettres");
							}else {
								destroyboite(block, Bukkit.getPlayer(infos.name) , infos);
								player.sendMessage("Suppression de la boite aux lettre de : " + infos.name + ".");
							}
						}
					}
				}
			}
		}
	}
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Inventory inventory = event.getInventory();
		Location location = inventory.getLocation();
		nameaddr infos = getchestloc(location);
		if(infos.name != "") {
			Player player = (Player)event.getWhoClicked();
			if(!infos.name.equals(player.getName())) {
				int nb = getNbObj(inventory);
				setMsgNb(infos.name, infos.address, nb);
				if(nb > 0) {
					updateLight(player, location.getBlock());
				}
			}
		}
	}
}
