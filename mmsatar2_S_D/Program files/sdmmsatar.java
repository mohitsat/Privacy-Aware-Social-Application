import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.json.*;

public class sdmmsatar {

    static int UID = 25;
    static String inputcheckin = null;
    static String placeid = null;
    static String companionid = null;
    static int policyid = 0;
    static Map<String, Map<String, List<String>>> feedbackMap = new HashMap<>();

    private static String streamToString(InputStream inputStream) {
        String text = new Scanner(inputStream, "UTF-8").useDelimiter("\\Z").next();
        return text;
    }

    public static String jsonGetRequest(String urlQueryString) {
        String json = null;
        try {
            URL url = new URL(urlQueryString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            InputStream inStream = connection.getInputStream();
            json = streamToString(inStream); // input stream to string
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    public static int getpolicy(String placeid, String companionid) throws JSONException {
        String json = jsonGetRequest(
                "http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox-sd/services.jsp?query=getPolicy&userId="
                        + UID + "&placeId=" + placeid + "&companionId=" + companionid + "&type=2");
        JSONObject jsonObject = new JSONObject(json);
        int classifierid = jsonObject.getInt("policyId");
        String classifierpolicy = jsonObject.getString("policy");
        return classifierid;
    }

    public static int vikoreval(String placeid, String companionid) throws JSONException {
        int vikorpolicy = 0;
        int classifierpolicy = getpolicy(placeid, companionid);
        checkinlist();
        Map<String, List<Double>> usersvalmap = new HashMap<>();
        String[] companid = companionid.split("\\|");
        Map<String, List<Double>> mypayoff = populatepayoffs();
        usersvalmap.put("25", mypayoff.get(placeid));
        for(String companion : companid){
            List<Double> initialPayoff = Arrays.asList(.25,.25,.25,.25);
            if(feedbackMap.containsKey(placeid)){
                Map<String, List<String>> placeFeedback = feedbackMap.get(placeid);
                if(placeFeedback.containsKey(companion)){
                    List<String> feedback = placeFeedback.get(companion);
                    for(int index = 1; index<=4; index++){
                        if(feedback.contains(String.valueOf(index))){
                            initialPayoff.set(index-1, initialPayoff.get(index-1) + redistiburePayoff(getSanctionId(feedback.get(0))));
                        }else{
                            initialPayoff.set(index-1, initialPayoff.get(index-1) - redistiburePayoff(getSanctionId(feedback.get(0)))/3);
                        }
                    }
                }
            }
            usersvalmap.put(companion, initialPayoff);
        }

        jsonPostRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox-sd/services.jsp?"+
                 "query=setCheckIn&userId=25&placeId=" + placeid +"&companions=" + companionid + "&classfierSuggested=" + classifierpolicy +"&policy=" + calculate(normalizeMap(usersvalmap)));
        return calculate(normalizeMap(usersvalmap));
    }

    public static Double redistiburePayoff(Integer sanction){
       Double value = 0.0;
       if(sanction == 5) {
           value = 0.25;
       }else if(sanction == 4){
           value = 0.15;
       }else if(sanction == 2){
           value = -0.15;
       }else if(sanction == 1){
           value = -0.25;
       }
        return value;
    }

    public static Integer getSanctionId(String sanction){
       if(sanction.equals("negative")){
           return 2;
        }else if(sanction.equals("positive")){
           return 4;
       }else if(sanction.equals("very positive")) {
           return 5;
       }else if(sanction.equals("neutral")){
            return 3;
       }else{
           return 1;
       }
    }

    public static Map<Integer, ArrayList<Double>> normalizeMap(Map<String, List<Double>> usersvalmap ) {

        for (String key : usersvalmap.keySet()) {
            List<Double> values = usersvalmap.get(key);
            ArrayList<Double> newVal = new ArrayList<>();
            for (Double temp : values) {
                Double maxVal = Collections.max(values);
                Double minVal = Collections.min(values);
                Double value = 0.0;
                if (maxVal != minVal) {
                    value = (maxVal - temp) / (maxVal - minVal);
                }
                newVal.add(value);
            }
            usersvalmap.put(key, newVal);
        }

        Map<Integer, ArrayList<Double>> n_map = new HashMap<>();
        for(String key: usersvalmap.keySet()) {
            List<Double> values = usersvalmap.get(key);
            for(Integer index = 1; index <= 4; index++) {
                ArrayList<Double> policyPayoffs = new ArrayList<>();
                if(n_map.containsKey(index)) {
                    policyPayoffs = n_map.get(index);
                }
                policyPayoffs.add(values.get(index - 1));
                n_map.put(index, policyPayoffs);
            }
        }
        return n_map;
    }

    public static int calculate(Map<Integer, ArrayList<Double>> n_map){
        int vikorPolicy = 4;
        List<Double> sj = new ArrayList<>();
        List<Double> rj = new ArrayList<>();
        List<Double> qj = new ArrayList<>();

        for(Integer key: n_map.keySet()){
            Double sum = 0.0;
            List<Double> temp = n_map.get(key);
            for(Double val: temp){
                sum = sum + val;
            }
            sj.add(sum);
            rj.add(Collections.max(temp));
        }

        double v = 0.5;
        double sjMax = Collections.max(sj);
        double sjMin = Collections.min(sj);
        double rjMax = Collections.max(rj);
        double rjMin = Collections.min(rj);

        for (int i=0; i<4; i++){
            Double temp2 = 1.0;
            try {
                temp2 = (v * ((sj.get(i) - sjMin) / (sjMax - sjMin))) + ((1 - v) * ((rj.get(i) - rjMin) / (rjMax - rjMin)));
            }catch (Exception e){

            }
            qj.add(temp2);
        }

        double qjMin = Collections.min(qj);
        for(int index = 0; index < 4; index++){
            if (qjMin == qj.get(index)){
                vikorPolicy = index + 1;
            }
        }
        
        return vikorPolicy;
    }

    public static void printMap(Map<Integer, ArrayList<Double>> n_map) {
        for(Integer key : n_map.keySet()) {
            ArrayList<Double> values = n_map.get(key);
            System.out.println(values);
        }
    }

    public static Map<String, List<Double>> populatepayoffs(){
        Map<String, List<Double>> context = new HashMap<>();
        context.put("1", new ArrayList<Double>(Arrays.asList(0.10,0.20,0.30,0.60)));
        context.put("2", new ArrayList<Double>(Arrays.asList(0.70,0.15,0.10,0.05)));
        context.put("3", new ArrayList<Double>(Arrays.asList(0.05,0.30,0.50,0.15)));
        context.put("4", new ArrayList<Double>(Arrays.asList(0.05,0.50,0.30,0.15)));
        context.put("5", new ArrayList<Double>(Arrays.asList(0.05,0.30,0.50,0.15)));
        context.put("6", new ArrayList<Double>(Arrays.asList(0.05,0.50,0.30,0.15)));
        context.put("7", new ArrayList<Double>(Arrays.asList(0.70,0.15,0.10,0.05)));
        return context;
    }

    public static void checkinlist() throws JSONException {
        String json = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox-sd/services.jsp?query=getAllCheckInByUser&userId=25");
        String checkinId = null;
        JSONObject jsonObject = new JSONObject(json);
        JSONArray checkins = jsonObject.getJSONArray("check-ins");
        for (int i = 0; i < checkins.length(); i++) {
            JSONObject dataObj = (JSONObject) checkins.get(i);
            checkinId = dataObj.getString("checkinId");
            feedback(checkinId);
        }
    }

    public static void feedback(String checkinId) throws JSONException {
        //To fetch userId and feedback
        String feedbackDetails = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox-sd/services.jsp?query=getCheckinFeedbacks&checkinId=" + checkinId + "");
        JSONObject feedbackDetailsObject = new JSONObject(feedbackDetails);
        JSONArray feedbacks = feedbackDetailsObject.getJSONArray("feedbacks");

        //To fetch placeID and policyID
        String checkInDetail = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox-sd/services.jsp?query=getCheckIn&checkinId=" + checkinId + "");
        JSONObject checkInDetailObject = new JSONObject(checkInDetail);
        String placeId = checkInDetailObject.getJSONObject("check-in").getString("placeId");
        String policyId = checkInDetailObject.getJSONObject("check-in").getString("policyId");

        Map<String, List<String>> userFeedback = new HashMap<>();
        for (int i = 0; i < feedbacks.length(); i++) {
            JSONObject dataObj = (JSONObject) feedbacks.get(i);
            String userId = dataObj.getString("userId");
            String feedbackID = dataObj.getString("feedback");
            Map<String, List<String>> userFeedbackForPlace = new HashMap<>();
            if (feedbackMap.containsKey(placeId)) {
                userFeedbackForPlace = feedbackMap.get(placeId);
            }
            List<String> feedback = new ArrayList<>();
            feedback.addAll(Arrays.asList(feedbackID, policyId));
            userFeedbackForPlace.put(userId, feedback);
            feedbackMap.put(placeId, userFeedbackForPlace);
        }
    }



    public static void Place() {
        System.out.println("Please enter corresponding number for place:");
        System.out.println("1. Graduation ceremony");
        System.out.println("2. Library during the day");
        System.out.println("3. Hurricane during the day");
        System.out.println("4. Airport at night");
        System.out.println("5. Hiking on a mountain");
        System.out.println("6. Presenting a paper at a conference");
        System.out.println("7. Bar with a fake ID");
    }

    public static void MainUI() {
        System.out.println("What would you like to do?");
        System.out.println("1. Make a check in");
        System.out.println("2. Feedback to your tagger");
        System.out.println("3. Exit");
    }

    public static void GetUnattendedCheckins(String checkinId) throws JSONException {
        String json = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox-sd/services.jsp?query=getCheckIn&checkinId=" + checkinId + "");
        JSONObject jsonObject = new JSONObject(json);
        JSONObject dataObj = jsonObject.getJSONObject("check-in");
        String place_Id = dataObj.getString("placeId");
        String userId = dataObj.getString("userId");
        String policyId = dataObj.getString("policyId");
        String username = "";
        JSONArray companions = dataObj.getJSONArray("companions");
        for (int j = 0; j < companions.length(); j++) {
            JSONObject dataObj2 = (JSONObject) companions.get(j);
            String compId = dataObj2.getString("userId");
            if (!compId.equals("25")) {
                username = username + compId + "|";
            }
        }
        username = username + userId;
        int classifierPolicy = getpolicy(place_Id, username);
        int vikorPolicy = vikoreval(place_Id, username);

        int classSanction = giveSanction(Integer.parseInt(policyId), classifierPolicy);
        int vikorSanction = giveSanction(Integer.parseInt(policyId), vikorPolicy);
        System.out.println("Sending classifier sanction: " + classSanction + " and Vikor sanction Id "+ vikorSanction);
        jsonPostRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox-sd/services.jsp?query=respondToCheckin&checkinId=+" +
                checkinId + "&companionId=25&classfierSanctionId=" + classSanction + "&sanctionId=" + vikorSanction);
    }

    public static int giveSanction(int givenPolicy, int generatedPolicy){
        int p = 3;
        int diff = Math.abs(givenPolicy - generatedPolicy);
        if(diff>=2){
            p = 1;
        }else if(diff==1){
            p = 2;
        }else{
            p = 4;
        }
        return p;
    }

    public static String jsonPostRequest(String urlQueryString) {
        String json = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlQueryString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();
            InputStream inStream = connection.getInputStream();
            json = streamToString(inStream); // input stream to string
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return json;
    }

    public static void main(String[] args) throws IOException, JSONException {
        Scanner console = new Scanner(System.in);
        MainUI();
        int choice = console.nextInt();
        while (choice == 1 || choice == 2) {
            switch (choice) {
                case 1:// Make a check in by providing place and companions
                    Place();
                    placeid = console.next();
                    System.out.println(
                            "Please enter the ids of the people you want to tag(For example, after colon please put ids like this-'id1|id2|id3'):");
                    companionid = console.next();
                    vikoreval(placeid, companionid);
                    System.out.println("Check-in completed");
                    break;
                case 2:// Provide feedback based on your goals
                    // GetUnattendedCheckins();
                    System.out.println("Please enter the checkin id to sanction:");
                    inputcheckin = console.next();
                    GetUnattendedCheckins(inputcheckin);
                    break;
            }
            MainUI();
            choice = console.nextInt();
        }
        console.close();
    }

}