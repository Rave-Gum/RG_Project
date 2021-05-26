package rainbot.functions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.fastily.jwiki.core.Wiki;

import java.awt.*;

public class WordSearch {
    private String word;

    public WordSearch(String _word) {
        word = _word.replaceAll(" ", "_");
    }

    public MessageEmbed search() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        Wiki.Builder builder = new Wiki.Builder();
        builder.withDomain("ko.wikipedia.org");
        Wiki wiki = builder.build();
        if (!wiki.exists(word))
            throw new IllegalArgumentException();

        String line = wiki.getTextExtract(word);
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setTitle(word);
        embedBuilder.setDescription(line);
        embedBuilder.setAuthor("Wikipedia");
        return embedBuilder.build();
    }

    public String searchHelper(){
        return "단어를 검색하려면 다음과 같이 입력해주세요.\n" +
                "단어가 나오지 않는다면 페이지가 존재하지 않는 것입니다.\n"+
                "!단어 Discord";
    }
}
