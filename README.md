# LeagueStatsBot

A Discord bot that provides in-depth information about League of Legends' champions, items, runes, and players. Written
with Java 8, deployed with Heroku.

To add this bot to your
server: [link](https://discord.com/api/oauth2/authorize?client_id=966457755642757120&permissions=3072&scope=bot).

To clone this repository:

```bash
git clone https://github.com/876pol/LeagueStatsBot.git
cd LeagueStatsBot
```

The Riot API Key and Discord Bot token should be updated
in [Keys.java](https://github.com/876pol/LeagueStatsBot/blob/main/src/main/java/com/pol/leaguestatsbot/Keys.java).

The bot can then be run as follows:

```bash
./gradlew run
```