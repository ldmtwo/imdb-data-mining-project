/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package old;

import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author ldtwo
 */
public class Metric2 {
/*
 This weight measure is analogous to the attraction of two molecules.
 * Small molecules have little pull. 
 * Certain types of molecules have a stronger pull for a given 2nd molecule.
 * 
 * When molecule x is split between A and B, which wins?
 * 
 * Weights are also like temperature in Celcius. -inf to +inf scale
 */
    final static double b = 2.5;
    final static double r = 1.05;
    static HashMap<Integer,Integer> f = new HashMap<Integer, Integer>();
    static HashMap<Integer,Double> c = new HashMap<Integer, Double>();
    static HashMap<Integer,Double> w = new HashMap<Integer, Double>();
    static double cmean = 0;//mean
    static double u = 0;//mean
    static double a = 0;//logb-sum-f
static int size=0;
    public static HashMap<Integer,Double> calc(HashMap<Integer,Integer> f_) {
        f = f_;
        size=f_.keySet().size();
        sum();
        A();
        u=u/size;
        C();
            //System.out.printf("%s\n\t%s\n", Arrays.toString(f),Arrays.toString(c));
        W();
            //System.out.printf("%s\n\t%s\n\n", Arrays.toString(f),Arrays.toString(w));
        return w;
    }


    public static void A() {
        a = Math.log(u) / Math.log(b);
    }

    public static void sum() {
        u = 0;
        for (Integer i:f.values()) {
            u += i;
        }
    }

    public static void C() {
        cmean=0;double k;
        for (Integer i:f.keySet())  {
            k=((f.get(i) - u) / u + 1) / 3;
            c.put(i,k);
            cmean+=k;
        }
        cmean=cmean/size;
    }

    public static void W() {
        double h;
        for (Integer i:f.keySet()) {
            h = a * (c.get(i) - cmean);
            w.put(i, h*(1+Math.pow(r, h)));
        }
    }

    public static void main(String[] args) throws Exception {
        HashMap<Integer,Integer> f;
        int[][] X={{0,0,1},{0,0,10},{0,0,100},{10,10,10},{400,400,400},{3,4,5},{30,40,50},{300,400,500}};
        for (int[] x1 : X) {
            f=new HashMap<Integer, Integer>();
            for (int j = 0; j < x1.length; j++) {
                f.put(j, x1[j]);
            }
            calc(f);
            System.out.printf("%s\n\t%s\n\n", Arrays.toString(x1), w.values());
        }
    }
}
