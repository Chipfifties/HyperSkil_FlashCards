package flashcards;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private static final Map<String, String> flashCards = new LinkedHashMap<>();
    private static final Map<String, Integer> cardStats = new LinkedHashMap<>();
    private final List<String> commandLine;
    private static boolean saveOnExit = false;
    private final Logger logger = new Logger();

    Game(String[] args) {
        commandLine = Arrays.stream(args).collect(Collectors.toList());
    }

    void run() {
        if (commandLine.contains("-import")) {
            loadCards(commandLine.get(commandLine.indexOf("-import") + 1));
        }
        if (commandLine.contains("-export")) {
            saveOnExit = true;
        }
        while (true) {
            logger.printToConsole("Input the action (add, remove, import, export, " +
                    "ask, exit, log, hardest card, reset stats):");
            switch (logger.getInput()) {
                case "add": addCard(); break;
                case "remove": removeCard(); break;
                case "import": loadCards(""); break;
                case "export": saveCards(""); break;
                case "ask": guessCards(); break;
                case "log": logger.saveLog(); break;
                case "hardest card": getHardestCard(); break;
                case "reset stats": resetStats(); break;
                case "exit":
                    logger.printToConsole("Bye Bye!");
                    if (saveOnExit) {
                        saveCards(commandLine.get(commandLine.indexOf("-export") + 1));
                    }
                    return;
                default: break;
            }
        }
    }

    private void addCard() {
        logger.printToConsole("The card:");
        String term = logger.getInput();
        if (flashCards.containsKey(term)) {
            logger.printToConsole(String.format("The card \"%s\" already exists.", term));
            return;
        }

        logger.printToConsole("The definition of the card:");
        String definition = logger.getInput();
        if (flashCards.containsValue(definition)) {
            logger.printToConsole(String.format("The definition \"%s\" already exists.", definition));
            return;
        }

        logger.printToConsole(String.format("The pair (\"%s\":\"%s\") has been added.", term, definition));
        flashCards.put(term, definition);
    }

    private void removeCard() {
        logger.printToConsole("The card:");
        String card = logger.getInput();
        if (flashCards.containsKey(card)) {
            flashCards.remove(card);
            cardStats.remove(card);
            logger.printToConsole("The card has been removed.");
        } else {
            logger.printToConsole(String.format("Can't remove \"%s\": there is no such card.", card));
        }
    }

    private void loadCards(String filePath) {
        if (filePath.isBlank()) {
            logger.printToConsole("File name:");
            filePath = logger.getInput();
        }
        try (ObjectInputStream objectInput = new ObjectInputStream(new FileInputStream(filePath))) {
            List<String[]> importedCards = (ArrayList) objectInput.readObject();
            importedCards.forEach(e -> {
                flashCards.put(e[0], e[1]);
                cardStats.put(e[0], Integer.parseInt(e[2]));
            });
            logger.printToConsole(String.format("%d cards have been loaded", importedCards.size()));
        } catch (IOException e) {
            logger.printToConsole("File not found.");
        } catch (ClassNotFoundException e) {
            logger.printToConsole("Class not found.");
        }
    }


    private void saveCards(String filePath) {
        if (filePath.isBlank()) {
            logger.printToConsole("File name:");
            filePath = logger.getInput();
        }
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(new FileOutputStream(filePath))) {
            List<String[]> exportList = new ArrayList<>();
            flashCards.forEach((k, v) ->
                    exportList.add(new String[]{k, v, Integer.toString(cardStats.getOrDefault(k, 0))}));
            objectOutput.writeObject(exportList);
            logger.printToConsole(String.format("%d cards have been saved.", flashCards.size()));
        } catch (IOException e) {
            logger.printToConsole("File not found.");
        }
    }

    private void getHardestCard() {
        int maxMistakesCounter = 0;
        String maxMistakesCardName = "";
        for (Map.Entry<String, Integer> entry : cardStats.entrySet()) {
            if (entry.getValue() > maxMistakesCounter) {
                maxMistakesCounter = entry.getValue();
                maxMistakesCardName = entry.getKey();
            }
        }
        if (maxMistakesCounter > 0) {
            if (Collections.frequency(cardStats.values(), maxMistakesCounter) > 1) {
                List<String> hardestCards = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : cardStats.entrySet()) {
                    if (entry.getValue() == maxMistakesCounter) {
                        hardestCards.add(entry.getKey());
                    }
                }
                StringBuilder hardestOutputString = new StringBuilder();
                hardestOutputString.append("The hardest cards are ");
                for (int i = 0; i < hardestCards.size(); i++) {
                    if (i == hardestCards.size() - 1) {
                        hardestOutputString.append(String.format("\"%s\".", hardestCards.get(i)));
                    } else {
                        hardestOutputString.append(String.format("\"%s\", ", hardestCards.get(i)));
                    }
                }
                hardestOutputString.append(" You have ")
                        .append(maxMistakesCounter)
                        .append("errors answering them.");
                logger.printToConsole(hardestOutputString.toString());
            } else {
                logger.printToConsole(String.format("The hardest card is \"%s\". " +
                        "You have %d errors answering it.", maxMistakesCardName, maxMistakesCounter));
            }
        } else {
            logger.printToConsole("There are no cards with errors.");
        }
    }

    private void resetStats() {
        cardStats.forEach((k, v) -> cardStats.replace(k, 0));
        logger.printToConsole("Card statistics have been reset.");
    }

    private void guessCards() {
        Random random = new Random();
        List<String> terms = new ArrayList<>(flashCards.keySet());

        logger.printToConsole("How many times to ask?");
        int numOfCards = Integer.parseInt(logger.getInput());

        for (int i = 0; i < numOfCards; i++) {
            String term = terms.get(random.nextInt(terms.size()));
            logger.printToConsole(String.format("Print the definition of \"%s\":", term));
            String definition = logger.getInput();

            if (definition.equals(flashCards.get(term))) {
                logger.printToConsole("Correct!");
            } else if (flashCards.containsValue(definition)) {
                logger.printToConsole(String.format("Wrong. The right answer is \"%s\", " +
                        "but your definition is correct for \"%s\".", flashCards.get(term), getKey(definition)));
                cardStats.put(term, cardStats.getOrDefault(term, 0) + 1);
            } else {
                logger.printToConsole(String.format("Wrong. The right answer is \"%s\".", flashCards.get(term)));
                cardStats.put(term, cardStats.getOrDefault(term, 0) + 1);
            }
        }
    }

    private static String getKey(String value) {
        for (Map.Entry<String, String> entry : flashCards.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
