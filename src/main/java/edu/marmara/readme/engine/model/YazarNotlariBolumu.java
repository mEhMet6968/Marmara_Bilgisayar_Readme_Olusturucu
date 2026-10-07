package edu.marmara.readme.engine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * yazarin_notlari.json kök yapısı.
 * Not: Python kodunda notlar listesi "notlar" değil "aciklamalar" anahtarıyla saklanır
 * (yazarin_notlari_duzenle_window.py içinde ACIKLAMALAR sabiti kullanılıyor) — bu isim birebir korunmuştur.
 */
public class YazarNotlariBolumu {
    public String baslik = "Yazarın Notları";
    public List<String> aciklamalar = new ArrayList<>();
}
