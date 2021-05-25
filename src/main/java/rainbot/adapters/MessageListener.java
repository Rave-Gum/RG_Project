package rainbot.adapters;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import rainbot.functions.Vote;
import rainbot.functions.WordSearch;

import java.util.HashMap;


public class MessageListener extends ListenerAdapter {

    private final String[] voteEmoji = {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣"};
    private HashMap<Long, Vote> votes = new HashMap<>();

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
