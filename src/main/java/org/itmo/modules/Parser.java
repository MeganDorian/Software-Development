package org.itmo.modules;

import org.itmo.utils.CommandInfo;

import java.util.ArrayList;
import java.util.List;
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
        variableAddition = Pattern.compile("^[^= ]+=[^ ]*");
        flag = Pattern.compile("-{1,2}[^- ]+ *");
    }
    
    /**
     * Handling a substring before/between/after inverted quotes
     *
     * @param startIndex -- start index of line processing
     * @param endIndex   -- end index of line processing
     * @param line       -- processing string
     * @return processed string
     */
    private StringBuilder substringProcessingWithoutQuotes(int startIndex, int endIndex, String line) {
        StringBuilder result = new StringBuilder();
        if (endIndex - startIndex > 0) {
            //тут перед подстановкой можно добавить обработку на пайпы
            result = substitutionVariables(line.substring(startIndex, endIndex).replaceAll(" +", " "));
        }
        return result;
    }
    
    /**
     * Removes unnecessary quotes and substitutes variables<p>
     * If no variable is found, substitutes an empty string
     * <p>
     * While not end of string reached do:
     * 1. searches is there any single quotes in the substring
     * 2. searches is there any double quotes in the substring
     * 3. checks different situations if found two types of quotes:
     * a) " ' ' "
     * b) ' " " '
     * c) ' " ' "
     * d) " ' " '
     * e) " " ' '
     * f) ' ' " "
     * 4. checks different situations if found only one type of quotes:
     * a) if found only " "
     * b) if found only ' '
     *
     * @param line -- processing string
     * @return substitution string
     */
    public StringBuilder substitutor(String line) {
        line = line.trim();
        StringBuilder result = new StringBuilder();
        Matcher matcherSingleQuotes = singleQuotes.matcher(line);
        Matcher matcherDoubleQuotes = doubleQuotes.matcher(line);
        int startIndexSubstring = 0;
        int startOfSingleQuotes = -1, endOfSingleQuotes = -1;
        int startOfDoubleQuotes = -1, endOfDoubleQuotes = -1;
        int endOfWithoutQuotes;
        int nextStartIndexSubstring;
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
            // we give it to substitute variables
            result.append(substringProcessingWithoutQuotes(startIndexSubstring, endOfWithoutQuotes, line));
            result.append(substitution);
            startIndexSubstring = nextStartIndexSubstring;
        }
        String res = result.toString().replaceAll("\\\\", "");
        result = new StringBuilder(res);
        return result;
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
            String beforeDollarSymbol = line.substring(index, matcherVariables.start());
            int countOfBackslashes = 0;
            while (beforeDollarSymbol.lastIndexOf("\\") != -1) {
                countOfBackslashes++;
                beforeDollarSymbol = beforeDollarSymbol.substring(0, beforeDollarSymbol.lastIndexOf("\\"));
            }
            if (countOfBackslashes % 2 == 0) {
                // add a line before the variable
                result.append(line, index, matcherVariables.start());
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
            }
        }
        if (line.length() - index > 0) {
            result.append(line, index, line.length());
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
