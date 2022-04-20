package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.core.staticdata.ReforgedRune;
import com.merakianalytics.orianna.types.core.staticdata.ReforgedRunePath;
import com.merakianalytics.orianna.types.core.staticdata.ReforgedRuneSlot;
import com.merakianalytics.orianna.types.core.staticdata.ReforgedRuneTree;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Runes extends Command {

    public Runes(MessageReceivedEvent event, String[] command) {
        this.event = event;
        this.command = command;
    }

    @Override
    public void setCommand() {
        runeStat(inputToRuneName(Main.convertStringArrayToString(command, 2)));
    }

    @Override
    public void invalidCommand(int i) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("No Rune was Found")
                .setColor(Main.randomColor())
                .setFooter("use \" + Main.prefix + \" rune *rune name* for item descriptions");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private void runeStat(String runeName) {
        ReforgedRune runeObj = Orianna.reforgedRuneNamed(runeName).get();
        try {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle(runeObj.getName() + ":")
                    .setColor(Main.randomColor())
                    .addField(runeObj.getPath() + " Tree, " + (runeObj.getSlot() == 0 ?
                            "Keystone Rune" : "Slot " + runeObj.getSlot()), runeObj.getLongDescription()
                            .replaceAll("<li>|<br>", "\n").replaceAll("<.*?>|@.*?@", ""), false)
                    .setThumbnail("https://ddragon.leagueoflegends.com/cdn/img/" + runeObj.getImage());
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        } catch (NullPointerException e) {
            invalidCommand(0);
        }
    }

    private static String inputToRuneName(String input) {
        if (!(input.length() >= 3)) {
            return null;
        }
        List<String> data = Orianna.getReforgedRunes().stream()
                .map(ReforgedRune::getName)
                .sorted().collect(Collectors.toList());
        return Main.mostSimilar(data, input);
    }
}
