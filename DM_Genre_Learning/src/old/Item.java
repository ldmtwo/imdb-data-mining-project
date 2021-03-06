





package old;

public class Item implements Comparable<Item> {

    public double merit = 1, rank = 1;
    public String name;
    public int cnt=0;

    public Item(String name) {
        this.name = name;
    }

    public int compareTo(Item o) {
        return o.merit < this.merit ? -1 :o.merit == this.merit?0: 1;
//        return o.rank > this.rank ? -1 :o.rank == this.rank?0: 1;
    }

    @Override
    public String toString() {
        return String.format("%6s %6s %s",(int) merit, (int) rank, name);
    }
    
}
