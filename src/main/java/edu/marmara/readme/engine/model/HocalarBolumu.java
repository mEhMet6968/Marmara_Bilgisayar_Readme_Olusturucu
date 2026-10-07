package edu.marmara.readme.engine.model;

import java.util.ArrayList;
import java.util.List;

/** hocalar.json kök yapısı. */
public class HocalarBolumu {
    public String bolum_adi = "Hocalar";
    public String bolum_aciklamasi = "";
    public EnPopulerHoca en_populer_hoca = new EnPopulerHoca();
    public List<Hoca> hocalar = new ArrayList<>();
}
