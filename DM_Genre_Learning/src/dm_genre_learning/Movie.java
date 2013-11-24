/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dm_genre_learning;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.bag.HashBag;

/**
 *
 * @author ldtwo
 */
public class Movie implements java.io.Serializable {
    final int hash_;
    final public String title;
    public int year;
    public static HashBag<Integer> GLOB_year = new HashBag<Integer>();
    public static HashBag<Genre> GLOB_genre = new HashBag<Genre>();
    public static HashMap<Set<Genre>,String> GLOB_genre_set = new HashMap();
    public static HashBag<String> GLOB_title = new HashBag<String>();
    public static HashBag<String> GLOB_plot = new HashBag<String>();
    public static HashBag<String> GLOB_keyword = new HashBag<String>();
    public HashSet<Genre> genre = new HashSet<Genre>(2);
    public HashSet<String> plot = new HashSet<String>(10);
    public HashSet<String> keyword = new HashSet<String>(10);
    public static HashSet<String> bad = new HashSet<String>();
    public int db_cnt = 0;//number of .list files this movie was found

    static {
        String[] bad_ = "a,of,the,for,to,be,an,if,s,i,will,am,PL,,in,and,is,are,or,on,it,be,so".split(",");
        bad.addAll(Arrays.asList(bad_));
    }

    public static void rebuildGLOB(HashMap<Movie, Movie> movies) {

        GLOB_year = new HashBag<Integer>();
        GLOB_genre = new HashBag<Genre>();
        GLOB_title = new HashBag<String>();
        GLOB_plot = new HashBag<String>();
        GLOB_keyword = new HashBag<String>();
        String[] arr;
        for (Movie m : movies.keySet()) {
            GLOB_year.add(m.year);
            GLOB_genre.addAll(m.genre);
            arr = m.title.toLowerCase().split("[ /!/\"/#/$/%/&/'/(/)/*/+/,/-///:/;/</=/>/?/@/[/\\/]/^/_/`/{/|/}/~]");
            for (String s : arr) {
                if (s.length() > 2) {
                    if (!bad.contains(s)) {
                        GLOB_title.add(s);
                    }
                }
            }
            GLOB_genre_set.put(m.genre,m.genre.toString());
            GLOB_plot.addAll(m.plot);
            GLOB_keyword.addAll(m.keyword);
        }
    }

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
        hash_ = (title).hashCode()*31+year;
        GLOB_year.add(year);
        String[] arr = title.toLowerCase().split("[ /!/\"/#/$/%/&/'/(/)/*/+/,/-///:/;/</=/>/?/@/[/\\/]/^/_/`/{/|/}/~]");
        for (String s : arr) {
            if (s.length() > 2) {
                if (!bad.contains(s)) {
                    GLOB_title.add(s);
                }
            }
        }
    }

//    public Movie() {
//    }
    Genre gtemp;

    public boolean setGenre(String g) {

        try {
            gtemp = Genre.valueOf(g);
            GLOB_genre.add(gtemp);
            return this.genre.add(gtemp);
        } catch (Exception e) {
            
//            try {
//            GLOB_genre.add(Genre.valueOf(g.replace("-", "_")));
//            return this.genre.add(Genre.valueOf(g.replace("-", "_")));
//            } catch (Exception e2) {
//                System.err.println("NOT FOUND: " +g);
//            }
//            e.printStackTrace();
//            System.err.println(g);
        }
        return false;
    }

    public void setKeyword(String k) {
        GLOB_keyword.add(k);
        this.keyword.add(k);

    }

    public boolean setPlot(String p) {
        if (p.length() > 2) {
            if (!bad.contains(p)) {
//                System.out.println(p);
                GLOB_plot.add(p);
                return this.plot.add(p);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash_;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        Movie m=(Movie) obj;
        return m.year!=year?false: m.title.compareTo(title)== 0;
    }

    @Override
    public String toString() {
        return (title + year);
    }
}
