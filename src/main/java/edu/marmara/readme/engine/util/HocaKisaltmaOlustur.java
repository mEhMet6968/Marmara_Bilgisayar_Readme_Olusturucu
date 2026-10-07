package edu.marmara.readme.engine.util;

import edu.marmara.readme.engine.Sabitler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Python hoca_kisaltma_olustur.py'nin Java portu — sabit çakışma override'ları dahil,
 * ayrıca Marmara'nın gerçek kadrosunda ortaya çıkan genel çakışmaları ("Ali Buldu",
 * "Anıl Baş", "Abdullah Bal" hepsi "AB") otomatik çözen {@link #benzersizKisaltmalarUret}.
 */
public final class HocaKisaltmaOlustur {

    private HocaKisaltmaOlustur() {
    }

    /** Unvanları (ve "Öğretim Üyesi" gibi ek unvan parçalarını) temizler. */
    private static String temizle(String isimGirdi) {
        String isim = isimGirdi;
        for (String unvan : Sabitler.UNVANLAR) {
            isim = isim.replace(unvan, "");
        }
        // "Dr. Öğretim Üyesi Ad Soyad" gibi unvanlarda "Öğretim Üyesi" kısaltmaya karışmasın
        // (örn. "ÖÜAA" yerine anlaşılır olması için sadece ad soyaddan "AA" üretilsin).
        for (String ekUnvan : Sabitler.EK_UNVAN_PARCALARI) {
            isim = isim.replace(ekUnvan, "");
        }
        return isim.replace(".", "").strip();
    }

    /** Tek bir isim için kısaltma üretir. Roster'daki diğer hocalarla çakışma kontrolü yapmaz — bkz. {@link #benzersizKisaltmalarUret}. */
    public static String hocaKisaltmaOlustur(String isimGirdi) {
        if (isimGirdi == null || isimGirdi.isEmpty()) {
            return null;
        }
        String isim = temizle(isimGirdi);

        if (isim.contains("Elbir")) {
            return "AEL";
        }
        if (isim.contains("Biricik")) {
            return "G1";
        }

        String[] parcalar = isim.split("\\s+");
        if (parcalar.length == 0 || (parcalar.length == 1 && parcalar[0].isEmpty())) {
            return "";
        }
        if (parcalar.length == 1) {
            String tek = parcalar[0];
            return tek.substring(0, Math.min(2, tek.length())).toUpperCase();
        }

        StringBuilder kisaltma = new StringBuilder();
        for (int i = 0; i < parcalar.length - 1; i++) {
            if (!parcalar[i].isEmpty()) {
                kisaltma.append(Character.toUpperCase(parcalar[i].charAt(0)));
            }
        }
        String sonParca = parcalar[parcalar.length - 1];
        kisaltma.append(Character.toUpperCase(sonParca.charAt(0)));
        if (sonParca.length() == 1) {
            kisaltma.append(parcalar.length);
        }
        return kisaltma.toString();
    }

    private record Parcalanmis(String ad, String onEkBasHarfler, String sonParca) {
    }

    /**
     * Tüm roster için kısaltma üretir; aynı temel kısaltmaya düşen isimler (örn. "Ali Buldu",
     * "Anıl Baş", "Abdullah Bal" hepsi "AB") soyadın ilk harflerini kademeli olarak uzatarak
     * otomatik ayrıştırılır (örn. "ABul", "ABaş", "ABal").
     */
    public static Map<String, String> benzersizKisaltmalarUret(List<String> adlar) {
        Map<String, String> sonuc = new LinkedHashMap<>();
        Map<String, List<Parcalanmis>> gruplar = new LinkedHashMap<>();

        for (String ad : adlar) {
            String temizlenmis = temizle(ad);
            if (temizlenmis.contains("Elbir")) {
                sonuc.put(ad, "AEL");
                continue;
            }
            if (temizlenmis.contains("Biricik")) {
                sonuc.put(ad, "G1");
                continue;
            }
            String[] parcalar = temizlenmis.split("\\s+");
            if (parcalar.length <= 1) {
                String tek = parcalar.length == 1 ? parcalar[0] : "";
                sonuc.put(ad, tek.substring(0, Math.min(2, tek.length())).toUpperCase());
                continue;
            }
            StringBuilder onEk = new StringBuilder();
            for (int i = 0; i < parcalar.length - 1; i++) {
                onEk.append(Character.toUpperCase(parcalar[i].charAt(0)));
            }
            String sonParca = parcalar[parcalar.length - 1];
            String temelAnahtar = onEk + String.valueOf(Character.toUpperCase(sonParca.charAt(0)));
            gruplar.computeIfAbsent(temelAnahtar, k -> new ArrayList<>())
                    .add(new Parcalanmis(ad, onEk.toString(), sonParca));
        }

        for (Map.Entry<String, List<Parcalanmis>> entry : gruplar.entrySet()) {
            List<Parcalanmis> grup = entry.getValue();
            if (grup.size() == 1) {
                sonuc.put(grup.get(0).ad(), entry.getKey());
                continue;
            }

            int maxLen = grup.stream().mapToInt(p -> p.sonParca().length()).max().orElse(1);
            Map<Parcalanmis, String> adaylar = null;
            for (int k = 1; k <= maxLen; k++) {
                Map<Parcalanmis, String> deneme = new LinkedHashMap<>();
                for (Parcalanmis p : grup) {
                    String uzanti = p.sonParca().substring(0, Math.min(k, p.sonParca().length()));
                    deneme.put(p, p.onEkBasHarfler() + buyukYapIlkHarf(uzanti));
                }
                if (new HashSet<>(deneme.values()).size() == deneme.size()) {
                    adaylar = deneme;
                    break;
                }
            }
            if (adaylar == null) {
                adaylar = new LinkedHashMap<>();
                int i = 1;
                for (Parcalanmis p : grup) {
                    adaylar.put(p, p.onEkBasHarfler() + buyukYapIlkHarf(p.sonParca()) + (i++));
                }
            }
            for (Map.Entry<Parcalanmis, String> e : adaylar.entrySet()) {
                sonuc.put(e.getKey().ad(), e.getValue());
            }
        }
        return sonuc;
    }

    private static String buyukYapIlkHarf(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
