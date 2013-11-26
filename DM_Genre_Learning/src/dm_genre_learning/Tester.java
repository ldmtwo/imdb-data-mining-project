package dm_genre_learning;

import static dm_genre_learning.Genre.*;
import static dm_genre_learning.Miner.path;
import static dm_genre_learning.Movie.GLOB_genre_set;
import static dm_genre_learning.Ranks.GOOD_PERCENTAGE;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import static java.lang.Math.*;
import java.util.Arrays;

/**
 *
 * @author ldtwo
 */
public class Tester extends Miner {

    static final boolean testing = true;
    static final boolean randomize = true;
    public static final boolean writeCSV = false;
    static final boolean serialize = false;
    static final boolean filterAttributes = false;
    static final boolean printARFF = false;
    static final int filterLevel=3;

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        //int cfgNum = 0;//0, 1, 2
        int ARFFformat = 2;//0=single,1=paired, 2=grid, 3=genre
        int MAX_LIMIT = 50000;
        int[][] CFG = {{MAX_LIMIT, 5}, {MAX_LIMIT, 10}, {MAX_LIMIT, 20},
        {10000, 5}, {10000, 10}, {10000, 20},
        {1000, 0}, {1000, 5}, {1000, 10}};
        int limit;// = CFG[cfgNum][0];
        int MIN;// = CFG[cfgNum][1];

