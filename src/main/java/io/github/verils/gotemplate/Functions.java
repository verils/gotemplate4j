package io.github.verils.gotemplate;

import java.util.LinkedHashMap;
import java.util.Map;

public class Functions {

    public static final Map<String, Function> BUILTIN = new LinkedHashMap<>();

    static {
        BUILTIN.put("call", noop());
        BUILTIN.put("html", noop());
        BUILTIN.put("index", noop());
        BUILTIN.put("slice", noop());
        BUILTIN.put("js", noop());
        BUILTIN.put("len", noop());
        BUILTIN.put("print", print());
        BUILTIN.put("printf", printf());
        BUILTIN.put("println", println());
        BUILTIN.put("urlquery", noop());

        // Logical operations
        BUILTIN.put("and", noop());
        BUILTIN.put("or", noop());
        BUILTIN.put("not", not());

        // Comparisons
        BUILTIN.put("eq", noop());
        BUILTIN.put("ge", noop());
        BUILTIN.put("gt", noop());
        BUILTIN.put("le", noop());
        BUILTIN.put("lt", noop());
        BUILTIN.put("ne", noop());
    }

    private static Function noop() {
        return args -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                stringBuilder.append(args[i]);
                if (i < args.length - 1) {
                    stringBuilder.append(' ');
                }
            }
            return stringBuilder.toString();
        };
    }

    private static Function print() {
        return null;
    }

    private static Function printf() {
        return args -> {
            int realArgsLength = args.length - 1;
            Object[] realArgs = new Object[realArgsLength];
            System.arraycopy(args, 1, realArgs, 0, realArgsLength);
            return String.format(String.valueOf(args[0]), realArgs);
        };
    }

    private static Function println() {
        return args -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                stringBuilder.append(args[i]);
                if (i < args.length - 1) {
                    stringBuilder.append(' ');
                }
            }
            stringBuilder.append("\n");
            return stringBuilder.toString();
        };
    }

    private static Function not() {
        return args -> !isTrue(args);
    }

    private static boolean isTrue(Object[] args) {
        Object arg = args[0];
        if (arg == null) {
            return false;
        }
        return true;
    }

}
