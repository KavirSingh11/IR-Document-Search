import java.io.*;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.*;
import javafx.util.*;

public class Eval extends Search {

    public File query = new File("resources/query.text");
    public File rel = new File("resources/qrels.text");
    public DecimalFormat numberFormat = new DecimalFormat("#.000");

    public ArrayList<String> stopWords;

    public HashMap<Integer, String> queries = new HashMap<>();
    public HashMap<Integer, ArrayList<Integer>> relDocs = new HashMap<>();

    public HashMap<Integer, ArrayList<Integer>> searchResults = new HashMap<>();
    public ArrayList<Pair<Double, Double>> evalStats = new ArrayList<>();

    public void eval(Search search, Invert invert, boolean stem) throws IOException{

        stopWords = invert.stopWords;
        System.out.println("Evaluation Started");
        getQueries(invert);
        getRel();


        System.out.println("Relevant Documents for each Query:");
        for(int i = 1; i < queries.size(); i++){
            if(relDocs.containsKey(i)) {
                System.out.println(i + ": " + relDocs.get(i));
            }
        }

        for(int i = 1; i < queries.size() + 1; i++){
            String fix = "";
            String[] wordArray = queries.get(i).split(" ");
            for(int x = 1; x < wordArray.length; x++){
                if(x == 1) fix = wordArray[x];
                else if(!wordArray[x].isEmpty()){
                    fix = fix+" "+wordArray[x];
                }
            }
            queries.put(i, fix);
        }

        for(int i = 1; i < queries.size(); i++) {
            System.out.println("Query " + i);
            ArrayList<Integer> temp = search.search(invert , queries.get(i), stem);
            searchResults.put(i, temp);
            //System.out.println("Query "+i+": "+searchResults.get(i)) ;
        }

        getStats();

        getMAP();

    }

    public void getQueries(Invert invert) throws IOException{
        Scanner s = new Scanner(query);
        String line = "";
        boolean readLine = false;
        int qID = 0;

        while(s.hasNextLine()){
            if(!readLine){
                line = s.nextLine();
            }
            readLine = false;

            if(line.length() >= 2) {
                if (line.substring(0, 2).equals(".I")) {
                    qID++;
                } else if (line.substring(0, 2).equals(".W")) {

                    line = s.nextLine();

                    while (!line.substring(0, 1).equals(".") && s.hasNextLine()) {
                        String[] temp = line.split(" ");
                        for (int i = 0; i < temp.length; i++) {
                            String word = temp[i];
                            word = invert.cleanWord(word);
                            if (!(stopWords.contains(word)) && !word.isEmpty()) {
                                String query = queries.get(qID);
                                query = query + " " + word;
                                queries.put(qID, query);
                            }

                        }


                        line = s.nextLine();
                        readLine = true;
                    }
                }
            }

        }
    }

    public void getRel() throws IOException{
        Scanner sc = new Scanner(rel);
        String line = "";

        while(sc.hasNextLine()){

            line = sc.nextLine();
            int qID = Integer.parseInt(line.substring(0,2));

            int relDoc = Integer.parseInt(line.substring(3,7));

            if(relDocs.containsKey(qID)) {
                ArrayList<Integer> temp = relDocs.get(qID);
                temp.add(relDoc);
                relDocs.put(qID, temp);
            }
            else{
                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(relDoc);

                relDocs.put(qID, temp);
            }


        }

    }

    public void getMAP(){
        double MAP = 0;
        for(int i = 0; i < evalStats.size(); i++){
            //System.out.println("R precision for Q" +i+": "+evalStats.get(i).getValue() );
            double AP = evalStats.get(i).getKey();
            MAP = MAP + AP;
        }
        double r = evalStats.size();
        MAP = MAP / r;

        System.out.println("MAP Value is " +numberFormat.format(MAP));
    }

    public void getStats(){

        for(int i = 0; i < searchResults.size(); i++){

            double hits = 0;
            double prec = 0;
            double AP = 0;

            ArrayList<Integer> ret;
            ArrayList<Integer> rel;

            if(relDocs.containsKey(i)) {
                ret = searchResults.get(i);
                rel = relDocs.get(i);
                for (int x = 0; x < ret.size(); x++) {

                    if (rel.contains(ret.get(x))) {
                        hits++;
                        prec = hits / (x + 1);
                        AP = AP + prec;
                    }
                }

                double r = rel.size();
                AP = AP / r;
                double rPrec = hits / r;

                System.out.println("Query "+i+ " || Hits: " + hits +" R Precision: " + numberFormat.format(rPrec));
                Pair<Double, Double> newStats = new Pair(AP, rPrec);

                evalStats.add(newStats);
            }


        }

    }
}
