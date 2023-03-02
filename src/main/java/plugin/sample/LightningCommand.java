package plugin.sample;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class LightningCommand implements CommandExecutor {

  //オンラインのプレイヤー全員に雷を落とすサーバー側専用コマンド(実行部)
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof ConsoleCommandSender) {
      for (Player player : sender.getServer().getOnlinePlayers()) {
        player.getWorld().strikeLightning(player.getLocation());
        System.out.println("プレイヤーどもに天罰を下しました");
      }
    } else {
      sender.sendMessage("このコマンドは神にしか使えません");
    }
    return false;
  }
}
