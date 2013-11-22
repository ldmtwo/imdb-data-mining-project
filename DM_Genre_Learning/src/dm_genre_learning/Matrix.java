/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dm_genre_learning;

import static dm_genre_learning.Movie.GLOB_genre_set;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author ldtwo
 */
public class Matrix {

    static int TP = 0, FP = 0, klen;
    static double minFP = Integer.MAX_VALUE, maxTP = 0, lastFP = Integer.MAX_VALUE;
    static double mi = 99999, mj = 9999;
    static HashMap<String, Integer> k2i = new HashMap<String, Integer>();
    static HashMap<String, Integer> p2i = new HashMap<String, Integer>();
    static EnumMap<Genre, Integer> g2i = new EnumMap<Genre, Integer>(Genre.class);
    static int[][] freq;
    static double[][] weight;

    final static String path = "d:\\";
    static File[] in = {new File(path + "movies.list"), new File(path + "genres.list"),
        new File(path + "keywords.list"), new File(path + "plot.list")};

    static {
        int i = 0;
        for (Genre g : Genre.values()) {
            g2i.put(g, i++);
        }
        Metric.genw = new double[i];
    }

    public static void buildMovieList(File out, HashMap<Movie, Movie> movies, String serFile) throws Exception, IOException {
        String status;
        
        long t0=System.currentTimeMillis();
        status = IndexEncoder.storeMovies(in[0], out, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis()-t0);
        t0=System.currentTimeMillis();
        System.out.printf("Movies: %s\n", movies.size());
        status += IndexEncoder.storeGenres(in[1], out, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis()-t0);
        t0=System.currentTimeMillis();
        System.out.printf("Movies: %s\n", movies.size());
        status += IndexEncoder.storeKeywords(in[2], out, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis()-t0);
        t0=System.currentTimeMillis();
        System.out.printf("Movies: %s\n", movies.size());
        status += IndexEncoder.storePlots(in[3], out, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis()-t0);
        t0=System.currentTimeMillis();
        save_HashMapMovMov(serFile, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis()-t0);
        t0=System.currentTimeMillis();
    }

    static public void learn_genreVsKeyword(HashMap<Movie, Movie> movies) {
        long t1, t0 = System.currentTimeMillis();

        Genre[] gen = Genre.values();
        Set<String> key = Movie.GLOB_keyword.uniqueSet();
//        System.out.printf("---------------\nmatrix sizes = 8*%s*%s = %s = 1024^%s\n\n", Movie.GLOB_keyword.size(), gen.length,
//                key.size() * gen.length * 8, Math.log(key.size() * gen.length * 8) / Math.log(1024));
        freq = new int[key.size()][gen.length];
        for (int[] freq1 : freq) {
            for (int j = 0; j < freq1.length; j++) {
                freq1[j] = 0;
            }
        }
        double genSum = Movie.GLOB_genre.size();
        for (Genre g : gen) {
            Metric.genw[g2i.get(g)] = Movie.GLOB_genre.getCount(g) / genSum;
        }
        int i = 0;
        for (String s : key) {
            k2i.put(s, i++);
        }
        int gen_idx, key_idx;
        for (Movie m : movies.keySet()) {
            GLOB_genre_set.put(m.genre, m.genre.toString());
            for (Genre g : m.genre) {
                gen_idx = g2i.get(g);
                for (String k : m.keyword) {
                    if (k2i.containsKey(k)) {
                        key_idx = k2i.get(k);
                        freq[key_idx][gen_idx]++;
                    }
                }
            }
        }
        t1 = System.currentTimeMillis();
//        System.out.printf("---------------\nFREQ: Finished in %s seconds!\n\n", (t1 - t0) / 1000);
        t0 = t1;
        weight = new double[key.size()][];

        i = 0;
        for (int[] freq1 : freq) {
            weight[i++] = Metric.calc(freq1);
        }
//        System.out.printf("---------------\nWEIGHT: Finished in %s seconds!\n\n", (t1 - t0) / 1000);

    }

