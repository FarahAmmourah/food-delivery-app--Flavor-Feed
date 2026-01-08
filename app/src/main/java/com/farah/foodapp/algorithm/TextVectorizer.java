package com.farah.foodapp.algorithm;

import java.util.HashMap;
import java.util.Map;

public class TextVectorizer {

    private static final int VOCAB_SIZE = 300;
    private static final Map<String, Integer> vocab = new HashMap<>();

    static {
        int idx = 0;

        // ================= POSITIVE (100) =================
        String[] positiveWords = {
                "tasty","delicious","yummy","flavorful","mouthwatering","savory","juicy",
                "crispy","fresh","soft","perfect","excellent","amazing","awesome",
                "fantastic","wonderful","great","nice","pleasant","love","loved",
                "enjoy","enjoyed","happy","satisfied","impressed","favorite","best",
                "good","positive","pleased","excited","fast","quick","friendly","kind",
                "polite","helpful","clean","hygienic","professional","organized",
                "smooth","easy","comfortable","worth","reasonable","affordable",
                "cheap","recommended","recommend","delightful","hot","warm","tender",
                "rich","creamy","balanced","again","always","definitely","surely",
                "absolutely","totally","highquality","top","five","stars","freshfood",
                "goodfood","greatfood","nicemeal","perfectmeal","excellentservice",
                "greatservice","bestfood","bestservice","wow","amazingfood"
        };

        // ================= NEGATIVE (100) =================
        String[] negativeWords = {
                "bad","terrible","awful","horrible","worst","tasteless","bland","salty",
                "sweet","burnt","overcooked","undercooked","raw","dry","hard","stale",
                "soggy","cold","greasy","oily","hate","hated","angry","annoyed","upset",
                "unhappy","disappointed","frustrated","regret","sad","mad","furious",
                "slow","late","delay","delayed","rude","impolite","unfriendly",
                "careless","unprofessional","dirty","messy","confusing","wrong",
                "missing","incomplete","problem","issue","mistake","error","refund",
                "complaint","complain","returned","cancelled","overpriced","expensive",
                "waste","wasted","poor","lowquality","notworth","badservice","badfood",
                "never","avoid","worstever","terribleexperience","disaster","fail",
                "failed","failure","coldfood","badexperience","angryservice"
        };

        // ================= NEUTRAL (100) =================
        String[] neutralWords = {
                "okay","normal","average","fine","acceptable","regular","standard",
                "food","meal","dish","menu","restaurant","service","delivery","order",
                "taste","flavor","portion","quantity","price","value","staff","waiter",
                "waitress","table","place","location","branch","kitchen","chef",
                "ingredients","packaging","hygiene","cleanliness","quality",
                "experience","drink","dessert","snack","breakfast","lunch","dinner",
                "burger","pizza","sandwich","chicken","meat","rice","pasta","salad",
                "sauce","cheese","the","is","it","this","that","very","so","not",
                "was","were","my","we","they","you","i","and","or","but","with","for",
                "to","of","in","on","at","from","time","day","today","yesterday",
                "now","before","after","again","sometimes","usually","often"
        };

        idx = addWords(positiveWords, idx);
        idx = addWords(negativeWords, idx);
        idx = addWords(neutralWords, idx);
    }


    // call by static to give each word in vocab an index
    private static int addWords(String[] words, int startIdx) {
        for (String w : words) {
            if (!vocab.containsKey(w) && startIdx < VOCAB_SIZE) {/* check that
     the word is not taken already no redandant and do no exceed limit 300*/
                vocab.put(w, startIdx++);// put the word in vocab of given indexand then raise counter by one
            }
        }
        return startIdx;
    }

    /**
     * Frequency-based Bag of Words vector (300)
     */
    public static float[] vectorize(String text) {

        float[] vector = new float[VOCAB_SIZE];

        if (text == null || text.isEmpty()) {
            return vector;
        }

        text = text.toLowerCase();
        String[] words = text.split("\\s+");// turn the sentence into array of words

        for (String word : words) {
            Integer index = vocab.get(word);
            if (index != null) {
                vector[index] += 1f;// add one to the vector
            }
        }

        return vector;
    }
}
