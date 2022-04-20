package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.datapipeline.riotapi.exceptions.ForbiddenException;
import com.merakianalytics.orianna.datapipeline.riotapi.exceptions.UnauthorizedException;
import com.merakianalytics.orianna.types.common.Queue;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.league.LeagueEntry;
import com.merakianalytics.orianna.types.core.match.Participant;
import com.merakianalytics.orianna.types.core.match.ParticipantStats;
import com.merakianalytics.orianna.types.core.match.Team;
import com.merakianalytics.orianna.types.core.spectator.CurrentMatch;
import com.merakianalytics.orianna.types.core.spectator.CurrentMatchTeam;
import com.merakianalytics.orianna.types.core.spectator.Player;
import com.merakianalytics.orianna.types.core.summoner.Summoner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Match extends Command {

    public Match(MessageReceivedEvent _event, String[] _command) {
        event = _event;
        command = _command;
    }

    public void setCommand() {
        try {
            match();
        } catch (NumberFormatException e) {
            getCurrentMatch();
        }
    }

    @Override
    public void invalidCommand(int i) {
        EmbedBuilder eb = new EmbedBuilder();
        switch (i) {
            case 0:
                eb.setTitle("Invalid Riot API Key")
                        .setColor(Main.randomColor())
                        .setFooter("contact @876pol#1477 for this issue");
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                break;
            case 1:
                eb.setTitle("No Summoner was Found")
                        .setColor(Main.randomColor())
                        .setFooter("use \" + Main.prefix + \" profile *Summoner Name* for profiles");
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                break;
        }
    }

    private void match() throws NullPointerException {
        final com.merakianalytics.orianna.types.core.match.Match match =
                Orianna.matchWithId(Long.parseLong(command[2])).get();
        long millis = match.getDuration().getMillis();
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        String description = match.getQueue().name().replaceAll("_", " ") + " - "
                + match.getMap().name().replaceAll("_", " ") + " - " + minutes + ":" + String.format("%02d", seconds);
        if (!match.exists()) {
            throw new NullPointerException();
        } else {
            Team blue = match.getBlueTeam();
            Team red = match.getRedTeam();
            getParticipantPast gpBlue = new getParticipantPast(blue);
            getParticipantPast gpRed = new getParticipantPast(red);
            Thread tBlue = new Thread(gpBlue);
            Thread tRed = new Thread(gpRed);
            tBlue.start();
            tRed.start();
            //noinspection StatementWithEmptyBody,LoopConditionNotUpdatedInsideLoop
            while (tBlue.isAlive() || tRed.isAlive()) {
            }
            String blueState;
            String redState;
            if (match.isRemake()) {
                blueState = "Remake";
                redState = "Remake";
            } else if (blue.isWinner()) {
                blueState = "Victory";
                redState = "Defeat";
            } else {
                blueState = "Defeat";
                redState = "Victory";
            }
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("Match " + match.getId())
                    .setDescription(description)
                    .addField("Blue Team - " + blueState, gpBlue.toString().trim(), false)
                    .addField("Red Team - " + redState, gpRed.toString().trim(), false)
                    .setColor(Main.randomColor())
                    .addField("More info:", "https://www.leagueofgraphs.com/match/na/" + command[2], false);
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }

    public void getCurrentMatch() {
        final Summoner summoner = Summoner.named(Main.convertStringArrayToString(command, 2)).get();
        try {
            final CurrentMatch currentGame = summoner.getCurrentMatch();
            if (currentGame.exists()) {
                summoner.getName();
                long millis = DateTime.now().getMillis() - currentGame.getCreationTime().getMillis();
                long minutes = (millis / 1000) / 60;
                long seconds = (millis / 1000) % 60;
                String description = currentGame.getQueue().name().replaceAll("_", " ") + " - "
                        + currentGame.getMap().name().replaceAll("_", " ") + " - " + minutes + ":" + String.format(
                        "%02d", seconds);
                String mode = currentGame.getQueue().toString();
                boolean sort =
                        mode.equals("NORMAL") || mode.equals("BLIND_PICK") || mode.equals("RANKED_SOLO") || mode.equals(
                                "RANKED_FLEX");
                getParticipant gpBlue = new getParticipant(currentGame.getBlueTeam(), sort);
                getParticipant gpRed = new getParticipant(currentGame.getRedTeam(), sort);
                Thread tBlue = new Thread(gpBlue);
                Thread tRed = new Thread(gpRed);
                tBlue.start();
                tRed.start();
                //noinspection StatementWithEmptyBody,LoopConditionNotUpdatedInsideLoop
                while (tBlue.isAlive() || tRed.isAlive()) {
                }
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle(summoner.getName() + "'s Current Match")
                        .setColor(Main.randomColor())
                        .setDescription(description)
                        .setThumbnail(summoner.getProfileIcon().getImage().getURL())
                        .addField("Blue Team", gpBlue.toString().trim(), false)
                        .addField("Red Team", gpRed.toString().trim(), false)
                        .setColor(Main.randomColor());
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
            } else {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("This Summoner is not in a Game");
                eb.setColor(Main.randomColor());
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
            }
        } catch (UnauthorizedException | ForbiddenException e) {
            invalidCommand(0);
        } catch (NullPointerException e) {
            invalidCommand(1);
        }
    }

    private static class getParticipant implements Runnable {
        private final StringBuilder sb = new StringBuilder();
        private final CurrentMatchTeam currentGame;
        private final boolean sort;

        public getParticipant(CurrentMatchTeam cg, boolean b) {
            currentGame = cg;
            sort = b;
        }

        public void run() {
            Player[] players;
            List<Player> l = currentGame.getParticipants();
            if (sort && l.size() == 5) {
                players = sortPlayers();
            } else {
                players = new Player[l.size()];
                for (int i = 0; i < l.size(); i++) {
                    players[i] = l.get(i);
                }
            }
            for (final Player player : players) {
                String s = player.getSummoner().getName();
                Summoner sm = Summoner.named(s).get();
                LeagueEntry le = sm.getLeaguePosition(Queue.RANKED_SOLO);
                String username = player.getChampion().getName();
                sb.append(username).append(Main.appendSpaces(14 - username.length())).append(s)
                        .append(Main.appendSpaces(16 - s.length()));
                if (le != null) {
                    String tier = le.getTier().name();
                    String division = "";
                    if (!(tier.contains("MASTER") || tier.contains("CHALLENGER"))) {
                        division = le.getDivision().name();
                    }
                    String rank = tier + " " + division + "\n";
                    sb.append(rank);
                } else {
                    String level = String.valueOf(sm.getLevel());
                    sb.append("Level ").append(level).append("\n");
                }
            }
        }

        private Player[] sortPlayers() {
            Player[] p = new Player[5];
            List<Player> remaining = new ArrayList<>();
            List<Player> players = currentGame.getParticipants();
            for (final Player player : players) {
                int summonerSpellD = player.getSummonerSpellD().getId();
                int summonerSpellF = player.getSummonerSpellF().getId();
                List<String> tags = player.getChampion().getTags();
                if (summonerSpellD == 11 || summonerSpellF == 11) {
                    if (p[1] == null) {
                        p[1] = player;
                    }
                } else if (summonerSpellD == 7 || summonerSpellF == 7
                        || tags.contains("Marksman")) {
                    if (p[3] == null) {
                        p[3] = player;
                    } else if (tags.contains("Marksman")) {
                        if (!p[3].getChampion().getTags().contains("Marksman") || ((summonerSpellD == 7
                                || summonerSpellF == 7) && !(p[3].getSummonerSpellD().getId() == 7 || p[3].getSummonerSpellF().getId() == 7))) {
                            p[3] = player;
                        }
                    }
                } else if (summonerSpellD == 12 || summonerSpellF == 12) {
                    if (p[0] == null) {
                        p[0] = player;
                    } else if (p[2] == null) {
                        if ((tags.contains("Tank") || tags.contains("Fighter"))
                                && !(p[0].getChampion().getTags().contains("Tank") || p[0].getChampion().getTags().contains("Fighter"))) {
                            p[2] = p[0];
                            p[0] = player;
                        } else {
                            p[2] = player;
                        }
                    }
                } else if (tags.contains("Assassin")) {
                    if (p[2] == null) {
                        p[2] = player;
                    } else if (p[4] == null) {
                        if (!(p[2].getChampion().getTags().contains("Assassin")) || p[2].getChampion().getTags().contains("Support")
                                || p[2].getChampion().getTags().contains("Tank")) {
                            p[4] = p[2];
                            p[2] = player;
                        } else {
                            p[4] = player;
                        }
                    }
                }
            }
            for (final Player player : players) {
                if (Arrays.asList(p).contains(player)) continue;
                remaining.add(player);
            }
            for (int i = 0; i < 5; i++) {
                if (p[i] == null) {
                    p[i] = remaining.get(0);
                    remaining.remove(0);
                }
            }
            return p;
        }

        @Override
        public String toString() {
            return "```" + (sb.toString().equals("") ? "No Information" : sb) + "```";
        }
    }

    private static class getParticipantPast implements Runnable {
        private final StringBuilder sb = new StringBuilder();
        private final Team currentGame;

        public getParticipantPast(Team cg) {
            currentGame = cg;
        }

        public void run() {
            Participant[] players = sortPlayers();
            for (final Participant player : players) {
                String s = player.getSummoner().getName();
                Summoner sm = Summoner.named(s).withRegion(Region.NORTH_AMERICA).get();
                LeagueEntry le = sm.getLeaguePosition(Queue.RANKED_SOLO);
                String username = player.getChampion().getName();
                sb.append(username).append(Main.appendSpaces(14 - username.length())).append(s)
                        .append(Main.appendSpaces(16 - s.length()));
                if (le != null) {
                    String tier = le.getTier().name();
                    String division = "";
                    if (!(tier.contains("MASTER") || tier.contains("CHALLENGER"))) {
                        division = le.getDivision().name();
                    }
                    String rank = tier + " " + division + "\n";
                    sb.append(rank);
                } else {
                    String level = String.valueOf(sm.getLevel());
                    sb.append("Level ").append(level).append("\n");
                }
                ParticipantStats ps = player.getStats();
                String level = "Level " + ps.getChampionLevel();
                String kda = ps.getKills() + "/" + ps.getDeaths() + "/" + ps.getAssists();
                String cs = (ps.getCreepScore() + ps.getNeutralMinionsKilled()) + " CS";
                String gold = ((float) Math.round(ps.getGoldEarned() / 100.0)) / 10 + "K Gold";
                sb.append(level).append(Main.appendSpaces(14 - level.length()));
                sb.append(kda).append(Main.appendSpaces(8 - kda.length()));
                sb.append(cs).append(Main.appendSpaces(7 - cs.length()));
                sb.append(gold).append("\n---\n");
            }
        }

        private Participant[] sortPlayers() {
            Participant[] p = new Participant[5];
            List<Participant> players = currentGame.getParticipants();
            for (int i = 0; i < 5; i++) {
                Participant par = players.get(i);
                if (par.getRole().name().equals("DUO_SUPPORT")) {
                    p[4] = par;
                } else {
                    switch (par.getLane()) {
                        case TOP:
                            p[0] = par;
                            break;
                        case JUNGLE:
                            p[1] = par;
                            break;
                        case MIDDLE:
                        case MID:
                            p[2] = par;
                            break;
                        case BOT:
                        case BOTTOM:
                            p[3] = par;
                            break;
                        default:
                            break;
                    }
                }
            }
            List<Participant> remaining = new ArrayList<>();
            for (final Participant player : players) {
                loop:
                {
                    for (int i = 0; i < 5; i++) {
                        if (p[i] == player) {
                            break loop;
                        }
                    }
                    remaining.add(player);
                }
            }
            for (int i = 0; i < 5; i++) {
                if (p[i] == null) {
                    p[i] = remaining.get(0);
                    remaining.remove(0);
                }
            }
            return p;
        }

        @Override
        public String toString() {
            return sb.toString().equals("") ? "No Information" : "```" + sb + "```";
        }
    }
}
