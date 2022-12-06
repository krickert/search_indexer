package com.krickert.search.indexer.pipe.nlp;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class SentenceExtractorTest {

    SentenceExtractor unit = new SentenceExtractor();
    String princeText = "The Chronicles of Narnia: Prince Caspian is a 2008 high fantasy film co-written, produced and directed by Andrew Adamson, based on Prince Caspian (1951), the second published, fourth chronological novel in C. S. Lewis's epic fantasy series, The Chronicles of Narnia. It is the second in The Chronicles of Narnia film series from Walden Media, following The Chronicles of Narnia: The Lion, the Witch and the Wardrobe (2005).\n" +
            "\n" +
            "William Moseley, Anna Popplewell, Skandar Keynes, Georgie Henley, Liam Neeson, and Tilda Swinton reprise their roles from the first film, while new cast includes Ben Barnes, Sergio Castellitto, Peter Dinklage, Eddie Izzard, Warwick Davis, Ken Stott, and Vincent Grass. In the film, the four Pevensie children return to Narnia to aid Prince Caspian in his struggle with the \"secret\" help of Aslan for the throne against his corrupt uncle, King Miraz.\n" +
            "\n" +
            "Prince Caspian, a British-American production, is the last Narnia film to be co-produced by Walt Disney Pictures, as 20th Century Fox became the distributor for the next film The Chronicles of Narnia: The Voyage of the Dawn Treader due to budgetary disputes between Disney and Walden Media, but as a result of Disney eventually purchasing Fox on March 20, 2019, Disney now owns the rights to all three Narnia movies.[3] Work on the script began before The Lion, the Witch and the Wardrobe was released, so filming could begin before the actors grew too old for their parts.";
    @Test
    void testSentenceExtraction() {
        Collection<String> sentences = unit.extractSentences(princeText);
        assertThat(sentences).hasSize(7);
    }

}