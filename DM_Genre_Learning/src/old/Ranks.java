package old;

import static old.IndexEncoder.patternGenre;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ldtwo
 */
public class Ranks {
 static double GOOD_PERCENTAGE = .9;
    final static String pQuote = "[\"].*[\"]";
    final static String pTitle = "(^[^ \t\n\r].+)";
    final static String pParenYear = "[/(]([0-9]{4,4})[/)]";
    final static String pNum = "[0-9]+";
    final static String pWs = "[ \r\n\t]+";
    final static String strPattern = String.format("%s .*%s .*%s .*%s .*([0-9]+).*[ \t]+(.+)",
            "([0-9]+[.]?[0-9]*)", "([0-9]+[.]?[0-9]*)", "([0-9]+[.]?[0-9]*)", "([0-9]+[.]?[0-9]*)"
    );
    final static Pattern pattern = Pattern.compile(strPattern);

    public static void mergeRanks(File input, HashMap<String, Item> words) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
        String line;
        Matcher m;
        System.out.println("mergeRanks: " + input);
        Item item;
        String key;
        while ((line = br.readLine()) != null) {

            m = pattern.matcher(line);
            if (m.find()) {
                try {
                    key = m.group(6).trim();
                    item = words.get(key);
                    if (item == null) {
                        item = new Item(key);
                        words.put(key, item);
                    }
                    item.merit += (1 + Double.parseDouble(m.group(1)));
                    item.rank += Double.parseDouble(m.group(3));
                    item.cnt++;
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.err.format("\n\n%s\nTitle:\t%s\nYear:\t%s\n", m.group(0), m.group(1), m.group(2));
//                    for (int i = 0; i <= m.groupCount(); i++) {
//                        if (m.group(i) != null) {
//                            System.err.format("Group %s: \t%s\n", i, m.group(i));
//                        }
//                    }
                }
            }
        } System.out.format("words %s\n", words.size());
        br.close();
    }

    public static void main(String[] s) throws Exception {
        for (Genre g : Genre.values()) {
            write(g);
        }

    }

    static public void write(Genre genre) throws Exception {
        File[] files = new File("d:\\").listFiles();
        HashMap<String, Item> words = new HashMap<String, Item>();
        for (File f : files) {
            if (!f.getName().toLowerCase().startsWith(genre.name().toLowerCase() + ".")) {
                continue;
            }
            mergeRanks(f, words);
        }
        if (words.size() < 1) {
            return;
        }
        Item[] items = words.values().toArray(new Item[0]);
        Arrays.sort(items, new Comparator<Item>() {

            public int compare(Item o1, Item o2) {
                return o1.compareTo(o2);
            }
        });
        Item item;
        String[] keys = words.keySet().toArray(new String[0]);
        for (String k : keys) {
            item = words.get(k);
            if (item.merit < items[(int)(items.length *GOOD_PERCENTAGE-1)].merit ) {
                words.remove(k);
            }
        }
        for (Genre g : Genre.values()) {
            words.remove(g.name());
        }
//        for (String k : keys) {
//            item=words.get(k);
//            if(item.merit<1||item.cnt<3)words.remove(k);
//        }
        FileOutputStream os = new FileOutputStream("d:\\keywords." + genre.name(), false);
        items = words.values().toArray(new Item[0]);
        Arrays.sort(items, new Comparator<Item>() {

            public int compare(Item o1, Item o2) {
                return o1.compareTo(o2);
            }
        });
        for (Item i : items) {
            System.out.println(i);
            os.write(i.name.getBytes());
            os.write("\n".getBytes());
        }

    }
    static public HashMap<String, Item> filter(Genre genre) throws Exception {
        File[] files = new File("d:\\").listFiles();
        HashMap<String, Item> words = new HashMap<String, Item>();
        for (File f : files) {
            if (!f.getName().toLowerCase().startsWith(genre.name().toLowerCase() + ".")) {
                continue;
            }
            mergeRanks(f, words);
        }
        if (words.size() < 1) {
            return words;
        }
        Item[] items = words.values().toArray(new Item[0]);
        Arrays.sort(items, new Comparator<Item>() {

            public int compare(Item o1, Item o2) {
                return o1.compareTo(o2);
            }
        });
        Item item;
        String[] keys = words.keySet().toArray(new String[0]);
        for (String k : keys) {
            item = words.get(k);
            if (item.merit < items[(int)(items.length *GOOD_PERCENTAGE-1)].merit ) {
                words.remove(k);
            }
        }
        for (Genre g : Genre.values()) {
            words.remove(g.name());
        }
//        for (String k : keys) {
//            item=words.get(k);
//            if(item.merit<1||item.cnt<3)words.remove(k);
//        }
//        FileOutputStream os = new FileOutputStream("d:\\keywords." + genre.name(), false);
//        items = words.values().toArray(new Item[0]);
//        Arrays.sort(items, new Comparator<Item>() {
//
//            public int compare(Item o1, Item o2) {
//                return o1.compareTo(o2);
//            }
//        });
//        for (Item i : items) {
//            System.out.println(i);
//            os.write(i.name.getBytes());
//            os.write("\n".getBytes());
//        }
            return words;

    }
}
