package Es3;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Es3 {
    public Es3() { }

    private static final String INPUT_FILE = "ticket.csv";
    private static final String REPORT_FILE = "report.txt";
    public static void Main() {
        createTestCsvFile();

        PriorityQueue<Ticket> processingQueue = new PriorityQueue<>();
        PriorityQueue<Ticket> pendingCriticalTickets = new PriorityQueue<>();

        Map<Level, Integer> countPerLevel = new EnumMap<>(Level.class);
        for (Level l : Level.values()) {
            countPerLevel.put(l, 0);
        }

        System.out.println("=== Ticket reading and validation ===");

        Path inputPath = Paths.get(INPUT_FILE);

        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.toLowerCase().contains("id")) {
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.err.println("[LOG] Line "+lineNumber+" malformed (insufficient fields): \""+line+"\"");
                    continue;
                }

                String id = parts[0].trim();
                String description = parts[1].trim();
                Level level = Level.parse(parts[2]);

                if (level == null) {
                    System.err.println("[LOG] Line "+lineNumber+" discarded (invalid level '"+parts[2]+"'): \""+line+"\"");
                    continue;
                }

                long timestamp;
                try {
                    timestamp = Long.parseLong(parts[3].trim());
                } catch (NumberFormatException e) {
                    System.err.println("[LOG] Line "+lineNumber+" discarded (invalid timestamp): \""+line+"\"");
                    continue;
                }

                Ticket ticket = new Ticket(id, description, level, timestamp);
                processingQueue.add(ticket);

                if (ticket.getLevel() == Level.CRITICAL) {
                    pendingCriticalTickets.add(ticket);
                    if (pendingCriticalTickets.size() > 2) {
                        System.out.println("[CRITICAL ALERT] Detected " + pendingCriticalTickets.size() + " CRITICAL tickets waiting");
                    }
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("CRITICAL ERROR: Unable to find input file " + INPUT_FILE);
            return;
        } catch (IOException e) {
            System.err.println("I/O ERROR during reading: " + e.getMessage());
            return;
        }

        System.out.println("\n=== Processing and report generation ===");

        Path outputPath = Paths.get(REPORT_FILE);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            writer.write("=== Ticket processing order report ===\n\n");

            int cumulativeTimeMinutes = 0;
            int consecutiveHighPriorityCount = 0;
            int totalProcessedTickets = 0;

            while (!processingQueue.isEmpty()) {
                Ticket ticket = processingQueue.poll();

                if (ticket.getLevel() == Level.CRITICAL) {
                    pendingCriticalTickets.remove(ticket);
                }

                if (ticket.getLevel() == Level.CRITICAL || ticket.getLevel() == Level.HIGH) {
                    consecutiveHighPriorityCount++;
                    if (consecutiveHighPriorityCount > 5) {
                        cumulativeTimeMinutes += 10;
                        writer.write("--- [Break: +10 min] ---\n");
                        System.out.println("Mandatory technician break (+10 min added)");
                        consecutiveHighPriorityCount = 1;
                    }
                } else {
                    consecutiveHighPriorityCount = 0;
                }

                cumulativeTimeMinutes += ticket.getLevel().getWorkMinutes();
                totalProcessedTickets++;
                countPerLevel.put(ticket.getLevel(), countPerLevel.get(ticket.getLevel()) + 1);

                String reportEntry = "Completed at "+cumulativeTimeMinutes+" min | ID: "+ticket.getId()+" | Priority: "+ticket.getLevel()+" | Description: "+ticket.getDescription();

                writer.write(reportEntry);
                writer.newLine();

                System.out.println("Processed: " + ticket.getId() + " (" + ticket.getLevel() + ") -> Cum time: " + cumulativeTimeMinutes + "m");
            }

            writer.write("\n===========================================\n");
            writer.write("Finally summary:\n");
            writer.write("Total tickets processed: " + totalProcessedTickets + "\n");
            for (Level l : Level.values()) {
                writer.write(" - "+l.name()+": "+countPerLevel.get(l)+"\n");
            }
            writer.write("Total estimated work time: " + cumulativeTimeMinutes + " minutes ("+String.format("%.2f", cumulativeTimeMinutes / 60.0) + " hours)\n");
            System.out.println("\nReport successfully generated in: " + REPORT_FILE);
        } catch (IOException e) {
            System.err.println("I/O ERROR during report writing: " + e.getMessage());
        }
    }

    private static void createTestCsvFile() {
        String content = """
                id,description,level,arrivalTimestamp
                T001,Production server unreachable,CRITICAL,1732000000
                T002,New email account request,LOW,1732000100
                T003,2nd floor printer not working,MEDIUM,1732000050
                T004,Corporate VPN disconnected,high,1732000020
                T005,SQL Database locked,CRITICAL,1732000010
                T006,Keyboard replacement,low,1732000200
                INVALID_ROW_TEST
                T007,Ransomware attack detected,CRITICAL,1732000005
                T008,Emails failing to send,HIGH,1732000030
                T009,User PC running slow,MEDIUM,1732000060
                T010,Firewall Down,CRITICAL,1732000002
                T011,CAD software update,MEDIUM,1732000080
                T012,Forgotten password,LOW,1732000300
                T013,Network switch broken,HIGH,1732000040
                T014,SSL Certificate expired,HIGH,1732000045
                T015,Wi-Fi configuration error,HIGH,1732000055
                T016,Secondary monitor assistance,UNKNOWN,1732000090
                """;

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(INPUT_FILE))) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Unable to create test file: " + e.getMessage());
        }
    }
}