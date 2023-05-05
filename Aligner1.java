import java.util.*;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
/**
 * IBM Model 1 Learner program
 *
 */
public class Aligner1 {

    ArrayList<ArrayList<String>> englishTrainSet;
    ArrayList<ArrayList<String>> foreignTrainSet;

    // count(f | e) key = f + " " + e
    HashMap<String, Double> countPairMap;
    // count(e)     key = e
    HashMap<String, Double> countMap;
    // p(f | e)     key = f + " " + e
    //Model 1
    HashMap<String, Double> tMap1;
    // p(f | e)     key = f + " " + e
    //Model 2
    HashMap<String, Double> tMap2;

    // c(i | j, l, m)
    // i = position of english word
    // j = position of foreign word
    // l = length of english sentence
    // m = length of foreign sentence
    // key = i+" "+j+" "+l+" "+m
    HashMap<String, Double> positionMap;

    // c(j, l, m)
    // j = position of foreign word
    // l = length of english words in a sentence
    // m = length of foreign sentence (will change based on sentences)
    // key = j+" "+l+" "+m
    HashMap<String, Double> totalMap;

    // q(i | j, l, m)
    HashMap<String, Double> aMap;
    public static void main(String[] args ){
        int numIterations = Integer.valueOf(args[2]);
        double threshold = Double.valueOf(args[3]);
        Aligner1 a = new Aligner1(args[0], args[1], numIterations, threshold);
    }

