package org.opencds.cqf.tooling.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class ArgUtils {
    public static final String[] HELP_OPTIONS = {"h", "help", "?"};

    public static OptionSet parse(String[] args, OptionParser parser) {
        OptionSet options = parser.parse(args);
        if (options.has(HELP_OPTIONS[0])) {
            try {
                parser.printHelpOn(System.out);
            }
            catch (Exception e) {
            }

            System.exit(0);
        }

        return options;
    }

    public static void ensure(String option, OptionSet options) {
        if (!options.has(option)) {
            throw new IllegalArgumentException(String.format("%s is a required option.", option));
        }
    }

    public static String defaultValue(OptionSet optionSet, String option, String value) {
        return optionSet.valueOf(option) == null ? value : (String)optionSet.valueOf(option); 
    }

    public static List<String> getOptionValues(OptionSet options, String name) {
        List<?> argValues = options.valuesOf(name);
        List<String> stringArgValues = new ArrayList<>();
        for (Object argValue : argValues) {
          stringArgValues.add(argValue.toString());
        }
        if (stringArgValues.isEmpty()) {
          return (options.has(name) ? Collections.emptyList() : null);
        }
        return Collections.unmodifiableList(stringArgValues);
      }
}