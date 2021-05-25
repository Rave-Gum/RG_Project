package rainbot.adapters;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import rainbot.functions.GuildMusicManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 음악 이벤트 리스너
 * 명령어를 인식해 음악을 재생
 */
public class MusicEventListener extends ListenerAdapter {
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicEventListener() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);

        if(event.getAuthor().isBot())
            return;

        if ("!play".equalsIgnoreCase(command[0]) && command.length == 2) {
            loadAndPlay(event.getChannel(), command[1]);
        } else if ("!skip".equalsIgnoreCase(command[0])) {
            skipTrack(event.getChannel());
        } else if ("!stop".equalsIgnoreCase(command[0])) {
            stop(event.getChannel());
        }


        super.onGuildMessageReceived(event);
    }

    /**
     * 음악 로드 및 재생 메소드
     *
     * @param channel 사용자의 텍스트 채널
     * @param trackUrl 음악 URL
     */
    private void loadAndPlay(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("재생목록에 추가합니다. - " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("재생목록에 추가합니다. - " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("노래를 찾지 못했습니다. " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("재생할 수 없습니다. - " + exception.getMessage()).queue();
            }
        });
    }

    /**
     * 음악재생목록 반환 메소드
     *
     * @param channel 사용자의 텍스트 채널
     * @return 재생목록
     */
    private List<String> getPlayList(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        //TODO : 구현예정
        return null;
    }

    /**
     * 음악재생 메소드
     *
     * @param guild 사용자의 소속 서버
     * @param musicManager
     * @param track 재생할 음악
     */
    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    /**
     * 음악재생 중지 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    private void stop(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.stopTrack();

        channel.sendMessage("음악재생을 중지합니다.").queue();
    }

    /**
     * 음악재생 일시정지 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    private void pause(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.onPlayerPause(musicManager.player);
        //TODO : onPlayerPause() 구현

        channel.sendMessage("음악재생을 일시정지합니다.").queue();
    }

    /**
     * 일시정지된 음악 재생 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    private void resume(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.onPlayerResume(musicManager.player);
        //TODO : onPlayerResume() 구현

        channel.sendMessage("음악재생을 재개합니다.").queue();
    }

    /**
     * 재생중인 음악 넘기는 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("현재 재생중인 노래를 건너 뜁니다.").queue();
    }

    /**
     * 서버의 첫번째 음성채널에 접속하는 메소드
     *
     * @param audioManager
     */
    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }
}
