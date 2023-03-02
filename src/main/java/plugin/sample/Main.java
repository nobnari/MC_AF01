package plugin.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {

  private int count = 1;

  @Override
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, this);
    getCommand("lvup").setExecutor(new LevelUpCommand());
    getCommand("strike").setExecutor(new LightningCommand());
  }

  /**
   * プレイヤーがスニークを開始/終了する際に起動されるイベントハンドラ。
   *
   * @param e イベント
   */
  //スニークで花火が上がる
  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent e) throws IOException {
    Player player = e.getPlayer();
    World world = player.getWorld();
    List<Color> colors = new ArrayList<>(List.of(Color.RED, Color.AQUA, Color.ORANGE));
    if (count % 4 == 0) {
      for (Color col : colors) {
        Firework firework = world.spawn(player.getLocation(), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(
            FireworkEffect.builder()
                .withColor(col)
                .withFlicker()
                .build());
        fireworkMeta.setPower(1);
        firework.setFireworkMeta(fireworkMeta);
      }
      Path path = Path.of("firework.txt");
      Files.writeString(path, "!!!");
      player.sendMessage(Files.readString(path));
    }
    count++;
  }

  //ベッドにアクセスするとサイズ64のアイテムの量が64になる
  @EventHandler
  public void onPlayerBedEnter(PlayerBedEnterEvent e) {
    Player player = e.getPlayer();
    ItemStack[] itemstacks = player.getInventory().getContents();
    Arrays.stream(itemstacks)
        .filter(
            item -> Objects.nonNull(item) && item.getMaxStackSize() == 64 && item.getAmount() < 64)
        .forEach(item -> item.setAmount(64));
    player.getInventory().setContents(itemstacks);
  }

  //アイテムをまとめて捨てると消える(16以上)
  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent e) {
    ItemStack is = e.getItemDrop().getItemStack();
    if (is.getAmount() > 15) {
      is.setAmount(0);
    }
  }

  //アイテムをクラフトすると経験値がもらえる(500)
  @EventHandler
  public void onCraftItem(CraftItemEvent e) {
    Player who = (Player) e.getWhoClicked();
    who.giveExp(500);
  }

  //プレイヤーリスポーンで特定の位置にラマ出現
  @EventHandler
  public void LlamaSpawn(PlayerRespawnEvent e) {
    World world = e.getPlayer().getWorld();
    Location l = new Location(world, -122, 68, -318);
    world.spawnEntity(l, EntityType.LLAMA);
  }

  ////ヤギの角笛による叫ぶヤギ検出メソッド (範囲)
  @EventHandler
  public void SearchScreamGoat(PlayerInteractEvent e) {
    Material item = e.getMaterial();
    Player player = e.getPlayer();
    Location l = player.getLocation();
    if (item == Material.GOAT_HORN) {
      Collection<Entity> nearby = player.getWorld().getNearbyEntities(l, 32, 32, 32);
      nearby.stream()
          .filter(entity -> Objects.nonNull(entity) && entity instanceof Goat goat
              && goat.isScreaming())
          .map(entity -> "キィエエェェェエエェッッッ!!!")
          .forEach(player::sendMessage);
    }


  }


}









