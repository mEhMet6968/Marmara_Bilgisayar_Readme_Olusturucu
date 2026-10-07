package edu.marmara.readme.engine.writer;

import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.DerslerBolumu;
import edu.marmara.readme.engine.model.Hoca;
import edu.marmara.readme.engine.model.HocaYildizYili;
import edu.marmara.readme.engine.model.HocalarBolumu;
import edu.marmara.readme.engine.model.Konfigurasyon;
import edu.marmara.readme.engine.model.OgrenciGorusu;
import edu.marmara.readme.engine.util.MetinIslemleri;

import java.util.List;

/** Python writers/hocalar_writer.py'nin Java portu. */
public class HocalarWriter {

    private final DerslerBolumu derslerData;

    public HocalarWriter(DerslerBolumu derslerData) {
        this.derslerData = derslerData != null ? derslerData : new DerslerBolumu();
    }

    private void writeOgrenciGorusu(BufferedReadmeWriter writer, List<OgrenciGorusu> gorusler, Konfigurasyon konfig) {
        for (OgrenciGorusu gorus : gorusler) {
            String kisi = gorus.kisi == null ? "" : gorus.kisi.strip();
            String tarih = Yardimci.gorustenTarihGetir(gorus);
            writer.writeline("  - 👤 **_" + kisi + "_**: " + gorus.yorum + " " + tarih);
        }
        writer.writeline("  - ℹ️ Siz de [linkten](" + konfig.hoca_yorumlama + ") anonim şekilde görüşlerinizi belirtebilirsiniz.");
    }

    private void writeYildizlar(BufferedReadmeWriter writer, double anlatim, double kolaylik, double ogretme,
                                  double eglence, String girinti, int oySayisi, String yilPrefix, Konfigurasyon konfig) {
        writer.writeline(girinti + "  - " + yilPrefix + "🎭 Dersi Zevkli Anlatır Mı:\t" + Yardimci.puanlariYildizaCevir(anlatim));
        writer.writeline(girinti + "  - " + yilPrefix + "🛣️ Dersi Kolay Geçer Miyim:\t" + Yardimci.puanlariYildizaCevir(kolaylik));
        writer.writeline(girinti + "  - " + yilPrefix + "🧠 Dersi Öğrenir Miyim:\t" + Yardimci.puanlariYildizaCevir(ogretme));
        writer.writeline(girinti + "  - " + yilPrefix + "🎉 Derste Eğlenir Miyim:\t" + Yardimci.puanlariYildizaCevir(eglence));
        writer.writeline(girinti + "    - ℹ️ Yıldızlar " + oySayisi + " oy üzerinden hesaplanmıştır. Siz de [linkten]("
                + konfig.hoca_oylama + ") anonim şekilde oylamaya katılabilirsiniz.");
    }

    private void writeYildizBolumu(BufferedReadmeWriter writer, Hoca hoca, Konfigurasyon konfig) {
        writer.writeline("- ⭐ **Yıldız Sayıları:**");
        if (hoca.oy_sayisi > 0) {
            writeYildizlar(writer, hoca.anlatim_puani, hoca.kolaylik_puani, hoca.ogretme_puani, hoca.eglence_puani,
                    "", hoca.oy_sayisi, "", konfig);
        } else {
            writer.writeline("    - ℹ️ Henüz yıldız veren yok. Siz de [linkten](" + konfig.hoca_oylama
                    + ") anonim şekilde oylamaya katılabilirsiniz.");
            return;
        }

        if (!hoca.yillara_gore_yildiz_sayilari.isEmpty()) {
            String yeniGirinti = "  ";
            Yardimci.DetayEtiketleri etiketler = Yardimci.detayEtiketleriOlustur("📅 Yıllara Göre Yıldız Sayıları", yeniGirinti);
            writer.write(etiketler.acilis());
            for (HocaYildizYili y : hoca.yillara_gore_yildiz_sayilari) {
                writer.writeline(yeniGirinti + "  - 📅 *" + y.yil + " yılı için yıldız bilgileri*");
                writeYildizlar(writer, y.anlatim_puani, y.kolaylik_puani, y.ogretme_puani, y.eglence_puani,
                        yeniGirinti + "  ", y.oy_sayisi, y.yil + " Yılında ", konfig);
            }
            writer.write(etiketler.kapanis());
        }
    }

