package edu.marmara.readme.engine.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Python github_metin_islemleri.py'nin Java portu. */
public final class GithubMetinIslemleri {

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("https?://github\\.com/([^/]+)/?.*");

    private GithubMetinIslemleri() {
    }

    public static String githubKullaniciAdiGetir(String url) {
        Matcher m = GITHUB_URL_PATTERN.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return "Invalid GitHub URL";
    }

    /** URL'nin SHA-256 hash'inin ilk 39 hex karakteri. */
    public static String hashUrl39(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(hash);
            return hex.substring(0, 39);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
