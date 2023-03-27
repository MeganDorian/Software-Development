package org.itmo.modules.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.itmo.commands.Command;
import org.itmo.modules.LocalStorage;
import org.itmo.utils.Pair;

/**
 * Performs command parsing and substitution
 */
public class Parser {
    
    private final LocalStorage localStorage;
    private final Pattern variables;
    
    private final Pair<Integer> singleQuotesIndexes;
    private final Pair<Integer> doubleQuotesIndexes;
    
    /**
     * marks whether to look for the next occurrence of the pattern <br> first - found single
     * quotes
     * <br> second - found double quotes
     */
    private final Pair<Boolean> quotesFlags;
    
    /**
     * store indexes where to need to continue parsing <br> first - index of last found quote
     * (double or single) <br> second - next start index to search next quotes in the string
     */
    private final Pair<Integer> toSearchIndexes;
    
    public Parser() {
        localStorage = new LocalStorage();
        variables = Pattern.compile("\\$+[^$ ]+ *");
        doubleQuotesIndexes = new Pair<>(-1, -1);
        singleQuotesIndexes = new Pair<>(-1, -1);
        toSearchIndexes = new Pair<>(-1, -1);
        quotesFlags = new Pair<>(false, false);
    }
    
    /**
     * Searches the string for the first unescaped quotes of the predefined type starting from the
     * specified index
     * <p>
     *
     * @param line           -- search string
     * @param indexOfQuotes  -- a starting index for a search
     * @param typeOfQuotes   -- a structure for finding an index
     * @param startSubstring -- starting index for a search
     *
     * @return <true> -- if an unescaped character was found, <false> -- otherwise
     */
    private boolean findQuotes(String line, Pair<Integer> indexOfQuotes, char typeOfQuotes,
                               int startSubstring) {
        Pair<Integer> forSearch = new Pair<>(startSubstring, startSubstring - 1);
        boolean isFindFirstQuotes = searchFirstUnescapedCharacter(forSearch, typeOfQuotes, line);
        indexOfQuotes.first = forSearch.second;
        boolean isFindSecondQuotes;
        isFindSecondQuotes =
            isFindFirstQuotes && searchFirstUnescapedCharacter(forSearch, typeOfQuotes, line);
        indexOfQuotes.second = forSearch.second + 1;
        return isFindFirstQuotes && isFindSecondQuotes;
    }
    
    /**
     * Searches the first unescaped transmitted character in a string starting at the specified
     * index
     * <p>
     *
     * @param forSearch -- a structure for storing and returning search indexes NB! the second index
     *                  of the structure at the start will be assigned to the first index with the
     *                  addition of one - this is the index from which the search will be performed
     * @param symbol    -- search symbol
     * @param line      -- search string
     *
     * @return <true> -- if an unshielded character was found, <false> -- otherwise
     * the second index of the "forSearch" structure corresponds to the index of the unshielded
     * character. minus one -- the character was not found
     */
    private boolean searchFirstUnescapedCharacter(Pair<Integer> forSearch, char symbol,
                                                  String line) {
        boolean isFind;
        do {
            forSearch.first = forSearch.second + 1;
            forSearch.second = line.substring(forSearch.first).indexOf(symbol);
            if (forSearch.second != -1) {
                forSearch.second += forSearch.first;
                isFind = !isEscaped(line.substring(forSearch.first, forSearch.second));
            } else {
                isFind = true;
            }
        } while (!isFind);
        return forSearch.second != -1;
    }
    
    /**
     * Checks if the substring has odd or even count of backslashes at the end. <br> Used to check
     * if the symbol which follows after end of substring in the whole string is escaped or not
     *
     * @param substring substring to check
     *
     * @return true if next symbol in the whole string is escaped  <br> false otherwise
     */
    private boolean isEscaped(String substring) {
        return getCountOfBackslashesAtTheEnd(substring) % 2 != 0;
    }
    
    /**
     * Counts the number of \ occurrences at the end of the string
     *
     * @param line string to check
     *
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
     *
     * @return string with all paired backslashes
     */
    private String replaceEvenCountOfBackslashesWithSingles(String substring) {
        int backslashesCount = getCountOfBackslashesAtTheEnd(substring);
        while (substring.endsWith("\\")) {
            substring = substring.substring(0, substring.lastIndexOf("\\"));
        }
        return substring + (backslashesCount != 0 ?
                            String.join("\\", Collections.nCopies(backslashesCount / 2, "\\")) :
                            "");
    }
    
