package org.plovdev.pvva;

import org.plovdev.pvva.utils.vars.Variable;
import org.plovdev.pvva.utils.vars.VariableHandler;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<Variable, String> vars = new HashMap<>();
        vars.put(Variable.VIDEO_ID, "12345");

        String str = "https://example.com/trailers/${video-id; CHRAT(0)}/${video-id; CHRAT(1)}/${video-id}/popular";
        System.out.println(VariableHandler.processVariables(str, vars));
    }
}