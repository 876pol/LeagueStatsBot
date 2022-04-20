package com.pol.leaguestatsbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Command class that every other command extends
 */
public abstract class Command extends Thread {
    public MessageReceivedEvent event;
    public String[] command;

    @Override
    public void run() {
        try {
            event.getChannel().sendTyping().queue();
        } catch (NullPointerException e) {
            e.printStackTrace();
            unexpectedError(e.toString());
        }
        try {
            setCommand();
        } catch (Exception e) {
            e.printStackTrace();
            unexpectedError(e.toString());
        }
    }

    public void unexpectedError(String error) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("An unexpected error has occurred")
                .setDescription(error)
                .setFooter("contact @876pol#1477 for this issue");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public abstract void setCommand() throws Exception;

    public abstract void invalidCommand(int i);
}