package de.c0debase.bot.commands.general;

import de.c0debase.bot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RoleStatsCommand extends Command {

    private static final List<String> FORBIDDEN;
    private static final String DESCRIPTION_PATTERN = "***%s*** (%d)\n";

    static {
        FORBIDDEN = Arrays.asList("Projekt", "Friend", "-_-", "Mute", "@everyone");
    }

    public RoleStatsCommand() {
        super("rolestats", "Zeigt wie viele Mitglieder eine Rolle haben", Category.GENERAL);
    }

    @Override
    public void execute(final String[] args, final Message message) {
        final Guild guild = message.getGuild();
        final EmbedBuilder embedBuilder = getEmbed(guild, message.getAuthor());
        embedBuilder.setTitle("Rollen Statistiken");
        final Member selfMember = guild.getSelfMember();
        final Map<Role, Long> roles = guild.getMembers()
                .stream().map(Member::getRoles)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        roles.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .filter(role -> !role.isManaged())
                .filter(role -> !FORBIDDEN.contains(role.getName()))
                .filter(role -> PermissionUtil.canInteract(selfMember, role))
                .map(role -> String.format(DESCRIPTION_PATTERN, role.getName(), roles.get(role)))
                .forEach(embedBuilder::appendDescription);
        message.getTextChannel().sendMessage(embedBuilder.build()).queue();
    }
}