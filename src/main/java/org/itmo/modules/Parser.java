package org.itmo.modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs command parsing and substitution
 */
public class Parser {
    
    LocalStorage localStorage;
    
    Pattern patternSingleQuotes;
    Pattern patternDoubleQuotes;
    Pattern patternVariables;
    
    Matcher matcherSingleQuotes;
    Matcher matcherDoubleQuotes;
    Matcher matcherVariables;
    
    public Parser() {
        localStorage = new LocalStorage();
        patternSingleQuotes = Pattern.compile("'[^']*'");
        patternDoubleQuotes = Pattern.compile("\"[^\"]*\"");
        patternVariables = Pattern.compile("\\$[^\\$ ] ");
    }
    
    /**
     * Removes unnecessary inverted commas and substitutes variables
     *
     * If no variable is found, substitutes an empty string
     *
     * @param line -- processing string
     * @return substitution string
     */
    public StringBuilder substitutor(String line) {
        StringBuilder result = new StringBuilder();;
        matcherSingleQuotes = patternSingleQuotes.matcher(line);
        matcherDoubleQuotes = patternDoubleQuotes.matcher(line);
        int startIndexSubstring = 0;
        int startIndexPatternSingleQuotes = -1, endIndexPatternSingleQuotes = -1;
        int startIndexPatternDoubleQuotes = -1, endIndexPatternDoubleQuotes = -1;
        // marks whether to look for the next occurrence of the pattern
        boolean flS = false, flD = false;
        while (startIndexSubstring != line.length()) {
            // if double quotes are to be searched for
            if(!flD) {
                flD = matcherDoubleQuotes.find(startIndexSubstring);
                if (flD) {
                    startIndexPatternDoubleQuotes = matcherDoubleQuotes.start();
                    endIndexPatternDoubleQuotes = matcherDoubleQuotes.end();
                } else {
                    startIndexPatternDoubleQuotes = -1;
                    endIndexPatternDoubleQuotes = -1;
                }
            }
            
            //if single quotes are to be searched for
            if(!flS) {
                flS = matcherSingleQuotes.find(startIndexSubstring);
                if(flS) {
                    startIndexPatternSingleQuotes = matcherSingleQuotes.start();
                    endIndexPatternSingleQuotes = matcherSingleQuotes.end();
                } else {
                    startIndexPatternSingleQuotes = -1;
                    endIndexPatternSingleQuotes = -1;
                }
            }
            
            // both types of quotes are found
            if(flD && flS)
            {
                // if inverted commas are nested and double quotes are on the outside
                if (startIndexPatternDoubleQuotes < startIndexPatternSingleQuotes
                        && endIndexPatternDoubleQuotes > endIndexPatternSingleQuotes) {
                    // if there is an unprocessed string between the current pattern found and the previous one
                    // we give it to substitute variables
                    if (startIndexPatternDoubleQuotes - startIndexSubstring > 0) {
                        result.append(substitutionVariables(line.substring(startIndexSubstring, startIndexPatternDoubleQuotes)));
                    }
                    // send everything inside the double quotes to substitute variables
                    // the double quotes themselves will be deleted
                    if (endIndexPatternDoubleQuotes - startIndexPatternDoubleQuotes > 0) {
                        result.append(substitutionVariables(
                                line.substring(startIndexPatternDoubleQuotes + 1, endIndexPatternDoubleQuotes - 1)));
                    }
                    startIndexSubstring = endIndexPatternDoubleQuotes;
                    // discount all single quotes within double quotes
                    flS = matcherSingleQuotes.find(endIndexPatternDoubleQuotes);
                    // mark that the inverted commas have been processed
                    flD = false;
                }
                // if the inverted commas are nested inside each other and there are single quotes on the outside
                else if (startIndexPatternSingleQuotes < startIndexPatternDoubleQuotes
                        && endIndexPatternSingleQuotes > endIndexPatternDoubleQuotes) {
                    // if there is an unprocessed string between the current pattern found and the previous one
                    // we give it to substitute variables
                    if (startIndexPatternSingleQuotes - startIndexSubstring > 0)
                    {
                        result.append(substitutionVariables(
                                line.substring(startIndexSubstring, startIndexPatternSingleQuotes)));
                    }
                    // add a line in single quotes, the quotes themselves will be cut out
                    if (endIndexPatternSingleQuotes - startIndexPatternSingleQuotes > 0) {
                        result.append(line, startIndexPatternSingleQuotes + 1, endIndexPatternSingleQuotes - 1);
                    }
                    startIndexSubstring = endIndexPatternSingleQuotes;
                    // discount all double quotes inside single quotes
                    flD = matcherDoubleQuotes.find(endIndexPatternSingleQuotes);
                    // mark that the inverted commas have been processed
                    flS = false;
                }
                // the inverted commas are not nested within each other
                else {
                    // if the beginning of a double quote type within a single quote type
                    if (startIndexPatternSingleQuotes < startIndexPatternDoubleQuotes
                            && endIndexPatternSingleQuotes < endIndexPatternDoubleQuotes) {
                        // if there is an unprocessed string between the current pattern found and the previous one
                        // we give it to substitute variables
                        if (startIndexPatternSingleQuotes - startIndexSubstring > 0)
                        {
                            result.append(substitutionVariables(
                                    line.substring(startIndexSubstring, startIndexPatternSingleQuotes)));
                        }
                        // add a line in single quotes, the quotes themselves will be cut out
                        if (endIndexPatternSingleQuotes - startIndexPatternSingleQuotes > 0) {
                            result.append(line, startIndexPatternSingleQuotes + 1, endIndexPatternSingleQuotes - 1);
                        }
                        startIndexSubstring = endIndexPatternSingleQuotes;
                        // discount all double quotes inside single quotes
                        flD = matcherDoubleQuotes.find(endIndexPatternSingleQuotes);
                        // mark that the inverted commas have been processed
                        flS = false;
                    }
                    // if the beginning of a single-quote type within a double-quote type
                    else if(startIndexPatternDoubleQuotes < startIndexPatternSingleQuotes
                            && endIndexPatternDoubleQuotes < endIndexPatternSingleQuotes) {
                        // if there is an unprocessed string between the current pattern found and the previous one
                        // we give it to substitute variables
                        if (startIndexPatternDoubleQuotes - startIndexSubstring > 0) {
                            result.append(substitutionVariables(line.substring(startIndexSubstring, startIndexPatternDoubleQuotes)));
                        }
                        // send everything inside the double quotes to substitute variables
                        // the double quotes themselves will be deleted
                        if (endIndexPatternDoubleQuotes - startIndexPatternDoubleQuotes > 0) {
                            result.append(substitutionVariables(
                                    line.substring(startIndexPatternDoubleQuotes + 1, endIndexPatternDoubleQuotes - 1)));
                        }
                        startIndexSubstring = endIndexPatternDoubleQuotes;
                        // discount all single quotes within double quotes
                        flS = matcherSingleQuotes.find(endIndexPatternDoubleQuotes);
                        // mark that the inverted commas have been processed
                        flD = false;
                    }
                    // the quotes do not overlap in any way, so we process the ones that come first
                    else {
                        // if double quotes go before
                        if(startIndexPatternDoubleQuotes < startIndexPatternSingleQuotes) {
                            // if there is an unprocessed string between the current pattern found and the previous one
                            // we give it to substitute variables
                            if (startIndexPatternDoubleQuotes - startIndexSubstring > 0) {
                                result.append(substitutionVariables(line.substring(startIndexSubstring, startIndexPatternDoubleQuotes)));
                            }
                            // send everything inside the double quotes to substitute variables
                            // the double quotes themselves will be deleted
                            if (endIndexPatternDoubleQuotes - startIndexPatternDoubleQuotes > 0) {
                                result.append(substitutionVariables(
                                        line.substring(startIndexPatternDoubleQuotes + 1, endIndexPatternDoubleQuotes - 1)));
                            }
                            startIndexSubstring = endIndexPatternDoubleQuotes;
                            // mark that the inverted commas have been processed
                            flD = false;
                        }
                        // if single quotes go before
                        else {
                            // if there is an unprocessed string between the current pattern found and the previous one
                            // we give it to substitute variables
                            if (startIndexPatternSingleQuotes - startIndexSubstring > 0)
                            {
                                result.append(substitutionVariables(
                                        line.substring(startIndexSubstring, startIndexPatternSingleQuotes)));
                            }
                            // add a line in single quotes, the quotes themselves will be cut out
                            if (endIndexPatternSingleQuotes - startIndexPatternSingleQuotes > 0) {
                                result.append(line, startIndexPatternSingleQuotes + 1, endIndexPatternSingleQuotes - 1);
                            }
                            startIndexSubstring = endIndexPatternSingleQuotes;
                            // mark that the inverted commas have been processed
                            flS = false;
                        }
                    }
                }
            }
            // if you have found even one type of inverted comma
            else if(flD || flS) {
                if(flD)
                {
                    // if there is an unprocessed string between the current pattern found and the previous one
                    // we give it to substitute variables
                    if (startIndexPatternDoubleQuotes - startIndexSubstring > 0)
                    {
                        result.append(substitutionVariables(line.substring(startIndexSubstring, startIndexPatternDoubleQuotes)));
                    }
                    // send everything inside the double quotes to substitute variables
                    // the double quotes themselves will be deleted
                    if (endIndexPatternDoubleQuotes - startIndexPatternDoubleQuotes > 0)
                    {
                        result.append(substitutionVariables(
                                line.substring(startIndexPatternDoubleQuotes + 1, endIndexPatternDoubleQuotes - 1)));
                    }
                    startIndexSubstring = endIndexPatternDoubleQuotes;
                    // mark that the inverted commas have been processed
                    flD = false;
                } else {
                    // if there is an unprocessed string between the current pattern found and the previous one
                    // we give it to substitute variables
                    if (startIndexPatternSingleQuotes - startIndexSubstring > 0)
                    {
                        result.append(substitutionVariables(
                                line.substring(startIndexSubstring, startIndexPatternSingleQuotes)));
                    }
                    // add a line in single quotes, the quotes themselves will be cut out
                    if (endIndexPatternSingleQuotes - startIndexPatternSingleQuotes > 0) {
                        result.append(line, startIndexPatternSingleQuotes + 1, endIndexPatternSingleQuotes - 1);
                    }
                    startIndexSubstring = endIndexPatternSingleQuotes;
                    // mark that the inverted commas have been processed
                    flS = false;
                }
            }
            // if there are no pattern entries
            else {
                // if there is an unprocessed string between the last pattern found
                // we give it to substitute variables
                if (line.length() - startIndexSubstring > 0)
                {
                    result.append(substitutionVariables(
                            line.substring(startIndexSubstring, line.length())));
                }
                startIndexSubstring = line.length();
            }
        }
        return result;
    }
    
    /**
     * Performs variable substitution
     * If no variable is found, an empty string will be substituted for the default
     * @param line -- substitution string
     * @return the line with the substitutions made
     */
    private StringBuilder substitutionVariables(String line) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        matcherVariables = patternVariables.matcher(line);
        while (matcherVariables.find()) {
            // add a line before the variable
            result.append(line, index, matcherVariables.end());
            localStorage.get(line.substring(matcherVariables.start() + 1,
                                            matcherVariables.end())).ifPresentOrElse(
                    (var) -> {
                        result.append(var);
                    },
                    ()->{
                        result.append("");
                    });
            index = matcherVariables.end();
        }
        if(line.length() - index > 0) {
            result.append(line, index, line.length());
        }
        return result;
    }
}