    /**
     * Removes the escaped backspace in front of special characters such as - ' - " - $ - |
     *
     * @param line -- processing string
     *
     * @return processed string
     */
    private String removingEscapingSlashForSpecialCharacters(String line) {
        return line.replaceAll("\\\\'", "'").replaceAll("\\\\\"", "\"").replaceAll("\\\\\\$", "\\$")
                   .replaceAll("\\\\\\|", "|");
    }
    
    /**
     * Replaces all double backspaces with single backspaces, also handles escaping wildcards
     *
     * @param line -- processing string
     *
     * @return processed string
     */
    private String removeUnnecessaryBackspaces(String line) {
        StringBuilder result = new StringBuilder();
        int index;
        do {
            index = line.indexOf("\\\\");
            if (index != -1) {
                result.append(removingEscapingSlashForSpecialCharacters(line.substring(0, index)));
                int indexStrartFind = index;
                do {
                    result.append("\\");
                    indexStrartFind += 2;
                } while (line.startsWith("\\\\", indexStrartFind));
                line = line.substring(indexStrartFind);
            }
        } while (index != -1);
        if (line.length() != 0) {
            result.append(removingEscapingSlashForSpecialCharacters(line));
        }
        return result.toString();
    }
    
    /**
     * Handling a substring before/between/after inverted quotes
     *
     * @param line -- processing string
     *
     * @return processed string
     */
    private List<StringBuilder> substringProcessingWithoutQuotes(String line) {
        List<StringBuilder> potentialCommands = new ArrayList<>();
        String[] piped = line.split("\\|");
        for (int i = 0; i < piped.length; i++) {
            if (isEscaped(piped[i])) {
                String concat;
                if (line.contains("|") && line.charAt(piped[i].length()) == '|') {
                    concat = replaceEvenCountOfBackslashesWithSingles(piped[i]) + "|" +
                             (i + 1 != piped.length ? piped[i + 1] : "");
                    i++;
                } else {
                    concat = replaceEvenCountOfBackslashesWithSingles(piped[i]);
                }
                potentialCommands.add(substitutionVariables(concat, true));
            } else {
                potentialCommands.add(substitutionVariables(piped[i], true));
            }
        }
        return potentialCommands;
    }
    
    /**
     * Processes a substring between double quotes
     *
     * @param line -- processing string
     *
     * @return processed substring
     */
    private Optional<String> doubleQuotesProcess(String line) {
        toSearchIndexes.first = doubleQuotesIndexes.first;
        toSearchIndexes.second = doubleQuotesIndexes.second;
        // mark that the quotes have been processed
        quotesFlags.second = false;
        // send everything inside the double quotes to substitute variables
        // the double quotes themselves will be deleted
        return doubleQuotesIndexes.second - doubleQuotesIndexes.first > 0 ?
               Optional.of(String.valueOf(substitutionVariables(line, false))) : Optional.empty();
    }
    
    /**
     * Processes a substring between single quotes
     *
     * @param line -- processing string
     *
     * @return processed substring
     */
    private Optional<String> singleQuotesProcess(String line) {
        toSearchIndexes.first = singleQuotesIndexes.first;
        toSearchIndexes.second = singleQuotesIndexes.second;
        quotesFlags.first = false;
        return singleQuotesIndexes.second - singleQuotesIndexes.first > 0 ? Optional.of(
            line.substring(singleQuotesIndexes.first + 1, singleQuotesIndexes.second - 1)) :
               Optional.empty();
    }
    
