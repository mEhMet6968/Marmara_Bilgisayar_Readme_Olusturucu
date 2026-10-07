package edu.marmara.readme.engine.writer;

import edu.marmara.readme.engine.model.Kavram;
import edu.marmara.readme.engine.model.RepoKullanimiBolumu;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Python writers/repo_kullanimi_writer.py'nin Java portu. */
public class RepoKullanimiWriter {

    public void write(BufferedReadmeWriter writer, RepoKullanimiBolumu data) {
        if (data == null) {
            return;
        }
        writer.writeline("<details>");
        writer.writeline("<summary><b>🛠 " + data.baslik + "</b></summary>\n");
        writer.writeline("\n\n\n## 🛠 " + data.baslik + "\n");

        writer.writeline("### ⚙️ " + data.aciklama + ":");
        for (String a : data.aciklamalar) {
            writer.writeline("- 📋 " + a);
        }

        writer.writeline("\n\n### 📝 " + data.talimat + ":");
        for (String t : data.talimatlar) {
            writer.writeline("- 👉 " + t);
        }
        writer.writeline("</details>\n");

        if (!data.kavramlar.isEmpty()) {
            writer.writeline("<details>");
            writer.writeline("<summary><b>🔍 " + data.kavram + "</b></summary>\n");
            writer.writeline("\n\n## 🔍 " + data.kavram);

            List<Kavram> sirali = data.kavramlar.stream()
                    .sorted(Comparator.comparing(k -> normalizeKey(k.kavram))).toList();
            for (Kavram k : sirali) {
                writer.writeline("- 💡 **" + k.kavram + "**");
                for (String a : k.aciklamalar) {
                    writer.writeline("  - 📘 " + a);
                }
            }
            writer.writeline("</details>\n");
        }
    }

    private static String normalizeKey(String s) {
        return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
    }
}
