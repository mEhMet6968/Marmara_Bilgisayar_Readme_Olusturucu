package edu.marmara.readme.engine.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Python'un {@code difflib.SequenceMatcher(None, a, b).ratio()} fonksiyonunun Java portu
 * (Ratcliff/Obershelp algoritması — Levenshtein DEĞİL).
 *
 * "autojunk" sezgisi difflib'de yalnızca dizi uzunluğu &gt;= 200 olduğunda devreye girer;
 * burada klasör/ders adları her zaman kısa olduğundan bilerek uygulanmamıştır (basitlik için).
 */
public final class RatcliffObershelp {

    private RatcliffObershelp() {
    }

    private record Block(int i, int j, int size) {
    }

    private record Range(int alo, int ahi, int blo, int bhi) {
    }

    public static double ratio(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 1.0;
        }
        int matches = matchingCharCount(a, b);
        return 2.0 * matches / (a.length() + b.length());
    }

    private static int matchingCharCount(String a, String b) {
        Map<Character, List<Integer>> b2j = buildB2J(b);
        List<Block> blocks = getMatchingBlocks(a, b, b2j);
        int total = 0;
        for (Block blk : blocks) {
            total += blk.size();
        }
        return total;
    }

    private static Map<Character, List<Integer>> buildB2J(String b) {
        Map<Character, List<Integer>> b2j = new HashMap<>();
        for (int j = 0; j < b.length(); j++) {
            b2j.computeIfAbsent(b.charAt(j), k -> new ArrayList<>()).add(j);
        }
        return b2j;
    }

    private static Block findLongestMatch(String a, String b, Map<Character, List<Integer>> b2j,
                                            int alo, int ahi, int blo, int bhi) {
        int besti = alo, bestj = blo, bestsize = 0;
        Map<Integer, Integer> j2len = new HashMap<>();
        for (int i = alo; i < ahi; i++) {
            Map<Integer, Integer> newJ2Len = new HashMap<>();
            List<Integer> indices = b2j.get(a.charAt(i));
            if (indices != null) {
                for (int j : indices) {
                    if (j < blo) {
                        continue;
                    }
                    if (j >= bhi) {
                        break;
                    }
                    int k = j2len.getOrDefault(j - 1, 0) + 1;
                    newJ2Len.put(j, k);
                    if (k > bestsize) {
                        besti = i - k + 1;
                        bestj = j - k + 1;
                        bestsize = k;
                    }
                }
            }
            j2len = newJ2Len;
        }
        return new Block(besti, bestj, bestsize);
    }

    private static List<Block> getMatchingBlocks(String a, String b, Map<Character, List<Integer>> b2j) {
        Deque<Range> queue = new ArrayDeque<>();
        queue.push(new Range(0, a.length(), 0, b.length()));
        List<Block> matchingBlocks = new ArrayList<>();
        while (!queue.isEmpty()) {
            Range r = queue.pop();
            Block m = findLongestMatch(a, b, b2j, r.alo(), r.ahi(), r.blo(), r.bhi());
            if (m.size() > 0) {
                matchingBlocks.add(m);
                if (r.alo() < m.i() && r.blo() < m.j()) {
                    queue.push(new Range(r.alo(), m.i(), r.blo(), m.j()));
                }
                if (m.i() + m.size() < r.ahi() && m.j() + m.size() < r.bhi()) {
                    queue.push(new Range(m.i() + m.size(), r.ahi(), m.j() + m.size(), r.bhi()));
                }
            }
        }
        return matchingBlocks;
    }
}
