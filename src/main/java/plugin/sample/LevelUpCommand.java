package plugin.sample;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelUpCommand implements CommandExecutor {

  private Main main;

  public LevelUpCommand(Main m) {
    this.main = m;
  }

  //レベルコントロールコマンド(実行部)
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player player) {
      if (args.length == 1) {
        player.giveExpLevels(Integer.parseInt(args[0]));
      } else {
        player.sendMessage(main.getConfig().getString("Message"));
      }

    }
    return false;
  }
}
