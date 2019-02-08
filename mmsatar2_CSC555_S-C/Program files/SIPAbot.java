import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class SIPAbot {

    static int UID = 25;
    static String inputcheckin = null;
    static String checkin = null;
    static String placeid = null;
    static String companionid = null;
    static int policyid = 0;
    static Map<String, Map<String, List<String>>> feedbackMap = new HashMap<>();
    static final Map<String, Integer> sanctionMap = Data.sanctionMap();

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

    public static void testpostrequest() throws JSONException {
        jsonPostRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=respondToCheckin&checkinId=2508&companionId=25&sanctionId=4");
    }

    public static String getRelation(String userID) throws JSONException, IOException {
        String people = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=getRelationshipList");
        JSONArray peoplejson = new JSONArray(people);
        Map<String, Object> resultMap;
        ObjectMapper mapperObj = new ObjectMapper();
        for (int j = 0; j < peoplejson.length(); j++) {
            if (peoplejson.getJSONObject(j).get("userId").equals("25")) {
                resultMap = mapperObj.readValue(peoplejson.getJSONObject(j).toString(),
                        new TypeReference<HashMap<String, Object>>() {
                        });
                for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                    Object x = entry.getValue();
                    if (!entry.getKey().equals("userId")) {
                        ArrayList<Integer> list = (ArrayList<Integer>) x;
                        if (list.contains(userID)) {
                            return entry.getKey();
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String recommend_checkIn(String placeId, String companionids) throws JSONException, IOException {
        checkinlist();
        Map<String, Map<String, List<String>>> policyMap = Data.Makecheckin();
        List<String> userIds = Arrays.asList(companionids.split("\\|"));
        List<String> userPolicies = new ArrayList<>();
        Map<String, List<String>> policyPreferences = Data.populateContext();
        for (String user : userIds) {
            String user_policy = null;
            String user_relation = getRelation(user);
            for (Map.Entry<String, Map<String, List<String>>> entry : policyMap.entrySet()) {
                String policy = entry.getKey();
                Map<String, List<String>> place = entry.getValue();
                List<String> allowed_relations = place.get(placeId);
                if (allowed_relations.contains(user_relation)) {
                    user_policy = policy;
                }
            }
            if (user_policy == null) {
                user_policy = policyPreferences.get(placeId).get(0);
            }
            user_policy = getUserPreferenceForFeedback(placeId, user_policy, user);
            userPolicies.add(user_policy);
        }
        return mostFrequent(userPolicies);
    }

    public static String getUserPreferenceForFeedback(String placeId, String policyId, String userId) {
        if (feedbackMap.containsKey(placeId)) {
            Map<String, List<String>> feedbackDetails = feedbackMap.get(placeId);
            if (feedbackDetails.containsKey(userId)) {
                String policy = feedbackDetails.get(userId).get(1);
                int sanction = sanctionMap.get(feedbackDetails.get(userId).get(0));
                if (sanction <= 2 && policyId.equals(policy)) {
                    if (policy.equals("1") || policy.equals("2")) {
                        return String.valueOf((Integer.parseInt(policy) + 1));
                    } else {
                        return String.valueOf((Integer.parseInt(policy) - 1));
                    }
                }
            }
        }
        return policyId;
    }


    public static void checkinlist() throws JSONException {
        String json = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=getAllCheckInByUser&userId=25");
        String checkinId = null;
        JSONObject jsonObject = new JSONObject(json);
        JSONArray checkins = jsonObject.getJSONArray("check-ins");
        for (int i = 0; i < checkins.length(); i++) {
            JSONObject dataObj = (JSONObject) checkins.get(i);
            checkinId = dataObj.getString("checkinId");
            feedback(checkinId);
        }
    }

    static String mostFrequent(List<String> policies) {
        Map<String, Integer> map = new HashMap<>();
        for (String policy : policies) {
            Integer count = 1;
            if (map.containsKey(policy)) {
                count = map.get(policy) + 1;
            }
            map.put(policy, count);
        }
        return Collections.max(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
    }

    public static void feedback(String checkinId) throws JSONException {
        //To fetch userId and feedback
        String feedbackDetails = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=getCheckinFeedbacks&checkinId=" + checkinId + "");
        JSONObject feedbackDetailsObject = new JSONObject(feedbackDetails);
        JSONArray feedbacks = feedbackDetailsObject.getJSONArray("feedbacks");

        //To fetch placeID and policyID
        String checkInDetail = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=getCheckIn&checkinId=" + checkinId + "");
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

    public static void GetUnattendedCheckins() throws JSONException {
        String json = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=getUnattendedCheckins&userId=25");
        JSONObject jsonObject = new JSONObject(json);
        JSONArray checkins = jsonObject.getJSONArray("check-ins");
        for (int i = 0; i < checkins.length(); i++) {
            JSONObject dataObj = (JSONObject) checkins.get(i);
            String checkinId = dataObj.getString("checkinId");
            String userName = dataObj.getString("userName");
            String placeName = dataObj.getString("placeName");
            String placeId = dataObj.getString("placeId");
            String userId = dataObj.getString("userId");
            String sharejson = dataObj.getString("policyId");
            System.out.println("Checkin ID:" + checkinId + " User Id: " + userId + " User Name:" + userName + " Place Name:" + placeName + " Place Id: " + placeId + " Share Id: " + sharejson);
        }
    }


    public static void provideSanctions() throws JSONException, IOException {
        Map<String, List<String>> placemap = Data.placeMap();
        Map<String, List<String>> sharemap = Data.shareMap();
        int sanctionid = 0;
        String checkinId = null;
        String json = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=getUnattendedCheckins&userId=25");
        JSONObject jsonObject = new JSONObject(json);
        JSONArray checkins = jsonObject.getJSONArray("check-ins");

        String people = jsonGetRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=getRelationshipList");
        JSONArray peoplejson = new JSONArray(people);
        for (int i = 0; i < checkins.length(); i++) {
            JSONObject dataObj = (JSONObject) checkins.get(i);
            checkinId = dataObj.getString("checkinId");
            String userId = dataObj.getString("userId");
            String placeId = dataObj.getString("placeId");
            String shareId = dataObj.getString("policyId");
            if (dataObj.getString("checkinId").equals(inputcheckin)) {
                for (int j = 0; j < peoplejson.length(); j++) {
                    if (peoplejson.getJSONObject(j).get("userId").equals("25")) {
                        if (placemap.get(placeId).contains(getRelation(userId)) && sharemap.get(placeId).contains(shareId)) {
                            System.out.println("Sending positive sanction");
                            sanctionid = 4;
                            jsonPostRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=respondToCheckin&checkinId=" + checkinId + "&companionId=25&sanctionId=" + sanctionid + "");
                        } else {
                            System.out.println("Sending negative sanction");
                            sanctionid = 2;
                            jsonPostRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=respondToCheckin&checkinId=" + checkinId + "&companionId=25&sanctionId=" + sanctionid + "");
                        }
                    }
                }
            }
        }
    }

    public static void MainUI() {
        System.out.println("What would you like to do?");
        System.out.println("1. Make a check in");
        System.out.println("2. Feedback to your tagger");
        System.out.println("3. Exit");
    }

    public static void main(String[] args) throws IOException, JSONException {
        Scanner console = new Scanner(System.in);
        MainUI();
        int choice = console.nextInt();
        while (choice == 1 || choice == 2) {
            switch (choice) {
                case 1://Make a check in by providing place and companions
                    Place();
                    placeid = console.next();
                    System.out.println("Please enter the ids of the people you want to tag(For example, after colon please put ids like this-'id1|id2|id3'):");
                    companionid = console.next();
                    String policyId = recommend_checkIn(placeid, companionid);
                    System.out.println("Making check-in based on recommended policy: " + policyId);
                    jsonPostRequest("http://yangtze.csc.ncsu.edu:9090/csc555checkinf18-sandbox/services.jsp?query=setCheckIn&userId=25&placeId=" + placeid + "&companions=" + companionid + "&policy=" + policyId + "");
                    break;
                case 2://Provide feedback based on your goals
                    GetUnattendedCheckins();
                    System.out.println("Please enter the checkin id to sanction:");
                    inputcheckin = console.next();
                    provideSanctions();
                    break;

            }
            MainUI();
            choice = console.nextInt();
        }
        console.close();
    }
}