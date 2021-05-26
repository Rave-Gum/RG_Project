package rainbot.functions;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import rainbot.models.GuildMusicManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * 음악 클래스
 * 명령어를 인식해 음악을 재생
 */
public class Music {
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public Music() {
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

    /**
     * 음악 로드 및 재생 메소드
     *
     * @param channel  사용자의 텍스트 채널
     * @param trackUrl 음악 URL
     */
    public void loadAndPlay(final TextChannel channel, final String trackUrl, Member member) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("재생목록에 추가합니다. - " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track, member);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("재생목록에 추가합니다. - " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack, member);
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
     * 음악재생목록 출력 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    public void printPlaylist(TextChannel channel) {
        AudioTrack playingTrack = getPlayingTrack(channel);
        BlockingQueue<AudioTrack> playList = getPlayList(channel);

        channel.sendMessage(buildPlayListMessage(playingTrack, playList)).queue();
    }

    /**
     * 음악재생목록 반환 메소드
     *
     * @param channel 사용자의 텍스트 채널
     * @return 재생목록
     */
    private BlockingQueue<AudioTrack> getPlayList(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        return musicManager.scheduler.getQueue();
    }

    /**
     * 재생중인 음악 반환 메소드
     *
     * @param channel 사용자의 텍스트 채널
     * @return 재생중인 음악
     */
    private AudioTrack getPlayingTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        return musicManager.scheduler.getPlayingTrack();
    }

    private MessageEmbed buildPlayListMessage(AudioTrack playingTrack, BlockingQueue<AudioTrack> playList) {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder sb = new StringBuilder();
        eb.setTitle(":musical_note: 플레이리스트 :musical_note:");
        if (playingTrack == null)
            sb.append("\t:arrow_forward: - 현재 재생중인 곡이 없어요:shushing_face:\n\n");
        else
            sb.append("\t:arrow_forward: - " + playingTrack.getInfo().title + "\n\n");
        Iterator<AudioTrack> it = playList.iterator();
        int num = 1;
        sb.append(":hourglass: 대기중인 곡 :hourglass:\n\n");
        if (!it.hasNext())
            sb.append("다음곡이 없어요 :sweat_smile:\n");
        while (it.hasNext()) {
            sb.append((num++) + ".\t " + it.next().getInfo().title + "\n\n");
        }
        eb.setDescription(sb.toString());
        return eb.build();
    }

    /**
     * 음악재생 메소드
     *
     * @param guild        사용자의 소속 서버
     * @param musicManager 음악관리자
     * @param track        재생할 음악
     */
    public void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, Member member) {
//        connectToFirstVoiceChannel(guild.getAudioManager());
        if (!guild.getAudioManager().isConnected())
            guild.getAudioManager().openAudioConnection(member.getVoiceState().getChannel());

        musicManager.scheduler.queue(track);
    }

    /**
     * 음악재생 중지 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    public void stop(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.stopTrack();

        channel.sendMessage("음악재생을 중지합니다.").queue();
    }

    /**
     * 음악재생 일시정지 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    public void pause(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.onPlayerPause(musicManager.player);

        channel.sendMessage("음악재생을 일시정지합니다.").queue();
    }

    /**
     * 일시정지된 음악 재생 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    public void resume(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.onPlayerResume(musicManager.player);

        channel.sendMessage("음악재생을 재개합니다.").queue();
    }

    /**
     * 재생중인 음악 넘기는 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("현재 재생중인 노래를 건너 뜁니다.").queue();
    }

}
