package dm_genre_learning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

/**
 *
 * @author ldtwo
 */
public class Words {

    public static final double L2 = Math.log1p(2);

    static String[] evaluate(HashMap<Movie, Movie> train_movies, String[] keyArr) {
        double[] kscores = new double[keyArr.length];
        HashMap<Genre, Set<Movie>> gm = new HashMap<Genre, Set<Movie>>();
        HashMap<Genre, HashBag<String>> gk = new HashMap<Genre, HashBag<String>>();
        HashMap<String, Integer> s2i = new HashMap<String, Integer>();
        HashMap<Genre, Integer> g2i = new HashMap<Genre, Integer>();
        HashMap< Integer, String> i2s = new HashMap<Integer, String>();
        HashMap<Integer, Genre> i2g = new HashMap<Integer, Genre>();
        HashBag<String> bag;
        int i = 0, j = 0;
        Genre[] genres = Genre.values();
        for (Genre G : genres) {
            System.out.println(G);
            i2g.put(i, G);
            g2i.put(G, i++);
            gm.put(G, new HashSet<Movie>());
            gk.put(G, new HashBag<String>());
        }
        for (String k : keyArr) {
            i2s.put(j, k);
            s2i.put(k, j++);
        }
        long[][] X = new long[i][j];
        for (Movie m : train_movies.keySet()) {
            for (Genre g : m.genre) {
                gm.get(g).add(m);
            }
        }

    for (Movie m : train_movies.keySet()) {
        for (Genre g :m.genre) {
            i = g2i.get(g);
                for (String k : m.keyword) {
                    j = s2i.get(k);
                    X[i][j]++;
                    gk.get(g).add(k);
                }
            }
        }
//        for (Genre g : gk.keySet()) {
//            bag = gk.get(g);
//            i = g2i.get(g);
//            for (String k : bag.uniqueSet()) {
//                j = s2i.get(k);
//                X[i][j] = bag.getCount(k);
//            }
//        }
        Genre g;
        String k;
        double avg, lg, entropy;
        String s;
//        for (int ii:i2g.keySet()) {
//            g = i2g.get(ii);
//            avg = 0;
//            for (j = 0; j < X[ii].length; j++) {
//                avg += X[ii][j];
//            }
//            avg = avg / X.length;
//            lg = Math.log1p(avg)/L2;
//            kscores[j]=lg;
//            for (j = 0; j < X[ii].length; j++) {
//                //X[i][j] = Math.log1p(X[i][j])*10/L2;
//            }
////                if (lg > 45) {
////            for (j = 0; j < X[i].length; j++) {
////                k = i2s.get(j);
////                }
////            }
//        }

        ChiSquareTest chi=new ChiSquareTest();
        HashSet<String> bad=new HashSet<String>();
        for (j = 0; j < keyArr.length; j++) {

            if (j % 20 == 0) {
                for (int ii : i2g.keySet()) {
                    g = i2g.get(ii);
                    System.out.printf("%7s", "", g);
                }
                System.out.printf("\n");
            }
            avg = 0;
            for (i = 0; i < genres.length; i++) {
                avg += X[i][j];
            }
            avg = avg / genres.length;
            if (avg < 1) {
                continue;
            }
            lg = Math.log1p(avg/genres.length) / L2;
            kscores[j] = lg;

            k = i2s.get(j);
            s=String.format("%5s | ", (int)(10*lg));
            for (i = 0; i < i2g.size(); i++) {
                 s+=String.format("%7s",  X[i][j]);
            }
           s+=String.format("|");
            for (i = 0; i < i2g.size(); i++) {
                 s+=String.format("%7s", (int)(10*Math.log1p(X[i][j])/L2-10*lg));
            }
             s=String.format("|");
            entropy=0;
            for (i = 0; i < i2g.size(); i++) {
                //System.out.printf("%7s",  Math.log1p(X[i][j])/L2-lg);
                entropy+=((X[i][j]/avg)*Math.log1p((X[i][j]/avg))/L2);
                s+=String.format("%7s", (int)(10*(X[i][j]*X[i][j]/avg)*Math.log1p((X[i][j]/avg))/L2));
            }
            chi.chiSquareTest(X);
            s+=String.format("|%5s | ", (int)(entropy*10));
            s+=String.format(" :%s ", k);
            s+=String.format("\n");
            if(entropy>3)System.out.print(s);
        }

        return keyArr;
    }

}
