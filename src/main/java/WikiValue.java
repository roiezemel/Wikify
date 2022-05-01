import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class WikiValue {

    private static final String WIKIPEDIA_URL = "wikipedia.org/wiki/";
    private static final String[] INVALID_URL_ENDINGS = {"Help:IPA/English", "Wikipedia:Citation_needed", ".ogg"};
    private Document document;
    private String valueName;
    private String url;
    private final String languageCode;

    /**
     * Initialize WikiValue with URL to a specific Wikipedia value.
     * @param url URL to a specific Wikipedia value
     * @throws IOException .
     */
    public WikiValue(String url) throws IOException {
        init(url);
        this.languageCode = url
                .replace("https://","").substring(0, url.indexOf("."));
    }

    /**
     * Initialize WikiValue with value name and language code.
     * @param valueName name of the value
     * @param languageCode language code (such as: he, en...).
     * @throws IOException .
     */
    public WikiValue(String valueName, String languageCode) throws IOException {
        init("https://" + languageCode + "." + WIKIPEDIA_URL + valueName);
        this.languageCode = languageCode;
    }

    /**
     * Initialize WikiValue with URL to a specific Wikipedia value.
     * @param url URL to a specific Wikipedia value
     * @throws IOException .
     */
    private void init(String url) throws IOException {
        this.url = url;
        this.document = Jsoup.connect(url).get();
        Element firstHeading = document.getElementById("firstHeading");
        if (firstHeading != null)
            this.valueName = firstHeading.text();
        else
            this.valueName = "";
    }

    /**
     * Get WikiValues references elements.
     * @return List of WikiValues references elements.
     */
    public List<Element> getReferences() {
        List<Element> paragraphs = getParagraphs();
        if (paragraphs == null)
            return null;
        List<Element> references = new ArrayList<>();
        for (Element p : paragraphs) {
            references.addAll(p.getElementsByTag("a")
                    .stream()
                    .filter(e -> !e.attr("href").isEmpty()
                            && e.attr("href").contains("wiki"))
                    .collect(Collectors.toList()));
        }
        return references;
    }

    /**
     * Get list of valid URL references.
     * @return list of valid URL references
     */
    public List<String> getValidUrlReferences() {
        List<String> urls = new ArrayList<>();
        getReferences().forEach(element -> {
            urls.add(validURL(element.attr("href")));
        });
        return urls
                .stream()
                .filter(url -> Arrays.stream(INVALID_URL_ENDINGS).noneMatch(url::endsWith))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get names of referenced WikiValues.
     * @return list of referenced WikiValues
     */
    public List<String> getReferencesNames() {
        return getValidUrlReferences()
                .stream()
                .map(s -> s.substring(s.lastIndexOf("/") + 1))
                .collect(Collectors.toList());
    }

    /**
     * Get WikiValues that are referenced from this one.
     * @param references List of references elements to WikiValues.
     * @return List of WikiValues that are referenced from this WikiValue.
     */
    public List<WikiValue> getReferencedValues(List<Element> references) {
        if (references == null)
            return null;
        List<WikiValue> values = new LinkedList<>();
        List<String> urls = new LinkedList<>();
        references.forEach(ref -> urls.add(ref.attr("href")));
        for (String url : urls.stream().distinct().collect(Collectors.toList())) {
            try {
                values.add(new WikiValue(validURL(url)));
            } catch (IOException | IllegalArgumentException ignored) {}
        }
        return values;
    }

    /**
     * Get the WikiValue's headers elements.
     * @return List of the WikiValue's headers elements
     */
    public List<Element> getHeaders() {
        if (document != null)
            return new ArrayList<>(document.getElementsByTag("h2"));
        return null;
    }

    /**
     * Get the WikiValue's paragraphs elements.
     * @return List of the WikiValue paragraphs elements
     */
    public List<Element> getParagraphs() {
        if (document != null)
            return new ArrayList<>(document.getElementsByTag("p"));
        return null;
    }

    /**
     * Get the WikiValue's name.
     * @return the WikiValue's name
     */
    public String getValueName() {
        return valueName;
    }

    /**
     * Get the WikiValue's url.
     * @return the WikiValue's url
     */
    public String getUrl() {
        return url.replaceAll(" ", "_");
    }

    /**
     * Get valid Wikipedia value URL.
     * @param url URL from Wikipedia's href attribute
     * @return valid URL
     */
    public String validURL(String url) {
        String result = url;
        if (url.startsWith("/wiki"))
            result = "https://" + languageCode + ".wikipedia.org" + url;
        else if (!url.startsWith("https:"))
            result = "https:" + url;
        result = result.replaceAll("\\.wikiped\\.", ".");
        return result;
    }

    /**
     * Get the WikiValue's name.
     * @return the WikiValue's name
     */
    public String toString() {
        return this.valueName;
    }

    /**
     * Get the name of a WikiValue.
     * @param url WikiValue's url
     * @return name of the WikiValue
     */
    public static String getName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

}
