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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author ldtwo
 */
public class Miner {

    static int TP = 0, FP = 0, klen;
    static double minFP = Integer.MAX_VALUE, maxTP = 0, lastFP = Integer.MAX_VALUE;
    static double mi = 99999, mj = 9999;
    static HashMap<String, Integer> k2i = new HashMap<String, Integer>();
    static HashMap<String, Integer> p2i = new HashMap<String, Integer>();
    static EnumMap<Genre, Integer> g2i = new EnumMap<Genre, Integer>(Genre.class);
    static int[][] freq;
    static double[][] weight;

    public final static String path = "d:\\";
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

        long t0 = System.currentTimeMillis();
        status = DB.storeMovies(in[0], out, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
        System.out.printf("Movies: %s\n", movies.size());
        status += DB.storeGenres(in[1], out, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
        System.out.printf("Movies: %s\n", movies.size());
        status += DB.storeKeywords(in[2], out, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
        System.out.printf("Movies: %s\n", movies.size());
        status += DB.storePlots(in[3], out, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis() - t0);
        System.out.printf("Movies: %s\nsaving...\n", movies.size());
        t0 = System.currentTimeMillis();
        save_HashMapMovMov(serFile, movies);
        System.out.printf("\t %s ms\n", System.currentTimeMillis() - t0);
        t0 = System.currentTimeMillis();
    }

    static public void learnGenre(HashMap<Movie, Movie> movies) {
        long t1, t0 = System.currentTimeMillis();

        int i = 0;
        Genre[] gen = Genre.values();
        Set<String> key = Movie.GLOB_word.uniqueSet();
//        System.out.printf("---------------\nmatrix sizes = 8*%s*%s = %s = 1024^%s\n\n", Movie.GLOB_keyword.size(), gen.length,
//                key.size() * gen.length * 8, Math.log(key.size() * gen.length * 8) / Math.log(1024));
        freq = new int[key.size() ][gen.length];
        for (int[] freq1 : freq) {
            for (int j = 0; j < freq1.length; j++) {
                freq1[j] = 0;
            }
        }
        i = 0;
        g2i.clear();
        for (Genre g : Genre.values()) {
            g2i.put(g, i++);
        }
        Metric.genw = new double[i];
        double genSum = Movie.GLOB_genre.size();
        for (Genre g : gen) {
            Metric.genw[g2i.get(g)] = Movie.GLOB_genre.getCount(g) / genSum;
        }
        i = 0;
        k2i.clear();
        for (String s : key) {
            k2i.put(s, i++);
        }
        int gen_idx, key_idx;
        for (Movie m : movies.keySet()) {
            //Why is this here?
//            if(!GLOB_genre_set.containsKey(m.genre))
//                GLOB_genre_set.put(m.genre, m.genre.toString());
            for (Genre g : m.genre) {
                gen_idx = g2i.get(g);
                for (String k : m.words) {
                    if (key.contains(k)) {
                        key_idx = k2i.get(k);
//                        try {
                            freq[key_idx][gen_idx]++;
//                        } catch (Exception e) {
//                            System.err.printf("key_idx=%s, gen_idx=%s, %s, %s\n", key_idx, gen_idx, k, g);
//                            e.printStackTrace();
//                        }
                    }
                }
            }
        }
        t1 = System.currentTimeMillis();
//        System.out.printf("---------------\nFREQ: Finished in %s seconds!\n\n", (t1 - t0) / 1000);
        t0 = t1;
        weight = new double[key.size()][];

        i = 0;
//        try {
            for (int[] freq1 : freq) {
                weight[i++] = Metric.calc(freq1);
            }
//        } catch (Exception e) {
//            System.err.printf("weight.len=%s, i=%s, freq.len=%s\n", weight.length, i, freq.length);
//            e.printStackTrace();
//        }
//        System.out.printf("---------------\nWEIGHT: Finished in %s seconds!\n\n", (t1 - t0) / 1000);

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
                for (String k : m.words) {
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
                    for (String k : m.words) {
                        if (k2i.containsKey(k)) {
                            key_idx = k2i.get(k);
                            if (weight[key_idx][gen_idx] > 0.5) {
                                klen = Math.max(k.length(), 8);
//                                l1 += String.format(" kw(%" + klen + "s)|", k);
//                                l2 += String.format(" w=%-" + klen + "s |", df.format(weight[key_idx][gen_idx])+",  f="+freq[key_idx][gen_idx]);
                                reg += weight[key_idx][gen_idx];
                            }
                        }
                    }
                    if (reg > max) {
                        max = reg;
                        chosen = g;
                    }
////                    if(100 * reg / regSum >7)
//                    {
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

    public static void learnAndTest(double i, double j, HashMap<Movie, Movie> train_movies, HashMap<Movie, Movie> test_movies) {
        Metric.logBase = i;
        Metric.poweR = j;

        learnGenre(train_movies);
        testHypothesis(test_movies);
        if (FP < minFP) {
            minFP = FP;
            maxTP = TP;
            mi = i;
            mj = j;
        }
    }

   public static void optimizePower(double hi, double lo, double i, HashMap<Movie, Movie> train_movies, HashMap<Movie, Movie> test_movies) {
         double mid, FPmid, FPhi = 99999999, FPlo = 88888888;

        lastFP = Integer.MAX_VALUE;
        FP = Integer.MAX_VALUE - 1;

        FPmid = 7777777;
        System.out.printf("&[i=%s, %s , %s, min=%s]\t ", i, hi, lo, minFP);
        learnAndTest(i, hi, train_movies, test_movies);
        FPhi = FP;
        learnAndTest(i, lo, train_movies, test_movies);
        FPlo = FP;
        while (hi - lo > 400 || !(FPhi == FPlo && FPmid == FPlo)) {
            System.out.printf("^(FP=%s + %s, [%s + %s]), ", (int) lastFP, (int) (FP - lastFP), (int) hi, (int) (hi - lo));
            mid = (hi + lo) / 2;//Math.random()*(hi-lo)*0.99+lo;//
            learnAndTest(i, mid, train_movies, test_movies);
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
        HashMap<Movie, Movie> movies=null;
        FileInputStream fis;
        try {
            fis = new FileInputStream(serFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            INFO("LOADING...\n");
            movies = (HashMap<Movie, Movie>) ois.readObject();
            ois.close();
            fis.close();
            Movie.rebuildGLOB(movies);
        } catch (FileNotFoundException e) {
            throw new MissingSerialFileException(e);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
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
    
    public static void INFO(String format){
       // System.out.printf(format);
    }
    public static void INFO(String format, Object a){
        //System.out.printf(format,a);
    }
    public static void INFO(String format, Object a, Object b){
       // System.out.printf(format,a,b);
    }
    public static void INFO(String format, Object a, Object b, Object c){
       // System.out.printf(format,a,b,c);
    }
    public static void INFO(String format, Object a, Object b, Object c, Object d){
        //System.out.printf(format,a,b,c,d);
    }
}
