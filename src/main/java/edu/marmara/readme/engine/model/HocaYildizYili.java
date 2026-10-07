package edu.marmara.readme.engine.model;

/** Python: {"anlatim_puani","kolaylik_puani","ogretme_puani","eglence_puani","yil","oy_sayisi"} — bir hocanın yıllara göre yıldız geçmişi öğesi.
 *  Not: Python tarafında sabit adı yanlışlıkla OGRETME_PUNAI = "ogretme_puani" şeklindedir; JSON anahtarı doğru (ogretme_puani), sadece Python sabit ismi hatalı — burada doğru JSON anahtarını kullanıyoruz. */
public class HocaYildizYili {
    public int anlatim_puani;
    public int kolaylik_puani;
    public int ogretme_puani;
    public int eglence_puani;
    public int yil;
    public int oy_sayisi;
}
