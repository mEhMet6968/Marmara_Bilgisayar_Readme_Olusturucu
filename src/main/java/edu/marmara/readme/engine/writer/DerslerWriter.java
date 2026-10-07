package edu.marmara.readme.engine.writer;

import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.Ders;
import edu.marmara.readme.engine.model.DerslerBolumu;
import edu.marmara.readme.engine.model.HocaKisaRef;
import edu.marmara.readme.engine.model.HocalarBolumu;
import edu.marmara.readme.engine.model.Konfigurasyon;
import edu.marmara.readme.engine.model.OgrenciGorusu;
import edu.marmara.readme.engine.util.FolderCache;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Python writers/dersler_writer.py'nin Java portu. */
public class DerslerWriter {

    private final FolderCache folderCache;
    private final HocalarBolumu hocalarData;
    private final Path dokumanlarRepoYolu;

    public DerslerWriter(FolderCache folderCache, HocalarBolumu hocalarData, Path dokumanlarRepoYolu) {
        this.folderCache = folderCache;
        this.hocalarData = hocalarData != null ? hocalarData : new HocalarBolumu();
        this.dokumanlarRepoYolu = dokumanlarRepoYolu;
    }

    private void writeOgrenciGorusu(BufferedReadmeWriter writer, List<OgrenciGorusu> gorusler, String girinti,
                                      Konfigurasyon konfig) {
        if (gorusler.isEmpty()) {
            return;
        }
        writer.writeline(girinti + "- 💭 **Öğrenci Görüşleri:**");
        List<OgrenciGorusu> gosterilecek = gorusler.size() > 2
                ? gorusler.subList(gorusler.size() - 2, gorusler.size()) : gorusler;
        for (OgrenciGorusu gorus : gosterilecek) {
            String kisi = gorus.kisi == null ? "" : gorus.kisi.strip();
            String tarih = Yardimci.gorustenTarihGetir(gorus);
            writer.writeline(girinti + "  - 👤 **_" + kisi + "_**: " + gorus.yorum + " " + tarih);
        }
        if (gorusler.size() > 2) {
            writer.writeline(girinti + "  - ℹ️ Diğer " + (gorusler.size() - 2)
                    + " yoruma dersin kendi klasöründen erişebilirsiniz.");
        }
        writer.writeline(girinti + "    - ℹ️ Siz de [linkten](" + konfig.ders_yorumlama
                + ") anonim şekilde görüşlerinizi belirtebilirsiniz.");
    }

    private void writeYildizlar(BufferedReadmeWriter writer, double kolaylik, double gereklilik, String girinti,
                                  int oySayisi, String yilPrefix, Konfigurasyon konfig) {
        writer.writeline(girinti + "  - ✅ " + yilPrefix + "Dersi Kolay Geçer Miyim: "
                + Yardimci.puanlariYildizaCevir(kolaylik));
        writer.writeline(girinti + "  - 🎯 " + yilPrefix + "Ders Mesleki Açıdan Gerekli Mi: "
                + Yardimci.puanlariYildizaCevir(gereklilik));
        writer.writeline(girinti + "    - ℹ️ Yıldızlar " + oySayisi + " oy üzerinden hesaplanmıştır. Siz de [linkten]("
                + konfig.ders_oylama + ") anonim şekilde oylamaya katılabilirsiniz.");
    }

    private void writeYildizBolumu(BufferedReadmeWriter writer, Ders ders, String girinti, Konfigurasyon konfig) {
        writer.writeline(girinti + "- ⭐ **Yıldız Sayıları:**");
        if (ders.oy_sayisi > 0) {
            writeYildizlar(writer, ders.kolaylik_puani, ders.gereklilik_puani, girinti, ders.oy_sayisi, "", konfig);
        } else {
            writer.writeline(girinti + "    - ℹ️ Henüz yıldız veren yok. Siz de [linkten](" + konfig.ders_oylama
                    + ") anonim şekilde oylamaya katılabilirsiniz.");
        }
    }

