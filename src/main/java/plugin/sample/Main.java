package plugin.sample;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.sample.command.LevelUpCommand;
import plugin.sample.command.LightningCommand;

public final class Main extends JavaPlugin implements Listener {

  private int count;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    Bukkit.getPluginManager().registerEvents(this, this);
    getCommand("lvup").setExecutor(new LevelUpCommand(this));
    getCommand("strike").setExecutor(new LightningCommand());
  }

  /**
   * スニークでチャット欄に座標と向いている方角を表示
   *
   * @param e 　プレイヤースニーク時
   */
  @EventHandler
  public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
    Player player = e.getPlayer();
    Location l = player.getLocation();
    String X = new String();
    String Z = new String();
    if ((int) l.getX() < 0) {
      X = "W " + (int) l.getX() * -1;
    } else if ((int) l.getX() > 0) {
      X = "E " + (int) l.getX();
    }
    if ((int) l.getZ() < 0) {
      Z = "N " + (int) l.getZ() * -1;
    } else if ((int) l.getZ() > 0) {
      Z = "S " + (int) l.getZ();
    }
    if (count % 2 == 0) {
      player.sendMessage(X + "  " + Z + "   H " + (int) l.getY() + "     " + player.getFacing());
    }
    count++;
  }

  /**
   * ベッドにアクセスするとサイズ64のアイテムの量が64になる
   *
   * @param e ベッドタッチ時
   */
  @EventHandler
  public void onPlayerBedEnter(PlayerBedEnterEvent e) {
    Player player = e.getPlayer();
    ItemStack[] itemStacks = player.getInventory().getContents();
    Arrays.stream(itemStacks)
        .filter(
            item -> Objects.nonNull(item) && item.getMaxStackSize() == 64 && item.getAmount() < 64)
        .forEach(item -> item.setAmount(64));
    player.getInventory().setContents(itemStacks);
  }

  /**
   * アイテムをまとめて捨てると消える(16以上)
   *
   * @param e プレイヤードロップ時
   */
  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent e) {
    ItemStack is = e.getItemDrop().getItemStack();
    if (is.getAmount() > 15) {
      is.setAmount(0);
    }
  }

  /**
   * アイテムをクラフトすると経験値がもらえる(500)
   *
   * @param e プレイヤークラフトアイテム時
   */
  @EventHandler
  public void onCraftItem(CraftItemEvent e) {
    if (e.getWhoClicked() instanceof Player player) {
      player.giveExp(500);
    }

  }

  /**
   * ヤギの角笛による叫ぶヤギ検出メソッド (範囲)
   *
   * @param e ヤギの角笛使用時
   */
  @EventHandler
  public void onUseGoatHone(PlayerInteractEvent e) {
    Material item = e.getMaterial();
    Player player = e.getPlayer();
    Location l = player.getLocation();
    if (item == Material.GOAT_HORN) {
      Collection<Entity> nearby = player.getWorld().getNearbyEntities(l, 32, 32, 32);
      for (Entity entity : nearby) {
        if (entity instanceof Goat goat && goat.isScreaming()) {
          String scream = getConfig().getString("StrangeVoice");
          player.sendMessage(scream);
        }
      }
    }
  }

  /**
   * 耕した畑をジャンプで荒らすとプレイヤーの近くにスライム出現
   *
   * @param e 耕地着地時
   */
  @EventHandler
  public void onJumpIntoSoil(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    World world = player.getWorld();
    Location l = player.getLocation();
    Location l2 = new Location(world, l.getX() - 3, l.getY() + 2, l.getZ() + 1);
    Material block = e.getClickedBlock().getType();
    Action action = e.getAction();
    if (Objects.nonNull(block)
        && block == Material.FARMLAND
        && action == Action.PHYSICAL) {
      world.spawn(l2, Slime.class);
    }
  }

  /**
   * 金でカボチャケーキ作る
   *
   * @param e
   */
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) {
    Block block = e.getBlockPlaced();
    World world = block.getWorld();
    Location l = block.getLocation();
    Location l2 = new Location(world, l.getX(), l.getY() - 1, l.getZ());
    Block baseBlock = l2.getBlock();
    if (baseBlock.getType() == Material.CARVED_PUMPKIN && block.getType() == Material.GOLD_BLOCK) {
      baseBlock.setType(Material.ORANGE_CANDLE_CAKE);
      block.setType(Material.AIR);
    }
  }

}









