/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dm_genre_learning;

import java.util.Arrays;

/**
 *
 * @author ldtwo
 */
public class M {
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
    static int[] f = {};
    static double[] c = {};
    static double cmean = 0;//mean
    static double[] w = {};
    static double u = 0;//mean
    static double a = 0;//logb-sum-f

    public static double[] calc(int[] f_) {
        f = f_;
        c = new double[f.length];
        w = new double[f.length];
        sum();
        A();
        u=u/f.length;
        C();
        W();
        return w;
    }

    public static void A() {
        a = Math.log(u) / Math.log(b);
    }

    public static void sum() {
        u = 0;
        for (int i = 0; i < f.length; i++) {
            u += f[i];
        }
    }

    public static void C() {
        cmean=0;
        for (int i = 0; i < f.length; i++) {
            c[i] = ((f[i] - u) / u + 1) / 3;
            cmean+=c[i];
        }
        cmean=cmean/f.length;
    }

    public static void W() {
        double h;
        for (int i = 0; i < f.length; i++) {
            h = a * (c[i] - cmean);
            w[i] = h*(1+Math.pow(r, h));
        }
    }

    public static void main(String[] args) throws Exception {
        int[][] x={{0,0,1},{0,0,10},{0,0,100},{10,10,10},{400,400,400},{3,4,5},{30,40,50},{300,400,500}};
        for(int i=0;i<x.length;i++){
            System.out.printf("%s\n\t%s\n\n", Arrays.toString(x[i]),Arrays.toString(calc(x[i])));
            
            
        }
    }
}
