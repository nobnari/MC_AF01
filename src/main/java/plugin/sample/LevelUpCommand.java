package plugin.sample;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelUpCommand implements CommandExecutor {

  //レベルコントロールコマンド(実行部)
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      player.giveExpLevels(Integer.parseInt(args[0]));
    }
    return false;
  }
}
