package edu.marmara.readme.engine.util;

import java.io.File;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Python folder_cache.py'nin Java portu. Klasör yapısını bir kez tarar, sonra
 * {@link #findBestMatch} ile Ratcliff/Obershelp benzerlik oranına göre fuzzy eşleştirme yapar.
 */
public class FolderCache {

    private final Path basePath;
    private final int maxDepth;
    private final Map<String, Path> cache = new LinkedHashMap<>();
    private final Map<String, List<Path>> cacheByBasename = new LinkedHashMap<>();

    public FolderCache(Path basePath, int maxDepth) {
        this.basePath = basePath;
        this.maxDepth = maxDepth;
        buildCache();
    }

    public FolderCache(Path basePath) {
        this(basePath, 4);
    }

    private void buildCache() {
        if (!Files.exists(basePath)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(basePath, maxDepth + 1, FileVisitOption.FOLLOW_LINKS)) {
            walk.filter(Files::isDirectory)
                    .filter(p -> !p.equals(basePath))
                    .filter(this::isWithinDepthAndVisible)
                    .forEach(this::addToCache);
        } catch (Exception e) {
            // Python tarafı erişim hatalarını sessizce yutmuyor ama os.walk pratikte nadiren
            // hata fırlatır; burada da aynı toleransı sağlamak için cache'i boş bırakıyoruz.
        }
    }

    /** Derinlik kontrolü (max_depth) ve gizli (nokta ile başlayan) klasörlerin elenmesi. */
    private boolean isWithinDepthAndVisible(Path dir) {
        Path relative = basePath.relativize(dir);
        if (relative.getNameCount() > maxDepth) {
            return false;
        }
        for (Path part : relative) {
            if (part.toString().startsWith(".")) {
                return false;
            }
        }
        return true;
    }

    private void addToCache(Path dir) {
        String basenameLower = dir.getFileName().toString().toLowerCase();
        cache.put(basenameLower, dir);
        cacheByBasename.computeIfAbsent(basenameLower, k -> new ArrayList<>()).add(dir);
    }

    private static boolean checkNumericConflict(String searchName, String folderName) {
        boolean has1InSearch = searchName.contains("1");
        boolean has2InSearch = searchName.contains("2");
        boolean has1InFolder = folderName.contains("1");
        boolean has2InFolder = folderName.contains("2");
        return (has1InSearch && has2InFolder) || (has2InSearch && has1InFolder);
    }

    public Optional<Path> findBestMatch(String name) {
        return findBestMatch(name, 88.0);
    }

    public Optional<Path> findBestMatch(String name, double threshold) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        String normalized = name.toLowerCase();

        Path exact = cache.get(normalized);
        if (exact != null) {
            return Optional.of(exact);
        }

        double bestScore = 0.0;
        Path bestPath = null;
        for (Map.Entry<String, List<Path>> entry : cacheByBasename.entrySet()) {
            String cachedName = entry.getKey();
            if (checkNumericConflict(normalized, cachedName)) {
                continue;
            }
            double score = RatcliffObershelp.ratio(normalized, cachedName) * 100;
            if (score > bestScore) {
                bestScore = score;
                bestPath = shallowest(entry.getValue());
            }
        }

        if (bestScore >= threshold) {
            if (bestPath != null && pathSegmentCount(bestPath) < 3 && !bestPath.toString().contains("Projesi")) {
                return Optional.empty();
            }
            return Optional.ofNullable(bestPath);
        }
        return Optional.empty();
    }

    private Path shallowest(List<Path> paths) {
        Path best = paths.get(0);
        int bestCount = pathSegmentCount(best);
        for (Path p : paths) {
            int c = pathSegmentCount(p);
            if (c < bestCount) {
                best = p;
                bestCount = c;
            }
        }
        return best;
    }

    private int pathSegmentCount(Path p) {
        return p.toString().split(java.util.regex.Pattern.quote(File.separator)).length;
    }

    public Map<String, Path> getAllFolders() {
        return new HashMap<>(cache);
    }

    public int folderCount() {
        return cache.size();
    }
}
