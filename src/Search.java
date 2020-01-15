import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javafx.util.Pair;

public class Search extends Invert{

    public ArrayList<Pair<Integer, Double>> allDocs= new ArrayList<>();
    public HashMap<Integer, ArrayList<Pair<String, Double>>> calculatedWeights = new HashMap<>();



    public void search(Invert invert, boolean stem){
        allDocs = new ArrayList<>();
        String query = "";

        query = JOptionPane.showInputDialog("Enter Query");
        System.out.println(query);
        query = query.toLowerCase();
        String[] q = query.split(" ");
        if(stem) {
            for (int i = 0; i < q.length; i++) {
                q[i] = invert.stemWord(q[i]);
            }
        }
        ArrayList<Pair<String, Double>> queryTF = getTF(q);
        ArrayList<Pair<String, Double>> qWeight = genVector(queryTF, invert.idfVals);
        double qNorm = normalizeVector(qWeight);
        printSDArray(qWeight);

        HashMap<Integer, ArrayList<String>> docInfo = invert.docInfo;

        for(int i = 1; i < docInfo.size(); i++){

            if(docInfo.containsKey(i) && !calculatedWeights.containsKey(i)) {
                ArrayList<String> temp = docInfo.get(i);
                String[] terms = new String[temp.size()];
                terms = temp.toArray(terms);

                ArrayList<Pair<String, Double>> docTF = getTF(terms);
                ArrayList<Pair<String, Double>> docWeight = genVector(docTF, invert.idfVals);
                double docNorm = normalizeVector(docWeight);

                calculatedWeights.put(i, docWeight);

                double simVal = calcSimVal(docWeight, qWeight, docNorm, qNorm);

                Pair<Integer, Double> add = new Pair(i , simVal);
                allDocs.add(add);

            }
            else if(calculatedWeights.containsKey(i)){
                double simVal = calcSimVal(calculatedWeights.get(i) , qWeight , normalizeVector(calculatedWeights.get(i)), qNorm);
                Pair<Integer, Double> add = new Pair(i, simVal);
                allDocs.add(add);
            }
        }

        getTop(allDocs);

        for(int i =0; i < 10; i++){
            if(allDocs.get(i).getValue() != 0) {
                System.out.println(allDocs.get(i).getKey() + " : " + allDocs.get(i).getValue() + " Title: "+ invert.titles.get(allDocs.get(i).getKey())+ " Authors: " + invert.authors.get(allDocs.get(i).getKey()));
            }
        }


        System.out.println("Search is done");


    }
    public ArrayList<Integer> search(Invert invert, String query, boolean stem){

            allDocs = new ArrayList<>();
           // query = JOptionPane.showInputDialog("Enter Query");
           // System.out.println(query);
            query = query.toLowerCase();
            String[] q = query.split(" ");
            if(stem) {
                for (int i = 0; i < q.length; i++) {
                    q[i] = invert.stemWord(q[i]);
                }
            }
            ArrayList<Pair<String, Double>> queryTF = getTF(q);
            //printSDArray(queryTF);
            ArrayList<Pair<String, Double>> qWeight = genVector(queryTF, invert.idfVals);
            double qNorm = normalizeVector(qWeight);

           //printSDArray(qWeight);

            HashMap<Integer, ArrayList<String>> docInfo = invert.docInfo;

        for(int i = 1; i < docInfo.size(); i++){

            if(docInfo.containsKey(i) && !calculatedWeights.containsKey(i)) {
                ArrayList<String> temp = docInfo.get(i);
                String[] terms = new String[temp.size()];
                terms = temp.toArray(terms);

                ArrayList<Pair<String, Double>> docTF = getTF(terms);
                ArrayList<Pair<String, Double>> docWeight = genVector(docTF, invert.idfVals);
                double docNorm = normalizeVector(docWeight);

                calculatedWeights.put(i, docWeight);

                double simVal = calcSimVal(docWeight, qWeight, docNorm, qNorm);

                Pair<Integer, Double> add = new Pair(i , simVal);
                allDocs.add(add);

            }
            else if(calculatedWeights.containsKey(i)){
                double simVal = calcSimVal(calculatedWeights.get(i) , qWeight , normalizeVector(calculatedWeights.get(i)), qNorm);
                Pair<Integer, Double> add = new Pair(i, simVal);
                allDocs.add(add);
            }
        }

            getTop(allDocs);


            System.out.println("Search is done");


           ArrayList<Integer> result = new ArrayList<>();
           for(int i = 0; i < 10; i++){
               result.add(allDocs.get(i).getKey());
           }
        for(int i =0; i < 10; i++){
            if(allDocs.get(i).getValue() != 0) {
                System.out.println(allDocs.get(i).getKey() + " : " + allDocs.get(i).getValue() + " Title: "+ invert.titles.get(allDocs.get(i).getKey())+ " Authors: " + invert.authors.get(allDocs.get(i).getKey()));
            }
        }

        return result;

    }

