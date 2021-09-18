package module;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class WikipediaClient { // TODO test call to wiki with a mock
    public String getFirstWikiParagraph(String word, String language) throws IOException {
        Document doc = Jsoup.connect(getWikipediaUrl(joinMultipleWords(word), language)).get();
        return doc.select("extract").text();
    }

    private String getWikipediaUrl(String wordSearchedFor, String language) {
        return String.format("http://%s.wikipedia.org/w/api.php?action=query&prop=extracts&format=xml&exintro=&explaintext=&titles=%s&redirects=", language, wordSearchedFor);
    }

    private String joinMultipleWords(String words) {
        return String.join("_", words.split(" "));
    }
}