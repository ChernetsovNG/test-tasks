package ru.nchernetsov.test.sbertech;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RunWith(Parameterized.class)
public class TransliterationsTest {

    private String word;
    private int numberOfTransliterations;
    private Set<String> expectedWords;

    public TransliterationsTest(String word, int numberOfTransliterations, Set<String> expectedWords) {
        this.word = word;
        this.numberOfTransliterations = numberOfTransliterations;
        this.expectedWords = expectedWords;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"BALANC", 16, new HashSet<>(Arrays.asList(
                // всевозможные варианты слова с учетом транслитерации букв B, A, A, C
                "BALANC", "ВALANC", "BАLANC", "BALАNC",
                "BALANС", "ВАLANC", "ВALАNC", "ВALANС",
                "BАLАNC", "BАLANС", "BALАNС", "ВАLАNC",
                "ВALАNС", "BАLАNС", "ВАLANС", "ВАLАNС"

            ))
            },
            {"ОПЛАТА", 16, new HashSet<>(Arrays.asList(
                // всевозможные варианты слова с учетом транслитерации букв О, А, Т, А
                "ОПЛАТА", "OПЛАТА", "ОПЛAТА", "ОПЛАTА",
                "ОПЛАТA", "OПЛAТА", "OПЛАTА", "OПЛАТA",
                "ОПЛATА", "ОПЛAТA", "ОПЛАTA", "OПЛATА",
                "OПЛАTA", "ОПЛATA", "OПЛAТA", "OПЛATA"

            ))
            },
            {"PHON", 8, new HashSet<>(Arrays.asList(
                // всевозможные варианты слова с учетом транслитерации букв P, H, O
                "PHON", "РHON", "PНON", "PHОN",
                "РНON", "РHОN", "PНОN", "РНОN"

            ))
            },
        });
    }

    @Test
    public void testTransliteration01() {
        Set<String> words = new HashSet<>(Transliterations.getWordTransliteration(word));
        Assert.assertEquals(expectedWords.size(), words.size());
        Assert.assertEquals(numberOfTransliterations, words.size());
        Assert.assertTrue(words.containsAll(expectedWords));
    }

}
