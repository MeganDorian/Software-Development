package org.itmo.commands.pwd;

/**
 * All supported PWD flags
 */
public enum PwdFlags {
    /**
     * Flag <b>--help</b>
     */
    HELP;
    
    /**
     * Checks if flag belongs to any supported pwd flags
     *
     * @param flag name of flag to check
     *
     * @return <b>true</b> if flag belongs to the supported PWD flags <br>
     * <b>false</b> if not
     */
    public static boolean isBelongs(String flag) {
        try {
            PwdFlags.valueOf(flag.replaceAll("^-{1,2}", "").toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
