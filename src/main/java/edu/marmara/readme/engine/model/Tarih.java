package edu.marmara.readme.engine.model;

/** Python: {"yil": int, "ay": int, "gun": int} — Google Form zaman damgasından türetilir, saat/dakika atılır. */
public class Tarih {
    public int yil;
    public int ay;
    public int gun;

    public Tarih() {
    }

    public Tarih(int yil, int ay, int gun) {
        this.yil = yil;
        this.ay = ay;
        this.gun = gun;
    }

    /** Python kodunda tarih "MM.YYYY" formatında yorumların yanında gösterilir. */
    public String mmYyyy() {
        return String.format("%02d.%04d", ay, yil);
    }
}
