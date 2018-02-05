import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.stream.Stream;

public class NaiveBayesClassifier {
    private Vector<Person> people = new Vector<>();

    public NaiveBayesClassifier(Vector<Person> people) {
        this.people = people;
    }

    public void getFileInformation() {
        try (Stream<String> stream = Files.lines(Paths.get(Constants.FILE_DIRECTORY))) {
            stream.filter(line -> isDataLine(line)).forEach(line -> readLine(line));
        } catch (FileNotFoundException fnf) {
            System.out.println("File not found!");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public boolean isDataLine(String line) {
        return (line.charAt(0) == '\'' || line.charAt(0) == '?');
    }

    public void readLine(String line) {
        Person person = new Person();
        int i = 0;
        for (int k = 0; k < line.lastIndexOf(','); k++) {
            if (line.charAt(k) == 'n' || line.charAt(k) == 'y' || line.charAt(k) == '?') {
                person.attributes[i++] = line.charAt(k);
            }
        }

//        for (int k = line.lastIndexOf(',') + 2; k < line.length() - 1; k++) {
//            person.className+= line.charAt(k);
//        }
        person.className = line.substring(line.lastIndexOf(',') + 2, line.length() - 1);
        people.add(person);
    }

    public void dataSeparationAndSolution() {
        int intervalsSize = Constants.NUM_OF_INSTANCES / Constants.SETS;
        int intervalsSizeRest = Constants.NUM_OF_INSTANCES % Constants.SETS;

        //data separation: 10 sets - first 5 sets with 44 elements and the rest 5 - with 43 elements
        //intervalDimension keeps the number of people in each of 10 sets
        Vector<Integer> intervalDimension = new Vector<>();
        for (int i = 0; i < Constants.SETS; i++) {
            intervalDimension.add(intervalsSize);
        }

        for (int i = 0; i < intervalsSizeRest; i++) {
            int newValue = intervalDimension.elementAt(i) + 1;
            intervalDimension.remove(i);
            intervalDimension.add(i, newValue);
        }

        int startDimension = 0;
        double sumAccuracy = 0.0;

        for (int setNum = 0; setNum < Constants.SETS; setNum++) {
            //[left, right] = [0, 34]; [35, 69]; etc
            int left = startDimension;
            int right = startDimension + intervalDimension.elementAt(setNum) - 1;

            int republicans = countPeoplePerClass("republican", left, right);
            int democrats = countPeoplePerClass("democrat", left, right);

            //compute probability
            double pRepublican = (double) (republicans / (republicans + democrats));
            double pDemocrat   = (double) (democrats / (republicans + democrats));

            //inspect results from data
            Vector<Vector<Double>> republicansResults = new Vector<>();
            Vector<Vector<Double>> democratsResults = new Vector<>();
            for (int attributeNum = 0; attributeNum < Constants.ATTRIBUTES; attributeNum++) {
                int countNRepublicans = 0;
                int countYRepublicans = 0;
                int countNDemocrats = 0;
                int countYDemocrats = 0;

                for (int peopleNum = 0; peopleNum < Constants.NUM_OF_INSTANCES; peopleNum++ ) {
                    if (peopleNum < left || peopleNum > right) {
                        if (people.elementAt(peopleNum).className.equals("republican")) {
                            if (people.elementAt(peopleNum).attributes[attributeNum] == 'n') countNRepublicans++;
                            if (people.elementAt(peopleNum).attributes[attributeNum] == 'y') countYRepublicans++;
                        } else {
                            if (people.elementAt(peopleNum).attributes[attributeNum] == 'n') countNDemocrats++;
                            if (people.elementAt(peopleNum).attributes[attributeNum] == 'y') countYDemocrats++;
                        }
                    }
                }

                Vector<Double> results = new Vector<>();
                results.add(0, (double)countNRepublicans / (double)republicans);
                results.add(1, (double)countYRepublicans / (double)republicans);
                republicansResults.add(results);

                results.add(0, (double)countNDemocrats / (double)democrats);
                results.add(1, (double)countYDemocrats/ (double)democrats);
                democratsResults.add(results);
            }

            int match = 0;
            for (int interval = left; interval <= right; interval++ ) {
                double pCurrentTestRepublican = 1.0;
                double pCurrentTestDemocrat = 1.0;
                for (int attributeNum = 0; attributeNum < Constants.ATTRIBUTES; attributeNum++) {
                    if (people.elementAt(interval).attributes[attributeNum] != '?' ) {
                        for (int i = 0; i < 2; i++) {
                            pCurrentTestRepublican *= republicansResults.elementAt(attributeNum)
                                    .elementAt(i);
                            pCurrentTestDemocrat *= democratsResults.elementAt(attributeNum)
                                    .elementAt(i);
                        }
                    }
                }
                pCurrentTestRepublican *= pRepublican;
                pCurrentTestDemocrat *= pDemocrat;

                if (pCurrentTestRepublican - pCurrentTestDemocrat < Constants.epsilon ) {
                    if (people.elementAt(interval).className.equals("democrat"))
                        match++;
                } else {
                    if (people.elementAt(interval).className.equals("republican"))
                        match++;
                }
            }
            double accuracy = ((double)match / intervalDimension.elementAt(setNum)) * 100.0;
            sumAccuracy += accuracy;
            System.out.println("ACCURANCY: " + accuracy + "% FOR TEST-SET NUMBER " + setNum);

            startDimension += intervalDimension.elementAt(setNum);
        }
        System.out.println("AVARAGE ACCURANCY: " + sumAccuracy / Constants.SETS);
    }

    public int countPeoplePerClass(String className, int left, int right) {
        int count = 0;
        for (int i = 0; i < Constants.NUM_OF_INSTANCES; i++) {
            if (i < left || i > right) {
                if (people.elementAt(i).className.equals(className))
                    count++;
            }
        }
        return count;
    }

    public void printPeople() {
        people.forEach(person -> printPerson(person));
    }

    public void printPerson(Person person) {
        for (int i = 0; i < Constants.ATTRIBUTES; i++) {
            System.out.print(person.attributes[i] + " ");
        }
        System.out.println(person.className);
        System.out.println("");
    }
}
