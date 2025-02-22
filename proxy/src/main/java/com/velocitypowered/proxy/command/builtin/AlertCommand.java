/*
 * Copyright (C) 2018-2024 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.command.builtin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Implements Velocity's {@code /alert} command.
 */
public class AlertCommand {

  private final ProxyServer server;
  private static final Map<String, String> colorMap = new HashMap<>();
  private static final Pattern symbolTranslator = Pattern.compile("(&#[A-Fa-f0-9]{6})|(#([A-Fa-f0-9]{6}))");

  static {
    colorMap.put("&0", "<reset><black>");
    colorMap.put("&1", "<reset><dark_blue>");
    colorMap.put("&2", "<reset><dark_green>");
    colorMap.put("&3", "<reset><dark_aqua>");
    colorMap.put("&4", "<reset><dark_red>");
    colorMap.put("&5", "<reset><dark_purple>");
    colorMap.put("&6", "<reset><gold>");
    colorMap.put("&7", "<reset><gray>");
    colorMap.put("&8", "<reset><dark_gray>");
    colorMap.put("&9", "<reset><blue>");
    colorMap.put("&a", "<reset><green>");
    colorMap.put("&b", "<reset><aqua>");
    colorMap.put("&c", "<reset><red>");
    colorMap.put("&d", "<reset><light_purple>");
    colorMap.put("&e", "<reset><yellow>");
    colorMap.put("&f", "<reset><white>");
    colorMap.put("&k", "<obfuscated>");
    colorMap.put("&l", "<bold>");
    colorMap.put("&m", "<strikethrough>");
    colorMap.put("&n", "<underlined>");
    colorMap.put("&o", "<italic>");
    colorMap.put("&r", "<reset>");
    colorMap.put("\\n", "<newline>");
  }

  public AlertCommand(ProxyServer server) {
    this.server = server;
  }

  /**
   * Registers the command.
   */
  public void register() {
    final LiteralArgumentBuilder<CommandSource> rootNode = BrigadierCommand
        .literalArgumentBuilder("alert")
        .requires(source ->
            source.getPermissionValue("velocity.command.alert") == Tristate.TRUE)
        .executes(this::usage)
        .then(BrigadierCommand
            .requiredArgumentBuilder("message", StringArgumentType.greedyString())
            .executes(this::alert));
    server.getCommandManager().register(new BrigadierCommand(rootNode.build()));
  }

  private int usage(final CommandContext<CommandSource> context) {
    context.getSource().sendMessage(
        Component.translatable("velocity.command.alert.usage", NamedTextColor.YELLOW)
    );
    return Command.SINGLE_SUCCESS;
  }

  private int alert(final CommandContext<CommandSource> context) {
    String message = StringArgumentType.getString(context, "message");
    if (message.isEmpty()) {
      context.getSource().sendMessage(
          Component.translatable("velocity.command.alert.no-message", NamedTextColor.YELLOW)
      );
      return 0;
    }

    message = message.replace("§", "&");

    for (Map.Entry<String, String> entry : colorMap.entrySet()) {
      message = message.replace(entry.getKey(), entry.getValue());
    }

    for (String s : colorMap.keySet()) {
      message = message.replace(s, colorMap.get(s));
    }

    Matcher matcher = symbolTranslator.matcher(message);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      String match = matcher.group();
      String replacement = "<" + match.replace("&", "") + ">";
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    message = sb.toString();

    server.sendMessage(Component.translatable("velocity.command.alert.message", NamedTextColor.YELLOW,
            MiniMessage.miniMessage().deserialize(message)));

    return Command.SINGLE_SUCCESS;
  }
}
