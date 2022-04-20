package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.Orianna;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.stream.Collectors;

public class Item extends Command {

    public Item(MessageReceivedEvent _event, String[] _command) {
        event = _event;
        command = _command;
    }

    public void setCommand() {
        try {
            itemStat(event, Main.convertStringArrayToString(command, 2));
        } catch (NullPointerException | IllegalStateException e) {
            invalidCommand(0);
        }
    }

    @Override
    public void invalidCommand(int i) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("No Item was Found")
                .setColor(Main.randomColor())
                .setFooter("use \" + Main.prefix + \" item *item* for item descriptions");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private static void itemStat(MessageReceivedEvent event, String input) {
        MessageChannel channel = event.getChannel();
        com.merakianalytics.orianna.types.core.staticdata.Item itemObj = Orianna.itemNamed(inputToItemName(input)).get();
        String cost = "**Buy:** " + itemObj.getTotalPrice() + "\n" + "**Sell:** "
                + itemObj.getSellPrice();
        String description = "\n\n" + itemObj.getDescription()
                .replaceAll("<li>|<br>", "\n")
                .replaceAll("<passive>|</passive>|<active>|</active>|<rarityMythic>|</rarityMythic>", "**")
                .replaceAll("<.*?>", "");
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(itemObj.getName() + ":")
                .setColor(Main.randomColor())
                .setDescription(cost)
                .addField("Description:", description, false)
                .setThumbnail(itemObj.getImage().getURL());
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private static String inputToItemName(String input) {
        if (!(input.length() >= 3)) {
            return null;
        }
        List<String> data = Orianna.getItems().stream()
                .map(com.merakianalytics.orianna.types.core.staticdata.Item::getName)
                .collect(Collectors.toList());
        return Main.mostSimilar(data, input);
    }
}
