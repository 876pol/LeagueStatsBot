package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.core.staticdata.Champion;
import com.merakianalytics.orianna.types.core.staticdata.Skin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;

public class Skins extends Command {

    public Skins(MessageReceivedEvent event, String[] command) {
        this.event = event;
        this.command = command;
    }

    public void setCommand() {
        HashMap<String, String> availableSkins = inputToSkinName(Main.convertStringArrayToString(command, 2));
        assert availableSkins != null;
        String[] keys = availableSkins.keySet().toArray(new String[0]);
        if (availableSkins.size() == 1) {
            championSkins(keys[0], Orianna.championNamed(availableSkins.get(keys[0])).get().getName());
        } else if (availableSkins.size() > 1 && availableSkins.size() < 20) {
            listSkins(keys);
        } else {
            invalidCommand(0);
        }
    }

    @Override
    public void invalidCommand(int i) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("No Skins were Found")
                .setColor(Main.randomColor())
                .setFooter("use \" + Main.prefix + \" skin *skin name* to shows skin, or return list of skins");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private void championSkins(String skin, String champion) {
        Champion champObj = Orianna.championNamed(champion).get();
        Skin skinObj = champObj.getSkins().find(skin);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(skinObj.getName())
                .setColor(Main.randomColor())
                .setThumbnail(champObj.getImage().getURL())
                .setImage(skinObj.getSplashImageURL());
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private HashMap<String, String> inputToSkinName(String skin) {
        if (skin.length() < 2) {
            return null;
        }
        HashMap<String, String> retVal = new HashMap<>();
        for (Champion champ : Orianna.getChampions()) {
            for (Skin s : champ.getSkins()) {
                String skinName = s.getName().toLowerCase();
                if (skinName.equals(skin)) {
                    retVal.clear();
                    retVal.put(s.getName(), champ.getName());
                    return retVal;
                }
                if (skinName.contains(skin) || skinName.replaceAll("[^A-Za-z0-9]", "")
                        .contains(skin.replaceAll(" ", "")) || Main.isSimilar(skin, skinName) > 0.85) {
                    retVal.put(s.getName(), champ.getName());
                }
            }
        }
        return retVal;
    }

    private void listSkins(String[] skins) {
        StringBuilder descriptionContent = new StringBuilder();
        for (String skinName : skins) {
            descriptionContent.append(skinName).append("\n");
        }
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Skins")
                .setDescription(descriptionContent.toString())
                .setColor(Main.randomColor());
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}