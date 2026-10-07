package edu.marmara.readme.engine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * repo_kullanimi.json kök yapısı.
 * Not: "aciklama"/"talimat"/"kavram" (TEKİL) alanları, kendi listelerinin (aciklamalar/
 * talimatlar/kavramlar) README'de gösterilen BÖLÜM BAŞLIĞI metnidir — Python
 * repo_kullanimi_writer.py bunları "### {aciklama}:" / "### {talimat}:" / "## {kavram}"
 * başlığı olarak kullanır. Her ikisi de ayrı ayrı düzenlenebilir.
 */
public class RepoKullanimiBolumu {
    public String baslik = "Repo Kullanımı";
    public String aciklama = "Açıklamalar";
    public List<String> aciklamalar = new ArrayList<>();
    public String talimat = "Talimatlar";
    public List<String> talimatlar = new ArrayList<>();
    public String kavram = "Kavramlar";
    public List<Kavram> kavramlar = new ArrayList<>();
}
