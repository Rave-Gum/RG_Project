package rainbot.functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

public class Vote {
    private String[] voteEmoji;
    private String author;
    private MessageEmbed voteEmbed;
    private HashMap<Integer, HashSet<Long>> totalVote = new HashMap<>();
    private HashMap<Integer, MessageReaction> reactions = new HashMap<>();

    public Vote(String[] _voteEmoji, String _author, int candidateSize) {
        this.voteEmoji = _voteEmoji;
        this.author = _author;
        for (int i = 1; i <= candidateSize; i++)
            totalVote.put(i, new HashSet<>());
    }

    /**
     * 투표 Embed를 생성하는 메소드
     *
     * @param args 투표 제목과 후보지들이 담긴 배열
     * @return 투표 Embed
     */
    public MessageEmbed createVoteEmbed(String[] args) {
        // args의 길이가 4미만(명령어, 제목, 후보1)으로 투표를 만들 수 없거나
        // 11 이상으로 후보지가 너무 많은 경우 에러 발생
        if (args.length < 4 || args.length > 11)
            throw new IllegalArgumentException();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.BLUE);
        embedBuilder.setTitle(args[0].trim());
        for (int i = 1; i < args.length; i++)
            embedBuilder.addField(i + ". " + args[i].trim() + ":", "0표", false);
        embedBuilder.setAuthor(author);
        voteEmbed = embedBuilder.build();
        return voteEmbed;
    }

    /**
     * 올바르지 못한 투표 명령어가 들어올 경우 올바른 사용법을 안내해준다.
     *
     * @return 투표의 올바른 사용법
     */
    public String voteHelper() {
        return "```" +
                "투표 사용법\n" +
                "!투표 제목/후보1/후보2/후보3 ...\n" +
                "후보지는 최대 9개까지 가능합니다." +
                "```";
    }

    /**
     * 후보지를 선택한 경우 실행되는 메소드
     * Reaction이 있는 Message의 ID값 및 선택한 MessageReaciton과 유저의 정보를 인자로 받아
     * 선택한 후보지를 투표에 적용시키고 Message의 Embed를 수정한다.
     *
     * @param messageReaction 선택한 후보지의 Reaction
     * @param user            선택한 User
     * @return 수정된 Embed
     */
    public MessageEmbed editVoteEmbed(MessageReaction messageReaction, User user) {
        int selectIdx = getSelectCandidateIdx(messageReaction.getReactionEmote().getEmoji());
        addVote(selectIdx, user.getIdLong());
        otherReactionRemove(selectIdx, messageReaction, user);
        int[] curVote = getTotalVote();
        int index = 0;
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(voteEmbed.getTitle());
        for (MessageEmbed.Field field : voteEmbed.getFields())
            embedBuilder.addField(field.getName(), curVote[index++] + "표", false);
        embedBuilder.setAuthor(author);
        voteEmbed = embedBuilder.build();
        return voteEmbed;
    }

    /**
     * 선택한 Emoji를 입력으로 받아 몇 번재 인덱스인지 반환하는 메소드
     *
     * @param select 선택한 Emoji
     * @return Emoji의 Index
     */
    private int getSelectCandidateIdx(String select) {
        for (int index = 0; index < voteEmoji.length; index++)
            if (select.equals(voteEmoji[index]))
                return index + 1;
        return -1;
    }

    /**
     * 유저가 다른 후보지에 투표한 적이 있다면 삭제한다.
     * MessageReaction은 HashMap을 사용하여 저장을 한다.
     *
     * @param selectIdx       유저가 선택한 후보지
     * @param messageReaction 후보지에 대한 리액션
     * @param user            유저
     */
    private void otherReactionRemove(int selectIdx, MessageReaction messageReaction, User user) {
        for (int key : reactions.keySet())
            if (key != selectIdx)
                reactions.get(key).removeReaction(user).queue();
        reactions.putIfAbsent(selectIdx, messageReaction);
    }

    /**
     * 전체 후보지에 대한 현재까지의 투표 결과를 반환하는 메소드
     *
     * @return 현재까지의 투표 결과에 대한 int 배열
     */
    private int[] getTotalVote() {
        return totalVote.keySet().stream().mapToInt(v -> totalVote.get(v.intValue()).size()).toArray();
    }

    /**
     * 유저의 ID와 유저가 선택한 후보를 입력으로 받아 투표하는 메소드
     * 이전에 투표한 후보가 있다면 삭제 후 투표한다.
     *
     * @param candidate 유저가 선택한 후보
     * @param userId    유저의 ID
     */
    private void addVote(int candidate, long userId) {
        for (int i = 1; i <= totalVote.size(); i++) {
            HashSet<Long> voters = totalVote.get(i);
            if (voters.contains(userId))
                voters.remove(userId);
        }
        totalVote.get(candidate).add(userId);
    }
}