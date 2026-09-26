package Es3;

public enum Level{
    CRITICAL(1, 15),
    HIGH(2, 30),
    MEDIUM(3, 60),
    LOW(4, 120);

    private final int priority;
    private final int workMinutes;

    Level(int priority, int workMinutes) {
        this.priority = priority;
        this.workMinutes = workMinutes;
    }

    public int getPriority() { return priority; }
    public int getWorkMinutes() { return workMinutes; }

    public static Level parse(String text) {
        if (text == null) return null;
        try {
            return Level.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}