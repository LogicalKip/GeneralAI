package module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikipediaModuleTest {
    private static final String LANGUAGE = "fr";

    private WikipediaModule wikipediaModule;

    @BeforeEach
    private void setup() {
        wikipediaModule = new WikipediaModule();
    }

    @Test
    void getDefinitionTest() throws IOException {
        String chat = "Le Chat domestique (Felis silvestris catus) est la sous-espèce issue de la domestication du Chat sauvage, mammifère carnivore de la famille des Félidés.";
        assertEquals(chat, wikipediaModule.getDefinition("chat", LANGUAGE));
    }

    @Test
    void getFirstWikiParagraphTest() throws IOException {
        String chat = "Le Chat domestique (Felis silvestris catus) est la sous-espèce issue de la domestication du Chat sauvage, mammifère carnivore de la famille des Félidés. Il est l’un des principaux animaux de compagnie et compte aujourd’hui une cinquantaine de races différentes reconnues par les instances de certification. Dans de très nombreux pays, le chat entre dans le cadre de la législation sur les carnivores domestiques à l’instar du chien et du furet. Essentiellement territorial, le chat est un prédateur de petites proies comme les rongeurs ou les oiseaux. Les chats ont diverses vocalisations dont les ronronnements, les miaulements, les feulements ou les grognements, bien qu’ils communiquent principalement par des positions faciales et corporelles et des phéromones. Selon les résultats de travaux menés en 2006 et 2007, le chat domestique est une sous-espèce du chat sauvage (Felis silvestris) issue d’ancêtres appartenant à la sous-espèce du chat sauvage d’Afrique (Felis silvestris lybica). Les premières domestications auraient eu lieu il y a 8 000 à 10 000 ans au Néolithique dans le Croissant fertile, époque correspondant au début de la culture de céréales et à l’engrangement de réserves susceptibles d’être attaquées par des rongeurs, le chat devenant alors pour l’Homme un auxiliaire utile se prêtant à la domestication. Tout d’abord vénéré par les Égyptiens, il fut diabolisé en Europe au Moyen Âge et ne retrouva ses lettres de noblesse qu’au XVIIIe siècle. En Asie, le chat reste synonyme de chance, de richesse ou de longévité. Ce félin a laissé son empreinte dans la culture populaire et artistique, tant au travers d’expressions populaires que de représentations diverses au sein de la littérature, de la peinture ou encore de la musique.";
        assertEquals(chat, wikipediaModule.getFirstWikiParagraph("chat", LANGUAGE));
    }

    @Test
    void languageTest() throws IOException {
        assertTrue(wikipediaModule.getFirstWikiParagraph("cat", "en").matches("The cat \\(Felis catus\\) is a domestic species of small carnivorous mammal. It is the only domesticated species in the family Felidae.*avoids human contact. Domestic cats are valued by humans for.*around .* million cats lived in more than .* million households as of .*."));

        String simpleCat = "Cats, also called domestic cats \\(Felis catus\\), are small, carnivorous \\(meat-eating\\) mammals, of the family Felidae.*indoor pets.*domesticated.*popular.*big cats.*dangerous.";
        assertTrue(wikipediaModule.getFirstWikiParagraph("cat", "simple").matches(simpleCat));
    }

    @Test
    void multipleWordsTest() throws IOException {
        String feral_cat = "A feral cat is an un-owned domestic cat (Felis catus) that lives outdoors and avoids human contact: it does not allow itself to be handled or touched, and usually remains hidden from humans. Feral cats may breed over dozens of generations and become an aggressive apex predator in urban, savannah and bushland environments. Some feral cats may become more comfortable with people who regularly feed them, but even with long-term attempts at socialization, they usually remain aloof and are most active after dusk. Feral cats are devastating to wildlife, and conservation biologists consider them to be one of the worst invasive species on Earth. Attempts to control feral cat populations are widespread but generally of greatest impact within purpose-fenced reserves. Some animal-rights groups advocate trap-neuter-return programs to prevent the cats from continuing to breed, as well as feeding the cats, socializing and adopting out young kittens, and providing healthcare. Others advocate euthanasia. Feral cats may live outdoors in colonies: these are regarded as managed colonies by animal rights advocates when they are provided with regular food and care by humans.";
        assertEquals(feral_cat, wikipediaModule.getFirstWikiParagraph("feral cat", "en"));
    }

    @Test
    void joinMultipleWordsTest() {
        assertEquals("cat", wikipediaModule.joinMultipleWords("cat"));
        assertEquals("feral_cat", wikipediaModule.joinMultipleWords("feral cat"));
        assertEquals("quite_a_few_words", wikipediaModule.joinMultipleWords("quite a few words"));
    }

    @Test
    void getFirstSentenceTest() {
        assertEquals("Le chat est un animal.", wikipediaModule.getFirstSentence("Le chat est un animal. C'est un mammifère."));

        // Only one sentence
        assertEquals("Le chat est un animal.", wikipediaModule.getFirstSentence("Le chat est un animal."));

        // Non final dots : J. R. R. Tolkien
        assertEquals("John Ronald Reuel Tolkien, plus connu sous la forme J. R. R. Tolkien, est un écrivain, poète, philologue, essayiste et professeur d’université britannique, né le 3 janvier 1892 à Bloemfontein et mort le 2 septembre 1973 à Bournemouth.", wikipediaModule.getFirstSentence("John Ronald Reuel Tolkien, plus connu sous la forme J. R. R. Tolkien, est un écrivain, poète, philologue, essayiste et professeur d’université britannique, né le 3 janvier 1892 à Bloemfontein et mort le 2 septembre 1973 à Bournemouth. Il est principalement connu pour ses romans Le Hobbit et Le Seigneur des anneaux. "));

        // Number as last sentence char
        assertEquals("Terminator (The Terminator) est un film de science-fiction américain réalisé par James Cameron, sorti en 1984.", wikipediaModule.getFirstSentence("Terminator (The Terminator) est un film de science-fiction américain réalisé par James Cameron, sorti en 1984. Il est célèbre !"));

    }
}