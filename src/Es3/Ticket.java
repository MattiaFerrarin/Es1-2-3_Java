package Es3;

public class Ticket implements Comparable<Ticket> {
    private final String id;
    private final String description;
    private final Level level;
    private final Long timestamp;

    public Ticket(String id, String description, Level level, Long timestamp) {
        this.id = id;
        this.description = description;
        this.level = level;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public Level getLevel() { return level; }
    public Long getTimestamp() { return timestamp; }

    @Override
    public int compareTo(Ticket other) {
        int cmpLevel = Integer.compare(this.level.getPriority(), other.level.getPriority());
        if (cmpLevel != 0) {
            return cmpLevel;
        }
        return Long.compare(this.timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return "["+id+"] "+description+" (Level: "+level+", Timestamp: "+timestamp+")";
    }
}
