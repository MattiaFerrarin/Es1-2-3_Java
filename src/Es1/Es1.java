package Es1;
import java.util.*;

public class Es1 {
    public Es1(){}

    public static void Main(){
        List<String> rawPackets = Arrays.asList(
                "temp=23.5;umid=61;ts=1732000000;batt_low=false",
                "temp=18.2;umid=45;ts=1732000100",
                "temp=324g;umid=50;ts=1732000200",
                "temp=31.0;umid=105;ts=1732000300",
                "umid=80;ts=1732000400;batt_low=true",
                "   ",
                "temp=-5.4;umid=90;ts=1732000600;batt_low=false",
                "temp=20.0;umid=-10;ts=1732000700;batt_low=fale"
        );

        int validCount = 0;
        int corruptCount = 0;
        int invalidCount = 0;

        double tempSum = 0.0;
        int tempCount = 0;

        for (int i = 0; i < rawPackets.size(); i++) {
            String packet = rawPackets.get(i);
            Optional<SensorReading> result = parsePacket(packet);

            if (result.isPresent()) {
                SensorReading reading = result.get();
                boolean allFieldsNotNull = reading.temperature() != null && reading.humidityPerc() != null && reading.timestampUnix() != null && reading.lowBattery() != null;

                if (allFieldsNotNull) {
                    validCount++;
                    System.out.println("[" + (i+1) + "] VALID     : " + reading);
                } else {
                    corruptCount++;
                    System.out.println("[" + (i+1) + "] CORRUPT   : Missing or invalid fields in " + reading);
                }

                if (reading.temperature() != null) {
                    tempSum += reading.temperature();
                    tempCount++;
                }
            } else {
                invalidCount++;
                System.out.println("[" + (i+1) + "] INVALID   : Empty or unparseable packet (\"" + packet + "\")");
            }
        }

        Double avgTemp = tempCount > 0 ? tempSum / tempCount : null;

        System.out.println("\n----------------------------------------\nReport\n----------------------------------------");
        System.out.println("Total parsed packets : " + rawPackets.size());
        System.out.println("Valid readings       : " + validCount);
        System.out.println("Corrupt packages     : " + corruptCount);
        System.out.println("Invalid packages     : " + invalidCount);
        if (avgTemp != null) {
            System.out.println("Valid temperatures average   : " + avgTemp + "°C (" + tempCount + " readings with temperature)\n");
        } else {
            System.out.println("Valid temperatures average   : N/D (No valid temperature)");
        }
        System.out.println("----------------------------------------");
    }

    public static Optional<SensorReading> parsePacket(String raw){
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        Double temp = null;
        Integer umid = null;
        Long ts = null;
        Boolean battLow = null;

        String[] splitted = raw.split(";");
        for (String item : splitted){
            if (item == null || item.isBlank()){
                continue;
            }
            String[] res = item.split("=");
            if(res.length == 2){
                res[0] = res[0].trim();
                res[1] = res[1].trim();
                try{
                    switch (res[0]){
                        case "temp":
                            temp = Double.parseDouble(res[1]);
                            break;
                        case "umid":
                            umid = Integer.valueOf(res[1]);
                            if(umid > 100 || umid < 0){
                                throw new InvalidReadingException("umid out of range");
                            }
                            break;
                        case "ts":
                            ts = Long.parseLong(res[1]);
                            break;
                        case "batt_low":
                            battLow = ("true".equalsIgnoreCase(res[1]) ? Boolean.TRUE : "false".equalsIgnoreCase(res[1]) ? Boolean.FALSE : null);
                            break;
                        default:
                            break;
                    }
                } catch (NumberFormatException | InvalidReadingException e) { }
            }
        }
        return Optional.of(new SensorReading(temp, umid, ts, battLow));
    }

    public static void sortReadingsByTempDescending(List<SensorReading> readings) {
        readings.sort((r1, r2) -> {
            Double t1 = r1.temperature();
            Double t2 = r2.temperature();

            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;

            return Double.compare(t2, t1);
        });
    }
}