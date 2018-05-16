package ru.nchernetsov.test.sbertech;

import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Transliterations {
    private static Map<Character, Character> sourceMap = ImmutableMap.<Character, Character>builder()
        .put('A', 'А')
        .put('B', 'В')
        .put('C', 'С')
        .put('E', 'Е')
        .put('K', 'К')
        .put('M', 'М')
        .put('H', 'Н')
        .put('O', 'О')
        .put('T', 'Т')
        .put('P', 'Р')
        .put('X', 'Х')
        .build();

    private static Map<Character, Character> transliterationMap = sourceMap
        .entrySet().stream()
        .flatMap(entry -> Stream.of(entry, new AbstractMap.SimpleEntry<>(entry.getValue(), entry.getKey())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public static void main(String[] args) {
        Collection<String> resultList = getWordTransliteration("BALANC");
        System.out.println(resultList);
    }

    public static Collection<String> getWordTransliteration(String word) {
        List<String> resultList = new ArrayList<>();

        char[] wordChars = word.toCharArray();

        int entranceCount = 0;
        for (char ch : wordChars) {
            if (isCharContainsInMap(ch, transliterationMap)) {
                entranceCount++;
            }
        }

        int transliterationCount = 1 << entranceCount;  // 2 ^ n

        int[] constCharsPositions = new int[wordChars.length - entranceCount];
        int[] varCharsPositions = new int[entranceCount];
        int indexConst = 0;
        int indexVar = 0;
        for (int i = 0; i < wordChars.length; i++) {
            char ch = wordChars[i];
            if (isCharContainsInMap(ch, transliterationMap)) {
                varCharsPositions[indexVar] = i;
                indexVar++;
            } else {
                constCharsPositions[indexConst] = i;
                indexConst++;
            }
        }

        String[] binaryStrings = new String[transliterationCount];
        String toLeadingZeros = "%" + entranceCount + "s";
        for (int i = 0; i < transliterationCount; i++) {
            String binaryString = String.format(toLeadingZeros, Integer.toBinaryString(i)).replace(' ', '0');
            binaryStrings[i] = binaryString;
        }

        for (String binaryString : binaryStrings) {
            char[] resultStringArray = new char[wordChars.length];
            char[] binaryStringChars = binaryString.toCharArray();
            int binaryStringIndex = 0;
            for (char binaryStringChar : binaryStringChars) {
                int varCharsPosition = varCharsPositions[binaryStringIndex];
                switch (binaryStringChar) {
                    case '0':
                        resultStringArray[varCharsPosition] = wordChars[varCharsPosition];
                        break;
                    case '1':
                        resultStringArray[varCharsPosition] = transliterationMap.get(wordChars[varCharsPosition]);
                        break;
                }
                binaryStringIndex++;
            }

            for (int constPos : constCharsPositions) {
                resultStringArray[constPos] = wordChars[constPos];
            }

            String resultString = new String(resultStringArray);
            resultList.add(resultString);
        }

        return resultList;
    }

    private static boolean isCharContainsInMap(char ch, Map<Character, Character> transliterationMap) {
        return transliterationMap.keySet().contains(ch) || transliterationMap.values().contains(ch);
    }

}
