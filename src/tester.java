import javax.swing.*;
import java.io.IOException;

public class tester extends Invert{

    public static void main(String[] args) throws IOException {

        System.out.println("Starting");
        Invert invert = new Invert();
        Search search = new Search();
        Eval eval = new Eval();

        boolean stem = false;
        int stemW = Integer.parseInt(JOptionPane.showInputDialog("Enter 1 to stem words, 0 to not stem words"));
        if(stemW == 1) stem = true;
        invert.run(stem);
        int input = -1;
        while(input != 0) {
            input = Integer.parseInt(JOptionPane.showInputDialog("Enter 1 to query, enter 2 for evaluation, enter 0 to quit"));

            if (input == 1) {
                search.search(invert, stem);
            } else if (input == 2) {
                eval.eval(search, invert, stem);
            }
        }
    }
}
