package edu.marmara.readme.engine.writer;

import edu.marmara.readme.engine.Sabitler;
import edu.marmara.readme.engine.model.IletisimBilgisi;
import edu.marmara.readme.engine.model.KatkidaBulunan;
import edu.marmara.readme.engine.model.KatkidaBulunanlarBolumu;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Python writers/katkida_bulunanlar_writer.py'nin Java portu. */
public class KatkidaBulunanlarWriter {

    public void write(BufferedReadmeWriter writer, KatkidaBulunanlarBolumu data) {
        if (data == null) {
            return;
        }
        List<String> oranlar = Sabitler.KATKIDA_BULUNMA_ORANI_DIZI;
        String varsayilanOran = oranlar.get(oranlar.size() - 1);

        List<KatkidaBulunan> sirali = data.katkida_bulunanlar.stream()
                .sorted(Comparator
                        .<KatkidaBulunan>comparingInt(k -> oranIndex(k.katkida_bulunma_orani, oranlar, varsayilanOran))
                        .thenComparing(k -> k.ad == null ? "" : k.ad))
                .toList();

        writer.writeline("<details>");
        writer.writeline("<summary><b>🤝 " + data.bolum_adi + "</b></summary>\n");
        writer.writeline("<h2 align='center'>🤝 " + data.bolum_adi + "</h2>\n");

        if (data.bolum_aciklamasi != null && !data.bolum_aciklamasi.isEmpty()) {
            writer.writeline(data.bolum_aciklamasi + "\n");
        }

        for (KatkidaBulunan k : sirali) {
            int oranIdx = oranIndex(k.katkida_bulunma_orani, oranlar, varsayilanOran);
            String emoji = Sabitler.KATKIDA_EMOJILER.get(Math.min(oranIdx, Sabitler.KATKIDA_EMOJILER.size() - 1));
            int headerSize = Math.min(oranIdx + 1, 6);
            String tag = "h" + headerSize;

            writer.writeline("<" + tag + " align='center'>" + emoji + " <b><i>" + k.ad + "</i></b> " + emoji + "</" + tag + ">");

            if (!k.iletisim_bilgileri.isEmpty()) {
                String html = k.iletisim_bilgileri.stream()
                        .map(b -> "<a href='" + b.link + "'><b>" + b.baslik + "</b></a>")
                        .collect(Collectors.joining(" &nbsp"));
                writer.writeline("<p align='center'>" + html + "</p>");
            }
            writer.writeline("");
        }
        writer.writeline("</details>\n");
    }

    private int oranIndex(String oran, List<String> oranlar, String varsayilan) {
        String key = (oran == null || oran.isEmpty()) ? varsayilan : oran;
        int idx = oranlar.indexOf(key);
        return idx >= 0 ? idx : oranlar.size() - 1;
    }
}