    /**
     * Removes unnecessary quotes and substitutes variables
     * <p>
     * If no variable is found, substitutes an empty string <br> While not end of string reached
     * do:
     * <p>
     * 1. searches is there any single quotes in the substring 2. searches is there any double
     * quotes in the substring 3. checks different situations if found two types of quotes:
     * <p>
     * a) " ' ' "
     * <p>
     * b) ' " " '
     * <p>
     * <p>
     * c) ' " ' "
     * <p>
     * d) " ' " '
     * <p>
     * e) " " ' '
     * <p>
     * f) ' ' " "
     * <p>
     * 4. checks different situations if found only one type of quotes:
     * <p>
     * a) if found only " "
     * <p>
     * b) if found only ' '
     *
     * @param line -- processing string
     *
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
                if (doubleQuotesIndexes.first < singleQuotesIndexes.first &&
                    doubleQuotesIndexes.second > singleQuotesIndexes.second) {
                    substitution = doubleQuotesProcess(line.substring(doubleQuotesIndexes.first + 1,
                                                                      doubleQuotesIndexes.second -
                                                                      1));
                    // discount all single quotes within double quotes
                    quotesFlags.first =
                        findQuotes(line, singleQuotesIndexes, '\'', doubleQuotesIndexes.second);
                } else if ((singleQuotesIndexes.first < doubleQuotesIndexes.first &&
                            singleQuotesIndexes.second >
                            doubleQuotesIndexes.second) /* ' " " ' situation */ ||
                           (singleQuotesIndexes.first <
                            doubleQuotesIndexes.first /* ' " ' "  situation */ &&
                            singleQuotesIndexes.second < doubleQuotesIndexes.second)) {
                    substitution = singleQuotesProcess(line);
                    // discount all double quotes inside single quotes
                    quotesFlags.second =
                        findQuotes(line, doubleQuotesIndexes, '\"', singleQuotesIndexes.second);
                }
                // incorrect quotes
                else {
                    // " ' " '
                    if (doubleQuotesIndexes.first < singleQuotesIndexes.first &&
                        doubleQuotesIndexes.second < singleQuotesIndexes.second) {
                        toSearchIndexes.first = doubleQuotesIndexes.first;
                        toSearchIndexes.second = doubleQuotesIndexes.second;
                        if (doubleQuotesIndexes.second - doubleQuotesIndexes.first > 0) {
                            substitution = Optional.of(line.substring(doubleQuotesIndexes.first + 1,
                                                                      doubleQuotesIndexes.second -
                                                                      1));
                        }
                        // discount all single quotes within double quotes
                        quotesFlags.first =
                            findQuotes(line, singleQuotesIndexes, '\'', doubleQuotesIndexes.second);
                        // mark that the quotes have been processed
                        quotesFlags.second = false;
                    }
                    // if quotes does not intersect
                    else {
                        substitution = doubleQuotesIndexes.first <
                                       singleQuotesIndexes.first ? /* " " ' ' situation */
                                       doubleQuotesProcess(
                                           line.substring(doubleQuotesIndexes.first + 1,
                                                          doubleQuotesIndexes.second - 1)) :
                                       singleQuotesProcess(line); // ' ' " " situation
                    }
                }
            }
            // only double quotes found
            else if (quotesFlags.second) {
                substitution = doubleQuotesProcess(
                    line.substring(doubleQuotesIndexes.first + 1, doubleQuotesIndexes.second - 1));
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
                String substring =
                    line.substring(startSubstring, toSearchIndexes.first).replaceAll(" +", " ");
                List<StringBuilder> piped = substringProcessingWithoutQuotes(substring);
                
                if (potentialCommands.size() == 0) {
                    potentialCommands.addAll(piped);
                } else if (!isEndsWithPipe) {
                    String concat = (potentialCommands.get(potentialCommands.size() - 1) +
                                     piped.get(0).toString()).trim();
                    potentialCommands.remove(potentialCommands.size() - 1);
                    potentialCommands.add(new StringBuilder(concat));
                    if (piped.size() > 1) {
                        potentialCommands.addAll(piped.subList(1, piped.size()));
                    }
                }
                isEndsWithPipe = substring.endsWith("|");
                if (!isEndsWithPipe) {
                    String concat = potentialCommands.get(potentialCommands.size() - 1) +
                                    substitution.orElse("");
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
     * Get the list of commands and constructs the list of Command object with mapped flags,
     * parameters etc.
     *
     * @param parsedCommands list if parsed commands
     *
     * @return list of mapped Commands
     */
    public List<Command> commandParser(List<String> parsedCommands) {
        return new CommandParser(parsedCommands, localStorage).commandParser();
    }
    
    /**
     * Performs variable substitution If no variable is found, an empty string will be substituted
     * for the default
     *
     * @param line           -- substitution string
     * @param isDoubleQuotes -- <true> -- if the substring is enclosed in double quotes,
     *                       <false> -- otherwise
     *                       This is necessary to understand whether double backspace cases need to
     *                       be handled.
     *
     * @return the line with the substitutions made
     */
    private StringBuilder substitutionVariables(String line, boolean isDoubleQuotes) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        Matcher matcherVariables = variables.matcher(line);
        while (matcherVariables.find()) {
            String beforeDollar = line.substring(index, matcherVariables.start());
            if (!isEscaped(beforeDollar)) {
                // add a line before the variable
                if (isDoubleQuotes) {
                    result.append(removeUnnecessaryBackspaces(beforeDollar));
                } else {
                    result.append(beforeDollar);
                }
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
                if (isDoubleQuotes) {
                    result.append(
                        removeUnnecessaryBackspaces(line.substring(index, matcherVariables.end())));
                } else {
                    result.append(line.substring(index, matcherVariables.end()));
                }
                index = matcherVariables.end();
            }
        }
        if (line.length() - index > 0) {
            String substring = line.substring(index);
            if (isDoubleQuotes) {
                result.append(removeUnnecessaryBackspaces(substring));
            } else {
                result.append(substring);
            }
        }
        return result;
    }
    
}
