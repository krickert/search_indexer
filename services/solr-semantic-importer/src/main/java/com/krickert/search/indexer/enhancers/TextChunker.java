package com.krickert.search.indexer.enhancers;

import java.util.*;
import java.lang.Math;

public class TextChunker implements Chunker {
    
    public static List<String> chunkText(String text, int overlap) {
        return chunkText(text, 300, overlap);
    }
    
    public static List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split(" ");
        List<String> currentChunkWords = new ArrayList<>();
        int currentChunkSize = 0;

        for (String word : words) {
            int wordLengthWithSpace = word.length() + 1;

            if (wordLengthWithSpace > chunkSize) { // for single word case
                if (currentChunkSize > 0) { // if there are words in the chunk already
                    chunks.add(String.join(" ", currentChunkWords));
                    currentChunkWords.clear();
                    currentChunkSize = 0;
                }
                chunks.add(word);
                continue;
            }

            if (currentChunkSize + wordLengthWithSpace <= chunkSize) { // add word to current chunk
                currentChunkWords.add(word);
                currentChunkSize += wordLengthWithSpace;
            } else { // start a new chunk
                chunks.add(String.join(" ", currentChunkWords));
                // transfer the last few words to the new chunk (according to the overlap size)
                currentChunkWords = currentChunkWords.size() > overlap 
                    ? currentChunkWords.subList(currentChunkWords.size() - overlap, currentChunkWords.size())
                    : new ArrayList<String>();
                currentChunkWords.add(word);
                currentChunkSize = wordLengthWithSpace + String.join(" ", currentChunkWords).length() - word.length();
            }
        }

        if (currentChunkSize > 0) {
            chunks.add(String.join(" ", currentChunkWords));
        }

        return chunks;
    }


    public static String mainText =
            "New York, often called New York City[b] or simply NYC, is the most populous city in the United States, located at the southern tip of New York State on one of the world's largest natural harbors. The city comprises five boroughs, each of which is coextensive with a respective county. New York is a global center of finance[11] and commerce, culture and technology,[12] entertainment and media, academics and scientific output,[13] and the arts and fashion, and, as home to the headquarters of the United Nations, is an important center for international diplomacy.[14][15][16][17][18] New York City is the center of the world's principal metropolitan economy.[19]\n" +
                    "\n" +
                    "With an estimated population in 2023 of 8,258,035[5] distributed over 300.46 square miles (778.2 km2),[4] the city is the most densely populated major city in the United States. New York has more than double the population of Los Angeles, the nation's second-most populous city.[20] New York is the geographical and demographic center of both the Northeast megalopolis and the New York metropolitan area, the largest metropolitan area in the U.S. by both population and urban area. With more than 20.1 million people in its metropolitan statistical area[21] and 23.5 million in its combined statistical area as of 2020, New York City is one of the world's most populous megacities.[22] The city and its metropolitan area are the premier gateway for legal immigration to the United States. As many as 800 languages are spoken in New York City,[23] making it the most linguistically diverse city in the world. In 2021, the city was home to nearly 3.1 million residents born outside the U.S.,[20] the largest foreign-born population of any city in the world.[24]\n" +
                    "\n" +
                    "New York City traces its origins to Fort Amsterdam and a trading post founded on the southern tip of Manhattan Island by Dutch colonists in approximately 1624. The settlement was named New Amsterdam (Dutch: Nieuw Amsterdam) in 1626 and was chartered as a city in 1653. The city came under English control in 1664 and was temporarily renamed New York after King Charles II granted the lands to his brother, the Duke of York,[25] before being permanently renamed New York in November 1674. New York City was the capital of the United States from 1785 until 1790.[26] The modern city was formed by the 1898 consolidation of its five boroughs: Manhattan, Brooklyn, Queens, The Bronx, and Staten Island, and has been the largest U.S. city ever since.\n" +
                    "\n" +
                    "Anchored by Wall Street in the Financial District of Lower Manhattan, New York City has been called both the world's premier financial and fintech center[27][28] and the most economically powerful city in the world.[29] As of 2022, the New York metropolitan area is the largest metropolitan economy in the world, with a gross metropolitan product of over US$2.16 trillion.[9] If the New York metropolitan area were its own country, it would have the tenth-largest economy in the world. The city is home to the world's two largest stock exchanges by market capitalization of their listed companies: the New York Stock Exchange and Nasdaq. New York City is an established safe haven for global investors.[30] As of 2023, New York City is the most expensive city in the world for expatriates to live.[31] New York City is home by a significant margin to the highest number of billionaires,[32] individuals of ultra-high net worth (greater than US$30 million),[33] and millionaires of any city in the world.[34]\n" +
                    "\n" +
                    "Etymology\n" +
                    "See also: Nicknames of New York City\n" +
                    "In 1664, New York was named in honor of the Duke of York (later King James II of England).[35] James's elder brother, King Charles II, appointed the Duke as proprietor of the former territory of New Netherland, including the city of New Amsterdam, when the Kingdom of England seized it from Dutch control.[36]\n" +
                    "\n" +
                    "History\n" +
                    "Main articles: History of New York City and Timeline of New York City\n" +
                    "Further information: History of Manhattan, Timeline of Brooklyn, Timeline of Queens, Timeline of the Bronx, and Timeline of Staten Island\n" +
                    "Early history\n" +
                    "Main article: History of New York City (prehistory–1664)\n" +
                    "In the pre-Columbian era, the area of present-day New York City was inhabited by Algonquians, including the Lenape. Their homeland, known as Lenapehoking, included the present-day areas of Staten Island, Manhattan, the Bronx, the western portion of Long Island (including Brooklyn and Queens), and the Lower Hudson Valley.[37]\n" +
                    "\n" +
                    "The first documented visit into New York Harbor by a European was in 1524 by Giovanni da Verrazzano, an explorer from Florence in the service of the French crown.[38] He claimed the area for France and named it Nouvelle Angoulême (New Angoulême).[39] A Spanish expedition, led by the Portuguese captain Estêvão Gomes sailing for Emperor Charles V, arrived in New York Harbor in January 1525 and charted the mouth of the Hudson River, which he named Río de San Antonio ('Saint Anthony's River').[40]\n" +
                    "\n" +
                    "In 1609, the English explorer Henry Hudson rediscovered New York Harbor while searching for the Northwest Passage to the Orient for the Dutch East India Company.[41] He proceeded to sail up what the Dutch called North River (now the Hudson River), named first by Hudson as the Mauritius after Maurice, Prince of Orange.[42]\n" +
                    "\n" +
                    "Hudson claimed the region for the Dutch East India Company. In 1614, the area between Cape Cod and Delaware Bay was claimed by the Netherlands and called Nieuw-Nederland ('New Netherland'). The first non–Native American inhabitant of what became New York City was Juan Rodriguez, a merchant from Santo Domingo who arrived in Manhattan during the winter of 1613–14, trapping for pelts and trading with the local population as a representative of the Dutch colonists.[43][44]";

    @Override
    public List<String> chunk(String text) {
        return chunkText(text,300, 30);
    }
}