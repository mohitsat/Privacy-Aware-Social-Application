import java.util.*;
import org.json.*;

public class Data {
    public static Map<String, List<String>> populateContext(){
        Map<String, List<String>> context = new HashMap<>();
        context.put("1", new ArrayList<String>(Arrays.asList("4","3","2","1")));
        context.put("2", new ArrayList<String>(Arrays.asList("1","2","3")));
        context.put("3", new ArrayList<String>(Arrays.asList("3","2")));
        context.put("4", new ArrayList<String>(Arrays.asList("2","3")));
        context.put("5", new ArrayList<String>(Arrays.asList("3","2")));
        context.put("6", new ArrayList<String>(Arrays.asList("2","3")));
        context.put("7", new ArrayList<String>(Arrays.asList("1")));
        return context;
    }

    public static Map<String, Integer> sanctionMap(){
        Map<String, Integer> policy = new HashMap<>();
        policy.put("very negative", 1);
        policy.put("negative", 2);
        policy.put("neutral", 3);
        policy.put("positive", 4);
        policy.put("very positive", 5);
        return policy;
    }

    public static Map<String, Map<String, List<String>>> Makecheckin() throws JSONException {
        Map<String, Map<String, List<String>>> policymap = new HashMap<String, Map<String, List<String>>>();

        Map<String, List<String>> shareallmap = new HashMap<String, List<String>>();
        Map<String, List<String>> sharefriendmap = new HashMap<String, List<String>>();
        Map<String, List<String>> sharecompmap = new HashMap<String, List<String>>();
        Map<String, List<String>> sharenonemap = new HashMap<String, List<String>>();

        List<String> SNGList = new ArrayList<String>();		//Share None Lists
        List<String> SNLList = new ArrayList<String>();
        {
            SNLList.add("family");
            SNLList.add("friends");
            SNLList.add("colleagues");
            SNLList.add("stranger");
        }
        List<String> SNHList = new ArrayList<String>();
        List<String> SNAList = new ArrayList<String>();
        List<String> SNMList = new ArrayList<String>();
        List<String> SNPList = new ArrayList<String>();
        List<String> SNBList = new ArrayList<String>();
        {
            SNBList.add("family");
            SNBList.add("friends");
            SNBList.add("colleagues");
            SNBList.add("stranger");
        }

        List<String> SCGList = new ArrayList<String>();     //Share Companions Lists
        List<String> SCLList = new ArrayList<String>();
        List<String> SCHList = new ArrayList<String>();
        List<String> SCAList = new ArrayList<String>();
        {
            SCAList.add("family");
            SCAList.add("friends");
            SCAList.add("colleagues");
        }
        List<String> SCMList = new ArrayList<String>();
        List<String> SCPList = new ArrayList<String>();
        {
            SCPList.add("colleagues");
        }
        List<String> SCBList = new ArrayList<String>();

        List<String> SFGList = new ArrayList<String>();		//Share Friends Lists
        List<String> SFLList = new ArrayList<String>();
        List<String> SFHList = new ArrayList<String>();
        {
            SFHList.add("family");
            SFHList.add("friends");
        }
        List<String> SFAList = new ArrayList<String>();
        List<String> SFMList = new ArrayList<String>();
        {
            SFMList.add("family");
            SFMList.add("friends");
        }
        List<String> SFPList = new ArrayList<String>();
        List<String> SFBList = new ArrayList<String>();

        List<String> SAGList = new ArrayList<String>();		//Share All Lists
        {
            SAGList.add("family");
            SAGList.add("friends");
            SAGList.add("colleagues");
            SAGList.add("stranger");
        }
        List<String> SALList = new ArrayList<String>();
        List<String> SAHList = new ArrayList<String>();
        List<String> SAAList = new ArrayList<String>();
        List<String> SAMList = new ArrayList<String>();
        List<String> SAPList = new ArrayList<String>();
        List<String> SABList = new ArrayList<String>();

        // put values into map
        {
            sharenonemap.put("1", SNGList); //Share None Graduation List--Abbreviations made this way
            sharenonemap.put("2", SNLList); //These 4 maps have place as key, and people groups in list as values
            sharenonemap.put("3", SNHList);
            sharenonemap.put("4", SNAList);
            sharenonemap.put("5", SNMList); //Hiking on a mountain is M, as Hurricane took the letter H already
            sharenonemap.put("6", SNPList);
            sharenonemap.put("7", SNBList);
        }
        {
            sharecompmap.put("1", SCGList);
            sharecompmap.put("2", SCLList);
            sharecompmap.put("3", SCHList);
            sharecompmap.put("4", SCAList);
            sharecompmap.put("5", SCMList); //Hiking on a mountain is M, as Hurricane took the letter H already
            sharecompmap.put("6", SCPList);
            sharecompmap.put("7", SCBList);
        }
        {
            sharefriendmap.put("1", SFGList);
            sharefriendmap.put("2", SFLList);
            sharefriendmap.put("3", SFHList);
            sharefriendmap.put("4", SFAList);
            sharefriendmap.put("5", SFMList); //Hiking on a mountain is M, as Hurricane took the letter H already
            sharefriendmap.put("6", SFPList);
            sharefriendmap.put("7", SFBList);
        }
        {
            shareallmap.put("1", SAGList);
            shareallmap.put("2", SALList);
            shareallmap.put("3", SAHList);
            shareallmap.put("4", SAAList);
            shareallmap.put("5", SAMList); //Hiking on a mountain is M, as Hurricane took the letter H already
            shareallmap.put("6", SAPList);
            shareallmap.put("7", SABList);
        }
        {

            policymap.put("1", sharenonemap); //Maps have policy id as key, and share maps as values
            policymap.put("2", sharecompmap);
            policymap.put("3", sharefriendmap);
            policymap.put("4", shareallmap);
        }
        return policymap;
    }

