package edu.marmara.readme.engine.model;

import java.util.ArrayList;
import java.util.List;

/** dersler.json kök yapısı. */
public class DerslerBolumu {
    public String bolum_adi = "Dersler";
    public String bolum_aciklamasi = "";
    public String ders_klasoru_bulunamadi_mesaji = "";
    public String guncel_olmayan_ders_aciklamasi = "";
    public EnPopulerDers en_populer_ders = new EnPopulerDers();
    public List<Ders> dersler = new ArrayList<>();
}
