package plugin.sample.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class LightningCommand implements CommandExecutor {

  /**オンラインのプレイヤー全員に雷を落とすサーバー側専用コマンド実行部
   * @param sender Source of the command
   * @param command Command which was executed
   * @param label Alias of the command which was used
   * @param args Passed command arguments
   * @return
   */
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
