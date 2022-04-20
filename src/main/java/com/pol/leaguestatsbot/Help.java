package com.pol.leaguestatsbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    }

    private static EmbedBuilder helpEmbed(int i) {
        return null;
    }
}
