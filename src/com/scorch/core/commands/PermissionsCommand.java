package com.scorch.core.commands;

import com.scorch.core.ScorchCore;
import com.scorch.core.modules.data.SQLSelector;
import com.scorch.core.modules.permissions.PermissionGroup;
import com.scorch.core.modules.permissions.PermissionPlayer;
import com.scorch.core.utils.MSG;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.io.Console;
import java.util.*;

public class PermissionsCommand extends BukkitCommand {

    public PermissionsCommand (String name) {
        super(name);
        this.setAliases(Collections.singletonList("perms"));
        this.setPermission("scorch.permissions.manage");
        this.setPermissionMessage(ScorchCore.getInstance().getMessages().getMessage("noperm").getMessage());

    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            MSG.tell(sender, getPermissionMessage());
            return true;
        }

        if(args.length == 0){
            MSG.tell(sender, "/permissions player|group");
            return true;
        }

        if(args.length >= 1 && args.length <= 3){
            if(args[0].equalsIgnoreCase("player")){
                MSG.tell(sender, "/permissions player <playerName> add <node>");
                MSG.tell(sender, "/permissions player <playerName> remove <node>");
                MSG.tell(sender, "/permissions player <playerName> group add <groupName>");
                MSG.tell(sender, "/permissions player <playerName> group remove <groupName>");
                MSG.tell(sender, "/permissions player <playerName> group set <groupName>");
            }
            else if(args[0].equalsIgnoreCase("group")){
                if(args.length == 3){
                    if(args[1].equalsIgnoreCase("delete")){
                        if(ScorchCore.getInstance().getPermissionModule().getGroup(args[2]) != null){
                            if(sender instanceof Player && !sender.isOp()){
                                if(ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player)sender)
                                        .getUniqueId()).getPrimaryGroup().getWeight() > ScorchCore.getInstance().getPermissionModule().getGroup(args[2]).getWeight()){
                                    ScorchCore.getInstance().getPermissionModule().deleteGroup(args[2]);
                                    MSG.tell(sender, "&aSuccessfully deleted the group!");
                                }
                                else {
                                    MSG.tell(sender,"&cYou cannot delete that group, because it's higher than yours!");
                                }
                            }
                            else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                                ScorchCore.getInstance().getPermissionModule().deleteGroup(args[2]);
                                MSG.tell(sender, "&aSuccessfully deleted the group!");
                            }
                        }
                        else {
                            MSG.tell(sender, "&cThat group doesn't exist!");
                        }
                    }
                }
                else {
                    MSG.tell(sender, "/permissions group <groupName> add <node>");
                    MSG.tell(sender, "/permissions group <groupName> remove <node>");
                    MSG.tell(sender, "/permissions group <groupName> prefix <prefix>");
                    MSG.tell(sender, "/permissions group <groupName> inherited add <groupName>");
                    MSG.tell(sender, "/permissions group <groupName> inherited remove <groupName>");
                    MSG.tell(sender, "/permissions group create <groupName> <prefix> [inherited groups]");
                    MSG.tell(sender, "/permissions group delete <groupName>");
                }
            }
            else {
                MSG.tell(sender, "/permissions player|group");
            }
            return true;
        }

        if(args.length == 4){
            if(args[0].equalsIgnoreCase("player")){
                // Player command
                UUID target = null;
                if(Bukkit.getPlayer(args[1]) != null){
                    target = Bukkit.getPlayer(args[1]).getUniqueId();
                }
                else {
                    if(Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()){
                        target = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                    }
                    else {
                        MSG.tell(sender, String.format("&c%s has never joined the server before!", args[1]));
                    }
                }

                if(target != null){
                    PermissionPlayer permissionPlayer = ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(target);
                    if(args[2].equalsIgnoreCase("add")){
                        if(sender instanceof Player && !sender.isOp()){
                            if(sender.hasPermission(args[3])){
                                if(permissionPlayer.addPermission(args[3])){
                                    MSG.tell(sender, String.format("&aSuccessfully added &e%s &ato &e%s", args[3], args[1]));
                                }
                                else {
                                    MSG.tell(sender, String.format("&cCouldn't add &e%s &&cto &e&s &cbecause they already have that permission!", args[3], args[1]));
                                }
                            }
                            else {
                                MSG.tell(sender, String.format("&cCouldn't add &e%s &&cto &e&s &cbecause you lack the permission yourself!", args[3], args[1]));
                            }
                        }
                        else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                            if(permissionPlayer.addPermission(args[3])){
                                MSG.tell(sender, String.format("&aSuccessfully added &e%s &ato &e%s", args[3], args[1]));
                            }
                            else {
                                MSG.tell(sender, String.format("&cCouldn't add &e%s &&cto &e&s &cbecause they already have that permission!", args[3], args[1]));
                            }
                        }
                        else {
                            MSG.tell(sender, "&cYou're not authorised to edit this player's permissions");
                        }
                    }
                    else if(args[2].equalsIgnoreCase("remove")){
                        if(sender instanceof Player && !sender.isOp()){
                            if(sender.hasPermission(args[3])){
                                if(permissionPlayer.removePermission(args[3])){
                                    MSG.tell(sender, String.format("&aSuccessfully removed &e%s &afrom &e%s", args[3], args[1]));
                                }
                                else {
                                    MSG.tell(sender, String.format("&cCouldn't remove &e%s &&cfrom &e&s &cbecause they don't have that permission!", args[3], args[1]));
                                }
                            }
                            else {
                                MSG.tell(sender, String.format("&cCouldn't add &e%s &&cto &e&s &cbecause you lack the permission yourself!", args[3], args[1]));
                            }
                        }
                        else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                            if(permissionPlayer.removePermission(args[3])){
                                MSG.tell(sender, String.format("&aSuccessfully removed &e%s &afrom &e%s", args[3], args[1]));
                            }
                            else {
                                MSG.tell(sender, String.format("&cCouldn't remove &e%s &&cfrom &e&s &cbecause they don't have that permission!", args[3], args[1]));
                            }
                        }
                        else {
                            MSG.tell(sender, "&cYou're not authorised to edit this player's permissions");
                        }
                    }
                    else {
                        MSG.tell(sender, "/permissions player <playerName> add|remove|group node|set|add|remove [groupName]");
                    }
                }
                else {
                    MSG.tell(sender, "&cThat player couldn't be found!");
                }
            }
            else if(args[0].equalsIgnoreCase("group")){
                if(ScorchCore.getInstance().getPermissionModule().getGroup(args[1]) != null){
                    PermissionGroup group = ScorchCore.getInstance().getPermissionModule().getGroup(args[1]);
                    if(args[2].equalsIgnoreCase("add")){
                        if(sender instanceof Player && !sender.isOp()){
                            if(sender.hasPermission(args[3])){
                                if(ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player)sender).getUniqueId()).getPrimaryGroup().getWeight() > group.getWeight()){
                                    group.addPermission(args[3]);
                                    MSG.tell(sender, String.format("&aSuccessfully added &e%s &ato &e%s&a!", args[3], group.getGroupName()));
                                }
                                else {
                                    MSG.tell(sender, String.format("&cCouldn't edit the group because its higher than yours!"));
                                }
                            }
                            else {
                                MSG.tell(sender, "&cCouldn't add the permission to the group because you don't have the permission!");
                            }
                        }
                        else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                            group.addPermission(args[3]);
                            MSG.tell(sender, String.format("&aSuccessfully added &e%s &ato &e%s&a!", args[3], group.getGroupName()));
                        }
                        else {
                            MSG.tell(sender, "&cYou're not authorised to edit this group!");
                        }
                    }
                    else if(args[2].equalsIgnoreCase("remove")){
                        if(sender instanceof Player && !sender.isOp()){
                            if(sender.hasPermission(args[3])){
                                if(ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player)sender).getUniqueId()).getPrimaryGroup().getWeight() > group.getWeight()){
                                    group.removePermission(args[3]);
                                    MSG.tell(sender, String.format("&aSuccessfully remove &e%s &afrom &e%s&a!", args[3], group.getGroupName()));
                                }
                                else {
                                    MSG.tell(sender, String.format("&cCouldn't edit the group because its higher than yours!"));
                                }
                            }
                            else {
                                MSG.tell(sender, "&cCouldn't add the permission to the group because you don't have the permission!");
                            }
                        }
                        else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                            group.removePermission(args[3]);
                            MSG.tell(sender, String.format("&aSuccessfully removed &e%s &afrom &e%s&a!", args[3], group.getGroupName()));
                        }
                        else {
                            MSG.tell(sender, "&cYou're not authorised to edit this group!");
                        }
                    }
                    else if(args[2].equalsIgnoreCase("prefix")){
                        if(sender instanceof Player && !sender.isOp()){
                            if(ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player)sender).getUniqueId()).getPrimaryGroup().getWeight() > group.getWeight()){
                                group.setPrefix(args[3]);
                                MSG.tell(sender, String.format("&aSuccessfully set &e%s's prefix &ato %s&a!", group.getGroupName(), group.getPrefix()));
                            }
                            else {
                                MSG.tell(sender, String.format("&cCouldn't edit the group because its higher than yours!"));
                            }
                        }
                        else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                            group.setPrefix(args[3]);
                            MSG.tell(sender, String.format("&aSuccessfully set &e%s's prefix &ato %s&a!", group.getGroupName(), group.getPrefix()));
                        }
                        else {
                            MSG.tell(sender, "&cYou're not authorised to edit this group!");
                        }
                    }
                    else {
                        MSG.tell(sender, "/permissions group <groupName> add <node>");
                        MSG.tell(sender, "/permissions group <groupName> remove <node>");
                        MSG.tell(sender, "/permissions group <groupName> prefix <prefix>");
                        MSG.tell(sender, "/permissions group <groupName> inherited add <groupName>");
                        MSG.tell(sender, "/permissions group <groupName> inherited remove <groupName>");
                        MSG.tell(sender, "/permissions group create <groupName> <prefix> [inherited groups]");
                        MSG.tell(sender, "/permissions group delete <groupName>");
                    }
                }
                else {
                    MSG.tell(sender, "&cThat group doesn't exist!");
                }
            }
            else {
                MSG.tell(sender, "/permissions player <playerName> add|remove|group node|set|add|remove [groupName]");
                MSG.tell(sender, "/permissions group <groupName> add|remove|set|create|delete node|prefix|inherit [value]");
            }
            return true;
        }

        if(args.length == 5){
            if(args[0].equalsIgnoreCase("player")){
                // Player command
                UUID target = null;
                if(Bukkit.getPlayer(args[1]) != null){
                    target = Bukkit.getPlayer(args[1]).getUniqueId();
                }
                else {
                    if(Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()){
                        target = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                    }
                    else {
                        MSG.tell(sender, String.format("&c%s has never joined the server before!", args[1]));
                    }
                }

                if(target != null){
                    PermissionPlayer permissionPlayer = ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(target);
                    if(args[2].equalsIgnoreCase("group")){
                        if(args[3].equalsIgnoreCase("add")){
                            if(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]) != null){
                                if(sender instanceof Player && !sender.isOp()){
                                    if(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]).getWeight() <
                                            ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player)sender)).getPrimaryGroup().getWeight()){
                                        if(permissionPlayer.addGroup(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]))){

                                            MSG.tell(sender, String.format("&aSuccessfully added the group &e%s &ato &e%s&a!", args[4], args[1]));
                                        }
                                        else {
                                            MSG.tell(sender, String.format("&cCouldn't add the group &e%s &cto &e%s &cbecause they're already in that group!", args[4], args[1]));
                                        }
                                    }
                                    else {
                                        MSG.tell(sender, "&cCouldn't add the group to the player because it's higher than yours!");
                                    }
                                }
                                else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                                    if(permissionPlayer.addGroup(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]))){
                                        MSG.tell(sender, String.format("&aSuccessfully added the group &e%s &ato &e%s&a!", args[4], args[1]));
                                    }
                                    else {
                                        MSG.tell(sender, String.format("&cCouldn't add the group &e%s &cto &e%s &cbecause they're already in that group!", args[4], args[1]));
                                    }
                                }
                                else {
                                    MSG.tell(sender, "&cYou're not authorised to edit this player's groups!");
                                }
                            }
                            else {
                                MSG.tell(sender, String.format("&e%s isn't a valid group!", args[4]));
                            }
                        }
                        else if(args[3].equalsIgnoreCase("remove")){
                            if(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]) != null){
                                if(sender instanceof Player && !sender.isOp()) {
                                    if (ScorchCore.getInstance().getPermissionModule().getGroup(args[4]).getWeight() <
                                            ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player) sender)).getPrimaryGroup().getWeight()) {
                                        if (permissionPlayer.removeGroup(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]))) {
                                            MSG.tell(sender, String.format("&aSuccessfully removed the group &e%s &afrom &e%s&a!", args[4], args[1]));
                                        }
                                        else {
                                            MSG.tell(sender, String.format("&cCouldn't remove the group &e%s &cfrom &e%s &cbecause they're not in that group!", args[4], args[1]));
                                        }
                                    } else {
                                        MSG.tell(sender, "&cCouldn't add the group to the player because it's higher than yours!");
                                    }
                                }
                                else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                                    if (permissionPlayer.removeGroup(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]))) {
                                        MSG.tell(sender, String.format("&aSuccessfully removed the group &e%s &afrom &e%s&a!", args[4], args[1]));
                                    }
                                    else {
                                        MSG.tell(sender, String.format("&cCouldn't remove the group &e%s &cfrom &e%s &cbecause they're not in that group!", args[4], args[1]));
                                    }
                                }
                                else {
                                    MSG.tell(sender, "&cYou're not authorised to edit this player's groups!");
                                }
                            }
                            else {
                                MSG.tell(sender, String.format("&e%s isn't a valid group!", args[4]));
                            }
                        }
                        else if(args[3].equalsIgnoreCase("set")){
                            if(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]) != null){
                                if(sender instanceof Player && !sender.isOp()) {
                                    if (ScorchCore.getInstance().getPermissionModule().getGroup(args[4]).getWeight() <
                                            ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player) sender)).getPrimaryGroup().getWeight()) {
                                        if (permissionPlayer.setGroup(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]))) {
                                            MSG.tell(sender, String.format("&aSuccessfully &e%s's &agroup to &e%s&a!", args[1], args[4]));
                                        }
                                        else {
                                            MSG.tell(sender, "&cCouldn't set the group, because the group is null!");
                                        }
                                    } else {
                                        MSG.tell(sender, "&cCouldn't set the player's group because it's higher than yours!");
                                    }
                                }
                                else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                                    if (permissionPlayer.setGroup(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]))) {
                                        MSG.tell(sender, String.format("&aSuccessfully &e%s's &agroup to &e%s&a!", args[1], args[4]));
                                    }
                                    else {
                                        MSG.tell(sender, "&cCouldn't set the group, because the group is null!");
                                    }
                                }
                                else {
                                    MSG.tell(sender, "&cYou're not authorised to edit this player's groups!");
                                }
                            }
                            else {
                                MSG.tell(sender, String.format("&e%s isn't a valid group!", args[4]));
                            }
                        }
                        else {
                            MSG.tell(sender, "/permissions player <playerName> add|remove|group node|set|add|remove [groupName]");
                        }

                    }
                }
                else {
                    MSG.tell(sender, "&cThat player couldn't be found!");
                }
            }
            else if(args[0].equalsIgnoreCase("group")){
                if(args[1].equalsIgnoreCase("create")) {
                    if (ScorchCore.getInstance().getPermissionModule().getGroup(args[2]) == null) {
                        if (ScorchCore.getInstance().getPermissionModule().getGroup(args[4]) != null) {
                            PermissionGroup group = new PermissionGroup(args[2], false, args[3], 0);
                            group.addInheritedGroup(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]));
                            ScorchCore.getInstance().getPermissionModule().addGroup(group);
                            MSG.tell(sender, String.format("&aSuccessfully created a group with the name &e%s&a!", group.getGroupName()));
                        } else {
                            MSG.tell(sender, "That inherited group doesn't exist!");
                        }

                    } else {
                        MSG.tell(sender, "&cA group with that name already exists!");
                    }
                }
                else if(args[1].equalsIgnoreCase("inherited")){
                    // get group
                    if(ScorchCore.getInstance().getPermissionModule().getGroup(args[1]) != null) {
                        PermissionGroup group = ScorchCore.getInstance().getPermissionModule().getGroup(args[1]);
                        if(args[2].equalsIgnoreCase("inherited")){
                            if(args[3].equalsIgnoreCase("add")){
                                if(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]) != null){
                                    PermissionGroup targetGroup = ScorchCore.getInstance().getPermissionModule().getGroup(args[4]);
                                    if(sender instanceof Player && !sender.isOp()){
                                        int senderWeight = ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player)sender).getUniqueId()).getPrimaryGroup().getWeight();
                                        if(senderWeight > group.getWeight() && senderWeight > targetGroup.getWeight()){
                                            group.addInheritedGroup(targetGroup);
                                            MSG.tell(sender, String.format("&aSuccessfully added &e%s &ato &e%s &aas inherited group!", targetGroup.getGroupName(), group.getGroupName()));
                                        }
                                        else {
                                            MSG.tell(sender, "&cCouldn't edit group because the group or target group is higher than yours!");
                                        }
                                    }
                                    else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                                        group.addInheritedGroup(targetGroup);
                                        MSG.tell(sender, String.format("&aSuccessfully added &e%s &ato &e%s &aas inherited group!", targetGroup.getGroupName(), group.getGroupName()));
                                    }
                                    else {
                                        MSG.tell(sender, "&cYou're not authorised to edit this group!");
                                    }
                                }
                                else {
                                    MSG.tell(sender, "&cThe group you're trying to add doesn't exist!");
                                }
                            }
                            else if(args[3].equalsIgnoreCase("remove")){
                                if(ScorchCore.getInstance().getPermissionModule().getGroup(args[4]) != null){
                                    PermissionGroup targetGroup = ScorchCore.getInstance().getPermissionModule().getGroup(args[4]);
                                    if(sender instanceof Player && !sender.isOp()){
                                        int senderWeight = ScorchCore.getInstance().getPermissionModule().getPermissionPlayer(((Player)sender).getUniqueId()).getPrimaryGroup().getWeight();
                                        if(senderWeight > group.getWeight() && senderWeight > targetGroup.getWeight()){
                                            group.removeInheritedGroup(targetGroup);
                                            MSG.tell(sender, String.format("&aSuccessfully removed &e%s &afrom &e%s &aas inherited group!", targetGroup.getGroupName(), group.getGroupName()));
                                        }
                                        else {
                                            MSG.tell(sender, "&cCouldn't edit group because the group or target group is higher than yours!");
                                        }
                                    }
                                    else if(sender instanceof ConsoleCommandSender || sender.isOp()){
                                        group.addInheritedGroup(targetGroup);
                                        MSG.tell(sender, String.format("&aSuccessfully remove &e%s &afrom &e%s &aas inherited group!", targetGroup.getGroupName(), group.getGroupName()));
                                    }
                                    else {
                                        MSG.tell(sender, "&cYou're not authorised to edit this group!");
                                    }
                                }
                                else {
                                    MSG.tell(sender, "&cThe group you're trying to remove doesn't exist!");
                                }
                            }
                            else {
                                MSG.tell(sender, "/permissions group <groupName> inherited add <groupName>");
                                MSG.tell(sender, "/permissions group <groupName> inherited remove <groupName>");
                            }
                        }
                        else {
                            MSG.tell(sender, "/permissions group <groupName> add <node>");
                            MSG.tell(sender, "/permissions group <groupName> remove <node>");
                            MSG.tell(sender, "/permissions group <groupName> prefix <prefix>");
                            MSG.tell(sender, "/permissions group <groupName> inherited add <groupName>");
                            MSG.tell(sender, "/permissions group <groupName> inherited remove <groupName>");
                            MSG.tell(sender, "/permissions group create <groupName> <prefix> [inherited groups]");
                            MSG.tell(sender, "/permissions group delete <groupName>");
                        }
                    }
                    else {
                        MSG.tell(sender, "&cThat group doesn't exist!");
                    }
                }
            }
            else {
                MSG.tell(sender, "/permissions player <playerName> add|remove|group node|set|add|remove [groupName]");
                MSG.tell(sender, "/permissions group <groupName> add|remove|set node|prefix|inherit [value]");
            }
            return true;
        }
        else if(args.length >= 5){
            if(args[0].equalsIgnoreCase("group")) {
                if (args[1].equalsIgnoreCase("create")) {
                    if (ScorchCore.getInstance().getPermissionModule().getGroup(args[2]) == null) {
                        List<PermissionGroup> inherited = new ArrayList<>();
                        for(int i = 4; i < args.length; i++){
                            if(ScorchCore.getInstance().getPermissionModule().getGroup(args[i]) != null){
                                inherited.add(ScorchCore.getInstance().getPermissionModule().getGroup(args[i]));
                            }
                            else {
                                MSG.tell(sender, String.format("&e%s &cis an invalid group, so it cannot be added as an inherited group!", args[i]));
                            }
                        }
                        if(inherited.size() > 0){
                            PermissionGroup group = new PermissionGroup(args[2], false, args[3], 0);
                            for(PermissionGroup inherit : inherited){
                                group.addInheritedGroup(inherit);
                            }
                            ScorchCore.getInstance().getPermissionModule().addGroup(group);
                            MSG.tell(sender, String.format("&aSuccessfully added a new group &e%s&a!", group.getGroupName()));
                        }
                        else {
                            MSG.tell(sender, "&cInvalid inherited groups!");
                        }
                    } else {
                        MSG.tell(sender, "&cA group with that name already exists!");
                    }
                }
                else {
                    MSG.tell(sender, "/permissions group <groupName> add <node>");
                    MSG.tell(sender, "/permissions group <groupName> remove <node>");
                    MSG.tell(sender, "/permissions group <groupName> prefix <prefix>");
                    MSG.tell(sender, "/permissions group <groupName> inherited add <groupName>");
                    MSG.tell(sender, "/permissions group <groupName> inherited remove <groupName>");
                    MSG.tell(sender, "/permissions group create <groupName> <prefix> [inherited groups]");
                    MSG.tell(sender, "/permissions group delete <groupName>");
                }
            }
            else {
                MSG.tell(sender, "/permissions group <groupName> add <node>");
                MSG.tell(sender, "/permissions group <groupName> remove <node>");
                MSG.tell(sender, "/permissions group <groupName> prefix <prefix>");
                MSG.tell(sender, "/permissions group <groupName> inherited add <groupName>");
                MSG.tell(sender, "/permissions group <groupName> inherited remove <groupName>");
                MSG.tell(sender, "/permissions group create <groupName> <prefix> [inherited groups]");
                MSG.tell(sender, "/permissions group delete <groupName>");
            }
        }
        return false;
    }
}
