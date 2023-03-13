package org.itmo.modules;

import org.itmo.utils.CommandInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs command parsing and substitution
 */
public class Parser {
    
    private final LocalStorage localStorage;
    
    private final Pattern singleQuotes;
    private final Pattern doubleQuotes;
    private final Pattern variables;
    private final Pattern variableAddition;
    private final Pattern flag;
    
    public Parser() {
        localStorage = new LocalStorage();
        singleQuotes = Pattern.compile("'[^']*'");
        doubleQuotes = Pattern.compile("\"[^\"]*\"");
        variables = Pattern.compile("\\$+[^$ ]+ *");
        variableAddition = Pattern.compile("^[^ ]+=[^ ]*");
        flag = Pattern.compile("-{1,2}[^- ]+ *");
    }
    
    /**
     * Checks if the substring has odd or even count of backslashes at the end. <br>
     * Used to check if the symbol which follows after end of substring in the whole string is escaped or not
     *
     * @param substring substring to check
     * @return true if next symbol in the whole string is escaped  <br>
     * false otherwise
     */
    private boolean isEscaped(String substring) {
        return getCountOfBackslashesAtTheEnd(substring) % 2 != 0;
    }
    
    /**
     * Counts the number of \ occurrences at the end of the string
     *
     * @param line string to check
     * @return number of \ occurrences
     */
    private int getCountOfBackslashesAtTheEnd(String line) {
        int countOfBackslashes = 0;
        while (line.endsWith("\\")) {
            countOfBackslashes++;
            line = line.substring(0, line.lastIndexOf("\\"));
        }
        return countOfBackslashes;
    }
    
    /**
     * Replaces all paired backslashes with the single ones e.g. \\ will be replaces with \
     *
     * @param substring string in which need to do replace
     * @return string with all paired backslashes
     */
    private String replaceEvenCountOfBackslashesWithSingles(String substring) {
        int backslashesCount = getCountOfBackslashesAtTheEnd(substring);
        while (substring.endsWith("\\")) {
            substring = substring.substring(0, substring.lastIndexOf("\\"));
        }
        return substring + (backslashesCount != 0 ? "\\".repeat(backslashesCount / 2) : "");
    }
    
    /**
     * Handling a substring before/between/after inverted quotes
     *
     * @param line -- processing string
     * @return processed string
     */
    private List<StringBuilder> substringProcessingWithoutQuotes(String line) {
        List<StringBuilder> potentialCommands = new ArrayList<>();
        String[] piped = line.split("\\|");
        for (int i = 0; i < piped.length; i++) {
            if (isEscaped(piped[i])) {
                String concat;
                if (line.contains("|") && line.charAt(piped[i].length()) == '|') {
                    concat = replaceEvenCountOfBackslashesWithSingles(piped[i]) + "|" + (i + 1 != piped.length ? piped[i + 1] : "");
                    i++;
                } else {
                    concat = replaceEvenCountOfBackslashesWithSingles(piped[i]);
                }
                potentialCommands.add(substitutionVariables(concat));
            } else {
                potentialCommands.add(substitutionVariables(piped[i]));
            }
        }
        return potentialCommands;
    }
    
