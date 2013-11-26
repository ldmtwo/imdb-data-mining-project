/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old;

 class II extends Number{
        int i,j;

        public II(int i, int j) {
            this.i = i;
            this.j = j;
        }
        
        @Override
        public boolean equals(Object obj) {
            II o=(II) obj;
            return i==o.i&&j==o.j; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int hashCode() {
            return i*j; //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int intValue() {
            return i*j;
        }

        @Override
        public long longValue() {
            return i*j;
        }

        @Override
        public float floatValue() {
            return i*j;
        }

        @Override
        public double doubleValue() {
            return i*j;
        }
        
    }