    public void getTop(ArrayList<Pair<Integer, Double>> array){
        int n = array.size();
        for(int i = 0; i < n -1; i++){
            int max = i;
            for(int x = i+1; x < n;x++){
                if(array.get(x).getValue() > array.get(max).getValue()){
                    max = x;
                }
            }
            Pair<Integer, Double> temp = new Pair<>(array.get(max).getKey(), array.get(max).getValue());
            Pair<Integer, Double> temp2 = new Pair(array.get(i).getKey(), array.get(i).getValue());
            array.set(max, temp2);
            array.set(i, temp);

        }

    }

    public double normalizeVector(ArrayList<Pair<String, Double>> vector){
        double result = 0;

        for(int i = 0; i < vector.size(); i ++){
            double value = vector.get(i).getValue();
            result = (value * value) + result;
        }

        result = Math.sqrt(result);

        return result;
    }

    public ArrayList<Pair<String, Double>> genVector(ArrayList<Pair<String, Double>> tfVals, ArrayList<Pair<String, Double>> idfVals){
        ArrayList<Pair<String, Double>> weightVector = new ArrayList<>();


        for(int i = 0; i < tfVals.size(); i++){

            String tfWord = tfVals.get(i).getKey();
            Double tfVal = tfVals.get(i).getValue();


            for(int x =0; x < idfVals.size(); x++){

                String idfWord = idfVals.get(x).getKey();
                Double idfVal = idfVals.get(x).getValue();

                if(tfWord.equals(idfWord)){
                    double w = tfVal * idfVal;
                    Pair<String, Double> newWeight = new Pair(tfWord , w);
                    weightVector.add(newWeight);
                }
            }
        }

        //printSDArray(weightVector);
        return weightVector;
    }

    public ArrayList<Pair<String, Double>> getTF(String[] words){
        ArrayList<Pair<String, Double>> tf = new ArrayList<>();

        for(int i = 0; i < words.length; i++){

            boolean contains = false;
            String word = words[i];
            int count = 0;

            for(int x = 0; x < words.length; x++){
                if(word.equals(words[x])){
                    count++;
                }

            }


            for(int x = 0; x < tf.size(); x++){
                if(tf.get(x).getKey().equals(word)){
                    contains = true;
                }
            }

            if(!contains) {
                Pair<String, Double> termCount = new Pair(word, (1 + (double)Math.log10(count)));
                tf.add(termCount);
            }
        }


        //printSDArray(tf);


        return tf;
    }

    public void printSDArray(ArrayList<Pair<String, Double>> array){
        for(int i =0; i < array.size(); i++){
            System.out.println(array.get(i).getKey() + ":" +array.get(i).getValue());
        }
    }

    public double calcSimVal(ArrayList<Pair<String, Double>> docWeight, ArrayList<Pair<String, Double>> qWeight, Double docNorm, Double qNorm){

        double numerator = 0;
        double denominator = docNorm * qNorm;

        for(int i = 0; i < qWeight.size() ; i++){
            String currWord = qWeight.get(i).getKey();
            double currWeight = qWeight.get(i).getValue();

            for(int x = 0; x < docWeight.size(); x++){
                String scanWord = docWeight.get(x).getKey();
                double scanWeight = docWeight.get(x).getValue();
                if(currWord.equals(scanWord)){
                    numerator = numerator + (currWeight * scanWeight);
                }
            }
        }


        return numerator/denominator;
    }
}