    /**
     * Removes unnecessary quotes and substitutes variables<p>
     * If no variable is found, substitutes an empty string <br>
     * While not end of string reached do:<br>
     * 1. searches is there any single quotes in the substring <br>
     * 2. searches is there any double quotes in the substring <br>
     * 3. checks different situations if found two types of quotes:  <br>
     * a) " ' ' " <br>
     * b) ' " " ' <br>
     * c) ' " ' " <br>
     * d) " ' " ' <br>
     * e) " " ' ' <br>
     * f) ' ' " " <br>
     * 4. checks different situations if found only one type of quotes: <br>
     * a) if found only " " <br>
     * b) if found only ' ' <br>
     *
     * @param line -- processing string
     * @return substitution string
     */
    public List<StringBuilder> substitutor(String line) {
        line = line.trim();
        Matcher matcherSingleQuotes = singleQuotes.matcher(line);
        Matcher matcherDoubleQuotes = doubleQuotes.matcher(line);
        int startIndexSubstring = 0;
        int startOfSingleQuotes = -1, endOfSingleQuotes = -1;
        int startOfDoubleQuotes = -1, endOfDoubleQuotes = -1;
        int endOfWithoutQuotes;
        int nextStartIndexSubstring;
        List<StringBuilder> potentialCommands = new ArrayList<>();
        boolean isEndsWithPipe = false;
        // marks whether to look for the next occurrence of the pattern
        boolean foundSingle = false, foundDouble = false;
        while (startIndexSubstring != line.length()) {
            String substitution = "";
            // if double quotes are to be searched for
            if (!foundDouble) {
                foundDouble = matcherDoubleQuotes.find(startIndexSubstring);
                startOfDoubleQuotes = foundDouble ? matcherDoubleQuotes.start() : -1;
                endOfDoubleQuotes = foundDouble ? matcherDoubleQuotes.end() : -1;
            }
            
            //if single quotes are to be searched for
            if (!foundSingle) {
                foundSingle = matcherSingleQuotes.find(startIndexSubstring);
                startOfSingleQuotes = foundSingle ? matcherSingleQuotes.start() : -1;
                endOfSingleQuotes = foundSingle ? matcherSingleQuotes.end() : -1;
            }
            
            // both types of quotes are found
            if (foundDouble && foundSingle) {
                // " ' ' " situation
                if (startOfDoubleQuotes < startOfSingleQuotes
                        && endOfDoubleQuotes > endOfSingleQuotes) {
                    endOfWithoutQuotes = startOfDoubleQuotes;
                    // send everything inside the double quotes to substitute variables
                    // the double quotes themselves will be deleted
                    if (endOfDoubleQuotes - startOfDoubleQuotes > 0) {
                        substitution = String.valueOf(substitutionVariables(line.substring(startOfDoubleQuotes + 1, endOfDoubleQuotes - 1)));
                    }
                    nextStartIndexSubstring = endOfDoubleQuotes;
                    // discount all single quotes within double quotes
                    foundSingle = matcherSingleQuotes.find(endOfDoubleQuotes);
                    // mark that the quotes have been processed
                    foundDouble = false;
                }
                // ' " " ' situation
                else if (startOfSingleQuotes < startOfDoubleQuotes
                        && endOfSingleQuotes > endOfDoubleQuotes) {
                    endOfWithoutQuotes = startOfSingleQuotes;
                    substitution = line.substring(startOfSingleQuotes + 1, endOfSingleQuotes - 1);
                    if (endOfSingleQuotes - startOfSingleQuotes > 0) {
                        substitution = line.substring(startOfSingleQuotes + 1, endOfSingleQuotes - 1);
                    }
                    nextStartIndexSubstring = endOfSingleQuotes;
                    // discount all double quotes inside single quotes
                    foundDouble = matcherDoubleQuotes.find(endOfSingleQuotes);
                    // mark that the quotes have been processed
                    foundSingle = false;
                }
                // incorrect quotes, e.g. ' " ' "
                else {
                    // ' " ' "  situation
                    if (startOfSingleQuotes < startOfDoubleQuotes
                            && endOfSingleQuotes < endOfDoubleQuotes) {
                        endOfWithoutQuotes = startOfSingleQuotes;
                        if (endOfSingleQuotes - startOfSingleQuotes > 0) {
                            substitution = line.substring(startOfSingleQuotes + 1, endOfSingleQuotes - 1);
                        }
                        nextStartIndexSubstring = endOfSingleQuotes;
                        // discount all double quotes inside single quotes
                        foundDouble = matcherDoubleQuotes.find(endOfSingleQuotes);
                        // mark that the quotes have been processed
                        foundSingle = false;
                    }
                    // " ' " '
                    else if (startOfDoubleQuotes < startOfSingleQuotes
                            && endOfDoubleQuotes < endOfSingleQuotes) {
                        endOfWithoutQuotes = startOfDoubleQuotes;
                        // send everything inside the double quotes to substitute variables
                        // the double quotes themselves will be deleted
                        if (endOfDoubleQuotes - startOfDoubleQuotes > 0) {
                            substitution = line.substring(startOfDoubleQuotes + 1, endOfDoubleQuotes - 1);
                        }
                        nextStartIndexSubstring = endOfDoubleQuotes;
                        // discount all single quotes within double quotes
                        foundSingle = matcherSingleQuotes.find(endOfDoubleQuotes);
                        // mark that the quotes have been processed
                        foundDouble = false;
                    }
                    // if quotes does not intersect
                    else {
                        // " " ' ' situation
                        if (startOfDoubleQuotes < startOfSingleQuotes) {
                            endOfWithoutQuotes = startOfDoubleQuotes;
                            // send everything inside the double quotes to substitute variables
                            // the double quotes themselves will be deleted
                            if (endOfDoubleQuotes - startOfDoubleQuotes > 0) {
                                substitution = String.valueOf(substitutionVariables(
                                        line.substring(startOfDoubleQuotes + 1, endOfDoubleQuotes - 1)));
                            }
                            nextStartIndexSubstring = endOfDoubleQuotes;
                            // mark that the quotes have been processed
                            foundDouble = false;
                        }
                        // ' ' " "
                        else {
                            endOfWithoutQuotes = startOfSingleQuotes;
                            // add a line in single quotes, the quotes themselves will be cut out
                            if (endOfSingleQuotes - startOfSingleQuotes > 0) {
                                substitution = line.substring(startOfSingleQuotes + 1, endOfSingleQuotes - 1);
                            }
                            nextStartIndexSubstring = endOfSingleQuotes;
                            // mark that the quotes have been processed
                            foundSingle = false;
                        }
                    }
                }
            }
            // if you have found even one type of inverted comma
            else if (foundDouble) {
                endOfWithoutQuotes = startOfDoubleQuotes;
                // send everything inside the double quotes to substitute variables
                // the double quotes themselves will be deleted
                if (endOfDoubleQuotes - startOfDoubleQuotes > 0) {
                    substitution = String.valueOf(substitutionVariables(
                            line.substring(startOfDoubleQuotes + 1, endOfDoubleQuotes - 1)));
                }
                nextStartIndexSubstring = endOfDoubleQuotes;
                // mark that the quotes have been processed
                foundDouble = false;
            } else if (foundSingle) {
                endOfWithoutQuotes = startOfSingleQuotes;
                if (endOfSingleQuotes - startOfSingleQuotes > 0) {
                    substitution = line.substring(startOfSingleQuotes + 1, endOfSingleQuotes - 1);
                }
                nextStartIndexSubstring = endOfSingleQuotes;
                // mark that the quotes have been processed
                foundSingle = false;
            }
            // if there are no pattern entries
            else {
                endOfWithoutQuotes = line.length();
                substitution = "";
                nextStartIndexSubstring = line.length();
            }
            // if there is an unprocessed string between the current pattern found and the previous one
            // we need to substitute variables
            if (endOfWithoutQuotes - startIndexSubstring > 0) {
                String substring = line.substring(startIndexSubstring, endOfWithoutQuotes).replaceAll(" +", " ");
                List<StringBuilder> piped = substringProcessingWithoutQuotes(substring);
                
                if (potentialCommands.size() == 0) {
                    potentialCommands.addAll(piped);
                } else if (!isEndsWithPipe) {
                    String concat = (potentialCommands.get(potentialCommands.size() - 1) + piped.get(0).toString()).trim();
                    potentialCommands.remove(potentialCommands.size() - 1);
                    potentialCommands.add(new StringBuilder(concat));
                    if (piped.size() > 1) {
                        potentialCommands.addAll(piped.subList(1, piped.size()));
                    }
                }
                isEndsWithPipe = substring.endsWith("|");
                if (!isEndsWithPipe) {
                    String concat = (potentialCommands.get(potentialCommands.size() - 1) + substitution);
                    potentialCommands.remove(potentialCommands.size() - 1);
                    potentialCommands.add(new StringBuilder(concat));
                } else if (!Objects.equals(substitution, "")) {
                    potentialCommands.add(new StringBuilder(substitution));
                }
            }
            startIndexSubstring = nextStartIndexSubstring;
        }
        return potentialCommands;
    }
    
