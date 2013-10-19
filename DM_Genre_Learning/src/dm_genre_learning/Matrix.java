/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dm_genre_learning;

import static dm_genre_learning.Main.encodeGivenGenre;
import static dm_genre_learning.Main.in;
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
import org.apache.commons.collections4.bag.HashBag;

/**
 *
 * @author ldtwo
 */
public class Matrix {

    private static void buildMovieList(File out, HashMap<Movie, Movie> movies, String serFile) throws Exception, IOException {
        String status;
        status = IndexEncoder.storeMovies(in[0], out, movies);
        System.out.printf("Movies: %s\n", movies.size());
        status += IndexEncoder.storeGenres(in[1], out, movies);
        System.out.printf("Movies: %s\n", movies.size());
        status += IndexEncoder.storeKeywords(in[2], out, movies);
        System.out.printf("Movies: %s\n", movies.size());
        status += IndexEncoder.storePlots(in[3], out, movies);
        save_HashMapMovMov(serFile, movies);
    }
    static HashMap<String, Integer> k2i = new HashMap<String, Integer>();
    static EnumMap<Genre, Integer> g2i = new EnumMap<Genre, Integer>(Genre.class);
    static int[][] freq;
    static double[][] weight;

    static {
        int i = 0;
        for (Genre g : Genre.values()) {
            g2i.put(g, i++);
        }
        Metric.genw=new double[i];
    }

