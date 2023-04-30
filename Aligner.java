import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import javax.xml.crypto.dsig.SignatureMethod;
import java.util.regex.Matcher;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

public class Aligner {

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    ArrayList<HashSet<String>> englishWords;
    ArrayList<HashSet<String>> foreignWords;

    // count(f | e) key = f + " " + e
    HashMap<String, Double> countPairMap;
    // count(e)     key = e  
    HashMap<String, Double> countMap;
    // p(f | e)     key = f + " " + e    
    HashMap<String, Double> alignMap;
    public static void main(String[] args ){
        int numIterations = Integer.valueOf(args[2]);
        double threshold = Double.valueOf(args[3]);
        Aligner a = new Aligner(args[0], args[1], numIterations, threshold);

    }

    public Aligner(String englishFile, String foreignFile, int numIterations, double threshold) {
        englishWords = new ArrayList<>();
        foreignWords = new ArrayList<>();
        countPairMap = new HashMap<>();
        countMap = new HashMap<>();
        alignMap = new HashMap<>();

        // args[0] = english sentences
        // args[1] = foreign sentences
        // args[2] = iterations
        // args[3] = probability_threshold


        try {
            File eFile = new File("./data/" + englishFile);
            File fFile = new File("./data/" + foreignFile);
            BufferedReader eRead = new BufferedReader(new InputStreamReader(new FileInputStream(eFile), "UTF-8"));
            BufferedReader fRead = new BufferedReader(new InputStreamReader(new FileInputStream(fFile), "UTF-8"));

            String s1 = eRead.readLine();
            String s2 = fRead.readLine();

            while(s1 != null && s2 != null) {
                String[] oneSplit = s1.split("\\s+");
                String[] twoSplit = s2.split("\\s+");

                HashSet<String> h1 = new HashSet<>();
                HashSet<String> h2 = new HashSet<>();

                for(String s : oneSplit) {
                    h1.add(s);
                }

                h1.add("NULL");

                for(String s : twoSplit) {
                    h2.add(s);
                }

                englishWords.add(h1);
                foreignWords.add(h2);

                s1 = eRead.readLine();
                s2 = fRead.readLine();
            }
            eRead.close();
            fRead.close();

            double constant = 1;
            double sigma = 0.0; 
            for(int i = 0; i < numIterations; i++) {
                for(int j = 0; j < englishWords.size(); j++) {
                    HashSet<String> eSet = englishWords.get(j);
                    HashSet<String> fSet = foreignWords.get(j);

                    for(String eWord: eSet) {
                        
                        for(String fWord : fSet) {
                            double sum = 0.0;
                            for(String eWord2: eSet) {
                                sum += i == 0 ? constant : alignMap.get(fWord + " " + eWord2);
                            }

                            double pArrow = (i == 0 ? constant : alignMap.get(fWord + " " + eWord)) / sum;
                            countPairMap.put(fWord + " " + eWord, countPairMap.getOrDefault(fWord + " " + eWord, 0.0) + pArrow);
                            countMap.put(eWord, countMap.getOrDefault(eWord, 0.0) + pArrow);
                        }
                    }
                }
                
                for (Map.Entry<String, Double> entry : countPairMap.entrySet()) {
                    String key = entry.getKey();
                    String[] split = key.split("\\s+");
                    String fTemp = split[0];
                    String eTemp = split[1];

                    if (alignMap.get(key) != null) {
                        double hi = alignMap.get(key); 
                        double temp = countPairMap.get(key) / countMap.get(eTemp); 
                        double shit = Math.abs(temp - hi); 
                        sigma += shit; 
                        //System.out.println("hi: " + hi); 
                        //System.out.println("temp: " + temp); 
                        //System.out.println("shit: " + shit); 
                        
                        
                        
                    }
                    


                    alignMap.put(key, countPairMap.get(key) / countMap.get(eTemp));
                    
                }

                


                countPairMap.clear();
                countMap.clear();
                //System.out.println("sugma: " + sigma + " iteration: " + i); 
                sigma = 0.0; 
            }

            

            PriorityQueue<String> q = new PriorityQueue<>(new SomeComparator());

            for(Map.Entry<String, Double> entry : alignMap.entrySet()) {
                q.offer(entry.getKey());
            }
            while(!q.isEmpty()) {
                String key = q.poll();
                double prob = alignMap.get(key);
                if(prob < threshold) continue;
                String[]split = key.split("\\s+");
                String fWord = split[0];
                String eWord = split[1];

                int ass = 0; 
                ass = getRandomNumber(0, 200); 
                if (ass == 1) { 
                    System.out.println(eWord + "\t" + fWord + "\t" + prob);
                }
                
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