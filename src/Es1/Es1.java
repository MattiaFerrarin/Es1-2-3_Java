package Es1;
import java.util.*;

public class Es1 {
    public Es1(){}

    public static void Main(){
        String input = "temp=23.5;umid=61;ts=1732000000;batt_low=false";

        Optional<SensorReading> res = parsePacket(input);

        if (res.isPresent()){
            System.out.println("Parsed successfully: " + res);
        } else {
            System.out.println("Failed to parse packet.");
        }
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
                            umid = Integer.parseInt(res[1]);
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
                } catch (NumberFormatException e){ }
            }
        }
        return Optional.of(new SensorReading(temp, umid, ts, battLow));
    }
}