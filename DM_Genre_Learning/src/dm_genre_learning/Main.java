/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dm_genre_learning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import org.apache.commons.collections4.bag.HashBag;

/**
 *
 * @author ldtwo
 */
public class Main {

   
    final static String path="C:\\Users\\Larry\\Desktop\\Data mining class\\";
       static File[] in = {new File(path+"movies.list"), new File(path+"genres.list"), 
           new File(path+"keywords.list"), new File(path+"plot.list")};
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        encodeGivenGenre(null);
        for (Genre g : Genre.values()) {
            if(Movie.GLOB_genre.getCount(g)>0)
            encodeGivenGenre(g);
        }
    }

    static public void encodeGivenGenre(Genre genre) throws Exception {
        File out = new File(in[0].getAbsolutePath() + ".enc");
        String folderPath = in[0].getParent() + genre + "\\";
        if (genre == null) {
            folderPath = in[0].getParent() + "ALL\\";
        }
        HashMap<Movie, Movie> movies = new HashMap<Movie, Movie>();
        long t0, t1;

        t0 = System.currentTimeMillis();
        String status = "";
        try {
            FileInputStream fis = new FileInputStream("d:\\movies.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            System.out.printf("LOADING...\n");
            movies = (HashMap<Movie, Movie>) ois.readObject();
            ois.close();
            fis.close();

            t1 = System.currentTimeMillis();
            System.out.printf("---------------\nLoaded in %s seconds!\n\n%s\n", (t1 - t0) / 1000, status);
        } catch (Exception exception) {

            status = IndexEncoder.storeMovies(in[0], out, movies);
            System.out.printf("Movies: %s\n", movies.size());
            status += IndexEncoder.storeGenres(in[1], out, movies);
            System.out.printf("Movies: %s\n", movies.size());
            status += IndexEncoder.storeKeywords(in[2], out, movies);
            System.out.printf("Movies: %s\n", movies.size());
            status += IndexEncoder.storePlots(in[3], out, movies);
            //save previous work
            FileOutputStream fileOut = new FileOutputStream("d:\\movies.ser");
            ObjectOutputStream oo = new ObjectOutputStream(fileOut);
            oo.writeObject(movies);
            oo.close();
            fileOut.close();
            t1 = System.currentTimeMillis();
            System.out.printf("---------------\nFinished in %s seconds!\n\n", (t1 - t0) / 1000);

        }

        System.out.printf("---------------\nMovies: %s\n", movies.size());

        IndexEncoder.filter(movies, 0);
        if (genre != null) {
            IndexEncoder.filterByGenre(movies, 0, genre);
            Movie.rebuildGLOB(movies);
        }
        System.out.printf("---------------\nFILTERED Movies: %s\n", movies.size());
        int k;
        new File(folderPath).mkdir();
        String strg = ((genre != null) ? "given%" + ("" + genre).toUpperCase() + "_" : "");
        for (int limit = 100; limit <= 1000000; limit *= 100) {
            for (Genre g : Genre.values()) {
                k = IndexEncoder.size(g, movies, limit);
                System.out.printf("---------------\nPrinting movies for %s: %s\n", g, k);
                out = new File(folderPath + strg + g + "_limit=" + k + ".csv");
                IndexEncoder.select(out, g, movies, limit);
            }
        }
        writeStats(folderPath, strg + "genre", Movie.GLOB_genre);
        writeStats(folderPath, strg + "keywords", Movie.GLOB_keyword);
        writeStats(folderPath, strg + "plot", Movie.GLOB_plot);
        writeStats(folderPath, strg + "title", Movie.GLOB_title);
        writeStats(folderPath, strg + "year", Movie.GLOB_year);
    }

    private static void writeStats(String folderPath, String type, HashBag b) throws Exception {
        File out;
        int k;
        for (int limit = 1000; limit <= 1000000000; limit *= 1000) {
            k = Math.min(limit, b.size());
            System.out.printf("---------------\nPrinting stats for %s: %s\n", type, k);
            out = new File(folderPath + "_FREQENCIES_" + type + "_limit=" + k + ".csv");
            IndexEncoder.writeHistogram(out, b, limit);
            if (k != limit) {
                break;
            }
        }
    }
}
