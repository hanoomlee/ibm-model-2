//package code.nlp.parser;
import java.util.Comparator;

public class Pair {
    public String key;
    public Double value;

    public Pair(String key, Double value) {
        this.key = key;
        this.value = value;
    }
}

class PairComparator implements Comparator<Pair> {
    public int compare(Pair p1, Pair p2) {
        if (p1.value > p2.value) {
            return 1;
        } else if (p1.value < p2.value) {
            return -1;
        } else {
            return 0;
        }
    }
}
