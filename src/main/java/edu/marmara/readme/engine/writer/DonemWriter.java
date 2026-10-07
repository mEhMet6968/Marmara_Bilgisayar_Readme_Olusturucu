package edu.marmara.readme.engine.writer;

import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.Ders;
import edu.marmara.readme.engine.model.Donem;
import edu.marmara.readme.engine.model.DersYildizYili;
import edu.marmara.readme.engine.model.HocaKisaRef;
import edu.marmara.readme.engine.model.Konfigurasyon;
import edu.marmara.readme.engine.model.OgrenciGorusu;
import edu.marmara.readme.engine.model.OneriGrubu;

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Python writers/donem_writer.py'nin Java portu. */
public class DonemWriter {

    private final Path dokumanlarRepoYolu;

    public DonemWriter(Path dokumanlarRepoYolu) {
        this.dokumanlarRepoYolu = dokumanlarRepoYolu;
    }

    private void writeOgrenciGorusu(BufferedReadmeWriter writer, List<OgrenciGorusu> gorusler, Konfigurasyon konfig) {
        if (gorusler.isEmpty()) {
            return;
        }
        writer.writeline("- 💭 **Öğrenci Görüşleri:**");
        for (OgrenciGorusu gorus : gorusler) {
            String kisi = gorus.kisi == null ? "" : gorus.kisi.strip();
            writer.writeline("  - 👤 **_" + kisi + "_**: " + gorus.yorum + " " + Yardimci.gorustenTarihGetir(gorus));
        }
        writer.writeline("    - ℹ️ Siz de [linkten](" + konfig.ders_yorumlama + ") anonim şekilde görüşlerinizi belirtebilirsiniz.");
    }

    private void writeYildizlar(BufferedReadmeWriter writer, double kolaylik, double gereklilik, String girinti,
                                  int oySayisi, String yilPrefix, Konfigurasyon konfig) {
        writer.writeline(girinti + "- ✅ " + yilPrefix + "Dersi Kolay Geçer Miyim: " + Yardimci.puanlariYildizaCevir(kolaylik));
        writer.writeline(girinti + "- 🎯 " + yilPrefix + "Ders Mesleki Açıdan Gerekli Mi: " + Yardimci.puanlariYildizaCevir(gereklilik));
        writer.writeline(girinti + "  - ℹ️ Yıldızlar " + oySayisi + " oy üzerinden hesaplanmıştır. Siz de [linkten]("
                + konfig.ders_oylama + ") anonim şekilde oylamaya katılabilirsiniz.");
    }

    private void writeYildizBolumu(BufferedReadmeWriter writer, Ders ders, Konfigurasyon konfig) {
        writer.writeline("- ⭐ **Yıldız Sayıları:**");
        if (ders.oy_sayisi > 0) {
            writeYildizlar(writer, ders.kolaylik_puani, ders.gereklilik_puani, "  ", ders.oy_sayisi, "", konfig);
        } else {
            writer.writeline("    - ℹ️ Henüz yıldız veren yok. Siz de [linkten](" + konfig.ders_oylama
                    + ") anonim şekilde oylamaya katılabilirsiniz.");
            return;
        }
        if (!ders.yillara_gore_yildiz_sayilari.isEmpty()) {
            Yardimci.DetayEtiketleri etiketler = Yardimci.detayEtiketleriOlustur("📅 Yıllara Göre Yıldız Sayıları", "  ");
            writer.write(etiketler.acilis());
            for (DersYildizYili y : ders.yillara_gore_yildiz_sayilari) {
                writer.writeline("    - 📅 *" + y.yil + " yılı için yıldız bilgileri*");
                writeYildizlar(writer, y.kolaylik_puani, y.gereklilik_puani, "      ", y.oy_sayisi, y.yil + " Yılında ", konfig);
            }
            writer.write(etiketler.kapanis());
        }
    }

    public void writeDonemReadme(BufferedReadmeWriter writer, Donem donem) {
        writer.writeline("# 📅 " + donem.donem_adi + "\n");
        writer.writeline("## 📝 Genel Tavsiyeler\n");
        for (String tavsiye : donem.genel_tavsiyeler) {
            writer.writeline("- 💡 " + tavsiye);
        }
        if (donem.yil != 0) {
            writer.writeline("## 📚 Dönemin Zorunlu Dersleri\n");
        }
    }

    public void writeDersToDonem(BufferedReadmeWriter writer, Ders ders, String guncelOlmayanDersAciklamasi,
                                   Path donemKlasoru, Path dersKlasoru, Konfigurasyon konfig) {
        writer.writeline("\n### 📘 " + ders.ad + "\n");
        writer.writeline("#### 📄 Ders Bilgileri\n");
        writer.writeline("- 📅 **Yıl:** " + ders.yil);
        writer.writeline("- 📆 **Dönem:** " + ders.donem);
        writer.writeline("- 🏫 **Ders Tipi:** " + ders.tip);

        writeOgrenciGorusu(writer, ders.ogrenci_gorusleri, konfig);
        writeYildizBolumu(writer, ders, konfig);

        if (!ders.derse_dair_oneriler.isEmpty()) {
            writer.writeline("#### 💡 Derse Dair Öneriler\n");
            for (OneriGrubu grup : ders.derse_dair_oneriler) {
                if (!grup.oneriler.isEmpty()) {
                    writer.writeline("##### 📌 Öneri sahibi: " + grup.oneri_sahibi);
                    for (String oneri : grup.oneriler) {
                        writer.writeline("- " + Yardimci.kaynakLinkleriniGoreceliYap(oneri, donemKlasoru, dersKlasoru, dokumanlarRepoYolu));
                    }
                }
            }
        }

        writer.writeline("\n#### 📚 Faydalı Olabilecek Kaynaklar\n");
        if (!ders.faydali_olabilecek_kaynaklar.isEmpty()) {
            List<String> sirali = ders.faydali_olabilecek_kaynaklar.stream()
                    .sorted(Comparator.comparing(DonemWriter::normalizeKey)).toList();
            for (String kaynak : sirali) {
                writer.writeline("- 📄 " + Yardimci.kaynakLinkleriniGoreceliYap(kaynak, donemKlasoru, dersKlasoru, dokumanlarRepoYolu) + " ✨");
            }
        }

        writer.write("- 📄 [Genel Çıkmış Sorular](" + konfig.cikmislar + ")\n");
        writer.writeline("  - ℹ️ " + Sabitler.FAYDALI_OLABILECEK_KAYNAKLAR_UYARI_MESAJI);

        if (!ders.dersi_veren_hocalar.isEmpty()) {
            writer.writeline("\n#### 👨‍🏫 👩‍🏫 Dersi Yürüten Akademisyenler:");
            for (HocaKisaRef hoca : ders.dersi_veren_hocalar) {
                writer.writeline("- **" + hoca.kisaltma + "** — " + hoca.ad);
            }
        }

        if (!ders.guncel_mi) {
            writer.writeline("\n#### ℹ️ Dersin içeriği güncel değil");
            writer.writeline("- " + guncelOlmayanDersAciklamasi);
        }
    }

    private static String normalizeKey(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
    }
}
