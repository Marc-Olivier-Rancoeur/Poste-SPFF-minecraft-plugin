package fr.bigbosses07.laposte;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class commandeCheh implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.sendTitle("§4CHEH", "", 10, 80, 10);
			player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10000, 1);
		}
		return true;
	}

}
