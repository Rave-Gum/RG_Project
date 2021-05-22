package rainbot.function;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import rainbot.MusicPlayerSendHandler;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User user = event.getAuthor();
        TextChannel tc = event.getTextChannel();
        Message msg = event.getMessage();
        
        if (user.isBot())
            return;
        
        if (msg.getContentRaw().charAt(0) == '!') {
            String[] args = msg.getContentRaw().substring(1).split(" ");
            if (args.length <= 0)
                return;
            if (args[0].equalsIgnoreCase("test"))
                tc.sendMessage("Hello, " + user.getAsMention()).queue();
            else if (args[0].equalsIgnoreCase("hello")) {
                if (args.length < 2)
                    return;
                if (args[1].equalsIgnoreCase("bot"))
                    tc.sendMessage("Hello, Sir!").queue();
                else if (args[1].equalsIgnoreCase("human"))
                    tc.sendMessage("I'm not human").queue();

            }
        }

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        // This makes sure we only execute our code when someone sends a message with "!play"
        if (!event.getMessage().getContentRaw().startsWith("!play")) return;
        // Now we want to exclude messages from bots since we want to avoid command loops in chat!
        // this will include own messages as well for bot accounts
        // if this is not a bot make sure to check if this message is sent by yourself!
        if (event.getAuthor().isBot()) return;
        Guild guild = event.getGuild();
        // This will get the first voice channel with the name "music"
        // matching by voiceChannel.getName().equalsIgnoreCase("music")
        VoiceChannel channel = guild.getVoiceChannelsByName("music", true).get(0);
        AudioManager manager = guild.getAudioManager();
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioPlayer player = playerManager.createPlayer();

        // MySendHandler should be your AudioSendHandler implementation
        manager.setSendingHandler(new MusicPlayerSendHandler(player));
        // Here we finally connect to the target voice channel
        // and it will automatically start pulling the audio from the MySendHandler instance
        manager.openAudioConnection(channel);
    }
}
