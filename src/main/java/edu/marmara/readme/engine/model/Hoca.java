package edu.marmara.readme.engine.model;

import java.util.ArrayList;
import java.util.List;

/** hocalar.json içindeki "hocalar" dizisinin bir öğesi. Ad, ünvanıyla birlikte saklanır: "Prof. Dr. Ad Soyad". */
public class Hoca {
    public String ad = "";
    public String ofis = "";
    public String link = "";
    public boolean hoca_aktif_gorevde_mi = true;
    public boolean erkek_mi = true;

    /** Hocanın verdiği derslerin (Ders.ad ile eşleşen) adları. */
    public List<String> dersler = new ArrayList<>();

    public List<OgrenciGorusu> ogrenci_gorusleri = new ArrayList<>();
    public List<HocaYildizYili> yillara_gore_yildiz_sayilari = new ArrayList<>();

    public int anlatim_puani;
    public int kolaylik_puani;
    public int ogretme_puani;
    public int eglence_puani;
    public int oy_sayisi;

    /** Marmara'ya özgü: hoca birden fazla bölümde ders veriyorsa (örn. BLM ve SGM). */
    public List<String> bolumler = new ArrayList<>();
}
