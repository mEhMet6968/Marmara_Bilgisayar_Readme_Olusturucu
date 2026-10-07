package edu.marmara.readme.engine.writer;

import edu.marmara.readme.engine.model.GirisBolumu;
import edu.marmara.readme.engine.model.Konfigurasyon;
import edu.marmara.readme.engine.util.GithubMetinIslemleri;

/** Python writers/giris_writer.py'nin Java portu. */
public class GirisWriter {

    public void write(BufferedReadmeWriter writer, GirisBolumu data, Konfigurasyon konfig) {
        if (data == null) {
            return;
        }
        writer.writeline("# 📖 " + data.baslik + "\n");
        writer.writeline(data.aciklama + "\n");
        writer.write(geriBildirimKismi(konfig));
        writer.write(tiklanmaSayisiHtml(konfig.github_url));

        writer.writeline("<details>");
        writer.writeline("<summary><b>🗂 İçindekiler</b></summary>\n");
        writer.writeline("## 🗂 İçindekiler\n");
        for (String item : data.icindekiler) {
            writer.writeline("- 🔗 " + item);
        }
        writer.writeline("</details>\n");
    }

    private String geriBildirimKismi(Konfigurasyon k) {
        return " ## 🗣️ Geri Bildirimde Bulunun\n\n"
                + "📬 Öğrenciler ve hocalar, derslerle ilgili hakaret içermeyen geri bildirimlerinizi "
                + "aşağıdaki linkler aracılığıyla anonim olarak paylaşabilirsiniz.\n\n"
                + "- [✍️ **Hocalar için yorum linki**](" + k.hoca_yorumlama + ")\n"
                + "- [⭐ **Hocalar için yıldız linki**](" + k.hoca_oylama + ")\n"
                + "- [✍️ **Dersler için yorum linki**](" + k.ders_yorumlama + ")\n"
                + "- [⭐ **Dersler için yıldız linki**](" + k.ders_oylama + ")\n";
    }

    /** degiskenler.py TIKLANMA_SAYISI: orijinal YTÜ sahibi dışında herkes için kullanıcı adı hash'lenir (anonimleştirme). */
    private String tiklanmaSayisiHtml(String githubUrl) {
        String kullaniciAdi = GithubMetinIslemleri.githubKullaniciAdiGetir(githubUrl);
        if (!"baselkelziye".equals(kullaniciAdi)) {
            kullaniciAdi = GithubMetinIslemleri.hashUrl39(githubUrl);
        }
        return "<p align=\"center\">\n<img src=\"https://komarev.com/ghpvc/?username=" + kullaniciAdi
                + "&label=Görüntülenme+Sayısı&abbreviated=true&style=for-the-badge&color=orange\" "
                + "width=\"400\" height=\"auto\"/>\n</p>\n\n";
    }
}
