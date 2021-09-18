package module;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WikipediaModuleTest {
    private static final String LANGUAGE = "fr";

    private WikipediaModule wikipediaModule;

    @Mock
    private WikipediaClient wikipediaClient;

    @BeforeEach
    public void setup() throws IOException {
        wikipediaModule = new WikipediaModule(wikipediaClient);
    }

    @Test
    void getDefinitionTest() throws IOException {
        String catWiki = "Le Chat domestique (Felis silvestris catus) est la sous-espèce issue de la domestication du Chat sauvage, mammifère carnivore de la famille des Félidés. " +
                "Il est l’un des principaux animaux de compagnie et compte aujourd’hui une cinquantaine de races différentes reconnues par les instances de certification. " +
                "Dans de très nombreux pays, le chat entre dans le cadre de la législation sur les carnivores domestiques à l’instar du chien et du furet. " +
                "Essentiellement territorial, le chat est un prédateur de petites proies comme les rongeurs ou les oiseaux. " +
                "Les chats ont diverses vocalisations dont les ronronnements, les miaulements, les feulements ou les grognements, " +
                "bien qu’ils communiquent principalement par des positions faciales et corporelles et des phéromones. " +
                "Selon les résultats de travaux menés en 2006 et 2007, le chat domestique est une sous-espèce du chat sauvage (Felis silvestris) " +
                "issue d’ancêtres appartenant à la sous-espèce du chat sauvage d’Afrique (Felis silvestris lybica). " +
                "Les premières domestications auraient eu lieu il y a 8 000 à 10 000 ans au Néolithique dans le Croissant fertile, " +
                "époque correspondant au début de la culture de céréales et à l’engrangement de réserves susceptibles d’être attaquées par des rongeurs, " +
                "le chat devenant alors pour l’Homme un auxiliaire utile se prêtant à la domestication. Tout d’abord vénéré par les Égyptiens, " +
                "il fut diabolisé en Europe au Moyen Âge et ne retrouva ses lettres de noblesse qu’au XVIIIe siècle. En Asie, le chat reste synonyme de chance, de richesse ou de longévité. " +
                "Ce félin a laissé son empreinte dans la culture populaire et artistique, tant au travers d’expressions populaires que de représentations diverses au sein de la littérature, de la peinture ou encore de la musique.";

        when(wikipediaClient.getFirstWikiParagraph(any(), eq("fr"))).thenReturn(catWiki);
        String chat = "Le Chat domestique (Felis silvestris catus) est la sous-espèce issue de la domestication du Chat sauvage, mammifère carnivore de la famille des Félidés.";
        assertEquals(chat, wikipediaModule.getDefinition("chat", LANGUAGE));
    }

    @Test
    void languageTest() throws IOException {
        String englishVersion = "The cat (Felis catus) is a domestic species of small carnivorous mammal. " +
            "It is the only domesticated species in the family Felidae that avoids human contact.";
        when(wikipediaClient.getFirstWikiParagraph(any(), eq("en"))).thenReturn(englishVersion);
        assertEquals(wikipediaModule.getDefinition("cat", "en"),"The cat (Felis catus) is a domestic species of small carnivorous mammal.");
    }

    @Test
    void getFirstSentenceTest() {
        assertEquals("Le chat est un animal.", wikipediaModule.getFirstSentence("Le chat est un animal. C'est un mammifère."));

        // Only one sentence
        assertEquals("Le chat est un animal.", wikipediaModule.getFirstSentence("Le chat est un animal."));

        // Non final dots : J. R. R. Tolkien
        String tolkienWiki = "John Ronald Reuel Tolkien, plus connu sous la forme J. R. R. Tolkien, est un écrivain, poète, philologue, essayiste et professeur d’université britannique, " +
                "né le 3 janvier 1892 à Bloemfontein et mort le 2 septembre 1973 à Bournemouth. " +
                "Il est principalement connu pour ses romans Le Hobbit et Le Seigneur des anneaux. ";
        assertEquals("John Ronald Reuel Tolkien, plus connu sous la forme J. R. R. Tolkien, est un écrivain, poète, philologue, essayiste et professeur d’université britannique, " +
                "né le 3 janvier 1892 à Bloemfontein et mort le 2 septembre 1973 à Bournemouth.", wikipediaModule.getFirstSentence(tolkienWiki));

        // Number as last sentence char
        String terminator = "Terminator (The Terminator) est un film de science-fiction américain réalisé par James Cameron, sorti en 1984. Il est célèbre !";
        assertEquals("Terminator (The Terminator) est un film de science-fiction américain réalisé par James Cameron, sorti en 1984.", wikipediaModule.getFirstSentence(terminator));

    }
}