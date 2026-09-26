package Es2;

import java.util.*;

public class Es2 {
    public Es2() { }
    private static ArrayList<HTTPRequest> requestsByArrival = new ArrayList<>();
    private static LinkedList<HTTPRequest> last10Requests = new LinkedList<>();
    private static HashSet<String> susIps = new HashSet<>();
    private static TreeSet<Long> responseTimes = new TreeSet<>();
    public static void Main() {
        requestsByArrival.clear();
        last10Requests.clear();
        susIps.clear();
        responseTimes.clear();

        List<HTTPRequest> requests = generateRequests(30);
        for(HTTPRequest req : requests){
            requestsByArrival.add(req);

            last10Requests.addLast(req);
            if(last10Requests.size()>10)
                last10Requests.removeFirst();

            if(req.statusCode() >= 400 && req.statusCode() <600)
                susIps.add(req.ip());

            responseTimes.add(req.tReplyMs());
        }
        printReport();
    }

    private static ArrayList<HTTPRequest> getLastNReqStatusGr500(int n){
        ListIterator i = requestsByArrival.listIterator(requestsByArrival.size());
        ArrayList<HTTPRequest> ret = new ArrayList<>();
        while(i.hasPrevious() && n>0){
            HTTPRequest temp = (HTTPRequest) i.previous();
            if(temp.statusCode() >= 500){
                n--;
                ret.add(temp);
            }
        }
        return ret;
    }

    public static Long get90thPercentile() {
        if (responseTimes.isEmpty()) {
            return null;
        }

        int targetIndex = (int) Math.ceil(0.90 * responseTimes.size()) - 1;

        Iterator<Long> iterator = responseTimes.iterator();
        Long thresholdValue = null;
        for (int i = 0; i <= targetIndex && iterator.hasNext(); i++) {
            thresholdValue = iterator.next();
        }

        return responseTimes.floor(thresholdValue);
    }

    public static void printReport() {
        System.out.println("=== REPORT ===");

        long susInWindowCount = last10Requests.stream().filter(req -> susIps.contains(req.ip())).count();

        System.out.println("\nSliding window analysis (last " + last10Requests.size() + " requests)");
        System.out.println("- Sus IPs identified: " + susIps.size());
        System.out.println("- Sus IPs in the current window: " + susInWindowCount + " / " + last10Requests.size());

        int n = 5;
        List<HTTPRequest> lastServerErrors = getLastNReqStatusGr500(n);
        System.out.println("\nServer errors (Last " + n + " requests with status >= 500)");
        if(lastServerErrors.isEmpty()){
            System.out.println("  [No server errors with status code >= 500]");
        }
        for (HTTPRequest req : lastServerErrors) {
            System.out.println("  -> IP: " + req.ip() + " | Path: " + req.path() + " | Status: " + req.statusCode() + " | ReplyTime: " + req.tReplyMs() + "ms");
        }

        Long p90 = get90thPercentile();
        System.out.println("\n90th Percentile");
        if (p90 != null) {
            System.out.println("- Response time at 90th percentile: " + p90 + " ms");
        }
    }

    public static List<HTTPRequest> generateRequests(int count) {
        List<HTTPRequest> requests = new ArrayList<>();
        Random random = new Random();

        String[] ips = {
                "192.168.1.1", "10.0.0.5", "172.16.0.2",
                "192.168.1.100", "10.0.0.42", "192.168.1.1"
        };

        String[] paths = {
                "/index.html", "/api/v1", "/images/logo.png",
                "/api/v1/users", "/contacts", "/info"
        };

        int[] statusCodes = {200, 200, 200, 201, 400, 403, 404, 500, 502, 503, 640};

        long currentTimestamp = System.currentTimeMillis() - (count * 1000L);

        for (int i = 0; i < count; i++) {
            String ip = ips[random.nextInt(ips.length)];
            String path = paths[random.nextInt(paths.length)];
            int statusCode = statusCodes[random.nextInt(statusCodes.length)];
            long tReplyMs = 20 + random.nextInt(980);
            long timestamp = currentTimestamp + (i * 1000L) + random.nextInt(200);

            requests.add(new HTTPRequest(ip, path, statusCode, tReplyMs, timestamp));
        }

        return requests;
    }
}