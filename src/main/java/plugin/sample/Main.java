package plugin.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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
      Files.writeString(path, "aho");
      player.sendMessage(Files.readString(path));
    }

    count++;
  }

  @EventHandler
  public void onPlayerBedEnter(PlayerBedEnterEvent e) {
    Player player = e.getPlayer();
    ItemStack[] itemstacks = player.getInventory().getContents();
    Arrays.stream(itemstacks).filter(
            item -> Objects.nonNull(item) && item.getMaxStackSize() == 64 && item.getAmount() < 64)
        .forEach(item -> item.setAmount(64));
    player.getInventory().setContents(itemstacks);
  }


  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent e) {
    ItemStack is = e.getItemDrop().getItemStack();
    if (is.getAmount() > 15) {
      is.setAmount(0);
    }
  }

  @EventHandler
  public void onCraftItem(CraftItemEvent e) {
    Player who = (Player) e.getWhoClicked();
    who.giveExp(500);
  }


}









