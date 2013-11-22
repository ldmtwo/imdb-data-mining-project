package dm_genre_learning;

import static dm_genre_learning.Matrix.path;
import static dm_genre_learning.Movie.GLOB_genre_set;
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
        int[][] CFG = {{10000000, 50}, {10000, 100}, {1000, 50}, {1000, 100}};
        int limit;// = CFG[cfgNum][0];
        int MIN;// = CFG[cfgNum][1];
        for (int cfgNum = 0; cfgNum < 3; cfgNum++) {
            limit = CFG[cfgNum][0];
            MIN = CFG[cfgNum][1];
            System.out.printf("cfg=%s, ARFFformat=%s\n", cfgNum, ARFFformat);
            if (ARFFformat == 3) {
                for (Genre G : Genre.values()) {
                    run(G, MIN, limit, ARFFformat);
                }
            } else {
                run(null, MIN, limit, ARFFformat);
            }
        }
//        for (Genre g : Genre.values()) {
//            if(Movie.GLOB_genre.getCount(g)>0)
//            encodeGivenGenre(g);
//        }
    }

    public static void run(Genre genre, int MIN, int limit, int ARFFformat) throws Exception {

        String[] sarr;
        long t0, t1;
        String serFile = path + "all_movies.ser";
        String trainSerFile = path + "train_" + limit + ".ser";
        String valSerFile = path + "val_" + limit + ".ser";
        String testSerFile = path + "test_" + limit + ".ser";
        HashMap<Movie, Movie> train_movies, test_movies, validate_movies;
        HashMap<Movie, Movie> all_movies = new HashMap<Movie, Movie>();
        System.out.println(Metric.toStr());
        File out = new File(in[0].getAbsolutePath() + ".enc");
        String folderPath = in[0].getParent() + genre + "\\";
        if (genre == null) {
            folderPath = in[0].getParent() + "ALL\\";
        }

        t0 = System.currentTimeMillis();

        try {
            /*attempt to only load the data we need (reduced DB). If that fails, we need to build it.*/
            train_movies = loadSerial_HashMapMovMov(trainSerFile);
            test_movies = loadSerial_HashMapMovMov(testSerFile);
            validate_movies = loadSerial_HashMapMovMov(valSerFile);
            Movie.rebuildGLOB(train_movies);
        } catch (Exception iOException) {

            try {
                /*attempt to only load the data we need (whole DB). If that fails, we need to build it.*/

                t0 = System.currentTimeMillis();
                all_movies = loadSerial_HashMapMovMov(serFile);
                System.out.printf("\t %s ms\n", System.currentTimeMillis() - t0);
                t0 = System.currentTimeMillis();
            } catch (Exception exception) {
                buildMovieList(out, all_movies, serFile);
                System.out.printf("Total to build:\t %s ms\n", System.currentTimeMillis() - t0);
                t0=System.currentTimeMillis();
            } catch (java.lang.OutOfMemoryError e) {
                System.err.printf("Heap: %s bytes\n", Runtime.getRuntime().totalMemory());
                e.printStackTrace();
            }
        }
//        limit=all_movies.size()/2;
        t1 = System.currentTimeMillis();
        System.out.printf("---------------\nFinished in %s seconds!\n\n", (t1 - t0) / 1000);
        t0 = System.currentTimeMillis();

        System.out.printf("---------------\nMovies: %s\n", all_movies.size());

        sarr = Movie.GLOB_keyword.uniqueSet().toArray(new String[0]);
//        sarr = Words.evaluate(all_movies, sarr);
        IndexEncoder.filter(all_movies, 0);
        System.out.printf("---------------\nFILTERED Movies: %s\n", all_movies.size());
        System.out.printf("\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
        

        new File(folderPath).mkdir();
        System.out.printf("---------------\nPrinting movies: %s\n", limit);
        out = new File(folderPath + "_limit=" + limit + ".csv");
        all_movies = IndexEncoder.rand_select(out, all_movies, limit);
        System.out.printf("Enforce limit:\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
        
        
        train_movies = new HashMap<Movie, Movie>();
        test_movies = new HashMap<Movie, Movie>();
        validate_movies = new HashMap<Movie, Movie>();
        IndexEncoder.split(all_movies, train_movies, test_movies, validate_movies, 2 / 3.0);
        System.out.printf("Split: \t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
        
        
//        for(Movie m: train_movies.keySet())all_movies.remove(m);
//        test_movies = all_movies;//IndexEncoder.rand_select(out, all_movies, limit);
        save_HashMapMovMov(trainSerFile, train_movies);
        save_HashMapMovMov(valSerFile, validate_movies);
        save_HashMapMovMov(testSerFile, test_movies);
        System.out.printf("Save x3:\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
        
        
        Movie.rebuildGLOB(train_movies);
        System.out.printf("Rebuild GLOB:\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
        
        
//        System.out.printf("---------------\nMovies2: %s\n", train_movies.size());
//        IndexEncoder.filter(train_movies, 0);
//        System.out.printf("---------------\nFILTERED Movies2: %s\n", train_movies.size());
//
        System.out.printf("---------------\nKeywords (before): %s\n", Movie.GLOB_keyword.size());
//        sarr = Movie.GLOB_keyword.uniqueSet().toArray(new String[0]);
//        sarr = Words.evaluate(train_movies, sarr);
//        for (String m : sarr) {
//            if (Movie.GLOB_keyword.getCount(m) < MIN) {
//                Movie.GLOB_keyword.remove(m);
//            }
//        }
//        System.out.printf("---------------\nKeywords (after): %s\n", Movie.GLOB_keyword.size());
        System.out.printf("---------------\nTitle (before): %s\n", Movie.GLOB_title.size());
//        sarr = Movie.GLOB_title.toArray(new String[0]);
//        for (String m : sarr) {
//            if (Movie.GLOB_title.getCount(m) < MIN) {
//                Movie.GLOB_title.remove(m);
//            }
//        }
//        System.out.printf("---------------\nTitle (after): %s\n", Movie.GLOB_title.size());
        System.out.printf("---------------\nPlot (before): %s\n", Movie.GLOB_plot.size());
//        sarr = Movie.GLOB_plot.toArray(new String[0]);
//        for (String m : sarr) {
//            if (Movie.GLOB_plot.getCount(m) < MIN) {
//                Movie.GLOB_plot.remove(m);
//            }
//        }
//        System.out.printf("---------------\nPlot (after): %s\n", Movie.GLOB_plot.size());

        for (Movie m : all_movies.keySet()) {
            GLOB_genre_set.put(m.genre, m.genre.toString());
        }
        limit = Math.min(limit, all_movies.size());
        System.out.printf("---------------\nprinting ARFF\n");
        if (ARFFformat == 0) {
            out = new File(String.format("%simdb_single_size=%s_MIN=%s_genres=%s.arff", folderPath, limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
            printARFF_single(out, all_movies, limit);
        } else if (ARFFformat == 1) {
            out = new File(String.format("%simdb_paired_size=%s_MIN=%s_genres=%s.arff", folderPath, limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
            printARFF_paired(out, all_movies, limit);
        } else if (ARFFformat == 2) {
            out = new File(String.format("%simdb_grid_size=%s_MIN=%s_genres=%s.arff", folderPath, limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
            printARFF_grid(out, all_movies, limit);
        } else {
            out = new File(String.format("%simdb_G-%s_size=%s_MIN=%s_genres=%s.arff", folderPath, genre, limit, MIN, Movie.GLOB_genre.uniqueSet().size()));
            printARFF_g(out, all_movies, limit, genre);
        }
        System.out.printf("Output: %s\n", out);
        System.out.printf("---------------\nMovies(train): %s\n", train_movies.size());
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
        /*
         double[] B = {5000, 1000, 500, 100, 50, 10, 5, 1};
         double[] R = {200,};
         double k;
         double hi, lo;
         minFP = Integer.MAX_VALUE;
         for (double i = 1; i < 90; i = i * 2) {
         k = 1000 - 0.0001;
         innerConvergence2(9, 0.0001, i, train_movies, test_movies);
         //            innerConvergence2(1000,50, i, train_movies, test_movies);
         //            innerConvergence2(50,-300, i, train_movies, test_movies);
         }
         TP = (int) maxTP;
         FP = (int) minFP;
         Metric.b = mi;
         Metric.r = mj;
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
            output += String.format("\"%s\", ", s);
        }
//        output = output.substring(0, output.length() - 2);
        output += String.format("}\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_keyword.uniqueSet()) {
            if (!s.contains("\"")) {
                index.put(s, i++);
            }

            sb.append(String.format("@ATTRIBUTE \"%s\" {0,1} \n", s));
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
            output += String.format("\"%s\", ", s.name());
        }
//        output = output.substring(0, output.length() - 2);
        output += String.format("}\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_keyword.uniqueSet()) {
            if (!s.contains("\"")) {
                index.put(s, i++);
            }

            sb.append(String.format("@ATTRIBUTE \"%s\" {0,1} \n", s));
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
            output += String.format("@ATTRIBUTE %s  {0,1}\n", s.name());
        }
        output += String.format("\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_keyword.uniqueSet()) {
            if (!s.contains("\"")) {
                index.put(s, i++);
            }

            sb.append(String.format("@ATTRIBUTE \"%s\" {0,1} \n", s));
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
            output += String.format("@ATTRIBUTE %s  {0,1}\n", s.name());
        }
        output += String.format("\n");
        sb.append(output);
        output = "";
        for (String s : Movie.GLOB_keyword.uniqueSet()) {
            if (!s.contains("\"")) {
                index.put(s, i++);
            }

            sb.append(String.format("@ATTRIBUTE \"%s\" {0,1} \n", s));
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
