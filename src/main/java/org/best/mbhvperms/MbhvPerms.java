package org.best.mbhvperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class MbhvPerms extends JavaPlugin {

    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();
        this.getCommand("perm").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if (args.length < 2) {
                    return false;
                }

                String action = args[0];
                String target = args[1];

                switch (action.toLowerCase()) {
                    case "add":
                    case "remove":
                        if (args.length < 3) {
                            return false;
                        }
                        String permission = args[2];
                        User user = luckPerms.getUserManager().getUser(target);
                        if (user != null) {
                            if ("add".equals(action)) {
                                user.data().add(PermissionNode.builder(permission).build());
                                sender.sendMessage("§a" + user.getUsername() + " now has the " + permission + " permission!");
                            } else {
                                user.data().remove(PermissionNode.builder(permission).build());
                                sender.sendMessage("§c" + user.getUsername() + " doesn't have the " + permission + " permission anymore!");
                            }
                            try {
                                luckPerms.getUserManager().saveUser(user).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "player":
                        User player = luckPerms.getUserManager().getUser(target);
                        if (player != null) {
                            Set<String> permissions = player.getNodes().stream()
                                    .filter(NodeType.PERMISSION::matches)
                                    .map(NodeType.PERMISSION::cast)
                                    .map(PermissionNode::getPermission)
                                    .collect(Collectors.toSet());
                            sender.sendMessage("§a Permissions for " + target + ": §f" + String.join(", ", permissions));
                        }
                        break;
                    case "permission":
                        Set<User> users = new HashSet<>();
                        for (User user1 : luckPerms.getUserManager().getLoadedUsers()) {
                            if (user1.getCachedData().getPermissionData().checkPermission(target).asBoolean()) {
                                users.add(user1);
                            }
                        }
                        sender.sendMessage("§a Users with permission " + target + ": §f" + users.stream().map(User::getUsername).collect(Collectors.joining(", ")));
                        break;
                    case "list":
                        break;
                }

                return true;
            }
        });

        this.getCommand("perm").setTabCompleter(new TabCompleter() {
            @Override
            public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
                List<String> completions = new ArrayList<>();
                if (args.length == 1) {
                    completions.add("add");
                    completions.add("remove");
                    completions.add("player");
                    completions.add("permission");
                    completions.add("list");
                }
                return completions;
            }
        });
    }

    @Override
    public void onDisable() {
    }
}
