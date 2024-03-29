package plugin.sample;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.Cat.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootContext.Builder;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import plugin.sample.command.LevelUpCommand;
import plugin.sample.command.LightningCommand;

import java.util.*;

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
    if (count % 2 == 1) {
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
   * プレイヤーが走ってる間、プレイヤーの周りに炎が出る
   *
   * @param e 　プレイヤーが走った時
   */
  @EventHandler
  public void onPlayerRun(PlayerMoveEvent e) {
    Player player = e.getPlayer();
    if (player.isSprinting()) {
      Location location = player.getLocation();
      player.getWorld().spawnParticle(Particle.FLAME, location, 10);
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
  public void CatMorningGift(PlayerBedLeaveEvent e) {
    Player player = e.getPlayer();
    World world = player.getWorld();
    for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
      if (entity instanceof Cat cat && !cat.isSitting()) {
        LootContext context = new Builder(cat.getLocation()).build();
        switch (cat.getCatType()) {
          case ALL_BLACK -> {
            Collection<ItemStack> itemStacks = LootTables.ANCIENT_CITY.getLootTable()
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

  /**
   * ピンクの羊がターゲットする時、羊の歩いた後に花が生える
   *
   * @param e 　ヒツジがプレイヤー見る時
   */
  @EventHandler
  public void sheepWalker(EntityTargetLivingEntityEvent e) {
    if (e.getEntity() instanceof Sheep sheep && sheep.getColor() == DyeColor.PINK) {
      Location location = sheep.getLocation();
      List<String> flowers = getConfig().getStringList("flowers");
      int i = new SplittableRandom().nextInt(flowers.size());
      location.getBlock().setType(Material.valueOf(flowers.get(i)));

    }
  }

  /**
   * 金のクワを空気に振るうと目線の先にテレポートする。 プレイヤーの向きはテレポート前と同じになる。
   *
   * @param e 　金のクワを振るった時
   */
  @EventHandler
  public void DiamondHoeTeleport(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    Location l = player.getLocation();
    Location l2 = player.getTargetBlock(null, 100).getLocation();
    ItemStack mainItem = player.getInventory().getItemInMainHand();
    if (mainItem.getType() == Material.GOLDEN_HOE && e.getAction() == Action.RIGHT_CLICK_AIR) {
      Location location = l2.setDirection(l.getDirection().toBlockVector());
      player.teleport(location);
      player.playSound(player.getLocation(), Sound.ENTITY_FOX_TELEPORT, 30, 45);
    }
  }

  /**
   * プレイヤーが金の斧を振ると、プレイヤーの速度を上昇させる
   * @param e プレイヤーが金の斧を振った時
   */
  @EventHandler
  public void onPlayerGoldAxeSwing(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    ItemStack mainItem = player.getInventory().getItemInMainHand();
    if (mainItem.getType() == Material.GOLDEN_AXE && e.getAction() == Action.RIGHT_CLICK_AIR) {
      player.setVelocity(player.getLocation().getDirection().multiply(1.5));

    }
  }

  /**
   * プレイヤーが金のブーツを履いてを歩くと、ワンブロック前方の溶岩が黒曜石に変わる。
   *
   * @param e プレイヤーが歩いた時
   */
  @EventHandler
  public void onPlayerLavaWalk(PlayerMoveEvent e) {
    Player player = e.getPlayer();
    Location location = player.getLocation();
    Vector vector = location.getDirection();
    Location locationB = location.add(vector);
    World world = player.getWorld();
    if (Objects.nonNull(player.getInventory().getBoots())
        && player.getInventory().getBoots().getType() == Material.GOLDEN_BOOTS
        && world.getBlockAt(locationB).getType() == Material.LAVA) {
      world.getBlockAt(locationB).setType(Material.OBSIDIAN);
    }
  }

  /**
   * プレイヤーが黒曜石の上に氷塊を乗せると、黒曜石が溶岩に変わる アイテムとして氷をドロップする
   *
   * @param e プレイヤーが氷を乗せた時
   */
  @EventHandler
  public void onPlayerIcePlace(BlockPlaceEvent e) {
    Player player = e.getPlayer();
    World world = player.getWorld();
    Location l = e.getBlock().getLocation();
    Location l2 = new Location(world, l.getX(), l.getY() - 1, l.getZ());
    if (e.getBlock().getType() == Material.PACKED_ICE
        && world.getBlockAt(l2).getType() == Material.OBSIDIAN) {
      world.getBlockAt(l2).setType(Material.LAVA);
      world.getBlockAt(l).setType(Material.AIR);
      world.dropItem(l, new ItemStack(Material.ICE));
    }
  }

  /**
   * 襲撃者がダメージを食らうと近くにいるエンティティのリストを作り
   * その中の襲撃者のドロップテーブルが宝釣りのテーブルに変わる
   *
   * @param e 襲撃者がダメージを食らった時
   */
    @EventHandler
    public void onPillagerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Pillager pillager) {
            List<Entity> entityList = pillager.getNearbyEntities(16, 2, 16);
            for (Entity entity : entityList) {
              if (entity instanceof Pillager pillager2) {
               pillager2.setLootTable(LootTables.FISHING_TREASURE.getLootTable());

            }
          }
        }
    }

  /**
   * クリーパーが爆発しようとする時、近くにダイヤモンドの帽子をかぶったプレイヤーがいると爆発しない
   * また、クリーパーがプレイヤーに対して、少し上昇して離れる
   *
   * @param e クリーパーが爆発しようとした時
   */
  @EventHandler
  public void defuseCreeper(ExplosionPrimeEvent e) {
    if (e.getEntity() instanceof Creeper creeper) {
      List<Entity> entityList = creeper.getNearbyEntities(16, 2, 16);
      for (Entity entity : entityList) {
        if (entity instanceof Player player) {
          ItemStack helmet = player.getInventory().getHelmet();
          if (Objects.nonNull(helmet) && helmet.getType() == Material.DIAMOND_HELMET) {
            e.setCancelled(true);
            Location l = creeper.getLocation();
            Vector v = l.getDirection();
            Vector v2 = v.multiply(-0.7).setY(0.8);
            creeper.setVelocity(v2);
          }
        }
      }
    }
  }

  /**
   * プレイヤーがダイヤモンドのクワを空気に向かって振ると、プレイヤーの居る地点から
   * 手前に１マスy軸にマイナス2マスの位置にその場所が空気の時ガラスを生成する
   *
   * @param e プレイヤーがダイヤモンドのクワを振った時
   */
    @EventHandler
    public void onPlayerDiamondHoeSwing(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack mainItem = player.getInventory().getItemInMainHand();
      Location l = player.getLocation();
      Vector v = l.getDirection();
      Location l2 = l.add(v).add(0, -1, 0);
        if (mainItem.getType() == Material.DIAMOND_HOE && e.getAction() == Action.RIGHT_CLICK_AIR
        && l2.getBlock().getType() == Material.AIR) {
            World world = player.getWorld();
            world.getBlockAt(l2).setType(Material.GLASS);
        }
    }

//  /**
//   * プレイヤーがジャンプ速度で地面を離れると、４種の中から1つランダムでキノコが生える(検証用)
//   *
//   * @param e プレイヤーがジャンプした時
//   */
//  @EventHandler
//  public void onPlayerJump(PlayerMoveEvent e) {
//    Player player = e.getPlayer();
//    Location fromL = e.getFrom();
//    World world = player.getWorld();
//    if (player.getVelocity().getY() > 0.4&& world.getBlockAt(fromL).getType()== Material.AIR) {
//      List<String> mushrooms = getConfig().getStringList("mushrooms");
//      int i = new SplittableRandom().nextInt(4);
//      fromL.getBlock().setType(Material.valueOf(mushrooms.get(i)));
//    }
//  }

//  /**
//   * スニークスポナー(検証用)
//   * @param e　プレイヤーがスニークした時
//   */
//  @EventHandler
//  public void onPlayerSneak(PlayerToggleSneakEvent e) {
//    Player player = e.getPlayer();
//    Location l = player.getLocation();
//    World world = player.getWorld();
//    if (e.isSneaking()) {
//      Entity entity= world.spawnEntity(l, EntityType.SHEEP);
//if (entity instanceof Sheep sheep ) {
//  sheep.setColor(DyeColor.PINK);
//}
//
//    }
//  }

}









