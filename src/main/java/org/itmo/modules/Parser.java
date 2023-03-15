package org.itmo.modules;

import org.itmo.utils.CommandInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs command parsing and substitution
 */
public class Parser {
    
    private final LocalStorage localStorage;
    private final Pattern variables;
    private final Pattern variableAddition;
    private final Pattern flag;
    
    private final Pair<Integer> singleQuotesIndexes;
    private final Pair<Integer> doubleQuotesIndexes;
    
    /**
     * marks whether to look for the next occurrence of the pattern <br>
     * first - found single quotes <br>
     * second - found double quotes
     */
    private final Pair<Boolean> quotesFlags;
    
    /**
     * store indexes where to need to continue parsing <br>
     * first - index of last found quote (double or single) <br>
     * second - next start index to search next quotes in the string
     */
    private final Pair<Integer> toSearchIndexes;
    
    private static class Pair<T> {
        private T first;
        private T second;
        
        Pair(T first, T second) {
            this.first = first;
            this.second = second;
        }
    }
    
    public Parser() {
        localStorage = new LocalStorage();
        variables = Pattern.compile("\\$+[^$ ]+ *");
        variableAddition = Pattern.compile("^[^ ]+=[^ ]*");
        flag = Pattern.compile("-{1,2}[^- ]+ *");
        doubleQuotesIndexes = new Pair<>(-1, -1);
        singleQuotesIndexes = new Pair<>(-1, -1);
        toSearchIndexes = new Pair<>(-1, -1);
        quotesFlags = new Pair<>(false, false);
    }
    
    /**
     *
     * @param line
     * @param indexOfQuotes
     * @param typeOfQuotes
     * @param startSubstring
     * @return
     */
    public boolean findQuotes(String line, Pair<Integer> indexOfQuotes, char typeOfQuotes, int startSubstring) {
        Pair<Integer> forSearch = new Pair<>(startSubstring, startSubstring - 1);
        boolean isFindFirstQuotes = searchFirstNoEscapedCharacter(forSearch, typeOfQuotes, line);
        indexOfQuotes.first = forSearch.second;
        boolean isFindSecondQuotes;
        isFindSecondQuotes = isFindFirstQuotes && searchFirstNoEscapedCharacter(forSearch, typeOfQuotes, line);
        indexOfQuotes.second = forSearch.second + 1;
        return isFindFirstQuotes && isFindSecondQuotes;
    }
    
