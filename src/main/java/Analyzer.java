
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Analyzer {

    private WikiValue wv1;
    private WikiValue wv2;
    private final List<CommonValue> commonValues;
    protected final Map<String, String> urlToReferences1;
    protected final Map<String, String> urlToReferences2;
    protected final Map<String, String> urlToReferencesBackwards1;
    protected final Map<String, String> urlToReferencesBackwards2;
    private final List<List<String>> paths;

    public Analyzer(WikiValue wv1, WikiValue wv2) {
        this.wv1 = wv1;
        this.wv2 = wv2;
        commonValues = new ArrayList<>();
        urlToReferences1 = new HashMap<>();
        urlToReferences2 = new HashMap<>();
        urlToReferencesBackwards1 = new HashMap<>();
        urlToReferencesBackwards2 = new HashMap<>();
        paths = new ArrayList<>();
    }

    /**
     * Find common WikiValues, find path from wv1 to wv2, and find path from wv2 to wv1.
     * @param lowerLimitReferences minimum number of URL references to check at each step
     * @param upperLimitReferences maximum number of URL references to check at each step
     * @param n number of common values to find
     * @param print whether to print information
     */
    public void analyse(int lowerLimitReferences, int upperLimitReferences, int n, boolean print) {
        if (print)
            System.out.print(Colorize.colors.get("cyan"));
        findCommonValues(lowerLimitReferences, n, print);
        findPath(lowerLimitReferences, upperLimitReferences, false, print);
        findPath(lowerLimitReferences, upperLimitReferences, true, print);
        if (print)
            System.out.println(Colorize.colors.get("reset"));
    }

    /**
     * Find WikiValues that both wv1 and wv2 lead to.
     * @param limitReferences maximum number of URL references to check at each step
     * @param n number of common values to find
     * @param print whether to print information
     * @return a list of CommonValue objects
     */
    public List<CommonValue> findCommonValues(int limitReferences, int n, boolean print) {

        if (print)
            System.out.println("Finding common values");

        WikiValue original1 = wv1;
        WikiValue original2 = wv2;

        List<String> references1;
        List<String> references2;

        Map<String, String> values1 = new HashMap<>();
        Map<String, String> values2 = new HashMap<>();

        List<CommonValue> commonValues = new ArrayList<>();
        Set<String> commonValuesNames = new HashSet<>();

        while (commonValues.size() < n) {

            List<String> savedRefs1 = Saver.load(wv1.getUrl(), urlToReferences1, true);
            List<String> savedRefs2 = Saver.load(wv2.getUrl(), urlToReferences2, true);

            List<String> unsavedRefs1 = wv1.getValidUrlReferences();
            List<String> unsavedRefs2 = wv2.getValidUrlReferences();

            references1 = savedRefs1 == null || savedRefs1.size() < unsavedRefs1.size() ? unsavedRefs1 : savedRefs1;
            references2 = savedRefs2 == null || savedRefs2.size() < unsavedRefs2.size() ? unsavedRefs2 : savedRefs2;

            references1.forEach(url -> {
                values1.put(WikiValue.getName(url), url);
                urlToReferences1.putIfAbsent(url, wv1.getUrl());
                Saver.load(url, urlToReferences1, true);
            });
            references2.forEach(url -> {
                values2.put(WikiValue.getName(url), url);
                urlToReferences2.putIfAbsent(url, wv2.getUrl());
                Saver.load(url, urlToReferences2, true);
            });
            values1.put(WikiValue.getName(wv1.getUrl()), wv1.getUrl());
            values2.put(WikiValue.getName(wv2.getUrl()), wv2.getUrl());

            String commonUrl1 = findCommonUrl(references1, values2, commonValuesNames);
            String commonUrl2 = findCommonUrl(references2, values1, commonValuesNames);

            if (commonUrl1 != null || commonUrl2 != null) {
                CommonValue commonValue = createCommonValue(commonUrl1, commonUrl2, urlToReferences1, urlToReferences2, original1, original2);
                if (!commonValuesNames.contains(commonValue.getCommonName())) {
                    commonValues.add(commonValue);
                    commonValuesNames.add(commonValue.getCommonName());
                }

            }

            if (commonValues.size() < n) {
                Set<String> p1 = new HashSet<>(getInformationFromValue(wv1));
                Set<String> p2 = new HashSet<>(getInformationFromValue(wv2));

                if (print)
                    System.out.println("Comparing " + wv1.getValueName() + " to " + wv2.getValueName());
                WikiValue nextWv1 = mostSimilar(limitList(references1, limitReferences), p2, wv2);
                WikiValue nextWv2 = mostSimilar(limitList(references2, limitReferences), p1, wv1);

                if (nextWv1 == null || nextWv2 == null)
                    return null;

                wv1 = nextWv1;
                wv2 = nextWv2;
            }
        }
        wv1 = original1;
        wv2 = original2;

        if (print)
            System.out.println("Found " + commonValues.size() + " common values\n");
        return commonValues;
    }

    /**
     * Find path from one WikiValue to another
     * @param lowerLimitReferences minimum number of URL references to check at each step
     * @param upperLimitReferences maximum number of URL references to check at each step
     * @param swap whether to swap wv1 and wv2
     * @param print whether to print information
     * @return list of URLs leading from wv1 to wv2
     */
    public List<String> findPath(int lowerLimitReferences, int upperLimitReferences, boolean swap, boolean print) {

        if (print)
            System.out.println("Finding path");
        int limitReferences = lowerLimitReferences;
        WikiValue original1 = swap ? wv2 : wv1;
        WikiValue original2 = swap ? wv1 : wv2;

        WikiValue wv1 = swap ? this.wv2 : this.wv1;
        WikiValue wv2 = swap ? this.wv1 : this.wv2;

        Map<String, String> urlToReferences1 = swap ? this.urlToReferences2 : this.urlToReferences1;
        Map<String, String> urlToReferencesBackwards2 = swap ? this.urlToReferencesBackwards1 : this.urlToReferencesBackwards2;
        urlToReferencesBackwards2.put(wv2.getUrl(), wv2.getUrl());

        Set<String> wv1s = new HashSet<>();
        Set<String> wv2s = new HashSet<>();

        String commonUrl = findCommonUrl(urlToReferences1.keySet(), urlToReferencesBackwards2);

        while (commonUrl == null) {

            if (wv1s.contains(wv1.getUrl()) || wv2s.contains(wv2.getUrl())) {
                if (limitReferences < upperLimitReferences)
                    limitReferences += (upperLimitReferences - lowerLimitReferences) / 2;
                else
                    limitReferences += 5;
            }
            else {
                if (limitReferences - (upperLimitReferences - lowerLimitReferences) / 2 >= lowerLimitReferences)
                    limitReferences -= (upperLimitReferences - lowerLimitReferences) / 2;
                else if (limitReferences > lowerLimitReferences)
                    limitReferences = lowerLimitReferences;
            }

            wv1s.add(wv1.getUrl());
            wv2s.add(wv2.getUrl());

            if (print)
                System.out.println("Comparing " + wv1.getValueName() + " to " + wv2.getValueName() + ", limit references: " + limitReferences);

            List<String> savedRefs1 = Saver.load(wv1.getUrl(), urlToReferences1, true);
            List<String> unsavedRefs1 = wv1.getValidUrlReferences();
            List<String> references1 = savedRefs1 == null || savedRefs1.size() < unsavedRefs1.size() ? unsavedRefs1 : savedRefs1;

            if (limitReferences > upperLimitReferences + 10) {
                references1.removeIf(wv1s::contains);
                limitReferences -= 3;
            }

            List<String> savedRefs2 = Saver.load(wv2.getUrl(), urlToReferencesBackwards2, false);
            List<String> unsavedRefs2 = wv2.getValidUrlReferences();
            List<String> limitedReferences2 = limitList((savedRefs2 == null || savedRefs2.size() < unsavedRefs2.size() ? unsavedRefs2 : savedRefs2), limitReferences);

            for (String url : references1) {
                urlToReferences1.putIfAbsent(url, wv1.getUrl());
                Saver.load(url, urlToReferences1, true);
            }
            urlToReferences1.putIfAbsent(wv1.getUrl(), wv1.getUrl());
            for (String url : limitedReferences2) {
                WikiValue optional = null;
                try {
                    optional = new WikiValue(url);
                } catch (IOException ignored) {}
                String backwardsUrl;
                if (optional != null && (backwardsUrl = leadingBackwardsUrl(optional, wv2s)) != null) {
                    urlToReferencesBackwards2.putIfAbsent(optional.getUrl(), backwardsUrl);
                }
            }

            Set<String> p1 = new HashSet<>(getInformationFromValue(wv1));
            Set<String> p2 = new HashSet<>(getInformationFromValue(wv2));
            WikiValue nextWv1 = mostSimilar(limitList(references1, limitReferences), p2, wv2);
            WikiValue nextWv2 = mostSimilar(limitedReferences2.stream()
                    .filter(urlToReferencesBackwards2::containsKey).collect(Collectors.toList()), p1, wv1);

            if (nextWv1 == null)
                return null;

            if (nextWv2 != null)
                wv2 = nextWv2;

            wv1 = nextWv1;

            commonUrl = findCommonUrl(urlToReferences1.keySet(), urlToReferencesBackwards2);
        }

        List<String> path = createPath(commonUrl, urlToReferences1, false, urlToReferencesBackwards2, true, original1, original2)[0];
        this.paths.add(path);

        if (print)
            System.out.println("Path found\n");
        return path;
    }

//    public List<CommonValue> findLeadingValues(int limitReferences, int n) {
//
//    }

    @SuppressWarnings("unchecked")
    private static List<String>[] createPath(String commonUrl, Map<String, String> urlToReferences1,
                                             boolean backwards1, Map<String, String> urlToReferences2,
                                             boolean backwards2, WikiValue original1, WikiValue original2) {
        if (!backwards1 && !backwards2) {
            List<String> path1 = new ArrayList<>();
            List<String> path2 = new ArrayList<>();
            String url1 = commonUrl;
            String url2 = commonUrl;
            while (urlToReferences1.containsKey(url1) && !url1.equals(original1.getUrl())) {
                path1.add(0, url1);
                url1 = urlToReferences1.get(url1);
            }
            path1.add(0, original1.getUrl());
            while (urlToReferences2.containsKey(url2) && !url2.equals(original2.getUrl())) {
                path2.add(0, url2);
                url2 = urlToReferences2.get(url2);
            }
            path2.add(0, original2.getUrl());
            return new List[] {shortenPath(path1), shortenPath(path2)};
        }
        if (!backwards1) {
            List<String> path = new ArrayList<>();
            String url = urlToReferences1.get(commonUrl);
            while(urlToReferences1.containsKey(url) && !url.equals(original1.getUrl())) {
                path.add(0, url);
                url = urlToReferences1.get(url);
            }
            path.add(0, original1.getUrl());

            url = commonUrl;
            while (urlToReferences2.containsKey(url) && !url.equals(original2.getUrl())) {
                path.add(url);
                url = urlToReferences2.get(url);
            }
            path.add(original2.getUrl());
            return new List[] {shortenPath(path)};
        }
        return null;
    }

    /**
     * Create CommonValue object
     * @param commonUrl1 optional common url 1
     * @param commonUrl2 optional common url 2
     * @param urlToReferences1 Map of URLs to references from wv1
     * @param urlToReferences2 Map or URLs to references from wv2
     * @return a CommonValue object
     */
    private CommonValue createCommonValue(String commonUrl1, String commonUrl2,
                                          Map<String, String> urlToReferences1,
                                          Map<String, String> urlToReferences2,
                                          WikiValue original1, WikiValue original2) {
        String commonUrl = commonUrl1 == null ? commonUrl2 : commonUrl1;

        List<String>[] paths = createPath(commonUrl, urlToReferences1,
                false, urlToReferences2, false, original1, original2);

        CommonValue commonValue = new CommonValue(original1, original2, commonUrl, paths[0], paths[1]);
        commonValues.add(commonValue);
        return commonValue;
    }

    /**
     * Shorten a path if it contains more than one appearance of the same URLs.
     * @param path path of URLs
     * @return shortened path
     */
    private static List<String> shortenPath(List<String> path) {
        Map<String, Integer> urlsToCounts = new HashMap<>();
        path.forEach(url -> {
            if (urlsToCounts.containsKey(url))
                urlsToCounts.put(url, urlsToCounts.get(url) + 1);
            else
                urlsToCounts.put(url, 0);
        });

        for (String url : urlsToCounts.keySet()) {
            if (urlsToCounts.get(url) > 0) {
                System.out.println("shortening " + url);
                List<String> shortenedList = new ArrayList<>();

                int first = path.indexOf(url);
                int last = path.lastIndexOf(url);

                int i = 0;
                for (String pathUrl : path) {
                    if (i < first || i >= last)
                        shortenedList.add(pathUrl);
                    i++;
                }

                path = shortenedList;
            }
        }
        return path;
    }

    /**
     * Check whether the WikiValue is leading to an already visited one.
     * @param optional an optional WikiValue
     * @param wv2s set of visited WikiValues
     * @return the url that the WikiValue is leading to if found, otherwise - null.
     */
    private static String leadingBackwardsUrl(WikiValue optional, Set<String> wv2s) {
        for (String url : optional.getValidUrlReferences())
            if (wv2s.contains(url))
                return url;
        return null;
    }

    /**
     * Limit a list to a given size limit.
     * If the list's size is greater than the size limit, then the list will be shortened.
     * @param l a list
     * @param limit size limit
     * @param <T> any type
     * @return a new list with limited size
     */
    private static <T> List<T> limitList(List<? extends T> l, int limit) {
        List<T> result = new ArrayList<>();
        int i = 0;
        for (T item : l) {
            result.add(item);
            if (result.size() == limit)
                return result;
        }
        return result;
    }

    /**
     * Find a URL that appears both in a references Set and in a Map.
     * @param references list of URLs
     * @param values Map of URLs to any String
     * @return the common String if found, null otherwise
     */
    private static String findCommonUrl(Set<String> references, Map<String, String> values) {
        for (String url : references) {
            if (values.containsKey(url))
                return url;
        }
        return null;
    }

    /**
     * Find a WikiValue's name that appears both in a references Set and in a Map.
     * @param references list of URLs
     * @param values Map of WikiValues' names to their URLs
     * @return the common URL if found, null otherwise
     */
    private static String findCommonUrl(List<String> references, Map<String, String> values, Set<String> foundNames) {
        for (String name : references.stream().map(WikiValue::getName).collect(Collectors.toList())) {
            if (values.containsKey(name) && !foundNames.contains(name))
                return values.get(name);
        }
        return null;
    }

    /**
     * Find the WikiValue that its content is most similar to the content of another WikiValue.
     * @param references list of URLs
     * @param words Set of words from another WikiValue
     * @param wv2 the WikiValue that is being compared to
     * @return the WikiValue which was found to be the most similar to the given words Set
     */
    private static WikiValue mostSimilar(List<String> references, Set<String> words, WikiValue wv2) {
        WikiValue mostSimilar = null;
        int max = -1;
        for (String url : references) {
            WikiValue optional = null;
            try {
                optional = new WikiValue(url);
            } catch (IOException ignored) {}
            if (optional != null) {
                int count = compareValues(optional, words, wv2);
                if (count > max) {
                    mostSimilar = optional;
                    max = count;
                }
            }
        }
        return mostSimilar;
    }

    /**
     * Compare a WikiValue's first paragraph to a set of words.
     * @param optional a WikiValue
     * @param words set of words from another WikiValue
     * @param wv2 the WikiValue that is being compared to
     * @return number of words which appear in both WikiValues' first paragraph
     */
    private static int compareValues(WikiValue optional, Set<String> words, WikiValue wv2) {
        int count = 0;
        for (String url : getInformationFromValue(optional).stream().distinct().collect(Collectors.toList())) {
            if (words.contains(url))
                count++;
            if (url.equals(wv2.getUrl()))
                return 1000;
        }
        return count;
    }

    /**
     * Get information out of a WikiValue.
     * @param value a WikiValue
     * @return list containing part of the WikiValue's information
     */
    private static List<String> getInformationFromValue(WikiValue value) {
//        List<Element> paragraphs = value.getParagraphs();
//        StringBuilder text = new StringBuilder();
////        for (Element p : paragraphs)
////            text.append(p.text()).append(" ");
//        text.append(paragraphs.get(0).text());
//        for (String word : wordsToRemove)
//            text = new StringBuilder(text.toString().replaceAll(word + " ", ""));
//        return text.toString();
        return value.getValidUrlReferences().stream().distinct().collect(Collectors.toList());
    }

    /**
     * Describe Analyzer's results.
     * @return a String describing Analyzer's results
     */
    public String toString() {
        StringBuilder result = new StringBuilder(Colorize.colorized("--------------------\n" +
                "Analyzer results:\n\nCommon Values:", "yellow"));
        for (CommonValue commonValue : commonValues) {
            result.append("\n").append(commonValue.toString().replaceAll("_", " ")).append("\n");
        }
        for (List<String> path : paths) {
            result.append(Colorize.colorized("\nPath from ", "yellow"))
                    .append(Colorize.colorized(WikiValue.getName(path.get(0)).replaceAll("_", " "), "purple"))
                    .append(Colorize.colorized(" to ", "yellow"))
                    .append(Colorize.colorized(WikiValue.getName(path.get(path.size() - 1)).replaceAll("_", " "), "purple"))
                    .append(Colorize.colorized(":", "yellow")).append(Colorize.colorized("\n • ", "green")).append(path
                    .stream()
                    .map(url -> Colorize.colorized(url.substring(url.lastIndexOf("/") + 1), "blue"))
                    .collect(Collectors.joining(" ➔ ")).replaceAll("_", " ")).append("\n");
        }
        result.append(Colorize.colorized("--------------------", "yellow"));
        return result.toString().replaceAll("#", "_");
    }
}
