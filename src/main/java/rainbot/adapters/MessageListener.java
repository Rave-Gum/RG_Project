package rainbot.adapters;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import rainbot.functions.Gamble;
import rainbot.functions.Music;
import rainbot.functions.Vote;
import rainbot.functions.WordSearch;

import java.util.HashMap;


public class MessageListener extends ListenerAdapter {

    private final String[] voteEmoji = {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};
    private HashMap<Long, Vote> votes = new HashMap<>();
    private Music music = new Music();
    private Gamble gamble = new Gamble();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User user = event.getAuthor();
        TextChannel tc = event.getTextChannel();
        Message msg = event.getMessage();

        //보낸 사람이 봇일 경우 실행하지 않는다.
        if (user.isBot())
            return;

        String line = msg.getContentRaw();
        if (line.charAt(0) != '!')
            return;

        String[] args = line.trim().split(" ");
        String command = args[0].trim();

        if (command.equals("!투표")) {
            String[] split = line.substring(4).split("/");
            Vote newVote = new Vote(voteEmoji, user.getName(), split.length - 1);
            try {
                MessageEmbed messageEmbed = newVote.createVoteEmbed(split);
                Message message = tc.sendMessage(messageEmbed).complete();
                for (int i = 1; i < split.length; i++)
                    message.addReaction(voteEmoji[i - 1]).queue();
                votes.putIfAbsent(message.getIdLong(), newVote);
            } catch (IllegalArgumentException e) {
                tc.sendMessage(newVote.voteHelper()).queue();
            }
        } else if (command.equals("!단어")) {
            WordSearch wordSearch = new WordSearch(line.substring(4));
            try {
                MessageEmbed messageEmbed = wordSearch.search();
                tc.sendMessage(messageEmbed).queue();
            } catch (IllegalArgumentException e) {
                tc.sendMessage(wordSearch.searchHelper()).queue();
            }
        } else if (command.equals("!돈")) {
            gamble.printMoney(tc, user);
        } else if (command.equals("!초기돈")) {
            gamble.initMoney(tc, user);
        } else if (command.equals("!도박")) {
            gamble.printGambleGuide(tc);
        } else if (command.equals("!배팅") && args.length == 3) {

            double bettingMoney = Integer.parseInt(args[1]);
            int scale = Integer.parseInt(args[2]);
            gamble.playGamble(tc, user, bettingMoney, scale);
        }

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        if (command[0].equalsIgnoreCase("!재생") && command.length == 2) {
            music.loadAndPlay(event.getChannel(), command[1], event.getMember());
        } else if (command[0].equalsIgnoreCase("!스킵")) {
            music.skipTrack(event.getChannel());
        } else if (command[0].equalsIgnoreCase("!정지")) {
            music.stop(event.getChannel());
        } else if (command[0].equalsIgnoreCase("!재생목록")) {
            music.printPlaylist(event.getChannel());
        } else if (command[0].equalsIgnoreCase("!일시정지")) {
            music.pause(event.getChannel());
        } else if (command[0].equalsIgnoreCase("!다시재생")) {
            music.resume(event.getChannel());
        }

    }


    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        User user = event.getUser();
        if (user.isBot())
            return;
        TextChannel tc = event.getChannel();
        long messageId = event.getMessageIdLong();
        // votes에 messageId가 존재할 경우
        if (votes.containsKey(messageId)) {
            MessageEmbed editEmbed = votes.get(messageId).editVoteEmbed(event.getReaction(), user);
            tc.editMessageById(messageId, editEmbed).queue();
        }
    }
}
