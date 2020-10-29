package fr.bigbosses07.laposte;

import org.bukkit.plugin.java.JavaPlugin;

public class laposte extends JavaPlugin{
	@Override
	public void onEnable(){
		saveDefaultConfig();
		getCommand("poste").setExecutor(new commandePoste(this));
		getCommand("poste").setTabCompleter(new commandePoste(this));
		getServer().getPluginManager().registerEvents(new ListenerPoste(this), this);
		getCommand("cheh").setExecutor(new commandeCheh());
	}
	@Override
	public void onDisable(){
	}
	public void saveConf() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
}