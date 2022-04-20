package com.pol.leaguestatsbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Help extends Command {
    public Help(MessageReceivedEvent _event) {
        event = _event;
    }

    @Override
    public void setCommand() {
        help();
    }

    @Override
    public void invalidCommand(int i) {

    }

    private void help() {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Help")
                .setColor(Main.randomColor())
                .addField(Main.prefix + " champion *champion name*", "Displays info about a champion.", false)
                .addField(Main.prefix + " item *item name*", "Displays info about an item.", false)
                .addField(Main.prefix + " ability *champion name*", "Displays info about a champion's abilities.",
                        false)
                .addField(Main.prefix + " rune *rune name*", "Displays info about a rune.", false)
                .addField(Main.prefix + " skin *skin name*", "Displays a skin's splash art.", false)
                .addField(Main.prefix + " profile *username*", "Displays info about a user.", false)
                .addField(Main.prefix + " history *username*", "Displays a user's match history.", false)
                .addField(Main.prefix + " match (*match id* | null)", "Displays info about a user's past or current " +
                        "match", false)
                .addField(Main.prefix + " ping", "Displays the bot's ping", false)
                .addField(Main.prefix + " help", "Displays this help message.", false);
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}