    /**
     * Performs variable substitution
     * If no variable is found, an empty string will be substituted for the default
     *
     * @param line -- substitution string
     * @return the line with the substitutions made
     */
    private StringBuilder substitutionVariables(String line) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        Matcher matcherVariables = variables.matcher(line);
        while (matcherVariables.find()) {
            String beforeDollar = line.substring(index, matcherVariables.start());
            if (!isEscaped(beforeDollar)) {
                // add a line before the variable
                result.append(replaceEvenCountOfBackslashesWithSingles(beforeDollar));
                String subline = line.substring(matcherVariables.start(), matcherVariables.end());
                while (subline.startsWith("$$")) {
                    result.append("$$");
                    subline = subline.substring(2);
                }
                if (subline.startsWith("$")) {
                    subline = subline.substring(1);
                    int indexSpace = subline.indexOf(' ');
                    localStorage.get(subline.replaceAll(" +", "")).ifPresent(result::append);
                    if (indexSpace != -1) {
                        result.append(' ');
                    }
                    index = matcherVariables.end();
                } else {
                    index = line.length() - subline.length();
                }
            } else {
                result.append(replaceEvenCountOfBackslashesWithSingles(beforeDollar));
                result.append(line, matcherVariables.start(), matcherVariables.end());
                index = matcherVariables.end();
            }
        }
        if (line.length() - index > 0) {
            String substring = line.substring(index);
            result.append(replaceEvenCountOfBackslashesWithSingles(substring));
        }
        return result;
    }
    
    /**
     * Parses the string into commands
     * <p>
     * If the command is a variable initialisation/reinitialisation, it performs this
     *
     * @param line processing string
     * @return command name, flags and parameters if it is a command
     * and an empty list if it is a variable initialisation/reinitialisation
     */
    public List<CommandInfo> commandParser(String line) {
        List<CommandInfo> commands = new ArrayList<>();
        Matcher matcherVariableAddition = variableAddition.matcher(line);
        if (matcherVariableAddition.find()) {
            int indexEq = line.indexOf("=");
            localStorage.set(line.substring(0, indexEq), line.substring(indexEq + 1));
        } else {
            int index = line.indexOf(" ");
            if (index == -1) {
                commands.add(new CommandInfo(line, new ArrayList<>(), new ArrayList<>()));
            } else {
                String name = line.substring(0, index);
                List<String> flags = new ArrayList<>();
                List<String> param = new ArrayList<>();
                String newLine = line.substring(index + 1);
                if (Checker.checkCommandIsInternal(name)) {
                    Matcher matcherFlag = flag.matcher(newLine);
                    index = 0;
                    while (matcherFlag.find()) {
                        if (matcherFlag.start() - index > 0) {
                            List<String> all = List.of(newLine.substring(index, matcherFlag.start()).split("[ ]+"));
                            for (String s : all) {
                                if (s.length() > 0) {
                                    param.add(s);
                                }
                            }
                        }
                        flags.add(newLine.substring(matcherFlag.start(), matcherFlag.end()).replaceAll(" ", ""));
                        index = matcherFlag.end();
                    }
                    if (newLine.length() - index > 0) {
                        List<String> all = List.of(newLine.substring(index).split("[ ]+"));
                        for (String s : all) {
                            if (s.length() > 0) {
                                param.add(s);
                            }
                        }
                    }
                } else {
                    param.add(newLine);
                }
                commands.add(new CommandInfo(name, flags, param));
            }
        }
        return commands;
    }
    
}