            CFG = new int[][]{{1000000, 5}, {600000, 5}, {300000, 5}, {100000, 5},
            {60000, 4}, {30000, 3}, {10000, 3}, {5000, 2}};
        if (serialize) {
             for (int cfgNum = 0; cfgNum < CFG.length; cfgNum++) {
                limit = CFG[cfgNum][0];
                MIN = (int) pow(log(limit) / log(50), filterLevel);//CFG[cfgNum][1];
                System.out.printf("cfg=%s, ARFFformat=%s\n", cfgNum, ARFFformat);
                serialize(null, MIN, limit, ARFFformat);
            }
        } else if (ARFFformat == 3) {
            for (int cfgNum = 0; cfgNum < CFG.length; cfgNum++) {
                limit = CFG[cfgNum][0];
                MIN = (int) pow(log(limit) / log(50), filterLevel);//CFG[cfgNum][1];
                System.out.printf("cfg=%s, ARFFformat=%s\n", cfgNum, ARFFformat);
                for (Genre G : Genre.values()) {
                    run(G, MIN, limit, ARFFformat);
                }
            }
        } else {
            for (int cfgNum = 0; cfgNum < CFG.length; cfgNum++) {
                limit = CFG[cfgNum][0];
                MIN = (int) pow(log(limit) / log(50), filterLevel);//CFG[cfgNum][1];
                System.out.printf("cfg=%s, ARFFformat=%s\n", cfgNum, ARFFformat);
                run(null, MIN, limit, ARFFformat);
            }
        }
    }

    public static void serialize(Genre genre, int MIN, int limit, int ARFFformat) throws Exception {

        String[] sarr;
        long t0, t1;
        String serFile = path + "all_movies.ser";
        String trainSerFile = path + "train_" + limit + ".ser";
        String valSerFile = path + "val_" + limit + ".ser";
        String testSerFile = path + "test_" + limit + ".ser";
        HashMap<Movie, Movie> train_movies, test_movies, validate_movies;
        HashMap<Movie, Movie> all_movies = new HashMap<>(1000000);
//        System.out.println(Metric.toStr());
        File out = new File(in[0].getAbsolutePath() + ".enc");
        String folderPath = in[0].getParent() + genre + "\\";
        if (genre == null) {
            folderPath = in[0].getParent() + "ALL\\";
        }

        t0 = System.currentTimeMillis();

        Miner.buildMovieList(out, all_movies, serFile);

        t1 = System.currentTimeMillis();
        INFO("---------------\nFinished in %s seconds!\n\n", (t1 - t0) / 1000);
        t0 = System.currentTimeMillis();

        INFO("---------------\nMovies: %s\n", all_movies.size());

        sarr = Movie.GLOB_word.uniqueSet().toArray(new String[0]);
//        sarr = Words.evaluate(all_movies, sarr);
        DB.filter(all_movies);
        INFO("---------------\nFILTERED Movies: %s\n", all_movies.size());
        INFO("\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();

        new File(folderPath).mkdir();
        INFO("---------------\nPrinting movies: %s\n", limit);
        out = new File(folderPath + "_limit=" + min(limit, all_movies.size()) + ".csv");
        all_movies = DB.rand_select(out, all_movies, limit);
        INFO("Enforce limit:\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();

        train_movies = new HashMap<>();
        test_movies = new HashMap<>();
        validate_movies = new HashMap<>();
        DB.split(all_movies, train_movies, test_movies, validate_movies, 2 / 3.0);
        INFO("Split: \t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();

//        for(Movie m: train_movies.keySet())all_movies.remove(m);
//        test_movies = all_movies;//IndexEncoder.rand_select(out, all_movies, limit);
        save_HashMapMovMov(trainSerFile, train_movies);
        save_HashMapMovMov(valSerFile, validate_movies);
        save_HashMapMovMov(testSerFile, test_movies);
        INFO("Save x3:\t %s ms\n", System.currentTimeMillis() - t0);

        //*/
    }

    public static void run(Genre genre, int MIN, int limit, int ARFFformat) throws Exception {

        String[] sarr;
        long t0, t1;
        String serFile = path + "all_movies.ser";
        String trainSerFile = path + "train_" + limit + ".ser";
        String valSerFile = path + "val_" + limit + ".ser";
        String testSerFile = path + "test_" + limit + ".ser";
        HashMap<Movie, Movie> train_movies, test_movies, validate_movies;
        HashMap<Movie, Movie> all_movies = new HashMap<>(1000000);
//        System.out.println(Metric.toStr());
        File out = new File(in[0].getAbsolutePath() + ".enc");
        String folderPath = in[0].getParent() + genre + "\\";
        if (genre == null) {
            folderPath = in[0].getParent() + "ALL\\";
        }

        t0 = System.currentTimeMillis();

//        try {
            /*attempt to only load the data we need (reduced DB). If that fails, we need to build it.*/
        train_movies = loadSerial_HashMapMovMov(trainSerFile);
        test_movies = loadSerial_HashMapMovMov(testSerFile);
        validate_movies = loadSerial_HashMapMovMov(valSerFile);
//            validate_movies=test_movies;
        all_movies.putAll(validate_movies);
        all_movies.putAll(train_movies);
        if (randomize) {
            train_movies = new HashMap<>();
            validate_movies = new HashMap<>();
            DB.split(all_movies, train_movies, null, validate_movies, 6 / 7.0);
        }
        if (testing) {
            train_movies.putAll(validate_movies);
            //train_movies = DB.rand_select(out, train_movies, train_movies.size() * 9 / 10);
        }
        Movie.rebuildGLOB(train_movies);
        System.out.printf("---------------\nMovies: %s\n", train_movies.size());

//
//        new File(folderPath).mkdir();
//        log("---------------\nPrinting movies: %s\n", limit);
//        out = new File(folderPath + "_limit=" + limit + ".csv");
//        all_movies = IndexEncoder.rand_select(out, all_movies, limit);
//        log("Enforce limit:\t %s ms\n", System.currentTimeMillis() - t0);
//        t0 = System.currentTimeMillis();
//
//        Movie.rebuildGLOB(train_movies);
        INFO("Rebuild GLOB:\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
//        log("---------------\nMovies2: %s\n", train_movies.size());
//        IndexEncoder.filter(train_movies, 0);
//        log("---------------\nFILTERED Movies2: %s\n", train_movies.size());

        INFO("---------------\nKeywords (before): %s   \tUnique: %s\n", Movie.GLOB_word.size(), Movie.GLOB_word.uniqueSet().size());
        sarr = Movie.GLOB_word.uniqueSet().toArray(new String[0]);
        if (filterAttributes) {
            HashMap<String, Item> words = Ranks.mergeAll(genre);
            INFO("words %s\n", words.size());
            for (String m : sarr) {
                if (!words.containsKey(m)) {
                    Movie.GLOB_word.remove(m);
                }
            }
        }
//        sarr = Words.evaluate(train_movies, sarr);
        for (String m : sarr) {
            if (Movie.GLOB_word.getCount(m) < MIN) {
                Movie.GLOB_word.remove(m);
            }
        }
        INFO("---------------\nKeywords (after): %s   \tUnique: %s\n", Movie.GLOB_word.size(), Movie.GLOB_word.uniqueSet().size());
//        if (!testing) {
//            for (Genre g : Genre.values()) {
//                if (!words.containsKey(g + "") && g != genre) {
//                    Movie.GLOB_genre.remove(g);
//                }
//            }
//        }
        if (printARFF) {
            for (Movie m : all_movies.keySet()) {
                GLOB_genre_set.put(m.genre, m.genre.toString());
            }
            new File(folderPath).mkdir();
            System.out.printf("---------------\nprinting ARFF\n");
            int dIndex = 0;
            int lim;
            String[] desc = {"[TRAIN]", "[TEST_ONLY]", "[VAL]", "[VAL+TR]"};
            for (HashMap<Movie, Movie> set : new HashMap[]{train_movies, test_movies, validate_movies, all_movies}) {
                lim = Math.min(limit, set.size());
                if (ARFFformat == 0) {
                    out = new File(String.format("%s%s_imdb_[single]_filterLevel=%s_movies=%s_MIN=%s_genres=%s.arff", 
                            folderPath, desc[dIndex], filterLevel,lim, MIN, Movie.GLOB_genre.uniqueSet().size()));
                    printARFF_single(out, set, lim);
                } else if (ARFFformat == 1) {
                    out = new File(String.format("%s%s_imdb_[paired]_filterLevel=%s_movies=%s_MIN=%s_genres=%s.arff", 
                            folderPath, desc[dIndex], filterLevel,lim, MIN, Movie.GLOB_genre.uniqueSet().size()));
                    printARFF_paired(out, set, lim);
                } else if (ARFFformat == 2) {
                    out = new File(String.format("%s%s_imdb_[grid]_filterLevel=%s_movies=%s_MIN=%s_attrib=%s_genres=%s.arff",
                            folderPath, desc[dIndex],filterLevel, lim, MIN, Movie.GLOB_word.uniqueSet().size(), Movie.GLOB_genre.uniqueSet().size()));
                    printARFF_grid(out, set, lim);
                } else {
                    out = new File(String.format("%s%s_imdb_[G]-%s_filterLevel=%s_movies=%s_MIN=%s_genres=%s.arff", 
                            folderPath, desc[dIndex],filterLevel, genre, lim, MIN, Movie.GLOB_genre.uniqueSet().size()));
                    printARFF_g(out, set, lim, genre);
                }
                dIndex++;
            }
            System.out.printf("Output: %s\n", out);
        }

        if (!testing) {
            return;
        }
            Genre[] validGenres = {Comedy,
                    Action,
                    Horror,
//                    Adventure,
//                    Fantasy,
//                    Romance,
//                    //    
//                    Western,
//                    Drama,
//                    Thriller,
//                    Animation,
//                    Sci_Fi,
//                    Mystery,
//                    History,
//                    //
//                    Biography,
//                    Short,
//                    Documentary,
//                    Musical,
//                    Sport,
//                    Family,
//                    Crime,
//                    Music,
//                    War
            };
            Movie.GLOB_genre.clear();
            Movie.GLOB_genre.addAll(Arrays.asList(validGenres));
        
        minFP = Integer.MAX_VALUE;
        double i = 2;
        for (;i < 150; i = i * 2)
        {
            optimizePower(1, 0.01, i, train_movies, validate_movies);
//            optimizePower(0.5, 0.01, 130, train_movies, validate_movies);
//            optimizePower(0.5, 0.01, 180, train_movies, validate_movies);
        }
        TP = (int) maxTP;
        FP = (int) minFP;
        Metric.logBase = mi;
        Metric.poweR = mj;
        System.out.println(Metric.toStr());
        System.out.printf("TP=%4s\tFP=%4s\n", TP, FP);
        System.out.printf("TP=%s%%\tFP=%s%%\n\n", 100 * TP / (TP + FP), 100 * FP / (TP + FP));
        //*/
    }

    public static void printARFF_paired(File out, HashMap<Movie, Movie> all_movies, int limit) {
        StringBuilder sb = new StringBuilder(1000000);
        HashMap<String, Integer> index = new HashMap<String, Integer>();
        int i = 1;
        String output = "";
        output += String.format("%% %s\n", new Date());
        output += String.format("%% Authors: \n");
        output += String.format("@RELATION imdb \n");
        Collection<String> genreSet = Movie.GLOB_genre_set.values();
        INFO("# global keys = %s\n", Movie.GLOB_word.size());
        INFO("# global genres = %s\n", Movie.GLOB_genre.size());
        INFO("# movies = %s\n", all_movies.size());

        output += String.format("@ATTRIBUTE genre {");
        for (String s : genreSet) {
//            index.put(s, i++);
//            output += String.format("@ATTRIBUTE %s  {0,1}\n", s);
            output += String.format("\"%s\", ", s.replace("'", "").replace("\"", ""));
        }
//        output = output.substring(0, output.length() - 2);
        output += String.format("}\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_word.uniqueSet()) {
            if (!s.contains("\"") && !s.contains("'")) {
                index.put(s, i++);
            }

            sb.append(String.format("@ATTRIBUTE \"%s\" {0,1} \n", s.replace("'", "").replace("\"", "")));
        }
        sb.append("@DATA\n");
        Integer idx;
        ArrayList<Integer> list = new ArrayList(100);
        for (Movie m : all_movies.keySet()) {
            sb.append("{");

            sb.append(String.format("0 \"%s\", ", Movie.GLOB_genre_set.get(m.genre)));
//            for (Genre G : genreSet) {
//                if (m.genre.contains(G)) {
//                    sb.append(String.format("%s 1,", index.get(G.name())));
//                }
//            }
            list.clear();
            i = m.words.size();
            for (String s : m.words) {
                idx = index.get(s);
                if (idx != null) {
                    list.add(idx);
                }
            }
            Collections.sort(list);
            for (Integer s : list) {
                sb.append(String.format("%s 1", s));
                if (--i > 0) {
                    sb.append(", ");
                }
            }
            sb.append("}\n");
            if (limit-- < 0) {
                break;
            }
        }
        try {
            FileOutputStream os = new FileOutputStream(out);
            os.write(sb.toString().getBytes());
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void printARFF_single(File out, HashMap<Movie, Movie> all_movies, int limit) {
        StringBuilder sb = new StringBuilder(1000000);
        HashMap<String, Integer> index = new HashMap<String, Integer>();
        int i = 1;
        String output = "";
        output += String.format("%% %s\n", new Date());
        output += String.format("%% Authors: \n");
        output += String.format("@RELATION imdb \n");
        Set<Genre> genreSet = Movie.GLOB_genre.uniqueSet();
        INFO("# global keys = %s\n", Movie.GLOB_word.size());
        INFO("# global genres = %s\n", Movie.GLOB_genre.size());
        INFO("# movies = %s\n", all_movies.size());

        output += String.format("@ATTRIBUTE genre {");
        for (Genre s : genreSet) {
//            index.put(s, i++);
//            output += String.format("@ATTRIBUTE %s  {0,1}\n", s);
            output += String.format("\"%s\", ", s.name().replace("'", "").replace("\"", ""));
        }
//        output = output.substring(0, output.length() - 2);
        output += String.format("}\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_word.uniqueSet()) {
            if (!s.contains("\"") && !s.contains("'")) {
                index.put(s, i++);
            }

            sb.append(String.format("@ATTRIBUTE \"%s\" {0,1} \n", s.replace("'", "").replace("\"", "")));
        }
        sb.append("@DATA\n");
        Integer idx;
        ArrayList<Integer> list = new ArrayList(100);
        for (Movie m : all_movies.keySet()) {

            for (Genre G : m.genre) {
                sb.append("{");
                sb.append(String.format("0 \"%s\",", G.name()));

                output = "";
                list.clear();
                i = m.words.size();
                for (String s : m.words) {
                    idx = index.get(s);
                    if (idx != null) {
                        list.add(idx);
                    }
                }
                Collections.sort(list);
                for (Integer s : list) {//build sparse matrix line
                    output += (String.format("%s 1", s));
                    if (--i > 0) {
                        output += (", ");
                    }
                }
                output += ("}\n");
                sb.append(output);
            }
            if (limit-- < 0) {
                break;
            }
        }
        try {
            FileOutputStream os = new FileOutputStream(out);
            os.write(sb.toString().getBytes());
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void printARFF_grid(File out, HashMap<Movie, Movie> all_movies, int limit) {
        StringBuilder sb = new StringBuilder(1000000);
        HashMap<String, Integer> index = new HashMap<String, Integer>();
        int i = 1;
        String output = "";
        output += String.format("%% %s\n", new Date());
        output += String.format("%% Authors: \n");
        output += String.format("@RELATION imdb \n");
        Set<Genre> genreSet = Movie.GLOB_genre.uniqueSet();
        INFO("# global keys = %s\n", Movie.GLOB_word.size());
        INFO("# global genres = %s\n", Movie.GLOB_genre.size());
        INFO("# movies = %s\n", all_movies.size());

        output += String.format("@ATTRIBUTE __YEAR NUMERIC\n");
        for (Genre s : genreSet) {
            index.put(s.name(), i++);
            output += String.format("@ATTRIBUTE __%s  {0,1}\n", s.name().replace("'", "_").replace("\"", "_"));
        }
        output += String.format("\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_word.uniqueSet()) {
            if (!s.contains("\"") && !s.contains("'")) {
                index.put(s, i++);
            }

            sb.append(String.format("@ATTRIBUTE \"%s\" {0,1} \n", s.replace("'", "_").replace("\"", "_")));
        }
        sb.append("@DATA\n");
        Integer idx;
        ArrayList<Integer> list = new ArrayList(100);
        for (Movie m : all_movies.keySet()) {
            sb.append("{");
            
              sb.append(String.format("0 %s, ", m.year));
            for (Genre G : genreSet) {
                if (m.genre.contains(G)) {
                    sb.append(String.format("%s 1, ", index.get(G.name())));
                }
            }
            list.clear();
            i = m.words.size();
            for (String s : m.words) {
                idx = index.get(s);
                if (idx != null) {
                    list.add(idx);
                }
            }
            Collections.sort(list);
            for (Integer s : list) {
                sb.append(String.format("%s 1", s));
                if (--i > 0) {
                    sb.append(", ");
                }
            }
            sb.append("}\n");
            if (limit-- < 0) {
                break;
            }
        }
        try {
            FileOutputStream os = new FileOutputStream(out);
            os.write(sb.toString().getBytes());
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void printARFF_g(File out, HashMap<Movie, Movie> all_movies, int limit, Genre genre) {
        StringBuilder sb = new StringBuilder(1000000);
        HashMap<String, Integer> index = new HashMap<String, Integer>();
        int i = 0;
        String output = "";
        output += String.format("%% %s\n", new Date());
        output += String.format("%% Authors: \n");
        output += String.format("@RELATION imdb \n");
        Set<Genre> genreSet = //Movie.GLOB_genre.uniqueSet();
                new HashSet<Genre>();
        genreSet.add(genre);
        INFO("# global keys = %s\n", Movie.GLOB_word.size());
        INFO("# global genres = %s\n", Movie.GLOB_genre.size());
        INFO("# movies = %s\n", all_movies.size());

        for (Genre s : genreSet) {
            index.put(s.name(), i++);
            output += String.format("@ATTRIBUTE %s  {0,1}\n", s.name().replace("'", "").replace("\"", ""));
        }
        output += String.format("\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_word.uniqueSet()) {
            if (!s.contains("\"") && !s.contains("'")) {
                index.put(s, i++);
            }

            sb.append(String.format("@ATTRIBUTE \"%s\" {0,1} \n", s.replace("'", "").replace("\"", "")));
        }
        sb.append("@DATA\n");
        Integer idx;
        ArrayList<Integer> list = new ArrayList(100);
        for (Movie m : all_movies.keySet()) {
            sb.append("{");

            for (Genre G : genreSet) {
                if (m.genre.contains(G)) {
                    sb.append(String.format("%s 1, ", index.get(G.name())));
                }
            }
            list.clear();
            i = m.words.size();
            for (String s : m.words) {
                idx = index.get(s);
                if (idx != null) {
                    list.add(idx);
                }
            }
            Collections.sort(list);
            for (Integer s : list) {
                sb.append(String.format("%s 1", s));
                if (--i > 0) {
                    sb.append(", ");
                }
            }
            sb.append("}\n");
            if (limit-- < 0) {
                break;
            }
        }
        try {
            FileOutputStream os = new FileOutputStream(out);
            os.write(sb.toString().getBytes());
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
