package me.byteful.plugin.interactables;

import org.bukkit.plugin.java.JavaPlugin;

public final class InteractablesPlugin extends JavaPlugin {
  private final Prototype prototype = new Prototype(this);

  @Override
  public void onEnable() {
    prototype.start();
    getServer().getPluginManager().registerEvents(prototype, this);
  }

  @Override
  public void onDisable() {
    prototype.stop();
  }
}
