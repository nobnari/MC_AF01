package plugin.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Cat.Type;
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
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootContext.Builder;
import org.bukkit.loot.LootTables;
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
    String X = "";
    String Z = "";
    if ((int) l.getX() < 0) {
      X = "西 " + (int) l.getX() * -1;
    } else if ((int) l.getX() > 0) {
      X = "東 " + (int) l.getX();
    }
    if ((int) l.getZ() < 0) {
      Z = "北 " + (int) l.getZ() * -1;
    } else if ((int) l.getZ() > 0) {
      Z = "南 " + (int) l.getZ();
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
  public void GoatHorn(PlayerInteractEvent e) {
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
   * 叫ぶヤギに紙を与えるとハートが出て叫ぶ
   *
   * @param e 　メインハンドに紙を持ってヤギに触れた時
   */
  @EventHandler
  public void PaperGoat(PlayerInteractEntityEvent e) {
    Player player = e.getPlayer();
    Entity entity = e.getRightClicked();
    ItemStack mainItem = player.getInventory().getItemInMainHand();
    if (count % 2 == 0 && mainItem.getType().equals(Material.PAPER) && entity instanceof Goat goat
        && goat.isScreaming()) {

      mainItem.setAmount(mainItem.getAmount() - 1);

      goat.playEffect(EntityEffect.LOVE_HEARTS);
      player.playSound(player.getLocation(), Sound.ENTITY_GOAT_SCREAMING_PREPARE_RAM, 30, 45);

      List<String> screamList = new ArrayList<>(getConfig().getStringList("StrangeVoiceList"));
      int random = new SplittableRandom().nextInt(screamList.size());
      player.sendMessage(screamList.get(random));
    }
    count++;
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
    Action action = e.getAction();
    if (Objects.nonNull(e.getClickedBlock())) {
      Material block = e.getClickedBlock().getType();
      if (block == Material.FARMLAND
          && action == Action.PHYSICAL) {
        world.spawn(l2, Slime.class);
      }
    }
  }

  /**
   * 金でカボチャケーキ作る
   *
   * @param e 　ブロック配置
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

  /**
   * 特定のアイテムを持って猫に触れると猫の毛色を変化する(黒、白、それ以外)
   *
   * @param e 　猫と交流時
   */
  @EventHandler
  public void CatColorChange(PlayerInteractEntityEvent e) {
    Player player = e.getPlayer();
    Entity entity = e.getRightClicked();
    ItemStack mainItem = player.getInventory().getItemInMainHand();
    if (count % 2 == 0 && entity instanceof Cat cat) {
      Material type = mainItem.getType();
      switch (type) {
        case INK_SAC -> {
          cat.setCatType(Type.ALL_BLACK);
          mainItem.setAmount(mainItem.getAmount() - 1);
        }
        case MILK_BUCKET -> {
          cat.setCatType(Type.WHITE);
          mainItem.setType(Material.BUCKET);
        }
        case RABBIT_FOOT -> {
          List<String> cats = getConfig().getStringList("CatColor");
          int i = new SplittableRandom().nextInt(cats.size());
          cat.setCatType(Type.valueOf(cats.get(i)));
          mainItem.setAmount(mainItem.getAmount() - 1);
        }
      }
    }
  }

  /**
   * プレイヤーが朝起きた時 黒猫と白猫が座ってない状態で近くにいるとプレゼントが豪華になる
   *
   * @param e 　プレイヤーが朝起きた時
   */
  @EventHandler
  public void CatTreasurePresent(PlayerBedLeaveEvent e) {
    Player player = e.getPlayer();
    World world = player.getWorld();
    for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
      if (entity instanceof Cat cat && !cat.isSitting()) {
        LootContext context = new Builder(cat.getLocation()).build();
        switch (cat.getCatType()) {
          case ALL_BLACK -> {
            Collection<ItemStack> itemStacks = LootTables.WOODLAND_MANSION.getLootTable()
                .populateLoot(null, context);
            itemStacks.forEach(itemStack -> world.dropItem(cat.getLocation(), itemStack));
          }
          case WHITE -> {
            Collection<ItemStack> itemStacks = LootTables.ANCIENT_CITY_ICE_BOX.getLootTable()
                .populateLoot(null, context);
            itemStacks.forEach(itemStack -> world.dropItem(cat.getLocation(), itemStack));
          }
        }
      }
    }
  }
}









