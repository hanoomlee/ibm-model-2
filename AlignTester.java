import java.util.*;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
/*
 *
 * Given test parallel corpus, produce aligment for IBM Model 1 and 2
 */
public class AlignTester {

    ArrayList<ArrayList<String>> englishTrainSet;
    ArrayList<ArrayList<String>> foreignTrainSet;

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
    // q(i | j, l, m)
    HashMap<String, Double> aMap;
    /**
     * @param englishFile name of file with english words
     * @param foreignFile  name of file with foreign words
     * @param model1t: IBM MODEL 1 translation parameter file p(f|e)
     * @param model2t: IBM MODEL 2 translation parameter file p(f|e)
     * @param model2a: IBM MODEL 2 translation parameter file q(i|j l m)
     * print out MODEL 1's alignment for each sentence as well as MODEL 2's
     */

    public AlignTester(String englishFile, String foreignFile, String model1t, String model2t, String model2a) {
        englishTrainSet = new ArrayList<>();
        foreignTrainSet = new ArrayList<>();

        //Model 1 p(f/e)
        tMap1 = new HashMap<>();

        //Model 2 p(f/e)
        tMap2 = new HashMap<>();

        //Model 2 q(i|j l m)
        aMap = new HashMap<>();


        //loading key and value (translation) for model 1
        try {

            File t1file = new File("./data/" + model1t);
            BufferedReader t1Read = new BufferedReader(new InputStreamReader(new FileInputStream(t1file), "UTF-8"));

            String t1 = t1Read.readLine();

            while (t1 != null) {

                String[] list = t1.split("=");
                String key = list[0];
                //System.out.println("HIHIHI: " + list[1]); 
                Double value = Double.parseDouble(list[1]);
                
                tMap1.put(key, value);

                t1 = t1Read.readLine();
            }
            /*
            for(Map.Entry<String, Double> entry : tMap1.entrySet()) {
                System.out.println("Entry: " + entry);
            }
            */
            System.out.println("MODEL 1 TRANSLATION DONE"); 
        }  catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //loading key and value (translation) for model 2
        try {

            File t2file = new File("./data/" + model2t);
            BufferedReader t2Read = new BufferedReader(new InputStreamReader(new FileInputStream(t2file), "UTF-8"));

            String t2 = t2Read.readLine();

            while (t2 != null) {

                String[] list = t2.split("=");
                String key = list[0];
                Double value = Double.parseDouble(list[1]);
                tMap2.put(key, value);

                t2 = t2Read.readLine();
            }
            System.out.println("MODEL 2 LOADING TRANSLATION DONE"); 
            /*
            for(Map.Entry<String, Double> entry : tMap2.entrySet()) {
                System.out.println("Entry: " + entry);
            }
            */
        }  catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //loading key and value (alignment) for model 2
        try {

            File tafile = new File("./data/" + model2a);
            BufferedReader taRead = new BufferedReader(new InputStreamReader(new FileInputStream(tafile), "UTF-8"));

            String ta = taRead.readLine();

            while (ta != null) {

                String[] list = ta.split("=");
                String key = list[0];
                Double value = Double.parseDouble(list[1]);
                aMap.put(key, value);

                ta = taRead.readLine();
            }
            System.out.println("ALIGNMENT DONE"); 

            /* 
            for(Map.Entry<String, Double> entry : aMap.entrySet()) {
                System.out.println("Entry: " + entry);
            }
            */ 

        }  catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

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
                    h1.add(s);
                }

            // adds null to each of english sentences
                h1.add("NULL");


                for(String s : twoSplit) {
                    h2.add(s);
                }

                englishTrainSet.add(h1);
                foreignTrainSet.add(h2);

                s1 = eRead.readLine();
                s2 = fRead.readLine();
            }
            eRead.close();
            fRead.close();
            System.out.println("reading test files done"); 
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //predict alignment for model1
        for (int k = 0; k < englishTrainSet.size(); k++) {

            ArrayList<String> eSet = englishTrainSet.get(k);
            ArrayList<String> fSet = foreignTrainSet.get(k);
            //System.out.println("hi"); 
            System.out.println(k+1 + " English sentence: " + eSet);
            System.out.println(k+1 + " Foreign sentence: " + fSet);

            System.out.println("Model1");

            //for each foreign word, we find the max prob english word (alignment)
            for (int j = 0; j < fSet.size(); j++) {
                //Model 1
                Double max_value = 0.0;
                int max_index = 0;

                //look at all english words to find max
                for (int i = 0; i < eSet.size(); i++) {
                    String key = fSet.get(j) + " " + eSet.get(i);
                    //System.out.println("key: " + key); 

                    Double value = 0.0;
                    //System.out.println(value); 
                    if (tMap1.containsKey(key)) { 
                        value = tMap1.get(key); 
                    } else { 

                    }

                    //System.out.println("value: " + value); 


                    if (value > max_value) {
                        max_index = i;
                        max_value = value;
                    }
                }
                System.out.print(max_index + " ");
            }
            System.out.print("\n");

            System.out.println("Model2");
            //for each foreign word, we find the max prob english word (alignment)
            for (int j = 0; j < fSet.size(); j++) {
                //Model 1
                Double max_value = 0.0;
                int max_index = 0;

                //look at all english words to find max
                for (int i = 0; i < eSet.size(); i++) {
                    String tkey = fSet.get(j) + " " + eSet.get(i);
                    Double tvalue = 0.0; 
                    if (tMap2.containsKey(tkey)) { 
                        tvalue = tMap2.get(tkey);
                    }
                    

                    String akey = i + " " + j + " " + (eSet.size()-1) + " " + fSet.size();

                    Double avalue = 0.0; 
                    if (aMap.containsKey(akey)) { 
                        avalue = aMap.get(akey);
                    }

                    Double value = tvalue * avalue;

                    if (value > max_value) {
                        max_index = i;
                        max_value = value;
                    }
                }
                System.out.print(max_index + " ");
            }
            System.out.print("\n");
        }









    }


    public static void main(String[]args) {

        String model1t = args[0];
        String model2t = args[1];
        String model2a = args[2];
        String test_en = args[3];
        String test_fr = args[4];


        AlignTester test = new AlignTester(test_en, test_fr, model1t, model2t, model2a);




    }




}


