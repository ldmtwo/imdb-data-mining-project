/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dm_genre_learning;

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
    final static double b = 2.3;
    final static double r = 1.1;
    static int[] f = {};
    static double[] c = {};
    static double cmean = 0;//mean
    static double[] w = {};
    static double u = 0;//mean
    static double a = 0;//logb-sum-f

    static      public String toStr() {
        return String.format("b=%s, r=%s",b,r); //To change body of generated methods, choose Tools | Templates.
    }

    public static double[] calc(int[] f_) {
        f = f_;
        c = new double[f.length];
        w = new double[f.length];
        sum();
        A();
        if(u<=0){
            for (int i = 0; i < f.length; i++)w[i]=0;            
        }else{
            u=u/f.length;
            C();
            W();
//               if(u>20){
//                   System.out.printf("%s\n\t%s\n", Arrays.toString(f),Arrays.toString(c));               
//                System.out.printf("u=%6s \t a=%6s \tcmean=%6s\n\t%s\n\n", u,a,cmean,Arrays.toString(w));
//               }
        }
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
            c[i] = ((f[i] - u) / u ) ;
            cmean+=c[i];
        }
    }

    public static void W() {
        double h;
        for (int i = 0; i < f.length; i++) {
            h = a * (c[i] - cmean);
            w[i] = h*(1+Math.pow(r, h));
            //w[i] = genw[i]*h*(1+Math.pow(r, h));
        }
    }
//        public static void C() {
//        cmean=0;
//        for (int i = 0; i < f.length; i++) {
//            c[i] = ((f[i] - u) / u + 1) / 3;
//            cmean+=c[i];
//        }
//        cmean=cmean/f.length;
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

    public static void main(String[] args) throws Exception {
        int[][] x={{0,0,1},{0,0,10},{0,0,100},{10,10,10},{400,400,400},{3,4,5},{30,40,50},{300,400,500}};
        for(int i=0;i<x.length;i++){
            System.out.printf("%s\n\t%s\n\n", Arrays.toString(x[i]),Arrays.toString(calc(x[i])));
            
            
        }
    }
}
