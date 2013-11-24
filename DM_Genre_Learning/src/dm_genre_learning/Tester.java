package dm_genre_learning;

import static dm_genre_learning.Matrix.path;
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

/**
 *
 * @author ldtwo
 */
public class Tester extends Matrix {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        //int cfgNum = 0;//0, 1, 2
        int ARFFformat = 2;//0=single,1=paired, 2=grid, 3=genre
        int MAX_LIMIT = 50000;
        int[][] CFG = {{MAX_LIMIT, 5}, {MAX_LIMIT, 10}, {MAX_LIMIT, 20}, 
            {10000, 5}, {10000, 10}, {10000, 20} ,
             {1000, 0}, {1000, 5},{1000, 10}};
        int limit;// = CFG[cfgNum][0];
        int MIN;// = CFG[cfgNum][1];
        for (int cfgNum = 0; cfgNum < CFG.length; cfgNum++) {
            limit = CFG[cfgNum][0];
            MIN = CFG[cfgNum][1];
            System.out.printf("cfg=%s, ARFFformat=%s\n", cfgNum, ARFFformat);
//            for (Ranks.GOOD_PERCENTAGE = 1; GOOD_PERCENTAGE > 0.1; GOOD_PERCENTAGE -= 0.1)
            {
                //if (true || ARFFformat == 3)
                if(false)
                {
                    for (Genre G : Genre.values()) {
//                        serialize(G, MIN, limit, ARFFformat);
                        run(G, MIN, limit, ARFFformat);
//                        System.out.printf("GOOD_PERCENTAGE=%s\t[min=%s]\n", GOOD_PERCENTAGE, minFP);
                    }
                } else {
                    serialize(null, MIN, limit, ARFFformat);
//                    run(null, MIN, limit, ARFFformat);
//                    System.out.printf("GOOD_PERCENTAGE=%s\t[min=%s]\n", GOOD_PERCENTAGE, minFP);
                }
            }
        }
//        for (Genre g : Genre.values()) {
//            if(Movie.GLOB_genre.getCount(g)>0)
//            encodeGivenGenre(g);
//        }
    }
    static final boolean testing = false;

    public static void serialize(Genre genre, int MIN, int limit, int ARFFformat) throws Exception {

        String[] sarr;
        long t0, t1;
        String serFile = path + "all_movies.ser";
        String trainSerFile = path + "train_" + limit + ".ser";
        String valSerFile = path + "val_" + limit + ".ser";
        String testSerFile = path + "test_" + limit + ".ser";
        HashMap<Movie, Movie> train_movies, test_movies, validate_movies;
        HashMap<Movie, Movie> all_movies = new HashMap<Movie, Movie>(1000000);
        System.out.println(Metric.toStr());
        File out = new File(in[0].getAbsolutePath() + ".enc");
        String folderPath = in[0].getParent() + genre + "\\";
        if (genre == null) {
            folderPath = in[0].getParent() + "ALL\\";
        }

        t0 = System.currentTimeMillis();

            buildMovieList(out, all_movies, serFile);
          
        t1 = System.currentTimeMillis();
        log("---------------\nFinished in %s seconds!\n\n", (t1 - t0) / 1000);
        t0 = System.currentTimeMillis();

        log("---------------\nMovies: %s\n", all_movies.size());

        sarr = Movie.GLOB_keyword.uniqueSet().toArray(new String[0]);
//        sarr = Words.evaluate(all_movies, sarr);
        IndexEncoder.filter(all_movies, 0);
        log("---------------\nFILTERED Movies: %s\n", all_movies.size());
        log("\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();

        new File(folderPath).mkdir();
        log("---------------\nPrinting movies: %s\n", limit);
        out = new File(folderPath + "_limit=" + limit + ".csv");
        all_movies = IndexEncoder.rand_select(out, all_movies, limit);
        log("Enforce limit:\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();

        train_movies = new HashMap<Movie, Movie>();
        test_movies = new HashMap<Movie, Movie>();
        validate_movies = new HashMap<Movie, Movie>();
        IndexEncoder.split(all_movies, train_movies, test_movies, validate_movies, 2 / 3.0);
        log("Split: \t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();

//        for(Movie m: train_movies.keySet())all_movies.remove(m);
//        test_movies = all_movies;//IndexEncoder.rand_select(out, all_movies, limit);
        save_HashMapMovMov(trainSerFile, train_movies);
        save_HashMapMovMov(valSerFile, validate_movies);
        save_HashMapMovMov(testSerFile, test_movies);
        log("Save x3:\t %s ms\n", System.currentTimeMillis() - t0);
       

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
        HashMap<Movie, Movie> all_movies = new HashMap<Movie, Movie>(1000000);
        System.out.println(Metric.toStr());
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
            
        train_movies = IndexEncoder.rand_select(out, train_movies, train_movies.size()*9/10);
            Movie.rebuildGLOB(train_movies);
//        } catch (Exception iOException) {
//
//        }
//        try {
//            /*attempt to only load the data we need (whole DB). If that fails, we need to build it.*/
//
//            t0 = System.currentTimeMillis();
//            all_movies = loadSerial_HashMapMovMov(serFile);
//            System.out.printf("\t %s ms\n", System.currentTimeMillis() - t0);
//            t0 = System.currentTimeMillis();
//        } catch (Exception exception) {
//            buildMovieList(out, all_movies, serFile);
//            System.out.printf("Total to build:\t %s ms\n", System.currentTimeMillis() - t0);
//            t0 = System.currentTimeMillis();
//        } catch (java.lang.OutOfMemoryError e) {
//            System.err.printf("Heap: %s bytes\n", Runtime.getRuntime().totalMemory());
//            e.printStackTrace();
//        }
////        limit=all_movies.size()/2;
//        t1 = System.currentTimeMillis();
//        log("---------------\nFinished in %s seconds!\n\n", (t1 - t0) / 1000);
//        t0 = System.currentTimeMillis();
//
//        log("---------------\nMovies: %s\n", all_movies.size());
//
//        sarr = Movie.GLOB_keyword.uniqueSet().toArray(new String[0]);
////        sarr = Words.evaluate(all_movies, sarr);
//        IndexEncoder.filter(all_movies, 0);
//        log("---------------\nFILTERED Movies: %s\n", all_movies.size());
//        log("\t %s ms\n", System.currentTimeMillis() - t0);
//        t0 = System.currentTimeMillis();
//
//        new File(folderPath).mkdir();
//        log("---------------\nPrinting movies: %s\n", limit);
//        out = new File(folderPath + "_limit=" + limit + ".csv");
//        all_movies = IndexEncoder.rand_select(out, all_movies, limit);
//        log("Enforce limit:\t %s ms\n", System.currentTimeMillis() - t0);
//        t0 = System.currentTimeMillis();
//
//        train_movies = new HashMap<Movie, Movie>();
//        test_movies = new HashMap<Movie, Movie>();
//        validate_movies = new HashMap<Movie, Movie>();
//        IndexEncoder.split(all_movies, train_movies, test_movies, validate_movies, 2 / 3.0);
//        log("Split: \t %s ms\n", System.currentTimeMillis() - t0);
//        t0 = System.currentTimeMillis();
//
////        for(Movie m: train_movies.keySet())all_movies.remove(m);
////        test_movies = all_movies;//IndexEncoder.rand_select(out, all_movies, limit);
//        save_HashMapMovMov(trainSerFile, train_movies);
//        save_HashMapMovMov(valSerFile, validate_movies);
//        save_HashMapMovMov(testSerFile, test_movies);
//        log("Save x3:\t %s ms\n", System.currentTimeMillis() - t0);
//        t0 = System.currentTimeMillis();

//        Movie.rebuildGLOB(train_movies);
        log("Rebuild GLOB:\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
//        log("---------------\nMovies2: %s\n", train_movies.size());
//        IndexEncoder.filter(train_movies, 0);
//        log("---------------\nFILTERED Movies2: %s\n", train_movies.size());

        log("---------------\nKeywords (before): %s   \tUnique: %s\n", Movie.GLOB_keyword.size(), Movie.GLOB_keyword.uniqueSet().size());
        sarr = Movie.GLOB_keyword.uniqueSet().toArray(new String[0]);
        HashMap<String, Item> words = Ranks.filter(genre);
//        sarr = Words.evaluate(train_movies, sarr);
        for (String m : sarr) {
            if (!words.containsKey(m)) //            if (Movie.GLOB_keyword.getCount(m) < MIN)
            {
                Movie.GLOB_keyword.remove(m);
            }
        }
        log("---------------\nKeywords (after): %s   \tUnique: %s\n", Movie.GLOB_keyword.size(), Movie.GLOB_keyword.uniqueSet().size());

//        if (!testing) {
//            for (Genre g : Genre.values()) {
//                if (!words.containsKey(g + "") && g != genre) {
//                    Movie.GLOB_genre.remove(g);
//                }
//            }
//        }

        log("---------------\nTitle (before): %s\n", Movie.GLOB_title.size());
//        sarr = Movie.GLOB_title.toArray(new String[0]);
//        for (String m : sarr) {
//            if (Movie.GLOB_title.getCount(m) < MIN) {
//                Movie.GLOB_title.remove(m);
//            }
//        }
//        log("---------------\nTitle (after): %s\n", Movie.GLOB_title.size());

        log("---------------\nPlot (before): %s\n", Movie.GLOB_plot.size());
//        sarr = Movie.GLOB_plot.toArray(new String[0]);
//        for (String m : sarr) {
//            if (Movie.GLOB_plot.getCount(m) < MIN) {
//                Movie.GLOB_plot.remove(m);
//            }
//        }
//        log("---------------\nPlot (after): %s\n", Movie.GLOB_plot.size());

        if (!testing) {
            for (Movie m : all_movies.keySet()) {
                GLOB_genre_set.put(m.genre, m.genre.toString());
            }
            limit = Math.min(limit, all_movies.size());
            log("---------------\nprinting ARFF\n");
            int dIndex = 0;
            String[] desc = {"train_movies", "test_movies", "validate_movies", "all_movies"};
            for (HashMap<Movie, Movie> set : new HashMap[]{train_movies, test_movies, validate_movies, all_movies}) {
                if (ARFFformat == 0) {
                    out = new File(String.format("%s%s_imdb_single_size=%s_MIN=%s_genres=%s.arff", folderPath, desc[dIndex], limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
                    printARFF_single(out, set, limit);
                } else if (ARFFformat == 1) {
                    out = new File(String.format("%s%s_imdb_paired_size=%s_MIN=%s_genres=%s.arff", folderPath, desc[dIndex], limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
                    printARFF_paired(out, set, limit);
                } else if (ARFFformat == 2) {
                    out = new File(String.format("%s%s_imdb_grid_size=%s_MIN=%s_genres=%s.arff", folderPath, desc[dIndex], limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
                    printARFF_grid(out, set, limit);
                } else {
                    out = new File(String.format("%s%s_imdb_G-%s_size=%s_MIN=%s_genres=%s.arff", folderPath, desc[dIndex], genre, limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
                    printARFF_g(out, set, limit, genre);
                }
                dIndex++;
            }
            log("Output: %s\n", out);
        }
//        System.out.println("\n0. Loading data");
//    ConverterUtils.DataSource source = new ConverterUtils.DataSource(out.getAbsolutePath());
//    Instances data = source.getDataSet();
//    data.setClass(new Attribute(genre+""));
//    if (data.classIndex() == -1)
//      data.setClassIndex(data.numAttributes() - 1);
//
//    System.out.println("\n2. Filter");
//    AttributeSelection filter = new AttributeSelection();
//    ChiSquaredAttributeEval eval = new ChiSquaredAttributeEval();
//    
//    Ranker search = new Ranker();
//    search.setThreshold(-1.7976931348623157E308);
//    search.setNumToSelect(1000);
//    filter.setEvaluator(eval);
//   
//    filter.setSearch(search);
//    filter.setInputFormat(data);
//    Instances newData = Filter.useFilter(data, filter);
//    
//    
//        System.out.printf("---------------\nprinting ARFF\n");
//        if (ARFFformat == 0) {
//            out = new File(String.format("%sF%s_imdb_single_size=%s_MIN=%s_genres=%s.arff", folderPath, genre,limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
//           } else if (ARFFformat == 1) {
//            out = new File(String.format("%sF%s_imdb_paired_size=%s_MIN=%s_genres=%s.arff", folderPath,genre, limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
//         } else if (ARFFformat == 2) {
//            out = new File(String.format("%sF%s_imdb_grid_size=%s_MIN=%s_genres=%s.arff", folderPath, genre,limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
//         } else {
//            out = new File(String.format("%sF%s_imdb_G-%s_size=%s_MIN=%s_genres=%s.arff", folderPath, genre,genre, limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
//         }
//        System.out.printf("Output: %s\n", out);
//    
//        try {
//            FileOutputStream os = new FileOutputStream(out);
//            os.write(newData.toString().getBytes());
//            os.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        System.out.printf("---------------\nMovies(train): %s\n", train_movies.size());
//        Movie.rebuildGLOB(movies2);
        //printMatrix(100, 100);
        /*
         TODO: harmonic convergence
         B= +/-delta b + B, if better/worse
         Db = Db*0.9
        
         i=UBOUND
         k=(UBOUND-LBOUND)
         loop while k>minK
         decrease by k
         if too low
         k=k*0.66
         loop
         increase by k
         if too high
         break LOOP2
         */

        double[] B = {5000, 1000, 500, 100, 50, 10, 5, 1};
        double[] R = {200,};
        minFP = Integer.MAX_VALUE;
        double i = 128; 
        //for (i < 150; i = i * 2)
        {
            innerConvergence2(0.5, 0.01, 64, train_movies, validate_movies);
            innerConvergence2(0.5, 0.01, 128, train_movies, validate_movies);
            innerConvergence2(0.5, 0.01, 250, train_movies, validate_movies);
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
        System.out.printf("# global keys = %s\n", Movie.GLOB_keyword.size());
        System.out.printf("# global genres = %s\n", Movie.GLOB_genre.size());
        System.out.printf("# movies = %s\n", all_movies.size());

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
        for (String s : Movie.GLOB_keyword.uniqueSet()) {
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
            i = m.keyword.size();
            for (String s : m.keyword) {
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
        System.out.printf("# global keys = %s\n", Movie.GLOB_keyword.size());
        System.out.printf("# global genres = %s\n", Movie.GLOB_genre.size());
        System.out.printf("# movies = %s\n", all_movies.size());

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
        for (String s : Movie.GLOB_keyword.uniqueSet()) {
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
                i = m.keyword.size();
                for (String s : m.keyword) {
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
        int i = 0;
        String output = "";
        output += String.format("%% %s\n", new Date());
        output += String.format("%% Authors: \n");
        output += String.format("@RELATION imdb \n");
        Set<Genre> genreSet = Movie.GLOB_genre.uniqueSet();
        System.out.printf("# global keys = %s\n", Movie.GLOB_keyword.size());
        System.out.printf("# global genres = %s\n", Movie.GLOB_genre.size());
        System.out.printf("# movies = %s\n", all_movies.size());

        for (Genre s : genreSet) {
            index.put(s.name(), i++);
            output += String.format("@ATTRIBUTE %s  {0,1}\n", s.name().replace("'", "_").replace("\"", "_"));
        }
        output += String.format("\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_keyword.uniqueSet()) {
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

            for (Genre G : genreSet) {
                if (m.genre.contains(G)) {
                    sb.append(String.format("%s 1, ", index.get(G.name())));
                }
            }
            list.clear();
            i = m.keyword.size();
            for (String s : m.keyword) {
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
        System.out.printf("# global keys = %s\n", Movie.GLOB_keyword.size());
        System.out.printf("# global genres = %s\n", Movie.GLOB_genre.size());
        System.out.printf("# movies = %s\n", all_movies.size());

        for (Genre s : genreSet) {
            index.put(s.name(), i++);
            output += String.format("@ATTRIBUTE %s  {0,1}\n", s.name().replace("'", "").replace("\"", ""));
        }
        output += String.format("\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_keyword.uniqueSet()) {
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
            i = m.keyword.size();
            for (String s : m.keyword) {
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
