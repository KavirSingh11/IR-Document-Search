import java.io.*;
import javafx.util.Pair;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.DoubleToLongFunction;

public class Invert {


    public ArrayList<String> stopWords;

    public HashMap<String, Integer> dictionary = new HashMap<>();
    public HashMap<String, ArrayList<Pair<Integer, Integer>>> postingsList = new HashMap<>();

    public ArrayList<Pair<String, Double>> idfVals = new ArrayList<>();
    public ArrayList<String> fullText = new ArrayList<>();

    public HashMap<Integer, ArrayList<String>> docInfo = new HashMap<>();
    public HashMap<Integer, ArrayList<String>> searchInfo = new HashMap<>();

    public HashMap<Integer, String> titles = new HashMap<>();
    public HashMap<Integer, String> authors = new HashMap<>();


    public File f = new File("resources/cacm.all");

    public void run(boolean stem) throws IOException{
        stopWords = getStop();

        readCACM(stem);

        Collections.sort(fullText);

        writeDictionary();
        writePostings();


        /*
        for(int i = 1; i < docInfo.size(); i++){
            ArrayList<String> words = docInfo.get(i);
            System.out.println("Document "+i);
            if(docInfo.containsKey(i)) {
                for (int x = 0; x < words.size(); x++) {
                    System.out.print(words.get(x) + " ");
                }
            }
            System.out.print("\n");
        }

        System.out.println(docInfo.size());
        System.out.println(fullText.size());
        System.out.println(dictionary.size());
        System.out.println(postingsList.size());*/

    }

    public void addDocTitle(int docID, String text){
        String add;
        if(titles.containsKey(docID)){
            add = titles.get(docID)+" "+text;
        }else{
            add = text;
        }
        titles.put(docID, add);
    }

    public void addDocAuthors(int docID, String text){
        authors.put(docID, text);
    }

    public void getDocWords(int docID, String[] line){

        ArrayList<String> words;
        if(!docInfo.containsKey(docID)){
            words = new ArrayList<>();
        }
        else{
            words = docInfo.get(docID);
        }
        for(int i = 0; i < line.length; i++){
            if(!stopWords.contains(line[i])) {
                String add = cleanWord(line[i]).toLowerCase();
                words.add(add);
            }
        }

        docInfo.put(docID, words);

    }

