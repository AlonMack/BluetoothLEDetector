import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecuteShellCommand {

    public static void main(String[] args) {

        ExecuteShellCommand obj = new ExecuteShellCommand();

        String detect = obj.detect(new String[]{"/bin/sh", "./detectLE.sh"});
        System.out.println(detect);
        System.out.println(obj.executeCommand(new String[]{"/bin/sh", "./start.sh"}, detect));

    }

    private String detect(String[] command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while (true) {
                String line = reader.readLine();
                if (line != null && checkWithRegExp(line)) {
                    System.out.println(line);
                    return line.split(" ")[0];
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "detected";
    }

    private String executeCommand(String[] command, String mac) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            InputStream inputStream = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);

            new Thread(new StreamGobbler(inputStream, "input")).start();
            new Thread(new StreamGobbler(process.getErrorStream(), "error")).start();

            PrintWriter writer = new PrintWriter(process.getOutputStream());

            System.out.println("Send a command");
            String command2 = "connect " + mac;
            writer.write(command2);
            writer.write("\n");
            writer.flush();
            process.waitFor(20, TimeUnit.SECONDS);

            System.out.println("Send a command");
            String command3 = "char-write-cmd 0x0053 03";
            writer.write(command3);
            writer.write("\n");
            writer.flush();

            process.waitFor(5, TimeUnit.SECONDS);

            System.out.println("Send a command");
            String command4 = "exit";
            writer.write(command4);
            writer.write("\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "executed";
    }

    public static boolean checkWithRegExp(String userNameString) {
        Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2}).*$");
        Matcher m = p.matcher(userNameString);
        return m.matches();
    }
}
