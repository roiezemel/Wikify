
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WikiPaths {

    private static final String[] wordsToRemove = {
            "to", "but", "a", "the", "or", "is", "in", "and", "it", "are"
    };

    public static CommonValue findCommon(WikiValue wv1, WikiValue wv2, int limitReferences) {
        WikiValue original1 = wv1;
        WikiValue original2 = wv2;

        List<String> references1 = wv1.getValidUrlReferences();
        List<String> references2 = wv2.getValidUrlReferences();

        List<String> path1 = new ArrayList<>();
        List<String> path2 = new ArrayList<>();
        path1.add(wv1.getUrl());
        path2.add(wv2.getUrl());

        Map<String, String> values1 = new HashMap<>();
        Map<String, String> values2 = new HashMap<>();
        references1.forEach(url -> values1.put(WikiValue.getName(url), url));
        references2.forEach(url -> values2.put(WikiValue.getName(url), url));
        values1.put(WikiValue.getName(wv1.getUrl()), wv1.getUrl());
        values2.put(WikiValue.getName(wv2.getUrl()), wv2.getUrl());

        Map<String, String> urlToReferences1 = new HashMap<>();
        Map<String, String> urlToReferences2 = new HashMap<>();
        WikiValue finalWv = wv1;
        references1.forEach(url -> urlToReferences1.put(url, finalWv.getUrl()));
        WikiValue finalWv1 = wv2;
        references2.forEach(url -> urlToReferences2.put(url, finalWv1.getUrl()));

        String commonUrl1 = findCommonUrl(references1, values2);
        String commonUrl2 = findCommonUrl(references2, values1);

        while (commonUrl1 == null && commonUrl2 == null) {

            Set<String> p1 = Arrays.stream(getTextFromValue(wv1).split(" ")).collect(Collectors.toSet());
            Set<String> p2 = Arrays.stream(getTextFromValue(wv2).split(" ")).collect(Collectors.toSet());

            System.out.println("Comparing " + wv1.getValueName() + " to " + wv2.getValueName());
            WikiValue nextWv1 = mostSimilar(limitList(references1, limitReferences), p2);
            WikiValue nextWv2 = mostSimilar(limitList(references2, limitReferences), p1);

            if (nextWv1 == null || nextWv2 == null)
                return null;

            wv1 = nextWv1;
            wv2 = nextWv2;

            references1 = wv1.getValidUrlReferences();
            references2 = wv2.getValidUrlReferences();
            path1.add(wv1.getUrl());
            path2.add(wv2.getUrl());
            references1.forEach(url -> {
                values1.put(WikiValue.getName(url), url);
                urlToReferences1.put(url, nextWv1.getUrl());
            });
            references2.forEach(url -> {
                values2.put(WikiValue.getName(url), url);
                urlToReferences2.put(url, nextWv2.getUrl());
            });
            values1.put(WikiValue.getName(wv1.getUrl()), wv1.getUrl());
            values2.put(WikiValue.getName(wv2.getUrl()), wv2.getUrl());

            commonUrl1 = findCommonUrl(references1, values2);
            commonUrl2 = findCommonUrl(references2, values1);
        }

        String commonUrl = commonUrl1 == null ? commonUrl2 : commonUrl1;
        String leadingUrl1 = urlToReferences1.get(commonUrl);
        String leadingUrl2 = urlToReferences2.get(commonUrl);
        List<String> actualPath1 = new ArrayList<>();
        List<String> actualPath2 = new ArrayList<>();
        actualPath1.add(path1.get(0));
        actualPath2.add(path2.get(0));

        int i =  1;
        while (i < path1.size() && !path1.get(i - 1).equals(leadingUrl1)) {
            actualPath1.add(path1.get(i));
            i++;
        }
        i =  1;
        while (i < path2.size() && !path2.get(i - 1).equals(leadingUrl2)) {
            actualPath2.add(path2.get(i));
            i++;
        }
        System.out.println("Found: " + original1.getValueName() + " to " + original2.getValueName() + ": " + commonUrl);
        return new CommonValue(original1, original2, commonUrl, actualPath1, actualPath2);
    }

    public static List<String> findPath(WikiValue wv1, WikiValue wv2, int limitReferences) {

        WikiValue original1 = wv1;
        WikiValue original2 = wv2;

        HashMap<String, String> urlsToReferences1 = new HashMap<>();
        HashMap<String, String> urlsToReferences2 = new HashMap<>();
        urlsToReferences2.put(wv2.getUrl(), wv2.getUrl());

        Set<String> wv1s = new HashSet<>();
        Set<String> wv2s = new HashSet<>();

        List<String> path = new ArrayList<>();
        String commonUrl = findCommonUrl(urlsToReferences1.keySet(), urlsToReferences2);

        while (commonUrl == null) {

            if (wv1s.contains(wv1.getUrl()) || wv2s.contains(wv2.getUrl())) {
                limitReferences += 5;
            }

            wv1s.add(wv1.getUrl());
            wv2s.add(wv1.getUrl());

            System.out.println("Comparing " + wv1.getValueName() + " to " + wv2.getValueName());
            path.add(wv1.getUrl());

            List<String> references1 = wv1.getValidUrlReferences();
            List<String> limitedReferences2 = limitList(wv2.getValidUrlReferences(), limitReferences);

            for (String url : references1)
                urlsToReferences1.put(url, wv1.getUrl());
            urlsToReferences1.put(wv1.getUrl(), wv1.getUrl());
            for (String url : limitedReferences2) {
                WikiValue optional = null;
                try {
                    optional = new WikiValue(url);
                } catch (IOException ignored) {}
                if (optional != null && isUrlLeadingBackwards(optional, wv2s))
                    urlsToReferences2.putIfAbsent(optional.getUrl(), wv2.getUrl());
            }

            Set<String> p1 = Arrays.stream(getTextFromValue(wv1).split(" ")).collect(Collectors.toSet());
            Set<String> p2 = Arrays.stream(getTextFromValue(wv2).split(" ")).collect(Collectors.toSet());
            WikiValue nextWv1 = mostSimilar(limitList(references1, limitReferences), p2);
            WikiValue nextWv2 = mostSimilar(limitedReferences2.stream()
                    .filter(urlsToReferences2::containsKey).collect(Collectors.toList()), p1);

            if (nextWv1 == null)
                return null;

            if (nextWv2 != null)
                wv2 = nextWv2;

            wv1 = nextWv1;

            commonUrl = findCommonUrl(urlsToReferences1.keySet(), urlsToReferences2);
        }

        String pathUrl = urlsToReferences1.get(commonUrl);
        List<String> actualPath = new ArrayList<>();
        int i = 0;
        while (i < path.size() && !path.get(i).equals(pathUrl)) {
            actualPath.add(path.get(i));
            i++;
        }
        actualPath.add(pathUrl);
        if (!commonUrl.equals(pathUrl))
            actualPath.add(commonUrl);

        String url = commonUrl;
        while (urlsToReferences2.containsKey(url) && !url.equals(original2.getUrl())) {
            url = urlsToReferences2.get(url);
            actualPath.add(url);
        }

        return actualPath;
    }

    private static boolean isUrlLeadingBackwards(WikiValue optional, Set<String> wv2s) {
        return optional.getValidUrlReferences().stream().anyMatch(wv2s::contains);
    }

    public static String pathToString(List<String> path) {
        return path.stream().map(url -> url.substring(url.lastIndexOf("/") + 1)).collect(Collectors.joining(" -> "));
    }

    private static <T> List<T> limitList(List<T> l, int limit) {
        List<T> result = new ArrayList<>();
        int i = 0;
        for (T item : l) {
            result.add(item);
            if (result.size() == limit)
                return result;
        }
        return result;
    }

    private static String findCommonUrl(Set<String> references, Map<String, String> values) {
        for (String url : references) {
            if (values.containsKey(url))
                return url;
        }
        return null;
    }

    private static String findCommonUrl(List<String> references, Map<String, String> values) {
        for (String name : references.stream().map(WikiValue::getName).collect(Collectors.toList())) {
            if (values.containsKey(name))
                return values.get(name);
        }
        return null;
    }

    private static WikiValue mostSimilar(List<String> references, Set<String> words) {
        WikiValue mostSimilar = null;
        int max = -1;
        for (String url : references) {
            WikiValue optional = null;
            try {
                optional = new WikiValue(url);
            } catch (IOException ignored) {}
            if (optional != null) {
                int count = compareValues(optional, words);
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
     * @return number of words which appear in both WikiValues' first paragraph
     */
    private static int compareValues(WikiValue optional, Set<String> words) {
        return (int) Arrays.stream(getTextFromValue(optional).split(" ")).filter(words::contains).count();
    }

    private static String getTextFromValue(WikiValue value) {
        List<Element> paragraphs = value.getParagraphs();
        StringBuilder text = new StringBuilder();
        for (Element p : paragraphs)
            text.append(p.text()).append(" ");
        for (String word : wordsToRemove)
            text = new StringBuilder(text.toString().replaceAll(word + " ", ""));
        return text.toString();
    }

//    public static List<WikiValue> findPath(WikiValue wv1, WikiValue wv2, int maxLevels) {
//        List<WikiValue> path = new LinkedList<>();
//        findPath(wv1, 1, wv2, maxLevels, path);
//        return path;
//    }

    private static boolean findPath(WikiValue current, int level, WikiValue target, int maxLevels, List<WikiValue> result) {
        if (current.getValueName().equals(target.getValueName())) {
            result.add(target);
            return true;
        }
        if (level >= maxLevels)
            return false;

        List<Element> references = current.getReferences();
        for (Element reference : references) {
            String url = current.validURL(reference.attr("href"));
            try {
                if (findPath(new WikiValue(url), level + 1, target, maxLevels, result)) {
                    result.add(current);
                    return true;
                }
            } catch (IOException ignored) {}
        }

        return false;
    }

}