    public void write(BufferedReadmeWriter writer, HocalarBolumu data, Konfigurasyon konfig) {
        if (data == null) {
            return;
        }
        List<Hoca> hocalar = data.hocalar.stream().filter(h -> h.ad != null && !h.ad.isEmpty()).toList();
        if (hocalar.isEmpty()) {
            return;
        }

        writer.writeline("<details>");
        writer.writeline("<summary><b>🎓 " + data.bolum_adi + "</b></summary>\n");
        writer.writeline("\n\n\n## 🎓 " + data.bolum_adi);
        if (data.bolum_aciklamasi != null && !data.bolum_aciklamasi.isEmpty()) {
            writer.writeline("📚 " + data.bolum_aciklamasi + "\n\n\n");
        }

        String enPopulerHocaAdi = data.en_populer_hoca.hoca_adi;
        int enPopulerHocaOy = data.en_populer_hoca.oy_sayisi;
        String enPopulerDersAdi = derslerData.en_populer_ders.ders_adi;
        int enPopulerDersOy = derslerData.en_populer_ders.oy_sayisi;

        int unvanSayaci = 0;
        List<Hoca> sirali = hocalar.stream().sorted(Yardimci.HOCA_SIRALAMA).toList();

        for (Hoca hoca : sirali) {
            String hocaAdi = hoca.ad;
            if (unvanSayaci < Sabitler.UNVANLAR.size() && hocaAdi.startsWith(Sabitler.UNVANLAR.get(unvanSayaci))) {
                if (unvanSayaci < Sabitler.UNVAN_BASLIKLARI.size()) {
                    writer.write("\n### " + Sabitler.UNVAN_BASLIKLARI.get(unvanSayaci) + "\n");
                }
                unvanSayaci++;
            } else if (unvanSayaci == Sabitler.UNVANLAR.size() && !hoca.hoca_aktif_gorevde_mi) {
                unvanSayaci++;
                writer.write("\n### Üniversitede Aktif Görevde Olmayan Hocalar\n");
            }

            String hocaEmoji = hoca.erkek_mi ? "👨‍🏫" : "👩‍🏫";
            String populerIsaret = hocaAdi.equals(enPopulerHocaAdi) ? "👑" : "";
            String populerBilgi = hocaAdi.equals(enPopulerHocaAdi) ? " En popüler hoca (" + enPopulerHocaOy + " oy)" : "";

            writer.writeline("\n\n\n#### " + hocaEmoji + " " + hocaAdi.strip() + " " + populerIsaret + populerBilgi);
            writer.writeline("- 🚪 **Ofis:** " + hoca.ofis);
            writer.writeline("- 🔗 **Araştırma Sayfası:** [" + hoca.link + "](" + hoca.link + ")");
            writer.writeline("- 💬 **Öğrenci Görüşleri:**");
            writeOgrenciGorusu(writer, hoca.ogrenci_gorusleri, konfig);

            writer.writeline("- 📚 **Verdiği Dersler:**");
            if (!hoca.dersler.isEmpty()) {
                for (String dersRaw : hoca.dersler) {
                    String ders = MetinIslemleri.dersAdiNormalize(dersRaw);
                    if (!ders.equals(enPopulerDersAdi)) {
                        writer.writeline("  - 📖 [" + ders + "]" + Yardimci.baslikLinkiOlustur(ders));
                    } else {
                        String dersId = ders + " 👑 En popüler ders (" + enPopulerDersOy + " oy)";
                        writer.writeline("  - 📖 [" + ders + "]" + Yardimci.baslikLinkiOlustur(dersId));
                    }
                }
            } else {
                writer.writeline("  - 📖 Ders bilgileri bulunamadı.");
            }

            writeYildizBolumu(writer, hoca, konfig);

            if (!hoca.hoca_aktif_gorevde_mi) {
                writer.writeline("- ℹ️ " + Sabitler.VARSAYILAN_HOCA_AKTIF_GOREVDE_DEGIL_MESAJI + ".");
            }
        }

        writer.writeline("</details>\n");
    }
}
