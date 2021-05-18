package rainbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import rainbot.function.MessageListener;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class RainBot {

    private static String token;
    private static JDA jda;

    public static void main(String[] args) {
        init();
        jda.setAutoReconnect(false);
        jda.addEventListener(new MessageListener());
    }

    private static void init() {
        try {
            Scanner inputFile = new Scanner(new File("src/main/resources/token.txt"));
            token = inputFile.nextLine();
        } catch (FileNotFoundException e) {
            System.out.println("file not founded");
            System.exit(0);
        }

        try {
            jda = JDABuilder.createDefault(token).build();
        } catch (LoginException e) {
            System.out.println("Login failed");
            System.exit(0);
        }

    }
}
