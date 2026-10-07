package edu.marmara.readme.engine.util;

import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.Donem;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Python metin_islemleri.py'nin Java portu. */
public final class MetinIslemleri {

    private MetinIslemleri() {
    }

    /** Metni belirtilen maksimum uzunlukta sınırlar ve gerekirse "..." ile kısaltır (sondan kısaltma). */
    public static String kisaltMetin(String metin, int maksUzunluk) {
        if (metin.length() > maksUzunluk) {
            return metin.substring(0, maksUzunluk - 3) + "...";
        }
        return metin;
    }

    public static String kisaltMetin(String metin) {
        return kisaltMetin(metin, 70);
    }

    /** text null ise "" döner; aksi halde ortadan "..." ile elide eder (başı ve sonu korunur). */
    public static String elideText(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        int keepLength = maxLength - 3;
        int prefixLength = keepLength / 2;
        int suffixLength = keepLength - prefixLength;
        return text.substring(0, prefixLength) + "..." + text.substring(text.length() - suffixLength);
    }

    public static String elideText(String text) {
        return elideText(text, 40);
    }

    public static int donemSayisiGetir(String donem) {
        if (Sabitler.GUZ.equals(donem)) {
            return 1;
        } else if (Sabitler.BAHAR.equals(donem)) {
            return 2;
        }
        return 0;
    }

    /** donem null olabilir; 3 kademeli öncelik: yil+donem -> donem_adi -> "Mesleki Seçmeli 1". */
    public static Path donemDosyaYoluGetir(Donem donem, String dokumanlarRepoYolu) {
        if (donem != null && donem.yil != 0 && donem.donem != null && !donem.donem.isEmpty()) {
            return Path.of(dokumanlarRepoYolu, donem.yil + "-" + donemSayisiGetir(donem.donem));
        }
        if (donem != null && donem.donem_adi != null && !donem.donem_adi.isEmpty()) {
            return Path.of(dokumanlarRepoYolu, donem.donem_adi);
        }
        return Path.of(dokumanlarRepoYolu, Sabitler.MESLEKI_SECMELI_1);
    }

    // --- ders_adi_normalize ve yardımcıları -------------------------------------------------

    private static final Set<String> BAGLAC_KUCUK =
            new LinkedHashSet<>(Set.of("ve", "ile", "ya", "veya", "için", "da", "de", "ki"));

    private static String turkceKucult(String kelime) {
        return kelime.replace("İ", "i").replace("I", "ı").toLowerCase();
    }

    private static String turkceBasHarfBuyut(String kelime) {
        if (kelime.isEmpty()) {
            return kelime;
        }
        char ilk = kelime.charAt(0);
        String ilkStr;
        if (ilk == 'i') {
            ilkStr = "İ";
        } else if (ilk == 'ı') {
            ilkStr = "I";
        } else {
            ilkStr = String.valueOf(ilk).toUpperCase();
        }
        return ilkStr + kelime.substring(1);
    }

    /**
     * Ders/hoca/başlık adını normalize eder: bağlaçlar küçük harf, diğer kelimelerin sadece
     * ilk harfi büyütülür (kalan harfler DEĞİŞTİRİLMEZ — Python'daki kelime[1:] davranışı).
     */
    public static String dersAdiNormalize(String ad) {
        if (ad == null || ad.isEmpty()) {
            return ad;
        }
        String[] kelimeler = ad.split(" ");
        StringBuilder sonuc = new StringBuilder();
        for (int i = 0; i < kelimeler.length; i++) {
            if (i > 0) {
                sonuc.append(" ");
            }
            String kelime = kelimeler[i];
            if (i > 0 && BAGLAC_KUCUK.contains(turkceKucult(kelime))) {
                sonuc.append(turkceKucult(kelime));
            } else {
                sonuc.append(turkceBasHarfBuyut(kelime));
            }
        }
        return sonuc.toString();
    }
}
