
import java.io.IOException;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {
        WikiValue wv1 = new WikiValue("Fish", "en");
        WikiValue wv2 = new WikiValue("Tibet", "en");
        Analyzer analyzer = new Analyzer(wv1, wv2);

        analyzer.analyse(5, 20, 10, true);
        System.out.println(analyzer);
        Saver saver = new Saver(analyzer);
        saver.save();

//        WikiValue wv1 = new WikiValue("Wind", "en");
//        WikiValue wv2 = new WikiValue("Rubik's cube", "en");
//
//        List<String> result = WikiPaths.findPath(wv1, wv2, 50);
//
//        if (result != null) {
//            System.out.println(WikiPaths.pathToString(result));
//            System.out.println(result);
//        }
//        else
//            System.out.println("Not found...");

    }

}
