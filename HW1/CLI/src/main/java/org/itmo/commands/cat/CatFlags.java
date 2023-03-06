package org.itmo.commands.cat;

public enum CatFlags {
    
    /**
     * Flag <b>--help</b>
     */
    HELP,
    
    /**
     * Flag -h
     */
    H,
    
    /**
     * Flag <b>-e</b>, adds symbol $ to the end of line
     */
    E,
    
    /**
     * Flag <b>-n</b>>, adds number of the line at the beginning of the line
     */
    N;
    
    boolean isBelongs(String flag) {
        try {
            CatFlags.valueOf(flag.replaceAll("-", "").toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