    /**
     * Aligns sentences from a set of english words and foreign words
     *
     * @param englishFile name of file with english words
     * @param foreignFile  name of file with foreign words
     * @param numIterations total number of iterations
     * @param threshold threshold for output p(f|e)
     * Write three files
     * IBM MODEL 1 translation parameter file
     * IBM MODEL 2 translation parameter file
     * IBM MODEL 2 alighment parameter file
     */
    public Aligner1(String englishFile, String foreignFile, int numIterations, double threshold) {
        englishTrainSet = new ArrayList<>();
        foreignTrainSet = new ArrayList<>();
        countPairMap = new HashMap<>();
        countMap = new HashMap<>();

        //Model 1 p(f/e)
        tMap1 = new HashMap<>();

        //Model 2 p(f/e)
        tMap2 = new HashMap<>();

        positionMap = new HashMap<>();
        totalMap = new HashMap<>();

        //Model 2 q(i|j l m)
        aMap = new HashMap<>();

        try {
            File eFile = new File("./data/" + englishFile);
            File fFile = new File("./data/" + foreignFile);
            BufferedReader eRead = new BufferedReader(new InputStreamReader(new FileInputStream(eFile), "UTF-8"));
            BufferedReader fRead = new BufferedReader(new InputStreamReader(new FileInputStream(fFile), "UTF-8"));

            String s1 = eRead.readLine();
            String s2 = fRead.readLine();

            // reads english and foreign sentences
            while(s1 != null && s2 != null) {
                String[] oneSplit = s1.split("\\s+");
                String[] twoSplit = s2.split("\\s+");

                // HashSet<String> h1 = new HashSet<>();
                // HashSet<String> h2 = new HashSet<>();

                ArrayList<String> h1 = new ArrayList<>();
                ArrayList<String> h2 = new ArrayList<>();

                

                for(String s : oneSplit) {

                    if (s.length() == 0) { 
                        continue; 
                    }
                    h1.add(s);
                }

                // adds null to each of english sentences
                h1.add("NULL");

                for(String s : twoSplit) {
                    if (s.length() == 0) { 
                        continue; 
                    }
                    h2.add(s);
                }

                englishTrainSet.add(h1);
                foreignTrainSet.add(h2);

                s1 = eRead.readLine();
                s2 = fRead.readLine();
            }
            eRead.close();
            fRead.close();

            // initalization constant
            double constant =0.1;

            /* IBM MODEL 1 */

            for (int i = 0; i < numIterations; i++) {
                // iterate over sentences
                for (int j = 0; j < englishTrainSet.size(); j++) {
                    ArrayList<String> eSet = englishTrainSet.get(j);
                    ArrayList<String> fSet = foreignTrainSet.get(j);

                    for (String eWord : eSet) {
                        for (String fWord : fSet) {
                            String temp = fWord + " " + eWord;
                            String[] list = temp.split("\\s+"); 
                            if (list.length == 0)  {
                                System.out.println("Print eset and fset: " + eSet + " " + fSet); 
                            }
                            double sum = 0.0;
                            for (String eWord2 : eSet) {
                                // sigma += p(f|e)
                                sum += i == 0 ? constant : tMap1.get(fWord + " " + eWord2);
                            }
    
                            // p(f -> e) = p(f|e) / sigma
                            double pArrow = (i == 0 ? constant : tMap1.get(fWord + " " + eWord)) / sum;
                            // count(e, f) += p(f -> e)

                            if (countPairMap.containsKey(fWord + " " + eWord)) {
                                countPairMap.put(fWord + " " + eWord, countPairMap.get(fWord + " " + eWord) + pArrow);
                            } else {
                                countPairMap.put(fWord + " " + eWord, pArrow);
                            }

                            //countPairMap.put(fWord + " " + eWord,
                                    //countPairMap.getOrDefault(fWord + " " + eWord, 0.0) + pArrow);
                            // count(e) += p(f -> e)

                            if (countMap.containsKey(eWord)) {
                                countMap.put(eWord, countMap.get(eWord) + pArrow);
                            } else {
                                countMap.put(eWord, pArrow);
                            }

                            //countMap.put(eWord, countMap.getOrDefault(eWord, 0.0) + pArrow);
                        }
                    }//showing the progress
                    if(j%100 ==0){
                        System.out.print("*");
                    }
                }
                System.out.println("\nModel1 Iteration "+ (i+1) + " E-Step DONE");
                // p(f | e) = count(e, f) / count(e)
                for (Map.Entry<String, Double> entry : countPairMap.entrySet()) {
                    String key = entry.getKey();

                    String[] split = key.split("\\s+");
                    String fTemp = split[0];
                    String eTemp = split[1];
                    tMap1.put(key, countPairMap.get(key) / countMap.get(eTemp));
                }
                System.out.println("Model1 Iteration "+ (i+1) + " M-Step DONE");

                // clear counts after each iterations
                countPairMap.clear();
                countMap.clear();
            }
            System.out.println("Model1 Traning DONE. Now start to write a model file.");

            //write model 1 in file
            try {

                String s = "./data/" + foreignFile + "_iter"+numIterations+"_tmap"+"_model1";
                FileWriter myWriter = new FileWriter(s);
                //write fWord + eWord = prob
                for(Map.Entry<String, Double> entry : tMap1.entrySet()) {
                    String line = entry + "\n";
                    myWriter.write(line);
                }
                System.out.println("Model1 Translation prob. file written.");

                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

            // IBM MODEL 2
            for(int i = 0; i < numIterations; i++) {
                // iterate over sentences
                for(int j = 0; j < englishTrainSet.size(); j++) {

                    ArrayList<String> eSet = englishTrainSet.get(j);
                    ArrayList<String> fSet = foreignTrainSet.get(j);

                    int english_length = eSet.size()-1;//note that eSet has NULL at the end
                    int foreign_length = fSet.size();

                    for(int eIndex = 0; eIndex < eSet.size(); eIndex++) {
                        String eWord = eSet.get(eIndex);
                        double sum = 0.0;
                        for(int fIndex = 0; fIndex < fSet.size(); fIndex++) {
                            // q(i | j l m) = 1 / (l + 1) initial value
                            double aVal = 1 / ((double)english_length+1);


                            String fWord = fSet.get(fIndex);
                            for(int temp = 0 ; temp < eSet.size(); temp++) {
                                String eWord2 = eSet.get(temp);

                                // i j l m
                                String positionWord = String.valueOf(temp) + " " + String.valueOf(fIndex) + " "
                                        + String.valueOf(english_length) + " " + String.valueOf(foreign_length);

                                // sum += p(f|e) * a(i | j l m)
                                // here note that for initial value, we use the learning from Model 1
                                sum += (i == 0 ? tMap1.get(fWord+" "+eWord2): tMap2.get(fWord + " " + eWord2)) * (i == 0 ? aVal : aMap.get(positionWord));
                            }

                            // i j l m
                            String positionWord = String.valueOf(eIndex) + " " + String.valueOf(fIndex) + " "
                                    + String.valueOf(english_length) + " " + String.valueOf(foreign_length);

                            // j l m
                            String modPositionWord = String.valueOf(fIndex) + " " + String.valueOf(english_length) + " "
                                    + String.valueOf(foreign_length);

                            double c = (i == 0 ? tMap1.get(fWord+" "+eWord): tMap2.get(fWord + " " + eWord)) * (i == 0 ? aVal : (aMap.get(positionWord))) / sum;

                            // count(e, f) += c
                            countPairMap.put(fWord + " " + eWord, countPairMap.getOrDefault(fWord + " " + eWord, 0.0) + c);

                            // count(e) += c
                            countMap.put(eWord, countMap.getOrDefault(eWord, 0.0) + c);

                            // count(i | j, l, m) += c
                            positionMap.put(positionWord, positionMap.getOrDefault(positionWord, 0.0)+c);

                            // total(j, l, m) += c
                            totalMap.put(modPositionWord, totalMap.getOrDefault(modPositionWord, 0.0) + c);
                        }
                    }
                    //showing the progress
                    if(j%100 ==0){
                        System.out.print(".");
                    }


                }

                System.out.println("\nModel2 Iteration "+ (i+1) + " E-Step DONE");

                // p(f | e) = count(e, f) / count(e)
                for (Map.Entry<String, Double> entry : countPairMap.entrySet()) {
                    String key = entry.getKey();
                    String[] split = key.split("\\s+");
                    String fTemp = split[0];
                    String eTemp = split[1];
                    tMap2.put(key, countPairMap.get(key) / countMap.get(eTemp));
                }

                for(Map.Entry<String, Double> entry : positionMap.entrySet()) {
                    String key = entry.getKey();
                    String[] split = key.split("\\s+");
                    String eIndex = split[0];
                    String fIndex = split[1];
                    String eLength = split[2];
                    String fLength = split[3];

                    aMap.put(key, positionMap.get(key) / totalMap.get(fIndex + " " + eLength + " " + fLength));
                }
                System.out.println("Model2 Iteration "+ (i+1) + " M-Step DONE");

                // clear counts after each iteration
                countPairMap.clear();
                countMap.clear();
                positionMap.clear();
                totalMap.clear();
            }

/* for testing
            for(int index = 0; index < englishTrainSet.size(); index++) {
                ArrayList<String> eSet = englishTrainSet.get(index);
                ArrayList<String> fSet = foreignTrainSet.get(index);
                for(int i = 0; i < eSet.size(); i++) {
                    String eWord = eSet.get(i);
                    for(int j = 0; j < fSet.size(); j++) {
                        String fWord = fSet.get(j);

                        String positionWord = String.valueOf(i) + " " + String.valueOf(j) + " " + String.valueOf(eSet.size()) + " " + String.valueOf(fSet.size());
                        String st = fWord+" "+eWord;
                        double probability = aMap.get(positionWord)*tMap2.get(st);
                        if(probability < threshold) continue;

                        System.out.println(eWord + "\t" + fWord + "\t" + probability);
                    }
                }
            }
*/
            // sorts output alphabetically;
            /*
            PriorityQueue<String> q = new PriorityQueue<>(new SomeComparator());

            for(Map.Entry<String, Double> entry : tMap2.entrySet()) {
                q.offer(entry.getKey());
            }

            while(!q.isEmpty()) {
                String key = q.poll();
                double prob0 = tMap1.get(key);
                double prob = tMap2.get(key);

                // checks threshold
                if(prob < threshold) continue;
                String[]split = key.split("\\s+");
                String fWord = split[0];
                String eWord = split[1];
                //System.out.println("MODEL1: " + eWord + "\t" + fWord + "\t" + prob0);
                //System.out.println("MODEL2: " +  eWord + "\t" + fWord + "\t" + prob);

            }
            */


            System.out.println("Model2 Traning DONE. Now start to write a model file.");

            //write model 2 in file
            try {

                String s = "./data/" + foreignFile + "_iter"+numIterations+"_tmap"+"_model2";
                FileWriter myWriter = new FileWriter(s);
                //write f + e + prob
                for(Map.Entry<String, Double> entry : tMap2.entrySet()) {
                    String line = entry + "\n";
                    myWriter.write(line);
                }
                System.out.println("Model2 Translation prob. file written.");

                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }

            try {

                String s = "./data/" + foreignFile + "_iter"+numIterations+"_amap"+"_model2";
                FileWriter myWriter = new FileWriter(s);
                //write i j l m = prob
                for(Map.Entry<String, Double> entry : aMap.entrySet()) {
                    String line = entry + "\n";
                    myWriter.write(line);
                }
                System.out.println("Model2 Alignment prob. file written.");

                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }

    }

}

class SomeComparator implements Comparator<String> {
    public int compare (String s1, String s2) {
        String[] split1 = s1.split("\\s+");
        String[] split2 = s2.split("\\s+");
        int result = split1[1].compareTo(split2[1]);
        return result == 0 ? split1[0].compareTo(split2[0]) : result ;
    }
}