    public void readCACM(boolean stem) throws IOException{
        Scanner s = new Scanner(f);


        int docID = 0;

        String line = "";
        boolean readLine = false;

        while(s.hasNextLine()){


            if(!readLine){
                line = s.nextLine();

            }

            readLine = false;

            if(line.substring(0,2).equals(".I")){

              //  System.out.println("New File found");
                docID++;
            }

            else if(line.substring(0,2).equals(".T")){
              //  System.out.println("New Title found");
                line = s.nextLine();
                while(!line.substring(0,1).equals(".")){

                   String[] temp = line.split(" ");

                   getDocWords(docID, temp);

                   addDocTitle(docID, line);
                   for(int i = 0; i < temp.length; i++){
                       String word = temp[i];
                       if(stem){word = stemWord(word.toLowerCase());}
                       word = cleanWord(word);
                       if(checkNew(word)){
                         // System.out.println("Adding new word from title");
                          fullText.add(word);
                          dictionary.put(word, 1);

                          Pair newPair = new Pair(docID, 1);
                          ArrayList<Pair<Integer, Integer>> tempArray = new ArrayList<>();
                          tempArray.add(newPair);
                          postingsList.put(word, tempArray);

                       }
                       else if(!(stopWords.contains(word))){

                          // System.out.println("Adding duplicate word from title");
                           dictionary.put(word, dictionary.get(word) + 1);

                           ArrayList<Pair<Integer, Integer>> tempArray = postingsList.get(word);
                           int lastDocID = tempArray.get(tempArray.size() - 1).getKey();
                           int lastDocFreq = tempArray.get(tempArray.size() - 1).getValue();
                           if(lastDocID == docID){
                               Pair newPair = new Pair(lastDocID, lastDocFreq + 1);
                               tempArray.set(tempArray.size() - 1, newPair);
                           }
                           else{
                               Pair newPair = new Pair(docID, 1);
                               tempArray.add(newPair);
                           }
                           postingsList.put(word, tempArray);


                       }

                   }


                   line = s.nextLine();
                   //System.out.println("Title scanned");
                   readLine = true;

                }
            }

            else if(line.substring(0,2).equals(".K")){

               // System.out.println("New K found");
                line = s.nextLine();
                while(!line.substring(0,1).equals(".")){

                    String[] temp = line.split(" ");

                    getDocWords(docID, temp);

                    for(int i = 0; i < temp.length; i++){
                        String word = temp[i];

                        if(stem){word = stemWord(word.toLowerCase());}
                        word = cleanWord(word);
                        if(checkNew(word)) {
                           // System.out.println("Adding word from k");
                            fullText.add(word);
                            dictionary.put(word, 1);

                            Pair newPair = new Pair(docID, 1);
                            ArrayList<Pair<Integer, Integer>> tempArray = new ArrayList<>();
                            tempArray.add(newPair);
                            postingsList.put(word, tempArray);

                        }
                        else if(!(stopWords.contains(word))){

                            dictionary.put(word, dictionary.get(word) + 1);

                            ArrayList<Pair<Integer, Integer>> tempArray = postingsList.get(word);
                            int lastDocID = tempArray.get(tempArray.size() - 1).getKey();
                            int lastDocFreq = tempArray.get(tempArray.size() - 1).getValue();
                            if(lastDocID == docID){
                                Pair newPair = new Pair(lastDocID, lastDocFreq + 1);
                                tempArray.set(tempArray.size() - 1, newPair);
                            }
                            else{
                                Pair newPair = new Pair(docID, 1);
                                tempArray.add(newPair);
                            }
                            postingsList.put(word, tempArray);




                        }

                    }
                    line = s.nextLine();
                    //System.out.println("K portion scanned");
                    readLine = true;

                }
            }

            else if(line.substring(0,2).equals(".A")){
                line = s.nextLine();
                addDocAuthors(docID, line);
                line = s.nextLine();
                readLine = true;
            }

            else if(line.substring(0,2).equals(".W")){
                line = s.nextLine();

              //  System.out.println("New body text found");
                while(!line.substring(0,1).equals(".") && s.hasNextLine()){
                    String[] temp = line.split(" ");

                    getDocWords(docID, temp);
                    for(int i = 0; i < temp.length; i++){
                        String word = temp[i];

                        if(stem){word = stemWord(word.toLowerCase());}

                        word = cleanWord(word);
                        if(checkNew(word)){
                         //   System.out.println("Adding word from body");
                            fullText.add(word);
                            dictionary.put(word, 1);


                            Pair newPair = new Pair(docID, 1);
                            ArrayList<Pair<Integer, Integer>> tempArray = new ArrayList<>();
                            tempArray.add(newPair);
                            postingsList.put(word, tempArray);




                        }
                        else if(!(stopWords.contains(word))){
                            dictionary.put(word, dictionary.get(word) + 1);

                            ArrayList<Pair<Integer, Integer>> tempArray = postingsList.get(word);
                            int lastDocID = tempArray.get(tempArray.size() - 1).getKey();
                            int lastDocFreq = tempArray.get(tempArray.size() - 1).getValue();
                            if(lastDocID == docID){
                                Pair newPair = new Pair(lastDocID, lastDocFreq + 1);
                                tempArray.set(tempArray.size() - 1, newPair);
                            }
                            else{
                                Pair newPair = new Pair(docID, 1);
                                tempArray.add(newPair);
                            }
                            postingsList.put(word, tempArray);


                        }
                    }
                    line = s.nextLine();
                    readLine = true;
                }
            }

        }

        System.out.println(docID);
    }


    public void writePostings() throws IOException{
        PrintWriter writePost = new PrintWriter("resources/postingsList.txt");

        for(int i = 0; i < fullText.size(); i++){

            ArrayList<Pair<Integer, Integer>> tempArray = postingsList.get(fullText.get(i));
            double logNumb = 3204/dictionary.get(fullText.get(i));
            logNumb = Math.log10(logNumb);
            writePost.print(fullText.get(i) +" - IDF Value: "+ logNumb +  "\n");

            Pair addIDF = new Pair(fullText.get(i), logNumb);
            idfVals.add(addIDF);


            for(int x = 0; x < tempArray.size(); x++){
                writePost.print(tempArray.get(x) + " ");
            }
            writePost.println(" ");
        }

        writePost.close();
    }

    public void writeDictionary() throws IOException{

        PrintWriter writeDict = new PrintWriter("resources/dictionary.txt");

        for(int i = 0; i < fullText.size(); i++){

            writeDict.println(fullText.get(i)+":"+ dictionary.get(fullText.get(i)));
        }

        writeDict.close();
    }

    public String cleanWord(String term) {
        term = term.toLowerCase();
        term = term.replaceAll("[^a-zA-Z]", "");
        //term = stemWord(term);
        return term;
    }
    
    public String stemWord(String term){
        Stemmer stem = new Stemmer();
        char[] termChars = term.toCharArray();
        for(int i = 0; i < termChars.length; i++){
            if(Character.isAlphabetic(termChars[i])) {
                stem.add(termChars[i]);
            }
        }
        stem.stem();
        String stemmed = stem.toString();

        return stemmed;
    }

    public boolean checkNew(String term){
        if(fullText.contains(term)) return false;
        if(stopWords.contains(term)) return false;
        else return true;
    }


    public ArrayList<String> getStop() throws IOException{
        Scanner s = new Scanner(new FileReader("resources/stopwords.txt"));
        ArrayList<String> ret = new ArrayList<>();
        while(s.hasNext()){
            ret.add(s.nextLine());
        }
        return ret;
    }
}
