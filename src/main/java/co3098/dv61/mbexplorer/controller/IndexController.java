/**
 * (C) Artur Boronat, 2016
 */
package co3098.dv61.mbexplorer.controller;

import co3098.dv61.mbexplorer.Triplet;
import co3098.dv61.mbexplorer.domain.*;
import co3098.dv61.mbexplorer.repository.*;
import co3098.dv61.mbexplorer.service.UserService;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Null;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class IndexController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @RequestMapping("/")
    public ModelAndView index(HttpServletRequest request) {

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        if (email == null){
            return new ModelAndView("login");
        }

        // get all folders available on the db
        List<Folder> folders = userRepository.findByEmail(email).getFolders();

        // create a list which will store the root folders, that is, folders which on path do not have |
        List<Folder> rootFolders = new ArrayList<>();

        // store all the folders such that path doesn't contain |
        for (Folder folder : folders){
            if (!folder.getPath().contains("|")){
                rootFolders.add(folder);
            }
        }

        Gson gson = new Gson();
        List<String> treeBranches = new ArrayList<>();
        // for each of the root folders convert it into a json object
        for (Folder root : rootFolders){
            JsonObject jsonObject = (JsonObject) gson.toJsonTree(root);

            // check the existence of respective keys
            boolean locked = jsonObject.has("locked");
            boolean linkItems = jsonObject.has("linkItems");
            boolean locationItems = jsonObject.has("locationItems");
            boolean textItems = jsonObject.has("textItems");
            boolean subfolders = jsonObject.has("subfolders");

            // remove unnecessary keys from the element's level
            if (locked) {
                jsonObject.remove("locked");
            }

            if (linkItems) {
                jsonObject.getAsJsonArray("linkItems").forEach(element -> element.getAsJsonObject().remove("url"));
            }

            if (locationItems) {
                jsonObject.getAsJsonArray("locationItems").forEach(element -> element.getAsJsonObject().remove("latitude"));
                jsonObject.getAsJsonArray("locationItems").forEach(element -> element.getAsJsonObject().remove("longitude"));
            }

            if (textItems){
                jsonObject.getAsJsonArray("textItems").forEach(element -> element.getAsJsonObject().remove("content"));
            }

            if (jsonObject.has("path")){
                jsonObject.remove("path");
            }

            // add "locationItems","linkItems","textItems" arrays to "subfolders" array
            if (subfolders) {
                if (linkItems) {
                    jsonObject.getAsJsonArray("linkItems").forEach(object -> jsonObject.getAsJsonArray("subfolders").add(object));
                }
                if (locationItems) {
                    jsonObject.getAsJsonArray("locationItems").forEach(object -> jsonObject.getAsJsonArray("subfolders").add(object));
                }
                if (textItems) {
                    jsonObject.getAsJsonArray("textItems").forEach(object -> jsonObject.getAsJsonArray("subfolders").add(object));
                }
            } else {
                // if there's no subfolders, create a new jsonarray which will have all the items together
                if (!jsonObject.has("nodes")) {
                    // if there's no nodes already, insert nodes

                    JsonArray itemsJson = new JsonArray();
                    if (jsonObject.has("linkItems")) {
                        jsonObject.getAsJsonArray("linkItems").forEach(object -> itemsJson.add(object));
                    }
                    if (jsonObject.has("locationItems")) {
                        jsonObject.getAsJsonArray("locationItems").forEach(object -> itemsJson.add(object));
                    }
                    if (jsonObject.has("textItems")) {
                        jsonObject.getAsJsonArray("textItems").forEach(object -> itemsJson.add(object));
                    }

                    if (itemsJson.size() > 0) {
                        jsonObject.add("nodes", itemsJson);
                    }
                }
            }

            // remove "locationItems","linkItems","textItems"
            if (linkItems) {
                jsonObject.remove("linkItems");
            }
            if (locationItems) {
                jsonObject.remove("locationItems");
            }
            if (textItems) {
                jsonObject.remove("textItems");
            }


            //System.out.println(jsonObject.toString());

            // go through subfolder by subfolder, removing "url", "latitude", "longitude" keys while at it. using recursion
            if (jsonObject.has("subfolders")) {
                restructureJsonTree(jsonObject.getAsJsonArray("subfolders"));
            }

            // replace the key names according to bootstrap treeview convention
            String treeView = gson.toJson(jsonObject).replace("\\u003cb\\u003e","<b>").replace("\\u003c/b\\u003e","</b>")
                    .replace("\"subfolders\":","\"nodes\":")
                    //.replace("\"path\":","\"href\":")
                    .replace("\"name\":","\"text\":")
                    .replace("\"title\":","\"text\":")
                    .replace("\"location\":","\"text\":");

            treeBranches.add(treeView);
        }

        // merge together
        String completeTree = "[";
        for (String component : treeBranches){
            completeTree = completeTree + component + ",";
        }
        completeTree = completeTree.substring(0,completeTree.length()-1);
        completeTree = completeTree + "]";

        //System.out.println(completeTree);
        boolean empty = false;
        if (completeTree.equals("]")){
            empty = true;
        }

        /////// TREEMAP ////////
        List<Triplet<String,String,Integer>> data = formatDataForTreemap(email);
        boolean treemapEmpty = true;

        // setup treemap
        // format the treemap into string
        String treemap = "[['root', null, 0],";
        for (Triplet<String,String,Integer> dataRow : data){
            // make every zero a one so that the folder/item appears on treemap
            // nothing appears if value is 0
            int count = dataRow.getThird() + 1;

            treemap = treemap + "[" + dataRow.getFirst() + "," + dataRow.getSecond() + "," + count + "]";

            // prepare for next row
            treemap = treemap + ",";

            if (dataRow.getThird() != 0){
                treemapEmpty = false;
            }
        }

        // remove the last comma
        treemap = treemap.substring(0,treemap.length()-1);

        // finalize
        treemap = treemap + "]";

        // System.out.println(treemap);

        /////// ORGANIZATION CHART ////////////
        data = formatDataForCharts(email);
        String chart = "[['root', ''],";
        for (Triplet<String,String,Integer> dataRow : data){
            chart = chart + "[" + dataRow.getFirst() + "," + dataRow.getSecond() + "]";

            // prepare for next row
            chart = chart + ",";
        }
        // remove the last comma
        chart = chart.substring(0,chart.length()-1);

        // finalize
        chart = chart + "]";

        // System.out.println(chart);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("index");
        mv.addObject("completetree", completeTree);
        mv.addObject("isempty",empty);
        mv.addObject("treemap", treemap);
        mv.addObject("chart", chart);
        mv.addObject("treemapIsEmpty", treemapEmpty);

        return mv;
    }

    public void restructureJsonTree(JsonArray subfolders){
        //System.out.println("================ Method ran! ========================");
        final int size = subfolders.size();
        //System.out.println("Subfolders array size: "+size);
        //System.out.println("+-+-+-+-+-+-+-+-+-+-+-+");
        //System.out.println(subfolders);

        for (int i = 0; i < size; i++) {
            //System.out.println("== Loop index: "+i+ " size: "+size);
            // get jsonobject

            JsonObject jsonElement = (JsonObject) subfolders.get(i);

            //System.out.println("-----------------");
            //System.out.println("No exception");
            //System.out.println(jsonElement.toString());
            //System.out.println("-----------------");

//            System.out.println("Name: "+jsonElement.get("name").toString());

            /// MODIFICATIONS AT THE CURRENT LEVEL ///

            // remove unnecessary keys from the element's level
            if (jsonElement.has("locked")) {
                jsonElement.remove("locked");
            }

            if (jsonElement.has("linkItems")) {
                jsonElement.getAsJsonArray("linkItems").forEach(element -> element.getAsJsonObject().remove("url"));
            }

            if (jsonElement.has("locationItems")) {
                jsonElement.getAsJsonArray("locationItems").forEach(element -> element.getAsJsonObject().remove("latitude"));
                jsonElement.getAsJsonArray("locationItems").forEach(element -> element.getAsJsonObject().remove("longitude"));
            }

            if (jsonElement.has("textItems")){
                jsonElement.getAsJsonArray("textItems").forEach(element -> element.getAsJsonObject().remove("content"));
            }

            if (jsonElement.has("path")){
                jsonElement.remove("path");
            }

            if (jsonElement.has("subfolders")) {
                //System.out.println("Triggered subfolders!");
                if (jsonElement.has("linkItems")) {
                    jsonElement.getAsJsonArray("linkItems").forEach(object -> jsonElement.getAsJsonArray("subfolders").add(object));
                }
                if (jsonElement.has("locationItems")) {
                    jsonElement.getAsJsonArray("locationItems").forEach(object -> jsonElement.getAsJsonArray("subfolders").add(object));
                }
                if (jsonElement.has("textItems")) {
                    jsonElement.getAsJsonArray("textItems").forEach(object -> jsonElement.getAsJsonArray("subfolders").add(object));
                }

            } else {
// if there's no subfolders, create a new jsonarray which will have all the items together
                if (!jsonElement.has("nodes")) {
                    // if there's no nodes already, insert nodes
                    //System.out.println("Creating new node: ");

                    JsonArray itemsJson = new JsonArray();
                    if (jsonElement.has("linkItems")) {
                        jsonElement.getAsJsonArray("linkItems").forEach(object -> itemsJson.add(object));
                    }
                    if (jsonElement.has("locationItems")) {
                        jsonElement.getAsJsonArray("locationItems").forEach(object -> itemsJson.add(object));
                    }
                    if (jsonElement.has("textItems")) {
                        jsonElement.getAsJsonArray("textItems").forEach(object -> itemsJson.add(object));
                    }

                    if (itemsJson.size() > 0) {
                        jsonElement.add("nodes", itemsJson);
                    }
                }
            }

            // remove "locationItems","linkItems","textItems"
            if (jsonElement.has("linkItems")) {
                jsonElement.remove("linkItems");
            }
            if (jsonElement.has("locationItems")) {
                jsonElement.remove("locationItems");
            }
            if (jsonElement.has("textItems")) {
                jsonElement.remove("textItems");
            }

            /// GET SUBFOLDER ///
            if (jsonElement.has("subfolders")) {
                //System.out.println("Element has subfolders, proceed to invocate restructureJsonTree method");
                restructureJsonTree(jsonElement.getAsJsonArray("subfolders"));

            }
            else{
                //System.out.println("Element does NOT have subfolders. No recursive method invocation");
            }

        }
    }

    public List<Triplet<String,String,Integer>> formatDataForCharts(String email){
        // get all folders
        List<Folder> folders = userRepository.findByEmail(email).getFolders();

        // get all items
        List<ItemLink> itemLinks = userRepository.findByEmail(email).getLinkItems();
        List<ItemLocation> itemLocations = userRepository.findByEmail(email).getLocationItems();
        List<ItemText> itemTexts = userRepository.findByEmail(email).getTextItems();

        List<Triplet<String,String,Integer>> finalOutput = new ArrayList<>();

        /////////////////FOLDERS SETUP/////////////////////////////////////////

        // loop through each folder from the repo
        for (Folder folder : folders){
            // get folder name
            String name = folder.getName();

            ///// get parent

            // get folder path
            String folderPath = folder.getPath();

            // split into |
            List<String> splitPath = new ArrayList<>(Arrays.asList(folderPath.split("\\|")));

            // remove last element, which is the current folder itself
            splitPath.remove(splitPath.size()-1);

            String parent = "root";

            // get parent (only has parent if the length is greater than 0, otherwise parent string will remain null, which will mean it is the root)
            if (splitPath.size() > 0){
                parent = splitPath.get(splitPath.size()-1);
            }

            // get number of items
            int numberOfItems = 0;

            if (folder.getLinkItems() != null){
                numberOfItems = numberOfItems + folder.getLinkItems().size();
            }

            if (folder.getLocationItems() != null){
                numberOfItems = numberOfItems + folder.getLocationItems().size();
            }

            if (folder.getTextItems() != null){
                numberOfItems = numberOfItems + folder.getTextItems().size();
            }

            // store it all as a triplet
            Triplet<String,String,Integer> triplet = new Triplet<>("'"+name+"'","'"+parent+"'",numberOfItems);

            // store it on the outputlist
            finalOutput.add(triplet);
        }

        return finalOutput;
    }

    public List<Triplet<String,String,Integer>> formatDataForTreemap(String email){
        // get all folders
        List<Folder> folders = userRepository.findByEmail(email).getFolders();

        // get all items
        List<ItemLink> itemLinks = userRepository.findByEmail(email).getLinkItems();
        List<ItemLocation> itemLocations = userRepository.findByEmail(email).getLocationItems();
        List<ItemText> itemTexts = userRepository.findByEmail(email).getTextItems();

        List<Triplet<String,String,Integer>> finalOutput = new ArrayList<>();

        /////////////////FOLDERS SETUP/////////////////////////////////////////

        // loop through each folder from the repo
        for (Folder folder : folders){
            // get folder name
            String name = folder.getName();

            ///// get parent

            // get folder path
            String folderPath = folder.getPath();

            // split into |
            List<String> splitPath = new ArrayList<>(Arrays.asList(folderPath.split("\\|")));

            // remove last element, which is the current folder itself
            splitPath.remove(splitPath.size()-1);

            String parent = "root";

            // get parent (only has parent if the length is greater than 0, otherwise parent string will remain null, which will mean it is the root)
            if (splitPath.size() > 0){
                parent = splitPath.get(splitPath.size()-1);
            }

            // get number of items
            int numberOfItems = 0;

            if (folder.getLinkItems() != null){
                numberOfItems = numberOfItems + folder.getLinkItems().size();
            }

            if (folder.getLocationItems() != null){
                numberOfItems = numberOfItems + folder.getLocationItems().size();
            }

            if (folder.getTextItems() != null){
                numberOfItems = numberOfItems + folder.getTextItems().size();
            }

            // store it all as a triplet
            Triplet<String,String,Integer> triplet = new Triplet<>("'"+name+"'","'"+parent+"'",numberOfItems);

            // store it on the outputlist
            finalOutput.add(triplet);
        }

        /////////////////ITEMS SETUP/////////////////////////////////////////

        // loop through each itemLink from the repo
        for (ItemLink item : itemLinks){
            // get item name
            String name = item.getTitle();

            ///// get parent

            // get item path
            String itemPath = item.getPath();

            // split into |
            List<String> splitPath = new ArrayList<>(Arrays.asList(itemPath.split("\\|")));

            // remove last element, which is the current item itself
            splitPath.remove(splitPath.size()-1);

            String parent = splitPath.get(splitPath.size()-1);

            // no items since it is an item itself
            int numberOfItems = 0;

            // store it all as a triplet
            Triplet<String,String,Integer> triplet = new Triplet<>("'"+name.replace("<b>","").replace("</b>","")+"'","'"+parent+"'",numberOfItems);

            // store it on the outputlist
            finalOutput.add(triplet);
        }

        // loop through each itemText from the repo
        for (ItemText item : itemTexts){
            // get item name
            String name = item.getTitle();

            ///// get parent

            // get item path
            String itemPath = item.getPath();

            // split into |
            List<String> splitPath = new ArrayList<>(Arrays.asList(itemPath.split("\\|")));

            // remove last element, which is the current item itself
            splitPath.remove(splitPath.size()-1);

            String parent = splitPath.get(splitPath.size()-1);

            // no items since it is an item itself
            int numberOfItems = 0;

            // store it all as a triplet
            Triplet<String,String,Integer> triplet = new Triplet<>("'"+name.replace("<b>","").replace("</b>","")+"'","'"+parent+"'",numberOfItems);

            // store it on the outputlist
            finalOutput.add(triplet);
        }

        // loop through each itemLocation from the repo
        for (ItemLocation item : itemLocations){
            // get item name
            String name = item.getLocation();

            ///// get parent

            // get item path
            String itemPath = item.getPath();

            // split into |
            List<String> splitPath = new ArrayList<>(Arrays.asList(itemPath.split("\\|")));

            // remove last element, which is the current item itself
            splitPath.remove(splitPath.size()-1);

            String parent = splitPath.get(splitPath.size()-1);

            // no items since it is an item itself
            int numberOfItems = 0;

            // store it all as a triplet
            Triplet<String,String,Integer> triplet = new Triplet<>("'"+name.replace("<b>","").replace("</b>","")+"'","'"+parent+"'",numberOfItems);

            // store it on the outputlist
            finalOutput.add(triplet);
        }

        return finalOutput;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public String indexPost(HttpServletRequest request, @RequestBody String email){

        // USES AJAX

        JsonElement jelement = new JsonParser().parse(email);
        JsonObject  jobject = jelement.getAsJsonObject();
        email = jobject.get("email").getAsString();

        //System.out.println("Parsed email: "+email);

        // make sure that the parameters are not null
        if (!email.equals("") || !email.equals("Undefined") || !email.equals("undefined")){

            // create session
            request.getSession(true).setAttribute("email", email);

            //System.out.println("On / POST, session attributed: "+email);

            // check if user is first time, if it is then add the user to db
            User user = userRepository.findByEmail(email);

            if (user == null){
                userService.addUser(email);
            }

            // return true
            String to_return = "true";
            //System.out.println("Returned: "+to_return);

            return to_return;
        }
        else{
            String to_return = "false";
            //System.out.println("Returned: "+to_return);

            return to_return;
        }
    }

    //// AUTHENTICATION PAGES ASSIGNMENT
    @RequestMapping("/login")
    public ModelAndView login() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("login");
        return mv;
    }

    @RequestMapping("/logout")
    public RedirectView logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // remove session
        request.getSession().invalidate();

        // redirect to login page
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/login");
        return redirectView;
    }

}