    public static void genreVsKeyword(HashMap<Movie, Movie> movies) {
        long t1, t0 = System.currentTimeMillis();

        Genre[] gen = Genre.values();
        Set<String> key = Movie.GLOB_keyword.uniqueSet();
        System.out.printf("---------------\nmatrix sizes = 8*%s*%s = %s = 1024^%s\n\n", Movie.GLOB_keyword.size(), gen.length,
                key.size() * gen.length * 8, Math.log(key.size() * gen.length* 8) / Math.log(1024));
        freq = new int[key.size()][gen.length];
        for (int[] freq1 : freq) {
            for (int j = 0; j < freq1.length; j++) {
                freq1[j] = 0;
            }
        }
        double genSum=Movie.GLOB_genre.size();
        for (Genre g: gen) {
            Metric.genw[g2i.get(g)]=Movie.GLOB_genre.getCount(g)/genSum;
        }
        int i = 0;
        for (String s : key) {
            k2i.put(s, i++);
        }
        int gen_idx, key_idx;
        for (Movie m : movies.keySet()) {
            for (Genre g : m.genre) {
                gen_idx = g2i.get(g);
                for (String k : m.keyword) {
                    if (k2i.containsKey(k)) {
                        key_idx = k2i.get(k);
                        try {
                            freq[key_idx][gen_idx]++;
                        } catch (Exception e) {
                            System.err.printf("k=%s, g=%s\n", key_idx, gen_idx);
                            System.err.printf("freq.len=%s\n", freq.length);
                            System.err.printf("freq[%s].len=%s\n",key_idx, freq[key_idx].length);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        t1 = System.currentTimeMillis();
        System.out.printf("---------------\nFREQ: Finished in %s seconds!\n\n", (t1 - t0) / 1000);
        t0 = t1;
        weight = new double[key.size()][];

        i = 0;
        for (int[] freq1 : freq) {
            weight[i++] = Metric.calc(freq1);
        }
        System.out.printf("---------------\nWEIGHT: Finished in %s seconds!\n\n", (t1 - t0) / 1000);

    }
    final static String path = "d:\\";
    static File[] in = {new File(path + "movies.list"), new File(path + "genres.list"),
        new File(path + "keywords.list"), new File(path + "plot.list")};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        encodeGivenGenre(null);
//        for (Genre g : Genre.values()) {
//            if(Movie.GLOB_genre.getCount(g)>0)
//            encodeGivenGenre(g);
//        }
    }

    static public void encodeGivenGenre(Genre genre) throws Exception {
        Movie[] marr;
        String[] sarr;
        long t0, t1;
        int limit = 100000;
        int MIN = 1;
        String status = "";
        String serFile = path + "movies.ser";

        HashMap<Movie, Movie> movies2;
        HashMap<Movie, Movie> movies = new HashMap<Movie, Movie>();
        System.out.println(Metric.toStr());
        File out = new File(in[0].getAbsolutePath() + ".enc");
        String folderPath = in[0].getParent() + genre + "\\";
        if (genre == null) {
            folderPath = in[0].getParent() + "ALL\\";
        }

        t0 = System.currentTimeMillis();

        try {
            /*attempt to only load the data we need (reduced DB). If that fails, we need to build it.*/
            movies2 = loadSerial_HashMapMovMov(path + "hmmm_" + limit + ".ser");
            Movie.rebuildGLOB(movies2);
        } catch (Exception iOException) {
         
        }   try {
                /*attempt to only load the data we need (whole DB). If that fails, we need to build it.*/
                movies = loadSerial_HashMapMovMov(serFile);
            } catch (Exception exception) {
                buildMovieList(out, movies, serFile);
            } catch (java.lang.OutOfMemoryError e) {
                System.err.printf("Heap: %s bytes\n", Runtime.getRuntime().totalMemory());
                e.printStackTrace();
            }
            t1 = System.currentTimeMillis();
            System.out.printf("---------------\nFinished in %s seconds!\n\n", (t1 - t0) / 1000);

            System.out.printf("---------------\nMovies: %s\n", movies.size());
            IndexEncoder.filter(movies, 0);
            System.out.printf("---------------\nFILTERED Movies: %s\n", movies.size());

            new File(folderPath).mkdir();
            System.out.printf("---------------\nPrinting movies: %s\n", limit);
            out = new File(folderPath + "_limit=" + limit + ".csv");
            movies2 = IndexEncoder.rand_select(out, movies, limit);
            save_HashMapMovMov(path + "hmmm_" + limit + ".ser", movies2);

        System.out.printf("---------------\nMovies: %s\n", movies.size());
        IndexEncoder.filter(movies2, 0);
        System.out.printf("---------------\nFILTERED Movies: %s\n", movies.size());

        System.out.printf("---------------\nKeywords (before): %s\n", Movie.GLOB_keyword.size());
        sarr = Movie.GLOB_keyword.toArray(new String[0]);
        for (String m : sarr) {
            if (Movie.GLOB_keyword.getCount(m) < MIN) {
                Movie.GLOB_keyword.remove(m);
            }
        }
        System.out.printf("---------------\nKeywords (after): %s\n", Movie.GLOB_keyword.size());
        System.out.printf("---------------\nTitle (before): %s\n", Movie.GLOB_title.size());
        sarr = Movie.GLOB_title.toArray(new String[0]);
        for (String m : sarr) {
            if (Movie.GLOB_title.getCount(m) < MIN) {
                Movie.GLOB_title.remove(m);
            }
        }
        System.out.printf("---------------\nTitle (after): %s\n", Movie.GLOB_title.size());
        System.out.printf("---------------\nPlot (before): %s\n", Movie.GLOB_plot.size());
        sarr = Movie.GLOB_plot.toArray(new String[0]);
        for (String m : sarr) {
            if (Movie.GLOB_plot.getCount(m) < MIN) {
                Movie.GLOB_plot.remove(m);
            }
        }
        System.out.printf("---------------\nPlot (after): %s\n", Movie.GLOB_plot.size());

        Movie.rebuildGLOB(movies2);
        genreVsKeyword(movies2);
        //printMatrix(100, 100);

        double reg;
        int TP=0,FP=0;
        Set<Genre> gen = Movie.GLOB_genre.uniqueSet();
        int gen_idx, key_idx;Genre chosen=null;double max;String str;
        for (Movie m : movies.keySet()) {
//            str=String.format("Movie --------------- %s (%s)\n\t%s\n\t%s\n", m.title,m.year, m.genre,m.keyword);
            max=0;chosen=null;
            for (Genre g :gen) {
                gen_idx = g2i.get(g);
                reg = 0;
                for (String k : m.keyword) {
                    if (k2i.containsKey(k)) {
                        key_idx = k2i.get(k);

                        //if(weight[key_idx][gen_idx]>0.2)
                            reg += weight[key_idx][gen_idx];
                    }
                    
                }
                if(reg>max){
                    max=reg;chosen=g;
                }

//               str+= String.format("\t\tw(%9s)= %6s\n", g, reg);

            }
            if(m.genre.contains(chosen))TP++;else FP++;
//                str+= String.format("GENRE=%s\t\t\t%s\n\n", chosen,m.genre.contains(chosen));
//                 if(!m.genre.contains(chosen))System.out.print(str);
        }
                System.out.printf("TP=%4s\tFP=%4s\n\n", TP,FP);
                System.out.printf("TP=%s%%\tFP=%s%%\n\n", 100*TP/(TP+FP),100*FP/(TP+FP));

    }

    private static void printMatrix(int m, int n) {
        NumberFormat df = new DecimalFormat("##0.000");
        df.setMaximumFractionDigits(3);
        for (int i = 0; i < weight.length && i < m; i++) {
            for (int j = 0; j < weight[i].length && j < n; j++) {
                System.out.printf("%-5s ", df.format(weight[i][j]));
            }
            System.out.printf("\n");
        }
    }

    private static HashMap<Movie, Movie> loadSerial_HashMapMovMov(String serFile) throws FileNotFoundException, IOException, ClassNotFoundException {
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

    private static void save_HashMapMovMov(String serFile, HashMap<Movie, Movie> movies) throws IOException, FileNotFoundException {
        //save previous work
        FileOutputStream fileOut = new FileOutputStream(serFile);
        ObjectOutputStream oo = new ObjectOutputStream(fileOut);
        oo.writeObject(movies);
        oo.close();
        fileOut.close();
    }
}
