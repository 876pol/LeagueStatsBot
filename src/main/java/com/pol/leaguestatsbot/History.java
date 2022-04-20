package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.datapipeline.riotapi.exceptions.ForbiddenException;
import com.merakianalytics.orianna.datapipeline.riotapi.exceptions.UnauthorizedException;
import com.merakianalytics.orianna.types.common.Queue;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.match.Match;
import com.merakianalytics.orianna.types.core.match.MatchHistory;
import com.merakianalytics.orianna.types.core.match.Participant;
import com.merakianalytics.orianna.types.core.match.ParticipantStats;
import com.merakianalytics.orianna.types.core.summoner.Summoner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class History extends Command {

    public History(MessageReceivedEvent event, String[] command) {
        this.event = event;
        this.command = command;
    }

    public void setCommand() {
        getMatchHistory();
    }

    @Override
    public void invalidCommand(int i) {
        EmbedBuilder eb = new EmbedBuilder();
        switch (i) {
            case 0:
                eb.setTitle("No Summoner was Found")
                        .setColor(Main.randomColor())
                        .setFooter("use \" + Main.prefix + \" profile *Summoner Name* for profiles");
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                break;
            case 1:
                eb.setTitle("Invalid Riot API Key")
                        .setColor(Main.randomColor())
                        .setFooter("contact @876pol#1477 for this issue");
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                break;
        }
    }

    private void getMatchHistory() {
        String[] editedCommand = command.clone();
        MatchHistory matchHistory = null;
        Summoner summoner = null;
        Queue[] queues = null;
        try {
            switch (command[command.length - 1].toLowerCase()) {
                case "ranked":
                    editedCommand[editedCommand.length - 1] = "";
                    queues = new Queue[]{Queue.RANKED_SOLO, Queue.RANKED_FLEX};
                    break;
                case "normal":
                    editedCommand[editedCommand.length - 1] = "";
                    queues = new Queue[]{Queue.NORMAL, Queue.BLIND_PICK};
                    break;
                case "aram":
                    editedCommand[editedCommand.length - 1] = "";
                    queues = new Queue[]{Queue.ARAM};
                    break;
                default:
                    break;
            }
            summoner = Summoner.named(Main.convertStringArrayToString(editedCommand, 2))
                    .withRegion(Region.NORTH_AMERICA).get();
            matchHistory = queues == null ? MatchHistory.forSummoner(summoner).get() :
                    MatchHistory.forSummoner(summoner).withQueues(queues).get();
        } catch (NullPointerException e) {
            invalidCommand(0);
        } catch (UnauthorizedException | ForbiddenException e) {
            invalidCommand(1);
        }
        assert summoner != null;
        summoner.getName();
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            assert matchHistory != null;
            Match pastMatch = matchHistory.get(i);
            Participant participant = pastMatch.getParticipants().find(summoner);
            ParticipantStats ps = participant.getStats();
            String champion = participant.getChampion().getName();
            String kda = ps.getKills() + "/" + ps.getDeaths() + "/" + ps.getAssists();
            String cs = (ps.getCreepScore() + ps.getNeutralMinionsKilled()) + " CS";
            String mode = pastMatch.getQueue().toString().replaceAll("_", " ");
            String title = pastMatch.isRemake() ? "Remake         " + mode : ps.isWinner() ?
                    "Victory        " + mode : "Defeat         " + mode;
            String role = mode.equals("NORMAL") || mode.equals("BLIND PICK") || mode.equals("RANKED SOLO")
                    || mode.equals("RANKED FLEX") ? getRole(participant).replaceAll("_", " ") : "NONE";
            title += Main.appendSpaces(15 - mode.length()) + role;
            String body = champion + Main.appendSpaces(14 - champion.length()) +
                    kda + Main.appendSpaces(8 - kda.length()) +
                    cs + Main.appendSpaces(6 - cs.length()) +
                    pastMatch.getCreationTime().toString("MM/dd/YY");
            description.append("                          ID:")
                    .append(pastMatch.getId()).append("\n")
                    .append(title).append("\n")
                    .append(body).append("\n---\n");
        }
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(summoner.getName() + "'s Match History")
                .setColor(Main.randomColor())
                .setThumbnail(summoner.getProfileIcon().getImage().getURL())
                .setDescription("```" + description + "```");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private String getRole(Participant participant) {
        String lane = participant.getLane().name();
        String role = participant.getRole().name();
        if (role.equals("DUO_SUPPORT")) {
            return "SUPPORT";
        } else if (role.equals("DUO_CARRY") || role.equals("DUO") && lane.equals("BOTTOM")) {
            return "ADC";
        }
        return lane;
    }
}