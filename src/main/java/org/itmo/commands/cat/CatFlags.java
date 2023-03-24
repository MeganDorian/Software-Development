package org.itmo.commands.cat;

/**
 * All supported CAT flags
 */
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
    
    /**
     * Checks if flag belongs to any supported cat flags
     *
     * @param flag name of flag to check
     *
     * @return <b>true</b> if flag belongs to the supported CAT flags <br>
     * <b>false</b> if not
     */
    public static boolean isBelongs(String flag) {
        try {
            CatFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
