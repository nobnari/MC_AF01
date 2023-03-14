package plugin.sample.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.sample.Main;

public class LevelUpCommand implements CommandExecutor {

  private Main main;

  public LevelUpCommand(Main m) {
    this.main = m;
  }

  /**レベルをプラスマイナスするコマンド実行部
   * @param sender Source of the command
   * @param command Command which was executed
   * @param label Alias of the command which was used
   * @param args Passed command arguments
   * @return
   */
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      if (args.length == 1) {
        player.giveExpLevels(Integer.parseInt(args[0]));
      } else {
        player.sendMessage(main.getConfig().getString("ExceptionMessage"));
      }

    }
    return false;
  }
}


