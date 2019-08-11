package com.scorch.core.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.messages.MessagesModule;
import com.scorch.core.utils.MSG;

public class MessageCommand extends BukkitCommand {

    public MessageCommand(String name) {
        super(name);
        setPermission("scorch.command.message");
        setPermissionMessage(ScorchCore.getInstance().getMessage("noperm"));
        setAliases(Arrays.asList("m", "w", "msg", "tell"));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        MessagesModule mm = ScorchCore.getInstance().getMessages();
        if (!sender.hasPermission(getPermission())) {
            MSG.tell(sender, getPermissionMessage());
            return true;
        }

        if (args.length < 2) {
            MSG.tell(sender, "/" + commandLabel + " [Player] [Message]");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        String message = "";
        for (int i = 1; i < args.length; i++) {
            message += args[i] + " ";
        }

        message = message.trim();

        if (target.isOnline()) {
            MSG.tell(target.getPlayer(), message);
            MSG.tell(sender, message);

            return true;
        }

        if (!sender.hasPermission("scorch.command.message.cross-server")) {
            MSG.cTell(sender, "noperm");
            return true;
        }

        if (!(sender instanceof Player)) {
            MSG.tell(sender, "Only players can send cross-server messages");
            return true;
        }

        Player player = (Player) sender;

        mm.sendMessage(player, target.getUniqueId(), message);
        return true;
    }

}
