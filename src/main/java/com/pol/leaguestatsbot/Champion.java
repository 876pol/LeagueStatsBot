package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.Orianna;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Champion extends Command {

    public Champion(MessageReceivedEvent event, String[] command) {
        this.event = event;
        this.command = command;
    }

    @Override
    public void setCommand() {
        try {
            championStats(Main.convertStringArrayToString(command, 2));
        } catch (NullPointerException | IllegalStateException e) {
            invalidCommand(0);
        }
    }

    @Override
    public void invalidCommand(int i) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("No Champion was Found")
                .setColor(Main.randomColor())
                .setFooter("use \" + Main.prefix + \" champion list to list all champions");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private void championStats(String input) throws NullPointerException, IllegalStateException {
        MessageChannel channel = event.getChannel();
        com.merakianalytics.orianna.types.core.staticdata.Champion champion =
                Orianna.championNamed(inputToChampionName(input)).get();
        String title = champion.getTitle();
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(champion.getName())
                .setDescription(title.substring(0, 1).toUpperCase() + title.substring(1))
                .setColor(Main.randomColor())
                .setThumbnail(champion.getImage().getURL())
                .setImage("http://ddragon.leagueoflegends.com/cdn/img/champion/splash/" + champion.getKey() + "_0.jpg")
                .addField("Lore:", champion.getBlurb().replaceAll("—", " ") + " [Read more]"
                                + "(https://universe.leagueoflegends.com/en_US/champion/" + champion.getKey().toLowerCase() + "/)"
                        , false)
                .addField("Tags:", champion.getTags().toString().replaceAll("[\\[\\]]", "")
                        .replaceAll(", ", "\n"), true)
                .addField("Secondary Resource Bar:", champion.getResource(), true);
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private static String inputToChampionName(String input) {
        if (!(input.length() >= 2)) {
            return null;
        }
        final List<String> data =
                Orianna.getChampions().stream().map(com.merakianalytics.orianna.types.core.staticdata.Champion::getName).collect(Collectors.toList());
        return Main.mostSimilar(data, input);
    }
}
