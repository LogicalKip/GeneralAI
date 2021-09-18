package module;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class WikipediaModule {

    private final WikipediaClient wikipediaClient;

    public WikipediaModule() {
        this.wikipediaClient = new WikipediaClient();
    }


    //FIXME
    // remove [quand ?] [ref souhaitée] etc ex firefox, end of paragraph
    // allow multiple words in the grammar
    /*
    Monte_Carlo_tree_search : que faire ?, special char (À in the first seds, for example, or typical é), disambiguation pages, no such article error, what about e.g/i.e (no space after i) ?, différence entre Monte_Carlo_tree_search et monte_Carlo_tree_search, apostrophe=ok?, no word given or only language etc, avoid double dl, clean code, english/pain and fr/simple ends first sentence with not-letter char therefore undetected, test fr napoleon
    there may be differences in runs with the same parameters
    ampersand not correct (ex : duck dynasty article)
    (listen to pronounciation) : ex : fr Dieu
    latin as a subtitle (ex : fr chat)
     */


    String getFirstSentence(String paragraph) {
        return paragraph.replaceAll("(.*?[a-zà-ÿ\\d][.;](?= [A-ZÀ-Ÿ]|$)).*", "$1");// Ends at the first lower+dot+space+upper and discards the space+upper
    }

    public String getDefinition(String word, String language) throws IOException {
        String paragraph = wikipediaClient.getFirstWikiParagraph(word, language);
        return getFirstSentence(paragraph);
    }
}
