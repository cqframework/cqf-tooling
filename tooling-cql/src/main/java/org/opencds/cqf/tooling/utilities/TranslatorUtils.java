package org.opencds.cqf.tooling.utilities;

import java.io.File;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.CqlTranslatorOptionsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranslatorUtils {

    private static final Logger logger = LoggerFactory.getLogger(TranslatorUtils.class);

    private TranslatorUtils() {}

    public static CqlTranslatorOptions getTranslatorOptions(String folder) {
        String optionsFileName = IOUtils.concatFilePath(folder, "cql-options.json");
        CqlTranslatorOptions options;
        File file = new File(optionsFileName);
        if (file.exists()) {
            options = CqlTranslatorOptionsMapper.fromFile(file.getAbsolutePath());
            logger.debug("cql-options loaded from: {}", file.getAbsolutePath());
        } else {
            options = CqlTranslatorOptions.defaultOptions();
            if (!options.getFormats().contains(CqlTranslatorOptions.Format.XML)) {
                options.getFormats().add(CqlTranslatorOptions.Format.XML);
            }
            logger.debug("cql-options not found. Using default options.");
        }

        return options;
    }
}
