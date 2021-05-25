package rainbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import rainbot.adapters.MessageListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import rainbot.adapters.MusicEventListener;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.Scanner;

public class RainBot {

    private static String token;
    private static JDA jda;

    public static void main(String[] args) {
        init();
        jda.setAutoReconnect(false);
        jda.addEventListener(new MessageListener());
        jda.addEventListener(new MusicEventListener());
    }

    private static void init() {
        try {
            Scanner inputFile = new Scanner(new File("src/main/resources/token.txt"));
            token = inputFile.nextLine();
        } catch (FileNotFoundException e) {
            System.out.println("file not founded");
            System.exit(0);
        }
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES
        );
        try {
            jda = JDABuilder.createDefault(token, intents)
                    .enableCache(CacheFlag.VOICE_STATE)
                    .build();
        } catch (LoginException e) {
            System.out.println("Login failed");
            System.exit(0);
        }
    }
}