    public static Map<String, List<String>> shareMap(){
        Map<String, List<String>> sharemap = new HashMap<String, List<String>>();

        List<String> Graduationsharelist = new ArrayList<String>();
        {
            Graduationsharelist.add("1");
            Graduationsharelist.add("2");
            Graduationsharelist.add("3");
            Graduationsharelist.add("4");
            // create list two and store values
        }
        List<String> Librarysharelist = new ArrayList<String>();
        {
            Librarysharelist.add("2");
            Librarysharelist.add("3");
            Librarysharelist.add("4");
            // create list three and store values
        }
        List<String> Hurricanesharelist = new ArrayList<String>();
        {
            Hurricanesharelist.add("2");
            Hurricanesharelist.add("3");
        }
        List<String> Airportsharelist = new ArrayList<String>();
        {
            Airportsharelist.add("2");
            Airportsharelist.add("3");
        }
        List<String> Hikingsharelist = new ArrayList<String>();
        {
            Hikingsharelist.add("2");
            Hikingsharelist.add("3");
        }
        List<String> Papersharelist = new ArrayList<String>();
        {
            Papersharelist.add("1");
        }
        List<String> Barsharelist = new ArrayList<String>();
        {
            Barsharelist.add("4");
        }

        {

            sharemap.put("1", Graduationsharelist);
            sharemap.put("2", Librarysharelist);
            sharemap.put("3", Hurricanesharelist);
            sharemap.put("4", Airportsharelist);
            sharemap.put("5", Hikingsharelist);
            sharemap.put("6", Papersharelist);
            sharemap.put("7", Barsharelist);
        }
        return sharemap;
    }

    public static  Map<String, List<String>> placeMap(){
        Map<String, List<String>> placemap = new HashMap<String, List<String>>();
        // create list one and store values
        List<String> Graduationlist = new ArrayList<String>();
        {
            Graduationlist.add("family members");
            Graduationlist.add("friends");
            Graduationlist.add("colleagues");
            Graduationlist.add("stranger");
            // create list two and store values
        }
        List<String> Librarylist = new ArrayList<String>();
        {
            Librarylist.add("family members");
            Librarylist.add("friends");
            Librarylist.add("colleagues");
            // create list three and store values
        }
        List<String> Hurricanelist = new ArrayList<String>();
        {
            Hurricanelist.add("family members");
            Hurricanelist.add("friends");
        }
        List<String> Airportlist = new ArrayList<String>();
        {
            Airportlist.add("family members");
            Airportlist.add("friends");
        }
        List<String> Hikinglist = new ArrayList<String>();
        {
            Hikinglist.add("family members");
            Hikinglist.add("friends");
        }
        List<String> Paperlist = new ArrayList<String>();
        {
            Paperlist.add("colleagues");
        }
        List<String> Barlist = new ArrayList<String>();
        {
            Barlist.add("family members");
            Barlist.add("friends");
            Barlist.add("colleagues");
            Barlist.add("stranger");
        }
        // put values into map
        {

            placemap.put("1", Graduationlist);
            placemap.put("2", Librarylist);
            placemap.put("3", Hurricanelist);
            placemap.put("4", Airportlist);
            placemap.put("5", Hikinglist);
            placemap.put("6", Paperlist);
            placemap.put("7", Barlist);
        }
        return placemap;
    }
}