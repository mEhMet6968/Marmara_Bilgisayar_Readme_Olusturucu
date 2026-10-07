package edu.marmara.readme.engine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * giris.json kök yapısı.
 * Not: "icindekiler" elemanları önceden biçimlendirilmiş serbest metinlerdir
 * (örn. "[Başlık](#çapa)") — GirisWriter bunları doğrudan satır olarak yazar,
 * {baslik,capa} şeklinde ayrı alanlar DEĞİLDİR. GUI tarafı ekleme ekranında
 * ayrı "başlık"+"çapa" girdilerini birleştirip tek string olarak kaydeder.
 */
public class GirisBolumu {
    public String baslik = "";
    public String aciklama = "";
    public List<String> icindekiler = new ArrayList<>();
}
