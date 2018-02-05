import java.util.Collections;
import java.util.Vector;

public class Main {

    public static void main(String[] args) {
        Vector<Person> people = new Vector<>();
        NaiveBayesClassifier nbc = new NaiveBayesClassifier(people);
        nbc.getFileInformation();
//        nbc.printPeople();
        Collections.shuffle(people);
        nbc.dataSeparationAndSolution();
    }
}
