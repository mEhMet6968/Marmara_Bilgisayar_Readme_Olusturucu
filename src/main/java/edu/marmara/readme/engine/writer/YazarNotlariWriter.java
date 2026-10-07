package edu.marmara.readme.engine.writer;

import edu.marmara.readme.engine.model.YazarNotlariBolumu;

/** Python writers/yazar_notlari_writer.py'nin Java portu. */
public class YazarNotlariWriter {

    public void write(BufferedReadmeWriter writer, YazarNotlariBolumu data) {
        if (data == null) {
            return;
        }
        writer.writeline("<details>");
        writer.writeline("<summary><b>🖋 " + data.baslik + "</b></summary>\n");
        writer.writeline("\n## 🖋 " + data.baslik + "\n");
        for (String aciklama : data.aciklamalar) {
            writer.writeline("- 📝 " + aciklama);
        }
        writer.writeline("</details>\n");
    }
}
