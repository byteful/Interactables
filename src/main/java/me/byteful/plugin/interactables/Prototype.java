package me.byteful.plugin.interactables;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static net.kyori.adventure.text.Component.keybind;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class Prototype implements Listener {
  private static final double MAX_SECONDS = 3;
  private static final int PROGRESS_BAR_SIZE = 15;

  private final InteractablesPlugin plugin;
  private ArmorStand testVisual;
  private double heldSeconds;
  private BukkitTask handler;

  public Prototype(InteractablesPlugin plugin) {
    this.plugin = plugin;
  }

  public void start() {
    final World world = Bukkit.getWorld("world");
    if (world == null) return;

    testVisual = world.spawn(new Location(world, 0, 100, 0), ArmorStand.class);
    testVisual.setInvulnerable(true);
    testVisual.setCustomNameVisible(true);
    testVisual.setCanTick(false);
    testVisual.setVisible(false);
    testVisual.setPersistent(false);
    testVisual.customName(
        text()
            .append(text("Hold "))
            .append(keybind("key.sneak"))
            .build()
    );
  }

  public void stop() {
    if (testVisual != null) {
      testVisual.remove();
    }
  }

  @EventHandler
  public void on(PlayerToggleSneakEvent event) {
    final Player player = event.getPlayer();
    if (player.getLocation().distance(testVisual.getLocation()) >= 2) {
      return;
    }

    if (event.isSneaking()) {
      handler = new BukkitRunnable() {
        @Override
        public void run() {
          heldSeconds += .05; // 50 ms = 1 tick, .05 seconds = 50 ms
          if (heldSeconds >= MAX_SECONDS) {
            heldSeconds = 0;
            cancel();
            testVisual.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, testVisual.getLocation(), 1);
            for (Entity nearby : testVisual.getNearbyEntities(2, 2, 2)) {
              if (nearby instanceof Player player1) player1.setSneaking(false);
            }
            return;
          }

          final double remaining = MAX_SECONDS - heldSeconds;
          testVisual.customName(text()
              .append(generateProgressBar())
              .append(text(" %.1fs".formatted(remaining), YELLOW))
              .build());
        }
      }.runTaskTimer(plugin, 1, 1);
    } else {
      handler.cancel();
      heldSeconds = 0;
      testVisual.customName(
          text()
              .append(text("Hold "))
              .append(keybind("key.sneak"))
              .build()
      );
    }
  }

  private Component generateProgressBar() {
    final double percentage = heldSeconds / MAX_SECONDS;
    final int completed = (int) (PROGRESS_BAR_SIZE * percentage);
    final int remaining = PROGRESS_BAR_SIZE - completed;

    return text()
        .append(text("[", DARK_GRAY))
        .append(text("|".repeat(completed), GREEN))
        .append(text("|".repeat(remaining), GRAY))
        .append(text("]", DARK_GRAY))
        .build();
  }
}
