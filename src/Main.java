import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Court.createCourt("A");
        Court.createCourt("B");
        Court.createCourt("C");
        Court.createCourt("D");
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()){
            Court.runCommand(in.nextLine());
        }
    }
}


