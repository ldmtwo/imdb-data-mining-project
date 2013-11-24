/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dm_genre_learning;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author ldtwo
 */
public class Metric {
    /*
     This weight measure is analogous to the attraction of two molecules.
     * Small molecules have little pull. 
     * Certain types of molecules have a stronger pull for a given 2nd molecule.
     * 
     * When molecule x is split between A and B, which wins?
     * 
     * Weights are also like temperature in Celcius. -inf to +inf scale
     */

    public static double[] genw;
    static double logBase = 19;//importance of higher certainty, more distinctiveness
    static double poweR = 4.5;//importance of higher frequency aka more evidence
    static int[] f = {};
    static double[] c = {};
    static double cmean = 0;//mean
    static double[] w = {};
    static double u = 0;//mean
    static double a = 0;//logb-sum-f

    static public String toStr() {
        return String.format("b=%s, r=%s", logBase, poweR); //To change body of generated methods, choose Tools | Templates.
    }

    public static double[] calc(int[] f_) {
        f = f_;
        c = new double[f.length];
        w = new double[f.length];
        sum();
        A();
        if (u <= 0) {
            for (int i = 0; i < f.length; i++) {
                w[i] = 0;
            }
        } else {

            cmean = 0;
            for (int i = 0; i < f.length; i++) {
                c[i] = ((f[i] / genw[i]) / u);// / 3;
                cmean += c[i];
            }
            cmean = cmean / f.length;

            u = u / f.length;

            double h;
//        System.out.printf("%s\n\t%s\n\t%s\n", Arrays.toString(f_), Arrays.toString(c), Arrays.toString(w));
            for (int i = 0; i < f.length; i++) {
//                h = a * (c[i] - cmean);
                w[i] = Math.log1p(f[i])* Math.pow(f[i]/genw[i], 1/7);
//                w[i] = Math.log1p(f[i])* Math.pow(c[i], poweR)/Math.log1p(logBase);
//                w[i] = f[i]* Math.pow(2, (Math.log1p(f[i])-a )/Math.log1p(logBase))/genw[i]/ u;
//        System.out.printf("%s = log(%s) * %s ^ %s / log(%s)\n",
//                       w[i], f[i]+1, c[i], poweR, logBase+1);
            }
        }
        return w;
    }
//    public static double[] calc(int[] f_) {
//        f = f_;
//        c = new double[f.length];
//        w = new double[f.length];
//        sum();
//        A();
//        if(u<=0){
//            for (int i = 0; i < f.length; i++)w[i]=0;            
//        }else{
//            u=u/f.length;
//            C();
//            W();
////               if(u>20){
////                   System.out.printf("%s\n\t%s\n", Arrays.toString(f),Arrays.toString(c));               
////                System.out.printf("u=%6s \t a=%6s \tcmean=%6s\n\t%s\n\n", u,a,cmean,Arrays.toString(w));
////               }
//        }
//        return w;
//    }

    public static void A() {
        a = Math.log1p(u) / Math.log1p(logBase);
    }

    public static void sum() {
        u = 0;
        for (int i = 0; i < f.length; i++) {
            u += f[i];
        }
    }

//    public static void C() {
//        cmean=0;
//        for (int i = 0; i < f.length; i++) {
//            c[i] = ((f[i] - u) / u ) ;
//            cmean+=c[i];
//        }
//    }
//
//    public static void W() {
//        double h;
//        for (int i = 0; i < f.length; i++) {
//            h = a * (c[i] - cmean);
//            w[i] = h*(1+Math.pow(r, h));
//            //w[i] = genw[i]*h*(1+Math.pow(r, h));
//        }
//    }
    public static void C() {
        cmean = 0;
        for (int i = 0; i < f.length; i++) {
            c[i] = ((f[i] - u) / u + 50);// / 3;
            cmean += c[i];
        }
        cmean = cmean / f.length;
    }
    final static double t = 0.9999;

    public static void W() {
        double h;
        for (int i = 0; i < f.length; i++) {
            h = a * (c[i] - cmean);
            //w[i] = h*(1+Math.pow(r, h));
            w[i] = h * (1 + Math.pow(poweR, h)) / Math.pow(genw[i], 2);
//            if(w[i]>0)w[i]=(1-t)*w[i]+t*Math.log(w[i]);
//            else w[i]=0;
        }
    }

    public static void main(String[] args) throws Exception {
        genw = new double[]{1, 1, 1};
        DecimalFormat df = new DecimalFormat("0.0");
        int[][] x = {
            //            {0,0,1},{0,0,10},
            //{10,10,10},{400,400,400},{3,4,5},{30,40,50},{300,400,500},
            //            {0,0,5},{0,0,50},{0,0,500},
            {0, 0, 5000},
            //{1,2,3},{11,12,13},{101,102,103},
            //            {0,200,200},{50,200,200},{100,200,200},
            {2400, 6300, 4000}};
        double[] B = {200, 50, 8};
        double[] R = {50, 10, 1, 0.5, 0.01};
        for (double i : B) {
            for (double j : R) {
                System.out.println(Metric.toStr());
                logBase = i;
                poweR = j;
                printTest(x);
            }
        }
    }

    private static void printTest(int[][] x) {
        System.out.println(Metric.toStr());
        for (int i = 0; i < x.length; i++) {
//            System.out.printf("%s\n\t%s\n\n", Arrays.toString(x[i]), Arrays.toString(calc(x[i])));

        }
    }
}
