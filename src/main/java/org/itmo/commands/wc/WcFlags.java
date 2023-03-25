package org.itmo.commands.wc;

import org.itmo.commands.pwd.PwdFlags;

/**
 * All supported WC flags
 */
public enum WcFlags {
    /**
     * Flag <b>--help</b>
     */
    HELP,
    
    /**
     * Flag <b>--h</b>
     */
    H,
    
    /**
     * Flag <b>-c</b> - byte count
     */
    C,
    
    /**
     * Flag <b>-l</b> - lines count
     */
    L,
    
    /**
     * Flag <b>-w</b> - words count
     */
    W;
    
    /**
     * Checks if flag belongs to any supported wc flags
     *
     * @param flag name of flag to check
     *
     * @return <b>true</b> if flag belongs to the supported WC flags <br>
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
