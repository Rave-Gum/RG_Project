package rainbot.functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 도박 클래스
 * 도박에 사용하는 돈과 도박기능이 있음
 */
public class Gamble {
    public static final double DEFAULT_MONEY = 100000.0;
    private final double[] bettingScale = {1.2, 1.5, 1.9, 2.4, 3.0, 10.0};
    private final double[] probabilityOfWinning = {0.8, 0.65, 0.5, 0.35, 0.2, 0.05};
    private final Map<Long, Double> moneyManagers;
    private final Random random;

    public Gamble() {
        this.moneyManagers = new HashMap<>();
        random = new Random();
    }

    /**
     * 현재 가진 돈 출력 메소드
     *
     * @param channel 사용자의 텍스트 채널
     * @param user    사용자
     */
    public void printMoney(TextChannel channel, User user) {
        channel.sendMessage(buildMoneyMessage(user)).queue();
    }

    /**
     * 돈 출력 메시지 생성 메소드
     *
     * @param user 사용자
     * @return 돈 출력 메시지
     */
    private MessageEmbed buildMoneyMessage(User user) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription(user.getName() + "님이 가진 돈은 " + getMoney(user) + "원 입니다. :money_mouth:");
        return eb.build();
    }

    /**
     * 돈을 반환하는 메소드
     *
     * @param user 사용자
     * @return 사용자가 가진 돈
     */
    private double getMoney(User user) {
        if (moneyManagers.containsKey(user.getIdLong()))
            return moneyManagers.get(user.getIdLong());
        else
            return 0;
    }


    /**
     * 초기 돈을 설정해주는 메소드
     *
     * @param channel 사용자의 텍스트 채널
     * @param user    사용자
     */
    public void initMoney(TextChannel channel, User user) {
        if (moneyManagers.containsKey(user.getIdLong())) {
            channel.sendMessage(buildInitMoneyMessage(user, true)).queue();
        } else {
            moneyManagers.put(user.getIdLong(), DEFAULT_MONEY);
            channel.sendMessage(buildInitMoneyMessage(user, false)).queue();
        }
    }

    /**
     * 초기 돈 메시지 생성 메소드
     *
     * @param user        사용자
     * @param isInitMoney 초기돈을 설정한 적 있는지 유무
     * @return 초기 돈 메시지
     */
    private MessageEmbed buildInitMoneyMessage(User user, boolean isInitMoney) {
        EmbedBuilder eb = new EmbedBuilder();

        if (isInitMoney) {
            eb.setDescription(user.getName() + "님 이미 돈을 받으신 적이 있네요. :face_with_raised_eyebrow:\n 돈은 한번만 받을 수 있어요.");
        } else {
            eb.setDescription(user.getName() + "님 초기 돈 " + DEFAULT_MONEY + "원을 입금해드렸어요. :moneybag:");
        }

        return eb.build();
    }


    /**
     * 도박 안내문 출력 메소드
     *
     * @param channel 사용자의 텍스트 채널
     */
    public void printGambleGuide(TextChannel channel) {
        channel.sendMessage(buildGambleGuideMessage()).queue();
    }

    /**
     * 도박 안내 메시지 생성 메소드
     *
     * @return 도박 안내 메시지
     */
    private MessageEmbed buildGambleGuideMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        StringBuilder sb = new StringBuilder();

        eb.setTitle(":money_mouth: 도박설명 :money_mouth:");
        for (int i = 0; i < bettingScale.length; i++)
            sb.append(i + 1 + "단계 성공시 배율: " + "x" + bettingScale[i] + " 성공확률: " + (int) (probabilityOfWinning[i] * 100) + "%\n");
        sb.append("\n");
        sb.append("도박방법 - !배팅 [배팅금액] [단계]\n");
        sb.append("ex) !배팅 5000 3\n");
        eb.setDescription(sb.toString());

        return eb.build();
    }


    /**
     * 도박 메소드
     * 단계마다 다른 배율과 확률로 도박함
     *
     * @param channel      사용자의 텍스트 채널
     * @param user         사용자
     * @param bettingMoney 도박에 배팅한 돈
     * @param scale        도박 단계
     */
    public void playGamble(TextChannel channel, User user, double bettingMoney, int scale) {
        if (!isValidBettingMoney(user, bettingMoney)) {
            channel.sendMessage(buildGambleErrorMessage(0));
            return;
        }
        if (scale <= 0 || scale > bettingScale.length) {
            channel.sendMessage(buildGambleErrorMessage(1));
            return;
        }

        int gambleResult = random.nextInt(100) + 1; //1 ~ 100 숫자
        double userMoney = moneyManagers.get(user.getIdLong());
        moneyManagers.put(user.getIdLong(), userMoney - bettingMoney);
        if (gambleResult <= probabilityOfWinning[scale - 1] * 100) {
            double takeMoney = bettingMoney * (bettingScale[scale - 1]);
            userMoney = moneyManagers.get(user.getIdLong());
            moneyManagers.put(user.getIdLong(), userMoney + takeMoney);
            channel.sendMessage(buildGambleResultMessage(takeMoney, true)).queue();
        } else {
            channel.sendMessage(buildGambleResultMessage(0, false)).queue();
        }

    }

    /**
     * 도박 오류 메시지 생성 메소드
     *
     * @param errorType 오류 종류
     * @return 오류 메시지
     */
    private MessageEmbed buildGambleErrorMessage(int errorType) {
        EmbedBuilder eb = new EmbedBuilder();

        switch (errorType) {
            case 0:
                eb.setDescription("가지고 있는 돈보다 배팅금액이 큽니다. :sweat_smile:\n유효한 금액을 입력해주세요.");
                break;
            case 1:
                eb.setDescription("유효하지 않은 배율이에요. :cry:\n배율은 1 ~ 6 사이의 숫자를 입력해주세요.");
                break;
        }

        return eb.build();
    }

    /**
     * 게임결과 메시지 생성 메소드
     *
     * @param takeMoney 도박에서 얻은 돈
     * @param isWin     승리 유무
     * @return 게임결과 메시지
     */
    private MessageEmbed buildGambleResultMessage(double takeMoney, boolean isWin) {
        EmbedBuilder eb = new EmbedBuilder();

        if (isWin) {
            eb.setDescription("도박에 성공했습니다. " + takeMoney + "원을 얻으셨습니다. :tada::tada::tada:");
        } else {
            eb.setDescription("도박에 실패했습니다. 돈을 잃었네요. :sob:");
        }
        return eb.build();
    }

    /**
     * 유효한 배팅을 했는지 확인하는 메소드
     *
     * @param user         사용자
     * @param bettingMoney 배팅한 금액
     * @return 유효 유무
     */
    private boolean isValidBettingMoney(User user, double bettingMoney) {
        double userMoney = moneyManagers.get(user.getIdLong());
        if (userMoney < bettingMoney)
            return false;
        else
            return true;
    }

}
