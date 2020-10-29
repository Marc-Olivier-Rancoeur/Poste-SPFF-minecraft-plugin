package fr.bigbosses07.laposte;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;


public class commandePoste implements CommandExecutor, TabCompleter{
	
	private laposte main;
	public commandePoste(laposte laposte) {
		this.main = laposte;
	}
	
	private int getNbAddresses(String name){
		return main.getConfig().getInt("players." + name + ".nbaddresses");
	}
	private int getNbMsg(String name, String address) {
		return main.getConfig().getInt("players." + name + ".addresses." + address);
	}
	private void addAddress(String name, String address) {
		main.getConfig().set("players." + name + ".addresses." + address, 0);
		main.getConfig().set("players." + name + ".nbaddresses", getNbAddresses(name)+1);
		main.getConfig().set("players." + name + ".locations." + address + ".x", 1000000000);
		main.saveConf();
	}
	private void setaddrloc(String name, String address, Location loc) {
		main.getConfig().set("players." + name + ".locations." + address + ".x", (int)loc.getX());
		main.getConfig().set("players." + name + ".locations." + address + ".y", (int)loc.getY());
		main.getConfig().set("players." + name + ".locations." + address + ".z", (int)loc.getZ());
		main.saveConf();
	}
	private void setMsgNb(String name, String address, int nb) {
		main.getConfig().set("players." + name + ".addresses." + address, nb);
		if(nb == -1) {
			main.getConfig().set("players." + name + ".nbaddresses", getNbAddresses(name)-1);
			main.getConfig().set("players." + name + ".locations." + address, 0);
		}
		main.saveConf();
	}
	private boolean addrexist(String name, String address) {
		for(String addresses : main.getConfig().getConfigurationSection("players."+name+".addresses").getKeys(false)) {
			if(addresses.equalsIgnoreCase(address) && main.getConfig().getInt("players."+name+".addresses." + addresses) >= 0) {
				return true;
			}
		}
		return false;
	}
	private boolean addrhaveloc(String name, String address) {
		if(main.getConfig().getInt("players." + name + ".locations." + address + ".x") == 1000000000) {
			return false;
		}
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args){
		int length = args.length;
		Player player = null;
		if(sender instanceof Player) {
			player = (Player)sender;
		}else {
			return false;
		}
		if(length > 0){
			if(Bukkit.getPlayer(args[0]) instanceof Player && length > 1){
				if(args[1].equalsIgnoreCase("address") && length > 2){
					//Partie ajout adresse
					if(args[2].equalsIgnoreCase("add") && length == 4){
						addAddress(args[0], args[3]);
						player.sendMessage("Ajout de l'adresses : " + args[3] + " a : §7" + args[0] + "§f");
						return true;
					}
					//Partie enlever adresse
					else if(args[2].equalsIgnoreCase("remove") && length == 4){
						if(addrexist(args[0], args[3])) {
							setMsgNb(args[0], args[3], -1);
							player.sendMessage("Suppression de l'adresse : " + args[3] + " de : §7" + args[0] + "§f");
							return true;
						}
						else {
							player.sendMessage("Cette adresse n'existe pas pour ce joueur.");
							return false;
						}
					}
					//Partie liste adresses
					else if(args[2].equalsIgnoreCase("list") && length == 3){
						player.sendMessage("Liste des adresses de : §7" + args[0] + "§f");
						if(getNbAddresses(args[0]) > 0) {
							int i = 1;
							for(String addresses : main.getConfig().getConfigurationSection("players."+args[0]+".addresses").getKeys(false)){
								if(getNbMsg(args[0], addresses) >= 0) {
									player.sendMessage(i + ": " + addresses);
									i++;
								}
							}
						}
						else {
							player.sendMessage("§7" + args[0] + " §fn'a pas d'adresses enregistree.");
						}
						return true;
					}
					//Partie création boite aux lettres
					else if(args[2].equalsIgnoreCase("coffre") && length == 4) {
						if(addrexist(args[0], args[3]) && !addrhaveloc(args[0], args[3])) {
							Location location = player.getLocation();
							int yaw = (int)location.getYaw();
							Block block = Bukkit.getServer().getWorld(player.getWorld().getName()).getBlockAt(location);
							block.setType(Material.CHEST);
							int lampx = 0;
							int lampz = 0;
							BlockData data = block.getBlockData();
							if(yaw > -45 || yaw <= -315) {
								((Directional)data).setFacing(BlockFace.NORTH);
								lampz = 1;
							}else if(yaw > -135 && yaw <= -45) {
								((Directional)data).setFacing(BlockFace.WEST);
								lampx = 1;
							}else if(yaw > -225 && yaw < -135) {
								((Directional)data).setFacing(BlockFace.SOUTH);
								lampz = -1;
							}else if(yaw > -315 && yaw < -225) {
								((Directional)data).setFacing(BlockFace.EAST);
								lampx = -1;
							}
							block.setBlockData(data);
							Block lamp = Bukkit.getServer().getWorld(player.getWorld().getName()).getBlockAt((int)location.getX()+lampx, (int)location.getY(), (int)location.getZ()+lampz);
							lamp.setType(Material.REDSTONE_LAMP);
							setaddrloc(args[0], args[3], location);
							player.sendMessage("La boite aux lettre de §7" + args[0] + " §fa ete installee");
						}else {
							player.sendMessage("L'adresse que vous avez entre est erronee, ou une boite aux lettre existe deja pour cette adresse.");
						}
							
					}
					// passer joueur opérateur du système postal
				}else if(args[1].equalsIgnoreCase("op") && length == 2) {
					if(main.getConfig().getInt("ops."+args[0]) == 1) {
						player.sendMessage("Le joueur est deja operateur de la poste.");
					}else {
						main.getConfig().set("ops." + args[0], 1);
						main.saveConf();
						player.sendMessage(args[0]+" est désormais operateur de la poste.");
					}
				}else if(args[1].equalsIgnoreCase("deop") && length == 2) {
					if(main.getConfig().getInt("ops."+args[0]) == 1) {
						main.getConfig().set("ops." + args[0], 0);
						main.saveConf();
						player.sendMessage(args[0]+" n'est desormais plus operateur de la poste.");
					}else {
						player.sendMessage("Le joueur n'etait deja pas operateur de la poste.");
					}
				}else {
					return false;
				}
			}else {
				player.sendMessage("Il n'y a pas de §7" + args[0] + " §fdans ce monde.");
				return false;
			}
		}
		return true;
	}
	
	@Nullable
	public List<String> onTabComplete(CommandSender sender, Command cmd, String msg, String[] args){
		int length = args.length;
		if(length == 2) {
			String[] completion1 = {"address", "op", "deop"};
			List<String> completions = new ArrayList<>();
			for(String val : completion1) {
				completions.add(val);
			}
			return completions;
		}
		if(length == 3) {
			if(args[1].equalsIgnoreCase("address")) {
				String[] completion2 = {"add", "remove", "list", "coffre"};
				List<String> completions = new ArrayList<>();
				for(String val : completion2) {
					completions.add(val);
				}
				return completions;
			}
		}
		if(length == 4) {
			if(args[1].equalsIgnoreCase("address")) {
				if(args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("coffre")) {
					List<String> completions = new ArrayList<>();
					for(String addresses : main.getConfig().getConfigurationSection("players."+args[0]+".addresses").getKeys(false)) {
						if(getNbMsg(args[0], addresses) >= 0) {
							completions.add(addresses);
						}
					}
					return completions;
				}
			}
		}
		return null;
	}
}
