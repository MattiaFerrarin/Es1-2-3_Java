package Es2;

public record HTTPRequest(String ip, String path, int statusCode, long tReplyMs, long timestamp) { }