    /**
     *
     * @param forSearch
     * @param symbol
     * @param line
     * @return
     */
    public boolean searchFirstNoEscapedCharacter (Pair<Integer> forSearch, char symbol, String line) {
        boolean isFind;
        do {
            forSearch.first = forSearch.second + 1;
            forSearch.second = line.substring(forSearch.first).indexOf(symbol);
            if(forSearch.second != -1) {
                forSearch.second += forSearch.first;
                isFind = !isEscaped(line.substring(forSearch.first, forSearch.second));
            } else {
                isFind = true;
            }
        } while (!isFind);
        return forSearch.second != -1;
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
        return substring + (backslashesCount != 0 ?
                String.join("\\", Collections.nCopies(backslashesCount / 2, "\\")) : "");
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
    
    private Optional<String> doubleQuotesProcess(String line) {
        toSearchIndexes.first = doubleQuotesIndexes.first;
        toSearchIndexes.second = doubleQuotesIndexes.second;
        // mark that the quotes have been processed
        quotesFlags.second = false;
        // send everything inside the double quotes to substitute variables
        // the double quotes themselves will be deleted
        return doubleQuotesIndexes.second - doubleQuotesIndexes.first > 0 ?
                Optional.of(String.valueOf(substitutionVariables(line))) : Optional.empty();
    }
    
    private Optional<String> singleQuotesProcess(String line) {
        toSearchIndexes.first = singleQuotesIndexes.first;
        toSearchIndexes.second = singleQuotesIndexes.second;
        quotesFlags.first = false;
        return singleQuotesIndexes.second - singleQuotesIndexes.first > 0 ?
                Optional.of(line.substring(singleQuotesIndexes.first + 1, singleQuotesIndexes.second - 1)) : Optional.empty();
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
    public List<String> substitutor(String line) {
        line = line.trim();
        int startSubstring = 0;
        List<StringBuilder> potentialCommands = new ArrayList<>();
        boolean isEndsWithPipe = false;
        Optional<String> substitution;
        while (startSubstring != line.length()) {
            substitution = Optional.empty();
            // if double quotes are to be searched for
            if (!quotesFlags.second) {
                quotesFlags.second = findQuotes(line, doubleQuotesIndexes, '\"', startSubstring);
            }
            
            //if single quotes are to be searched for
            if (!quotesFlags.first) {
                quotesFlags.first = findQuotes(line, singleQuotesIndexes, '\'', startSubstring);
            }
            
            // both types of quotes are found
            if (quotesFlags.second && quotesFlags.first) {
                // " ' ' " situation
                if (doubleQuotesIndexes.first < singleQuotesIndexes.first
                        && doubleQuotesIndexes.second > singleQuotesIndexes.second) {
                    substitution = doubleQuotesProcess(line.substring(doubleQuotesIndexes.first + 1, doubleQuotesIndexes.second - 1));
                    // discount all single quotes within double quotes
                    quotesFlags.first = quotesFlags.first = findQuotes(line, singleQuotesIndexes, '\'', doubleQuotesIndexes.second);;
                } else if ((singleQuotesIndexes.first < doubleQuotesIndexes.first
                        && singleQuotesIndexes.second > doubleQuotesIndexes.second) /* ' " " ' situation */
                        ||
                        (singleQuotesIndexes.first < doubleQuotesIndexes.first /* ' " ' "  situation */
                                && singleQuotesIndexes.second < doubleQuotesIndexes.second)) {
                    substitution = singleQuotesProcess(line);
                    // discount all double quotes inside single quotes
                    quotesFlags.second = findQuotes(line, doubleQuotesIndexes, '\"', singleQuotesIndexes.second);
                }
                // incorrect quotes
                else {
                    // " ' " '
                    if (doubleQuotesIndexes.first < singleQuotesIndexes.first
                            && doubleQuotesIndexes.second < singleQuotesIndexes.second) {
                        toSearchIndexes.first = doubleQuotesIndexes.first;
                        toSearchIndexes.second = doubleQuotesIndexes.second;
                        if (doubleQuotesIndexes.second - doubleQuotesIndexes.first > 0) {
                            substitution = Optional.of(line.substring(doubleQuotesIndexes.first + 1, doubleQuotesIndexes.second - 1));
                        }
                        // discount all single quotes within double quotes
                        quotesFlags.first = findQuotes(line, singleQuotesIndexes, '\'', doubleQuotesIndexes.second);
                        // mark that the quotes have been processed
                        quotesFlags.second = false;
                    }
                    // if quotes does not intersect
                    else {
                        substitution = doubleQuotesIndexes.first < singleQuotesIndexes.first ? /* " " ' ' situation */
                                doubleQuotesProcess(line.substring(doubleQuotesIndexes.first + 1, doubleQuotesIndexes.second - 1))
                                :
                                singleQuotesProcess(line); // ' ' " " situation
                    }
                }
            }
            // only double quotes found
            else if (quotesFlags.second) {
                substitution = doubleQuotesProcess(line.substring(doubleQuotesIndexes.first + 1, doubleQuotesIndexes.second - 1));
            }
            // only single quotes found
            else if (quotesFlags.first) {
                substitution = singleQuotesProcess(line);
            }
            // no quotes found
            else {
                toSearchIndexes.first = toSearchIndexes.second = line.length();
                substitution = Optional.empty();
            }
            // if there is an unprocessed string between the current pattern found and the previous one
            // we need to substitute variables
            if (toSearchIndexes.first - startSubstring > 0) {
                String substring = line.substring(startSubstring, toSearchIndexes.first).replaceAll(" +", " ");
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
                    String concat = potentialCommands.get(potentialCommands.size() - 1) + substitution.orElse("");
                    potentialCommands.remove(potentialCommands.size() - 1);
                    potentialCommands.add(new StringBuilder(concat));
                } else {
                    substitution.ifPresent(s -> potentialCommands.add(new StringBuilder(s)));
                }
            }
            startSubstring = toSearchIndexes.second;
        }
        List<String> parsedCommand = new ArrayList<>();
        potentialCommands.forEach(p -> parsedCommand.add(p.toString().trim()));
        return parsedCommand;
    }
    
    /**
     * Parses the string into commands
     * <p>
     * If the command is a variable initialisation/reinitialisation, it performs this
     *
     * @param parsedCommands list of parsed commands
     * @return command name, flags and parameters if it is a command
     * and an empty list if it is a variable initialisation/reinitialisation
     */
    public List<CommandInfo> commandParser(List<String> parsedCommands) {
        List<CommandInfo> commands = new ArrayList<>();
        for (String parsedCommand : parsedCommands) {
            Matcher matcherVariableAddition = variableAddition.matcher(parsedCommand);
            if (matcherVariableAddition.find()) {
                int indexEq = parsedCommand.indexOf("=");
                localStorage.set(parsedCommand.substring(0, indexEq), parsedCommand.substring(indexEq + 1));
            } else {
                int index = parsedCommand.indexOf(" ");
                if (index == -1) {
                    commands.add(new CommandInfo(parsedCommand, new ArrayList<>(), new ArrayList<>()));
                } else {
                    String name = parsedCommand.substring(0, index);
                    List<String> flags = new ArrayList<>();
                    List<String> param = new ArrayList<>();
                    String newLine = parsedCommand.substring(index + 1);
                    if (Checker.checkCommandIsInternal(name)) {
                        Matcher matcherFlag = flag.matcher(newLine);
                        index = 0;
                        while (matcherFlag.find()) {
                            if (matcherFlag.start() - index > 0) {
                                List<String> all = List.of(newLine.substring(index, matcherFlag.start()).split(" +"));
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
                            List<String> all = List.of(newLine.substring(index).split(" +"));
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
            
        }
        return commands;
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
    
}
