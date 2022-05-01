import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class CommonValue {

    private final WikiValue wv1;
    private final WikiValue wv2;
    private final String common;
    private final List<String> path1;
    private final List<String> path2;

    public CommonValue(WikiValue wv1, WikiValue wv2, String common, List<String> namesPath1, List<String> namesPath2) {
        this.wv1 = wv1;
        this.wv2 = wv2;
        this.common = common;
        this.path1 = namesPath1;
        this.path2 = namesPath2;
    }

    public WikiValue getWv1() {
        return wv1;
    }

    public WikiValue getWv2() {
        return wv2;
    }

    public String getCommon() {
        return common;
    }

    public List<String> getPath1() {
        return path1;
    }

    public List<String> getPath2() {
        return path2;
    }

    public String getCommonName() {
        return common.substring(common.lastIndexOf("/") + 1);
    }

    public List<String> getNamesPath1() {
        return toNames(path1);
    }

    public List<String> getNamesPath2() {
        return toNames(path2);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(
                Colorize.colorized(" • Found Common Value: ", "green")
                + Colorize.colorized(getCommonName(), "purple", true)
                + "," + Colorize.colorized(" Url: ", "green") + Colorize.colorized(getCommon().replaceAll("_", "#"), "blue")
                        + "\n" + Colorize.colorized(" • Path from ", "green")
                        + Colorize.colorized(wv1.getValueName() + ": ", "purple"));
        List<String> namesPath1 = getNamesPath1();
        List<String> namesPath2 = getNamesPath2();
        for (String name : namesPath1.subList(0, namesPath1.size() - 1))
            result.append(Colorize.colorized(name, "blue")).append(" ➔ ");
        result.append(Colorize.colorized(namesPath1.get(namesPath1.size() - 1), "blue"));

        result.append(Colorize.colorized("\n • Path from ", "green")).append(Colorize.colorized(wv2.getValueName() + ": ", "purple"));
        for (String name : namesPath2.subList(0, namesPath2.size() - 1))
            result.append(Colorize.colorized(name, "blue")).append(" ➔ ");
        result.append(Colorize.colorized(namesPath2.get(namesPath2.size() - 1), "blue"));

        return java.net.URLDecoder.decode(result.toString(), StandardCharsets.UTF_8);
    }

    private static List<String> toNames(List<String> urls) {
        return urls.stream().map(WikiValue::getName).collect(Collectors.toList());
    }
}
