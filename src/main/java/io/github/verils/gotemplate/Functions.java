package io.github.verils.gotemplate;

import io.github.verils.gotemplate.parse.Function;

import java.util.LinkedHashMap;
import java.util.Map;

public class Functions {

    public static final Map<String, Function> BUILT_IN = new LinkedHashMap<>();

    static {
        BUILT_IN.put("println", args -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                stringBuilder.append(args[i]);
                if (i < args.length - 1) {
                    stringBuilder.append(' ');
                }
            }
            stringBuilder.append("\n");
            return stringBuilder.toString();
        });
    }
}
