package org.plovdev.pvva.utils.vars;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VariableHandler {
    // Исправленное регулярное выражение - захватывает имя переменной и опциональные модификаторы
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([\\w-]+)(?:;([^}]+))?}");
    private static final Logger log = LoggerFactory.getLogger(VariableHandler.class);

    private VariableHandler() {
    }

    public static @NonNull String processVariables(String src, Map<Variable, String> providesVars) {
        Matcher matcher = VAR_PATTERN.matcher(src);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String modifiersPart = matcher.group(2);

            String varValue = Objects.requireNonNull(
                    providesVars.get(Variable.ofName(varName)),
                    "Variable not found: " + varName
            );

            if (modifiersPart != null && !modifiersPart.isEmpty()) {
                String[] modifierNames = modifiersPart.split(";");
                varValue = processModifiers(varValue, modifierNames);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(varValue));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static @NonNull String processModifiers(String var, String @NonNull [] mods) {
        String result = var;
        for (String mod : mods) {
            try {
                String[] modParams = null;
                String modName = mod.trim();

                if (modName.contains("(") && modName.contains(")")) {
                    String paramsData = modName.substring(modName.indexOf("(") + 1, modName.lastIndexOf(")"));
                    modParams = paramsData.split(",");
                    modName = modName.substring(0, modName.indexOf("(")).trim();
                }

                modName = modName.trim().toUpperCase();

                VariableModifier.Modifier modifier = VariableModifier.Modifier.getByName(modName);
                VariableModifier varMod = getVariableModifier(modParams, modifier);
                result = varMod.process(result);
            } catch (Exception e) {
                log.error("Error processing modifier '{}': {}", mod, e.getMessage());
            }
        }
        return result;
    }

    private static @NonNull VariableModifier getVariableModifier(String[] modParams, VariableModifier.@NonNull Modifier modifier) {
        return switch (modifier) {
            case CHAR_AT -> s -> String.valueOf(s.charAt(Integer.parseInt(modParams[0].trim())));
            case TO_UPPER_CASE -> String::toUpperCase;
            case TO_LOWER_CASE -> String::toLowerCase;
            case REPLACE -> s -> s.replace(modParams[0].trim(), modParams[1].trim());
            case SUBSTRING -> s -> s.substring(Integer.parseInt(modParams[0].trim()), Integer.parseInt(modParams[1].trim()));
        };
    }
}