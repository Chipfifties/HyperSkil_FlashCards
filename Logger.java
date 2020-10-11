package flashcards;

import java.io.*;
import java.util.Scanner;

public class Logger {
    private static final Scanner sc = new Scanner(System.in);
    private static final StringBuilder log = new StringBuilder();

    protected void printToConsole(String str) {
        System.out.println(str);
        log.append(str).append("\n");
    }

    protected String getInput() {
        String input = sc.nextLine();
        log.append(input).append("\n");
        return input;
    }

    protected void saveLog() {
        printToConsole("File name:");
        String filePath = getInput();
        try (OutputStreamWriter bos = new OutputStreamWriter(new FileOutputStream(filePath))) {
            bos.append(log);
            printToConsole("The log has been saved.");
        } catch (IOException ignored) {
        }
    }
}
