package com.notequiz.common.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {

    @Bean
    public Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();
        // Typically tesseract data files are located in /usr/local/share/tessdata or similar.
        // On macOS with brew: /opt/homebrew/share/tessdata
        // We'll try to use environment variable if provided, otherwise assume standard path or let it fail if not found.
        String datapath = System.getenv("TESSDATA_PREFIX");
        if (datapath != null) {
            tesseract.setDatapath(datapath);
        }
        tesseract.setLanguage("kor+eng");
        return tesseract;
    }
}
