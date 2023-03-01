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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {

  private int count = 1;

  @Override
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, this);
  }

  /**
   * プレイヤーがスニークを開始/終了する際に起動されるイベントハンドラ。
   *
   * @param e イベント
   */
  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent e) throws IOException {

    // イベント発生時のプレイヤーやワールドなどの情報を変数に持つ。
    Player player = e.getPlayer();
    World world = player.getWorld();

    List<Color> colors = new ArrayList<>(List.of(Color.RED, Color.AQUA, Color.ORANGE));

    if (count % 4 == 0) {
      for (Color col : colors) {
        // 花火オブジェクトをプレイヤーのロケーション地点に対して出現させる。
        Firework firework = world.spawn(player.getLocation(), Firework.class);

        // 花火オブジェクトが持つメタ情報を取得。
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        // メタ情報に対して設定を追加したり、値の上書きを行う。
        fireworkMeta.addEffect(
            FireworkEffect.builder()
                .withColor(col)
                .withFlicker()
                .build());
        fireworkMeta.setPower(1);

        // 追加した情報で再設定する。
        firework.setFireworkMeta(fireworkMeta);
      }
      Path path = Path.of("firework.txt");
      Files.writeString(path, "!!!");
      player.sendMessage(Files.readString(path));
    }

    count++;
  }

  //ベッドにアクセスすると64サイズのアイテムの量が64になる
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

  //アニマルスポーン　PlayerJoinEvent　playerRespawnEvent
  @EventHandler
  public void AnimalSpawn(PlayerJoinEvent e) {
    World world = e.getPlayer().getWorld();
    Location l = new Location(world, -122, 68, -318);
    world.spawnEntity(l, EntityType.LLAMA);
  }

  ////叫ぶヤギ判別メソッド
  @EventHandler
  public void SearchScreamGoat(PlayerInteractEvent e) {
    Material item = e.getMaterial();
    Player player = e.getPlayer();
    Location l = player.getLocation();
    if (item == Material.GOAT_HORN) {
      Collection<Entity> nearE = player.getWorld().getNearbyEntities(l, 32, 32, 32);
      for (Entity entity : nearE) {
        if (Objects.nonNull(entity) && entity instanceof Goat goat
            && goat.isScreaming()) {
          String s = "キィエエェェェエエェッッッ!!!";
          player.sendMessage(s);
        }
      }
    }


  }


}









