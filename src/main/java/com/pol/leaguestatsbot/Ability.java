package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.core.staticdata.Champion;
import com.merakianalytics.orianna.types.core.staticdata.ChampionSpell;
import com.merakianalytics.orianna.types.core.staticdata.SummonerSpell;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.stream.Collectors;

public class Ability extends Command {

    public Ability(MessageReceivedEvent event, String[] command) {
        this.event = event;
        this.command = command;
    }

    public void setCommand() {
        try {
            championAbilities(event, Main.convertStringArrayToString(command, 2));
        } catch (NullPointerException | IllegalStateException e) {
            summonerAbilities(event, Main.convertStringArrayToString(command, 2));
        }
    }

    @Override
    public void invalidCommand(int i) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("No Champion/Ability was Found")
                .setColor(Main.randomColor())
                .setFooter("use " + Main.prefix + " ability *Champion Name or Summoner Spell Name* for ability descriptions");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private void championAbilities(MessageReceivedEvent event, String champion) throws NullPointerException,
            IllegalStateException {
        MessageChannel channel = event.getChannel();
        Champion champObj = Orianna.championNamed(inputToChampionName(champion)).get();
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(champObj.getName() + " Abilities")
                .setColor(Main.randomColor())
                .setThumbnail(champObj.getImage().getURL())
                .addField(new MessageEmbed.Field("Passive: " + champObj.getPassive().getName(),
                        champObj.getPassive().description()
                                .replaceAll("<br>|<li>", "\n")
                                .replaceAll("<.*?>", "")
                        , false));
        String[] keys = new String[]{"Q: ", "W: ", "E: ", "R: "};
        for (int i = 0; i < 4; i++) {
            ChampionSpell spell = champObj.getSpells().get(i);
            eb.addField(new MessageEmbed.Field(keys[i] + spell.getName(),
                    spell.getDescription()
                            .replaceAll("<br>|<li>", "\n")
                            .replaceAll("<.*?>", "")
                            + "\n**Cooldown:** " + spell.getCooldowns()
                            .stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining("/"))
                            + "\n**Cost:** " + spell.getCosts()
                            .stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining("/"))
                    , false)
            );
        }
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private void summonerAbilities(MessageReceivedEvent event, String summonerSpell) {
        try {
            summonerSpell = summonerNameToIdentifier(summonerSpell);
            SummonerSpell spell = Orianna.summonerSpellsNamed(summonerSpell).get().get(0);
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle(spell.getName() + ":")
                    .setColor(Main.randomColor())
                    .setDescription(spell.getDescription().replaceAll("<.*?>", ""))
                    .setThumbnail(spell.getImage().getURL());
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        } catch (NullPointerException | IllegalStateException e) {
            invalidCommand(0);
        }
    }

    private static String inputToChampionName(String input) {
        if (!(input.length() >= 2)) {
            return null;
        }
        final List<String> data = Orianna.getChampions()
                .stream()
                .map(com.merakianalytics.orianna.types.core.staticdata.Champion::getName)
                .collect(Collectors.toList());
        return Main.mostSimilar(data, input);
    }

    private static String summonerNameToIdentifier(String input) {
        if (!(input.length() >= 2)) {
            return null;
        }
        final List<String> data = Orianna.getSummonerSpells()
                .stream()
                .map(SummonerSpell::getName)
                .collect(Collectors.toList());
        return Main.mostSimilar(data, input);
    }
}
