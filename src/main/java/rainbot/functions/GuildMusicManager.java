package rainbot.functions;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 * 음악플레이어와 음악스케줄러를 관리하는 클래스
 */
public class GuildMusicManager {
    public final AudioPlayer player;
    public final TrackScheduler scheduler;

    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    /**
     * 핸들러 객체 반환 메소드
     * @return 핸들러
     */
    public MusicPlayerSendHandler getSendHandler() {
        return new MusicPlayerSendHandler(player);
    }
}