    private Map<String, List<Ders>> gruplaDersler(List<Ders> dersler) {
        Map<String, List<Ders>> gruplanmis = new LinkedHashMap<>();
        for (Ders ders : dersler) {
            String donemKey;
            if (!ders.guncel_mi) {
                donemKey = Sabitler.ARTIK_MUFREDATA_DAHIL_OLMAYAN_DERSLER;
            } else if (ders.yil > 0) {
                donemKey = ders.yil + ". Yıl - " + (ders.donem == null ? "" : ders.donem);
            } else if (ders.tip != null && !ders.tip.isEmpty()) {
                donemKey = ders.tip;
            } else {
                continue;
            }
            gruplanmis.computeIfAbsent(donemKey, k -> new ArrayList<>());
            Yardimci.siraliEkle(gruplanmis.get(donemKey), ders, d -> Yardimci.dersSiralamaAnahtari(d.ad));
        }
        return gruplanmis;
    }

    public void write(BufferedReadmeWriter writer, DerslerBolumu data, Konfigurasyon konfig) {
        if (data == null || data.dersler.isEmpty()) {
            return;
        }

        String enPopulerDersAdi = data.en_populer_ders.ders_adi;
        int enPopulerDersOy = data.en_populer_ders.oy_sayisi;
        String enPopulerHocaAdi = hocalarData.en_populer_hoca.hoca_adi;
        int enPopulerHocaOy = hocalarData.en_populer_hoca.oy_sayisi;

        writer.writeline("<details>");
        writer.writeline("<summary><b>📖 " + data.bolum_adi + "</b></summary>\n");
        writer.writeline("\n\n\n## 📖 " + data.bolum_adi);
        writer.writeline("📄 " + data.bolum_aciklamasi + "\n\n\n");

        Map<String, List<Ders>> gruplanmisDersler = gruplaDersler(data.dersler);
        List<String> donemAnahtarlari = new ArrayList<>(gruplanmisDersler.keySet());
        donemAnahtarlari.sort(Yardimci.DONEM_GRUP_SIRALAMA);

        for (String donem : donemAnahtarlari) {
            writer.writeline("\n### 🗓 " + donem);
            for (Ders ders : gruplanmisDersler.get(donem)) {
                writer.writeline("\n");
                String dersAdi = ders.ad;
                boolean ortak = ders.isOrtakDers();
                String populerIsaret = dersAdi.equals(enPopulerDersAdi) ? "👑" : "";
                String populerBilgi = dersAdi.equals(enPopulerDersAdi)
                        ? " En popüler ders (" + enPopulerDersOy + " oy)" : "";
                String ortakRozet = ortak ? " 🔗 _Ortak Ders_" : "";

                writer.writeline("#### 📘 " + dersAdi + " " + populerIsaret + populerBilgi + ortakRozet);
                writer.writeline("  - 🏷️ **Ders Tipi:** " + ders.tip);

                writeOgrenciGorusu(writer, ders.ogrenci_gorusleri, "  ", konfig);
                writeYildizBolumu(writer, ders, "  ", konfig);

                if (!ders.dersi_veren_hocalar.isEmpty()) {
                    writer.writeline("  - 👨‍🏫 👩‍🏫 **Dersi Yürüten Akademisyenler:**");
                    for (HocaKisaRef hoca : ders.dersi_veren_hocalar) {
                        if (!hoca.ad.equals(enPopulerHocaAdi)) {
                            writer.writeline("    - [" + hoca.kisaltma + "]" + Yardimci.baslikLinkiOlustur(hoca.ad));
                        } else {
                            String hocaId = hoca.ad + " 👑 En popüler hoca (" + enPopulerHocaOy + " oy)";
                            writer.writeline("    - [" + hoca.kisaltma + "]" + Yardimci.baslikLinkiOlustur(hocaId));
                        }
                    }
                }

                if (folderCache != null) {
                    folderCache.findBestMatch(dersAdi).ifPresent(dersKlasoru -> {
                        String githubLink = Yardimci.yerelYoldanGithubLinkine(dersKlasoru, dokumanlarRepoYolu);
                        if (githubLink != null) {
                            writer.writeline("  - 📂 [Ders Klasörü](" + githubLink + ")");
                        }
                    });
                }

                if (!ders.guncel_mi) {
                    writer.writeline("  - ℹ️ Dersin içeriği güncel değil");
                    if (data.guncel_olmayan_ders_aciklamasi != null && !data.guncel_olmayan_ders_aciklamasi.isEmpty()) {
                        writer.writeline("    - " + data.guncel_olmayan_ders_aciklamasi);
                    }
                }
            }
        }
        writer.writeline("</details>\n");
    }
}
