package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.datapipeline.riotapi.exceptions.ForbiddenException;
import com.merakianalytics.orianna.datapipeline.riotapi.exceptions.UnauthorizedException;
import com.merakianalytics.orianna.types.common.Queue;
import com.merakianalytics.orianna.types.core.championmastery.ChampionMastery;
import com.merakianalytics.orianna.types.core.league.LeagueEntry;
import com.merakianalytics.orianna.types.core.searchable.SearchableList;
import com.merakianalytics.orianna.types.core.summoner.Summoner;
import io.quickchart.QuickChart;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;

public class Profile extends Command {

    public Profile(MessageReceivedEvent _event, String[] _command) {
        event = _event;
        command = _command;
    }

    @Override
    public void setCommand() {
        getProfile();
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

    public void getProfile() {
        Summoner summoner = null;
        try {
            summoner = Summoner.named(Main.convertStringArrayToString(command, 2)).get();
        } catch (UnauthorizedException | ForbiddenException e) {
            invalidCommand(1);
        }
        assert summoner != null;
        if (!summoner.exists()) {
            invalidCommand(0);
            return;
        }
        StringBuilder[] rankedFields = new StringBuilder[3];
        for (int i = 0; i < 2; i++) {
            rankedFields[i] = new StringBuilder();
            final LeagueEntry rankedSolo = i == 0 ? summoner.getLeaguePosition(Queue.RANKED_SOLO) :
                    summoner.getLeaguePosition(Queue.RANKED_FLEX);
            if (rankedSolo == null) {
                rankedFields[i].append("Unranked");
            } else {
                String tier = rankedSolo.getTier().name();
                String division = "";
                if (!(tier.contains("MASTER") || tier.contains("CHALLENGER"))) {
                    division = rankedSolo.getDivision().name() + " ";
                }
                rankedFields[i].append(tier).append(" ").append(division).append(rankedSolo.getLeaguePoints()).append("LP");
                if (rankedSolo.getPromos() != null) {
                    rankedFields[i].append("\nPromos ").append(rankedSolo.getPromos().getProgess());
                }
                int wins = rankedSolo.getWins();
                int losses = rankedSolo.getLosses();
                rankedFields[i].append("\n").append(wins).append("W ").append(losses).append("L (")
                        .append((int) ((float) wins / (float) (wins + losses) * 100)).append("% WR)");
            }
        }
        final SearchableList<ChampionMastery> masteryList = summoner.getChampionMasteries();
        TreeMap<Integer, String> masteries = new TreeMap<>();
        rankedFields[2] = new StringBuilder();
        for (int i = 0; i < Math.min(masteryList.size(), 8); i++) {
            ChampionMastery mastery = masteryList.get(i);
            masteries.put(mastery.getPoints(), mastery.getChampion().getName());
            rankedFields[2].append(mastery.getChampion().getName()).append(" Mastery ").append(mastery.getLevel()).append("\n");
        }
        summoner.getName();
        String[] champMasteries = rankedFields[2].toString().split("\n");
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(summoner.getName() + "'s Profile")
                .setColor(Main.randomColor())
                .setDescription("Level " + summoner.getLevel())
                .setThumbnail(summoner.getProfileIcon().getImage().getURL())
                .addField("Ranked Solo/Duo", rankedFields[0].toString(), true)
                .addField("Ranked Flex", rankedFields[1].toString(), true)
                .addBlankField(true)
                .addField("Champion Masteries", Arrays.toString(
                                Arrays.copyOfRange(champMasteries, 0, champMasteries.length / 2))
                        .replaceAll("[\\[\\]]", "").replaceAll(", ", "\n"), true)
                .addField("", Arrays.toString(
                                Arrays.copyOfRange(champMasteries, champMasteries.length / 2,
                                        champMasteries.length))
                        .replaceAll("[\\[\\]]", "").replaceAll(", ", "\n"), true)
                .setImage(getMasteryImage(masteries));
        event.getChannel().sendMessageEmbeds(eb.build()).queue();

    }

    public String getMasteryImage(TreeMap<Integer, String> masteries) {
        List<Integer> pointList = new ArrayList<>(masteries.keySet());
        List<String> champMasteryList = new ArrayList<>(masteries.values());
        StringBuilder labels = new StringBuilder();
        StringBuilder data = new StringBuilder();
        for (int i = champMasteryList.size() - 1; i >= 0; i--) {
            labels.append(",\"").append(champMasteryList.get(i)).append("\"");
            data.append(",\"").append(pointList.get(i)).append("\"");
        }
        labels.deleteCharAt(0);
        data.deleteCharAt(0);
        QuickChart chart = new QuickChart();
        chart.setWidth(450);
        chart.setHeight(300);
        chart.setConfig("{type:'bar',data:{labels:[" + labels + "],datasets: [{label: '',data: [" + data + "],}]}," +
                "options: {legend: {display: false,},scales: {yAxes: [{ticks: {beginAtZero:true,fontFamily:'Bold'," +
                "fontColor: '#bbbbbb',},},],xAxes: [{ticks: {fontFamily: 'Bold',fontColor: '#bbbbbb',},},],}}}");
        return chart.getUrl();
    }
}