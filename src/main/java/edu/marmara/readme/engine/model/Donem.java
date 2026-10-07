package edu.marmara.readme.engine.model;

import java.util.ArrayList;
import java.util.List;

/** donemler.json içindeki "donemler" dizisinin bir öğesi. */
public class Donem {
    public String donem_adi = "";
    public int yil;
    public String donem = "";
    public List<String> genel_tavsiyeler = new ArrayList<>();
}
