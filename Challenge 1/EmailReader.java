import java.util.Scanner;

public class EmailReader {
  public static String GetNameFromEmail(String emailId) {
    HttpAgent agent = new HttpAgent("https://www.ecs.soton.ac.uk/people/" + emailId);

    return agent.GetOutput(84, " property=\"name\">", "<em property=");
  }

  public static void main(String[] args) {
    System.out.print("Enter an email ID >> ");
    Scanner scanner = new Scanner(System.in);
    String email = scanner.nextLine();
    System.out.println("\n\nGetting HTTP...\n\n");

    String name = GetNameFromEmail(email);
    System.out.println("/// " + name + " ///");
  }
}
