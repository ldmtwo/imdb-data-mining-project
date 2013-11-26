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

    private static final long serialVersionUID = 123123123;

    final int hash_;
    final public String title;
    public int year;
    public static HashBag<Integer> GLOB_year = new HashBag<>();
    public static HashBag<Genre> GLOB_genre = new HashBag<>();
    public static HashMap<Set<Genre>, String> GLOB_genre_set = new HashMap();
    public static HashBag<String> GLOB_word = new HashBag<>();
    public HashSet<Genre> genre = new HashSet<>(2);
    public HashSet<String> words = new HashSet<>(15);
    public static HashSet<String> bad = new HashSet<>();
    public int db_cnt = 0;//number of .list files this movie was found
    final static int WORD_MIN_LEN = 4;
    private static Genre gtemp;

    static {
        String[] bad_ = "a,of,the,for,to,be,an,if,s,i,will,am,PL,,in,and,is,are,or,on,it,be,so".split(",");
        bad.addAll(Arrays.asList(bad_));
    }

    public static boolean isValidMovie(Movie m) {
        return m.words.size() > 0
                && m.genre.size() > 0
                && m.year > 1800 && m.year < 2013
                && m.db_cnt > 0
                && !(m.genre.contains(Genre.Adult)
                || m.genre.contains(Genre.Game_Show)
                || m.genre.contains(Genre.UNKNOWN) || m.genre.contains(Genre.Talk_Show) || m.genre.contains(Genre.Reality_TV)
                || m.genre.contains(Genre.Experimental) || m.genre.contains(Genre.Lifestyle)
                || m.words.contains("sex") || m.words.contains("orgasm"));
    }

    public static void rebuildGLOB(HashMap<Movie, Movie> movies) {

        GLOB_year = new HashBag<>();
        GLOB_genre = new HashBag<>();
        GLOB_word = new HashBag<>();
        String[] arr;
        for (Movie m : movies.keySet()) {
            GLOB_year.add(m.year);
            GLOB_genre.addAll(m.genre);
//            arr = m.title.toLowerCase().split("[ /!/\"/#/$/%/&/'/(/)/*/+/,/-///:/;/</=/>/?/@/[/\\/]/^/_/`/{/|/}/~]");
//            for (String s : arr) {
//                if (s.length() >= WORD_MIN_LEN) {
//                    if (!bad.contains(s)) {
//                        GLOB_word.add(s);
//                    }
//                }
//            }
            GLOB_genre_set.put(m.genre, m.genre.toString());
            GLOB_word.addAll(m.words);
            GLOB_word.add("_YEAR_" + m.year);
        }
    }

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
        hash_ = (title).hashCode() * 31 + year;
        GLOB_year.add(year);
        String[] arr = title.toLowerCase().split("[ /!/\"/#/$/%/&/'/(/)/*/+/,/-///:/;/</=/>/?/@/[/\\/]/^/_/`/{/|/}/~]");
        for (String s : arr) {
            if (s.length() >= WORD_MIN_LEN) {
                if (!bad.contains(s)) {
//                    GLOB_word.add(s);
//                    words.add(s);
                }
            }
        }
        words.add("_YEAR_" + year);
        GLOB_word.add("_YEAR_" + year);
    }

    public boolean setGenre(String g) {

        try {
            gtemp = Genre.valueOf(g);
            GLOB_genre.add(gtemp);
            return this.genre.add(gtemp);
        } catch (Exception e) {

        }
        return false;
    }

    public void addWord(String k) {
        GLOB_word.add(k);
        if (k.length() >= WORD_MIN_LEN) {
             if (!bad.contains(k))this.words.add(k);
        }

    }

    @Override
    public int hashCode() {
        return hash_;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        Movie m = (Movie) obj;
        return m.year != year ? false : m.title.compareTo(title) == 0;
    }

    @Override
    public String toString() {
        return (title + year);
    }
}
