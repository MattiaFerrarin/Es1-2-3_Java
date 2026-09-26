package Es1;

public record SensorReading(Double temperature, Integer humidityPerc, Long timestampUnix, Boolean lowBattery){}