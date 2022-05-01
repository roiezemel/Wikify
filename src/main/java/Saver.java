import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Saver {

    Analyzer analyzer;

    public Saver(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void save() throws IOException {
        saveUrlToReferences(analyzer.urlToReferences1, true);
        saveUrlToReferences(analyzer.urlToReferences2, true);
        saveUrlToReferences(analyzer.urlToReferencesBackwards1,  false);
        saveUrlToReferences(analyzer.urlToReferencesBackwards2, false);
    }

    public static List<String> load(String value, Map<String, String> urlToReferences, boolean forward) {
        List<String> references = new LinkedList<>();
        File file = new File(getFileName(value, forward));
        if (file.exists()) {
            Scanner sc = null;
            try {
                sc = new Scanner(file);
            }
            catch (FileNotFoundException ignored) {}
            if (sc == null)
                return null;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                urlToReferences.putIfAbsent(line, value);
                references.add(line);
            }
            sc.close();
        }
        return references.isEmpty() ? null : references;
    }

    private static String getFileName(String url, boolean forward) { // 1 - everything that filename leads to, 2 - everything that leads to filename
        return "C:\\Users\\OWNER\\IdeaProjects\\Wikify\\src\\main\\java\\files\\" + (forward ? "1" : "2")
                + WikiValue.getName(url) + ".txt";
    }

    private static Set<String> fileLines(File file) throws FileNotFoundException {
        Scanner sc = new Scanner(file);
        Set<String> lines = new HashSet<>();
        while (sc.hasNextLine())
            lines.add(sc.nextLine());
        sc.close();
        return lines;
    }

    private void saveUrlToReferences(Map<String, String> urlToReferences, boolean forward) throws IOException {
        Map<String, List<String>> toFiles = new HashMap<>();
        urlToReferences.forEach((key, value) -> {
            String name = getFileName(value, forward);
            if (!toFiles.containsKey(name)) {
                toFiles.put(name, new LinkedList<>() {{ add(key); }});
            }
            else
                toFiles.get(name).add(key);
        });
        for (String key : toFiles.keySet()) {
            File file = new File(key);
            if (!file.exists())
                file.createNewFile();
            FileWriter writer = new FileWriter(key, true);
            Set<String> lines = fileLines(file);
            for (String value : toFiles.get(key))
                if (!lines.contains(value))
                    writer.write(value + "\n");
            writer.close();
        }
    }

}
