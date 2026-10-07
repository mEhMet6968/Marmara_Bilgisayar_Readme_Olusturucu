package edu.marmara.readme.engine.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tek, paylaşılan Jackson ObjectMapper. Model sınıfları snake_case public
 * alanlar kullanır (Python JSON anahtarlarıyla birebir aynı isim) — bu yüzden
 * alan görünürlüğü ANY yapılır ve getter/setter gerekmez.
 *
 * Python tarafı json.dump(..., ensure_ascii=False, indent=4) kullanıyor;
 * burada da 4 boşluklu girinti + UTF-8 + Türkçe karakterlerin kaçışsız
 * yazılması (Jackson varsayılanı zaten böyle) korunuyor.
 */
public final class JsonUtil {

    public static final ObjectMapper MAPPER = buildMapper();

    private JsonUtil() {
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultIndenter indenter = new DefaultIndenter("    ", "\n");
        pp.indentObjectsWith(indenter);
        pp.indentArraysWith(indenter);
        mapper.setDefaultPrettyPrinter(pp);
        return mapper;
    }

    /** JSON dosyasını verilen tipe oku; dosya yoksa null döner (Python'daki FileNotFoundError -> None). */
    public static <T> T read(Path path, Class<T> type) {
        if (!Files.exists(path)) {
            return null;
        }
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return MAPPER.readValue(content, type);
        } catch (IOException e) {
            throw new RuntimeException("JSON okunamadı: " + path, e);
        }
    }

    /** Veriyi JSON olarak UTF-8 ile, 4 boşluklu girintiyle yazar; üst dizinleri oluşturur. */
    public static void write(Path path, Object data) {
        try {
            File parent = path.toFile().getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            String json = MAPPER.writer(MAPPER.getSerializationConfig().getDefaultPrettyPrinter())
                    .writeValueAsString(data);
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("JSON yazılamadı: " + path, e);
        }
    }
}
