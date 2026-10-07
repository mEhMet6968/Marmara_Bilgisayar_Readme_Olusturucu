package edu.marmara.readme.engine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * dersler.json içindeki "dersler" dizisinin bir öğesi.
 *
 * mufredatlar alanı Python aracında YOK — Marmara sürümüne özgü yeni alan:
 * bir dersin ait olduğu müfredat yıllarının listesi (örn. ["2026"] ya da
 * ["2021","2026"]). Bu liste 1'den fazla eleman içeriyorsa ders render
 * sırasında otomatik "Ortak Ders" rozeti alır (bkz. writer.Yardimci).
 */
public class Ders {
    public String ad = "";
    /** 0-4 arası sınıf yılı (0 = yıla bağlı olmayan ders, örn. seçmeli). */
    public int yil;
    public String donem = "";
    public String tip = "";
    public boolean guncel_mi = true;

    public List<HocaKisaRef> dersi_veren_hocalar = new ArrayList<>();

    /** Python: derse_dair_oneriler — öneri sahibi başına gruplanmış serbest-metin öneri listesi. */
    public List<OneriGrubu> derse_dair_oneriler = new ArrayList<>();

    /** Python: faydali_olabilecek_kaynaklar — her biri markdown link(ler) içerebilen serbest metin. */
    public List<String> faydali_olabilecek_kaynaklar = new ArrayList<>();

    public List<OgrenciGorusu> ogrenci_gorusleri = new ArrayList<>();
    public List<DersYildizYili> yillara_gore_yildiz_sayilari = new ArrayList<>();

    public int kolaylik_puani;
    public int gereklilik_puani;
    public int oy_sayisi;

    /** Marmara'ya özgü: bu dersin ait olduğu müfredat etiketleri. */
    public List<String> mufredatlar = new ArrayList<>();

    public boolean isOrtakDers() {
        return mufredatlar != null && mufredatlar.size() > 1;
    }
}
