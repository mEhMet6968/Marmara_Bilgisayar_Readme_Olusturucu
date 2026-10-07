package edu.marmara.readme.engine.model;

/** Python: {"kisi": str, "yorum": str, "tarih": {yil,ay,gun}} — kişi adı case-insensitive tekilleştirme anahtarıdır. */
public class OgrenciGorusu {
    public String kisi = "";
    public String yorum = "";
    public Tarih tarih = new Tarih();

    public OgrenciGorusu() {
    }

    public OgrenciGorusu(String kisi, String yorum, Tarih tarih) {
        this.kisi = kisi;
        this.yorum = yorum;
        this.tarih = tarih;
    }
}
