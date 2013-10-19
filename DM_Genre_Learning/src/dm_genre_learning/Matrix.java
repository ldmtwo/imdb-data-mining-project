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
import java.util.EnumMap;
import java.util.HashMap;

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

    public static void genreVsKeyword(HashMap<Movie, Movie> movies) {
        long t1,t0=System.currentTimeMillis();
        
        freq = new int[Movie.GLOB_keyword.size()][Movie.GLOB_genre.size()];
        int gen_idx, key_idx;
        for (Movie m : movies.keySet()) {
            for (Genre g : m.genre) {
                gen_idx = g2i.get(g);
                for (String k : m.keyword) {
                    key_idx = k2i.get(k);
                    freq[key_idx][gen_idx]++;
                }
            }
        }
        t1=System.currentTimeMillis();
            System.out.printf("---------------\nFREQ: Finished in %s seconds!\n\n", (t1 - t0) / 1000);
            t0=t1;
        weight =new double[Movie.GLOB_keyword.size()][];
        for(int i=0;i<freq.length;i++){
            weight[i]=Metric.calc(freq[i]);
        }
            System.out.printf("---------------\nWEIGHT: Finished in %s seconds!\n\n", (t1 - t0) / 1000);

    }
    final static String path = "C:\\Users\\Larry\\Desktop\\Data mining class\\";
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
        long t0, t1;
        int limit = 100;

        String status = "";
        String serFile = "d:\\movies.ser";

        HashMap<Movie, Movie> movies2;
        HashMap<Movie, Movie> movies = new HashMap<Movie, Movie>();

        File out = new File(in[0].getAbsolutePath() + ".enc");
        String folderPath = in[0].getParent() + genre + "\\";
        if (genre == null) {
            folderPath = in[0].getParent() + "ALL\\";
        }

        t0 = System.currentTimeMillis();


        try {
            movies2 = loadSerial_HashMapMovMov(path + "hmmm_" + limit + ".ser");
            Movie.rebuildGLOB(movies2);
        } catch (Exception iOException) {
            try {
                movies = loadSerial_HashMapMovMov(serFile);
            } catch (Exception exception) {
                buildMovieList(out, movies, serFile);
            }catch(java.lang.OutOfMemoryError e){
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
            save_HashMapMovMov(path + "hmmm_" + limit + ".ser", movies);
        }

        System.out.printf("---------------\nKeywords (before): %s\n", Movie.GLOB_keyword.size());
        marr = Movie.GLOB_keyword.toArray(new Movie[0]);
        for (Movie m : marr) {
            if (Movie.GLOB_keyword.getCount(m) < 3) {
                Movie.GLOB_keyword.remove(m);
            }
        }
        System.out.printf("---------------\nKeywords (after): %s\n", Movie.GLOB_keyword.size());
        System.out.printf("---------------\nTitle (before): %s\n", Movie.GLOB_title.size());
        marr = Movie.GLOB_title.toArray(new Movie[0]);
        for (Movie m : marr) {
            if (Movie.GLOB_title.getCount(m) < 3) {
                Movie.GLOB_title.remove(m);
            }
        }
        System.out.printf("---------------\nTitle (after): %s\n", Movie.GLOB_title.size());
        System.out.printf("---------------\nPlot (before): %s\n", Movie.GLOB_plot.size());
        marr = Movie.GLOB_plot.toArray(new Movie[0]);
        for (Movie m : marr) {
            if (Movie.GLOB_plot.getCount(m) < 3) {
                Movie.GLOB_plot.remove(m);
            }
        }
        System.out.printf("---------------\nPlot (after): %s\n", Movie.GLOB_plot.size());

        genreVsKeyword(movies2);
    }

    private static HashMap<Movie, Movie> loadSerial_HashMapMovMov(String serFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        HashMap<Movie, Movie> movies;
        FileInputStream fis = new FileInputStream(serFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        System.out.printf("LOADING...\n");
        movies = (HashMap<Movie, Movie>) ois.readObject();
        ois.close();
        fis.close();
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
