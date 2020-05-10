package grammar.token;

import simplenlg.framework.ElementCategory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final Lexicon lexicon;

    public Tokenizer(Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    public List<Token> tokenize(String input) {
        List<Token> res = new ArrayList<>();
        input = input.replaceAll("\\s*'\\s*", "'");
        for (String stringTrimmed : input.trim().split(" +")) {
            String[] split = stringTrimmed.split("(?<![.!;?])(?=[.!;?])|(?<=')"); // Splits before strings of punctuation "??" and after quotes
            for (String currentString : split) {
                Token token;

                if (currentString.matches("[?!]*\\?[?!]*")) {
                    token = new QuestionMarkToken(currentString);
                } else if (currentString.matches("[.!;]+")) {
                    token = new MiscFinalPunctuationToken(currentString);
                } else if (currentString.matches("ne|n'")) {
                    token = new NeToken(currentString);
                } else if (currentString.matches("l'")) {
                    token = new DeterminerToken(currentString);
                } else {
                    List<WordElement> correspondingWords = lexicon.getWordsFromVariant(currentString);

                    if (correspondingWords.isEmpty()) {
                        token = new UndefinedToken(currentString);
                    } else if (correspondingWords.size() == 1) {
                        String baseForm = correspondingWords.get(0).getBaseForm();
                        ElementCategory category = lexicon.getWord(baseForm).realiseSyntax().getCategory();
                        token = computeTokenFromCategory(category.toString(), currentString);
                    } else {
                        token = computeTokenWhenMultiplePossibilities(correspondingWords, currentString);
                    }
                }

                res.add(token);
            }
        }
        return res;
    }

    private Token computeTokenWhenMultiplePossibilities(List<WordElement> correspondingWords, String originalString) {
        Token res;
        ElementCategory firstWordCategory = correspondingWords.get(0).getCategory();
        boolean allSameCategory = true;
        for (WordElement word : correspondingWords) {
            if (!word.getCategory().equals(firstWordCategory)) {
                allSameCategory = false;
                break;
            }
        }
        if (allSameCategory) {
            res = computeTokenFromCategory(firstWordCategory.toString(), originalString);
        } else {
            res = new MiscToken(originalString);
        }

        return res;
    }

    private Token computeTokenFromCategory(String category, String originalString) {
        Token token;
        switch (category) {
            case "NOUN":
                token = new NounToken(originalString);
                break;
            case "VERB":
                token = new VerbToken(originalString);
                break;
            case "ADJECTIVE":
                token = new AdjectiveToken(originalString);
                break;
            case "PRONOUN":
                token = new PronounToken(originalString);
                break;
            case "DETERMINER":
                token = new DeterminerToken(originalString);
                break;
            default:
                token = new UndefinedToken(originalString);
                break;
        }
        return token;
    }

}