    public static void learn_genreVsPlot(HashMap<Movie, Movie> movies) {
        long t1, t0 = System.currentTimeMillis();

        Genre[] gen = Genre.values();
        Set<String> plot = Movie.GLOB_plot.uniqueSet();
//        System.out.printf("---------------\nmatrix sizes = 8*%s*%s = %s = 1024^%s\n\n", Movie.GLOB_keyword.size(), gen.length,
//                key.size() * gen.length * 8, Math.log(key.size() * gen.length * 8) / Math.log(1024));
        freq = new int[plot.size()][gen.length];
        for (int[] freq1 : freq) {
            for (int j = 0; j < freq1.length; j++) {
                freq1[j] = 0;
            }
        }
        double genSum = Movie.GLOB_genre.size();
        for (Genre g : gen) {
            Metric.genw[g2i.get(g)] = Movie.GLOB_genre.getCount(g) / genSum;
        }
        int i = 0;
        for (String s : plot) {
            p2i.put(s, i++);
        }
        int gen_idx, plot_idx;
        for (Movie m : movies.keySet()) {
            for (Genre g : m.genre) {
                gen_idx = g2i.get(g);
                for (String k : m.plot) {
                    if (p2i.containsKey(k)) {
                        plot_idx = p2i.get(k);
                        try {
                            freq[plot_idx][gen_idx]++;
                        } catch (Exception e) {
                            System.err.printf("k=%s, g=%s\n", plot_idx, gen_idx);
                            System.err.printf("freq.len=%s\n", freq.length);
                            System.err.printf("freq[%s].len=%s\n", plot_idx, freq[plot_idx].length);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        t1 = System.currentTimeMillis();
//        System.out.printf("---------------\nFREQ: Finished in %s seconds!\n\n", (t1 - t0) / 1000);
        t0 = t1;
        weight = new double[plot.size()][];

        i = 0;
        for (int[] freq1 : freq) {
            weight[i++] = Metric.calc(freq1);
        }
//        System.out.printf("---------------\nWEIGHT: Finished in %s seconds!\n\n", (t1 - t0) / 1000);

    }

    public static void innerConvergence(double k, double i, HashMap<Movie, Movie> train_movies, HashMap<Movie, Movie> test_movies) {
        double j = 0;
        double n = 1;
        lastFP = Integer.MAX_VALUE;
        FP = Integer.MAX_VALUE - 1;
        while (Math.abs(k) > 0.1) {
            n = 1;
            System.out.printf("+(i=%s, j=%s + %s, %s, %s), \t", (int) i, j, -k, FP, minFP);
            for (j = Math.max(j, 0); FP - lastFP < 0 || (j >= -300 && n > 0 && TP > 0); j = j - k) {
                evaluate_plot(i, j, train_movies, test_movies);
                System.out.printf("^(FP=%s + %s, j=%s + %s), ", lastFP, FP - lastFP, j, -k);
                if (FP - lastFP >= 0) {
                    break;
                }
            }
//            k = k * 0.9;
//            j = j - k;
//            evaluate_plot(i, j, train_movies, all_movies);
//            System.out.printf("+(lFP=%s < FP=%s, j=%s, k=%s, min=%s), ", lastFP, FP, (int) j, (int) k, minFP);
//            for (; lastFP > FP&& j>0; j = j + k) {//do while results improve
//                System.out.printf("$(i=%s, j=%s, k=%s, %s, %s), ", (int) i, (int) j, (int) k, FP, minFP);
//
//                evaluate_plot(i, j, train_movies, all_movies);
//            }
            j = j + k;
            evaluate_plot(i, j, train_movies, test_movies);
            System.out.printf("\n\t");
            k = -k;
            n = 1;
            for (j = Math.max(j, 0); FP - lastFP < 0 || (j >= -300 && n > 0 && TP > 0); j = j - k) {
                evaluate_plot(i, j, train_movies, test_movies);
                System.out.printf("*(FP=%s + %s, j=%s + %s), ", lastFP, FP - lastFP, j, -k);
                if (FP - lastFP >= 0) {
                    break;
                }
            }
            j = j + k;
            evaluate_plot(i, j, train_movies, test_movies);
            k = -k * .1 - 0.8;
            System.out.printf("&(FP=%s + %s, j=%s + %s)\n", lastFP, FP - lastFP, j, -k);
            n--;
        }
        System.out.printf(".\n");
//            for (double j = 500; j > 0.01; j = j * 0.9) {
//                Metric.b = i;
//                Metric.r = j;
////                System.out.println(Metric.toStr());
////                learn_genreVsKeyword(movies2);
////                testHypothesis_keyword(movies);
//                learn_genreVsPlot(movies2);
//                testHypothesis_plot(movies);
//                if (FP < minFP) {
//                    minFP = FP;
//                    maxTP = TP;
//                    mi = i;
//                    mj = j;
//                }
//                if (lastFP < FP) {
//                    break f2;
//                }
//            }
    }

    public static void innerConvergence2(double hi, double lo, double i, HashMap<Movie, Movie> train_movies, HashMap<Movie, Movie> test_movies) {
        double j = 0;
        double n = 1;
        double mid, FPmid, FPhi = 99999999, FPlo = 88888888;

        lastFP = Integer.MAX_VALUE;
        FP = Integer.MAX_VALUE - 1;

        FPmid = 7777777;
        System.out.printf("&[i=%s, %s , %s, min=%s]\t ", i, hi, lo, minFP);
        evaluate_plot(i, hi, train_movies, test_movies);
        FPhi = FP;
        evaluate_plot(i, lo, train_movies, test_movies);
        FPlo = FP;
        while (hi - lo > 400 || !(FPhi == FPlo && FPmid == FPlo)) {
            System.out.printf("^(FP=%s + %s, [%s + %s]), ", (int) lastFP, (int) (FP - lastFP), (int) hi, (int) (hi - lo));
            mid = (hi + lo) / 2;//Math.random()*(hi-lo)*0.99+lo;//
            evaluate_plot(i, mid, train_movies, test_movies);
            FPmid = FP;
            if (FPhi > FPlo) {
                hi = mid;
                FPhi = FPmid;
            } else if (FPlo > FPhi) {
                lo = mid;
                FPlo = FPmid;
            } else if (FPhi > FPmid) {
                hi = mid;
                FPhi = FPmid;
            } else {
                lo = mid;
                FPlo = FPmid;
            }
        }
        System.out.printf(".\n");
//int binary_search(int A[], int key, int imin, int imax)
//{
//  // continue searching while [imin,imax] is not empty
//  while (imax >= imin)
//    {
//      // calculate the midpoint for roughly equal partition
//      int imid = midpoint(imin, imax);
//
//      // determine which subarray to search
//      if (A[imid] < key)
//        // change min index to search upper subarray
//        imin = imid + 1;
//      else if (A[imid] > key)
//        // change max index to search lower subarray
//        imax = imid - 1;
//      else
//        // key found at index imid
//        return imid;
//    }
//  // key not found
//  return KEY_NOT_FOUND;
//}

    }

    public static void evaluate_plot(double i, double j, HashMap<Movie, Movie> train_movies, HashMap<Movie, Movie> test_movies) {
        Metric.b = i;
        Metric.r = j;
        lastFP = FP;
        learn_genreVsPlot(train_movies);
        testHypothesis_plot(test_movies);
        if (FP < minFP) {
            minFP = FP;
            maxTP = TP;
            mi = i;
            mj = j;
        }
        learn_genreVsKeyword(train_movies);
        testHypothesis_keyword(test_movies);
        if (FP < minFP) {
            minFP = FP;
            maxTP = TP;
            mi = i;
            mj = j;
        }
//            System.out.printf("\n\t");
    }

    public static void testHypothesis_keyword(HashMap<Movie, Movie> movies) {
        DecimalFormat df = new DecimalFormat("0");
        double reg, regSum;
        TP = 0;
        FP = 0;
        Set<Genre> gen = Movie.GLOB_genre.uniqueSet();
        int gen_idx, key_idx;
        Genre chosen = null;
        double max;
        String str, l1, l2;
        for (Movie m : movies.keySet()) {
            max = 0;
            chosen = null;

            regSum = 0;
            for (Genre g : gen) {
                gen_idx = g2i.get(g);
                reg = 0;
                for (String k : m.keyword) {
                    if (k2i.containsKey(k)) {
                        key_idx = k2i.get(k);
                        if (weight[key_idx][gen_idx] > 0.5) {
                            reg += weight[key_idx][gen_idx];
                        }
                    }
                }
                if (reg > max) {
                    max = reg;
                    chosen = g;
                }
                regSum += reg;
            }
            if (m.genre.contains(chosen)) {
                TP++;
            } else {
                FP++;
            }
            if (!m.genre.contains(chosen)) {

//                str = String.format("Movie --------------- %s (%s)\n\t%s\n", m.title, m.year, m.genre);
                for (Genre g : gen) {
//                    l1 = "";
//                    l2 = "";
                    gen_idx = g2i.get(g);
                    reg = 0;
                    for (String k : m.keyword) {
                        if (k2i.containsKey(k)) {
                            key_idx = k2i.get(k);
                            if (weight[key_idx][gen_idx] > 0.5) {
//                                klen = Math.max(k.length(),8);
//                                l1 += String.format(" kw(%" + klen + "s)|", k);
//                                l2 += String.format(" w=%-" + klen + "s |", df.format(weight[key_idx][gen_idx])+",  f="+freq[key_idx][gen_idx]);
                                reg += weight[key_idx][gen_idx];
                            }
                        }
                    }
                    if (reg > max) {
                        max = reg;
//                        chosen = g;
                    }
//                    if(100 * reg / regSum >7){
//                        str += String.format("\tw(%9s)= %6s  %6s%%\n", g, df.format(reg), df.format(100 * reg / regSum));
//                        str += String.format("\t\t%s\n\t\t%s\n", l1, l2);
//                    }
                }
//                str += String.format("GENRE=%s\t\t\t%s\n\n", chosen, m.genre.contains(chosen));

//                System.out.print(str);
            }
        }
//        System.out.printf("TP=%4s\tFP=%4s\n", TP, FP);
//        System.out.printf("TP=%s%%\tFP=%s%%\n\n", 100 * TP / (TP + FP), 100 * FP / (TP + FP));
    }

    public static void testHypothesis(HashMap<Movie, Movie> movies) {
        DecimalFormat df = new DecimalFormat("0");
        double reg, regSum;
        TP = 0;
        FP = 0;
        Set<Genre> gen = Movie.GLOB_genre.uniqueSet();
        int gen_idx, key_idx;
        Genre chosen = null;
        double max;
        String str, l1, l2;
        for (Movie m : movies.keySet()) {
            max = 0;
            chosen = null;

            regSum = 0;
            for (Genre g : gen) {
                gen_idx = g2i.get(g);
                reg = 0;
                for (String k : m.keyword) {
                    if (k2i.containsKey(k)) {
                        key_idx = k2i.get(k);
                        if (weight[key_idx][gen_idx] > 0.5) {
                            reg += weight[key_idx][gen_idx];
                        }
                    }
                }
                if (reg > max) {
                    max = reg;
                    chosen = g;
                }
                regSum += reg;
            }
            if (m.genre.contains(chosen)) {
                TP++;
            } else {
                FP++;
            }

        }
    }

    public static void testHypothesis_plot(HashMap<Movie, Movie> movies) {
        DecimalFormat df = new DecimalFormat("0");
        double reg, regSum;
        TP = 0;
        FP = 0;
        Set<Genre> gen = Movie.GLOB_genre.uniqueSet();
        int gen_idx, plot_idx;
        Genre chosen = null;
        double max;
        String str, l1, l2;
        for (Movie m : movies.keySet()) {
            max = 0;
            chosen = null;

            regSum = 0;
            for (Genre g : gen) {
                gen_idx = g2i.get(g);
                reg = 0;
                for (String k : m.plot) {
                    if (p2i.containsKey(k)) {
                        plot_idx = p2i.get(k);
                        if (weight[plot_idx][gen_idx] > 0.01) {
                            reg += weight[plot_idx][gen_idx];
                        }
                    }
                }
                if (reg > max) {
                    max = reg;
                    chosen = g;
                }
                regSum += reg;
            }
            if (m.genre.contains(chosen)) {
                TP++;
            } else {
                FP++;
            }
            if (!m.genre.contains(chosen)) {

//                str = String.format("Movie --------------- %s (%s)\n\t%s\n", m.title, m.year, m.genre);
                for (Genre g : gen) {
//                    l1 = "";
//                    l2 = "";
                    gen_idx = g2i.get(g);
                    reg = 0;
                    for (String k : m.plot) {
                        if (p2i.containsKey(k)) {
                            plot_idx = p2i.get(k);
                            if (weight[plot_idx][gen_idx] > 0.01) {
//                                klen = Math.max(k.length(),8);
//                                l1 += String.format(" kw(%" + klen + "s)|", k);
//                                l2 += String.format(" w=%-" + klen + "s |", df.format(weight[key_idx][gen_idx])+",  f="+freq[key_idx][gen_idx]);
                                reg += weight[plot_idx][gen_idx];
                            }
                        }
                    }
                    if (reg > max) {
                        max = reg;
//                        chosen = g;
                    }
//                    if(100 * reg / regSum >7){
//                        str += String.format("\tw(%9s)= %6s  %6s%%\n", g, df.format(reg), df.format(100 * reg / regSum));
//                        str += String.format("\t\t%s\n\t\t%s\n", l1, l2);
//                    }
                }
//                str += String.format("GENRE=%s\t\t\t%s\n\n", chosen, m.genre.contains(chosen));

//                System.out.print(str);
            }
        }
//        System.out.printf("TP=%4s\tFP=%4s\n", TP, FP);
//        System.out.printf("TP=%s%%\tFP=%s%%\n\n", 100 * TP / (TP + FP), 100 * FP / (TP + FP));
    }

    public static void printMatrix(int m, int n) {
        NumberFormat df = new DecimalFormat("##0.000");
        df.setMaximumFractionDigits(3);
        for (int i = 0; i < weight.length && i < m; i++) {
            for (int j = 0; j < weight[i].length && j < n; j++) {
                System.out.printf("%-5s ", df.format(weight[i][j]));
            }
            System.out.printf("\n");
        }
    }

    public static HashMap<Movie, Movie> loadSerial_HashMapMovMov(String serFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        HashMap<Movie, Movie> movies;
        FileInputStream fis = new FileInputStream(serFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        System.out.printf("LOADING...\n");
        movies = (HashMap<Movie, Movie>) ois.readObject();
        ois.close();
        fis.close();
        Movie.rebuildGLOB(movies);
        return movies;
    }

    public static void save_HashMapMovMov(String serFile, HashMap<Movie, Movie> movies) throws IOException, FileNotFoundException {
        //save previous work
        FileOutputStream fileOut = new FileOutputStream(serFile);
        ObjectOutputStream oo = new ObjectOutputStream(fileOut);
        oo.writeObject(movies);
        oo.close();
        fileOut.close();
    }
}
