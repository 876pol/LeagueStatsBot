package com.pol.leaguestatsbot;

import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Region;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main extends ListenerAdapter {
    public static JDA jda;
    public static final String prefix = "ls";

    public static void main(String[] args) throws LoginException {
        Orianna.setRiotAPIKey(Keys.getRiot());
        Orianna.setDefaultRegion(Region.NORTH_AMERICA);
        jda = JDABuilder.createDefault(Keys.getDiscord())
                .addEventListeners(new Main())
                .setStatus(OnlineStatus.ONLINE)
                .build();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        if (event.getAuthor().isBot()) {
            return;
        }
        String[] splitMessage = msg.getContentRaw().toLowerCase().trim().split("\\s+");
        if (splitMessage[0].equals(prefix)) {
            switch (splitMessage[1]) {
                case "champion":
                case "champ":
                    new Champion(event, splitMessage).start();
                    break;
                case "ability":
                case "abilities":
                    new Ability(event, splitMessage).start();
                    break;
                case "skin":
                case "skins":
                    new Skins(event, splitMessage).start();
                    break;
                case "item":
                case "items":
                    new Item(event, splitMessage).start();
                    break;
                case "rune":
                case "runes":
                    new Runes(event, splitMessage).start();
                    break;
                case "profile":
                case "pro":
                    new Profile(event, splitMessage).start();
                    break;
                case "history":
                case "his":
                    new History(event, splitMessage).start();
                    break;
                case "match":
                case "mat":
                    new Match(event, splitMessage).start();
                    break;
                case "help":
                    new Help(event).start();
                    break;
                case "ping":
                    ping(event);
                    break;
            }
        }
        System.gc();
    }

    private static void ping(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        long time = System.currentTimeMillis();
        channel.sendMessage("Ping:")
                .queue(response -> response.editMessageFormat("Ping: %d ms", System.currentTimeMillis() - time).queue());
    }

    public static String convertStringArrayToString(String[] strArr, int startIndex) {
        return Arrays.stream(strArr, startIndex, strArr.length)
                .collect(Collectors.joining(" "));
    }

    public static Color randomColor() {
        Random rand = new Random();
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        return new Color(r, g, b);
    }

    private static Map<Character, Integer> generateCharMap(String str) {
        Map<Character, Integer> map = new HashMap<>();
        Integer currentChar;
        for (char c : str.toCharArray()) {
            currentChar = map.get(c);
            if (currentChar == null) {
                map.put(c, 1);
            } else {
                map.put(c, currentChar + 1);
            }
        }
        return map;
    }

    public static float isSimilar(String str, String compareStr) {
        str = str.toLowerCase().replaceAll("['& ]", "");
        compareStr = compareStr.toLowerCase().replaceAll("['& ]", "");
        boolean less = false;
        if (str.length() < compareStr.length()) {
            compareStr = compareStr.substring(0, str.length());
            less = true;
            if (str.equals(compareStr)) {
                return (float) 0.99;
            }
        }
        Map<Character, Integer> compareStrMap = generateCharMap(compareStr);
        Set<Character> charSet = compareStrMap.keySet();
        int similarChars = 0;
        int totalStrChars = str.length();
        float thisThreshold;
        if (totalStrChars < compareStrMap.size()) {
            totalStrChars = compareStr.length();
        }
        Iterator<Character> it = charSet.iterator();
        char currentChar;
        Integer currentCountStrMap;
        while (it.hasNext()) {
            currentChar = it.next();
            currentCountStrMap = generateCharMap(str).get(currentChar);
            if (currentCountStrMap != null) {
                similarChars += Math.max(currentCountStrMap, compareStrMap.get(currentChar));
            }
        }
        thisThreshold = ((float) similarChars) / ((float) totalStrChars);
        if (less) {
            thisThreshold -= 0.02;
        }
        return thisThreshold;
    }

    public static String mostSimilar(List<String> data, String query) {
        float highest = 0;
        String answer = "";
        for (String datum : data) {
            float similarity = Main.isSimilar(query, datum.toLowerCase());
            if (similarity > highest) {
                highest = similarity;
                answer = datum;
            }
        }
        return highest > 0.85 ? answer : null;
    }

    public static String appendSpaces(int num) {
        return IntStream.range(0, Math.max(0, num + 1)).mapToObj(i -> " ").collect(Collectors.joining());
    }
}
