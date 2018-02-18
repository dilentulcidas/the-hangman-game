package co3098.dv61.mbexplorer.controller;

import co3098.dv61.mbexplorer.domain.Folder;
import co3098.dv61.mbexplorer.domain.ItemLink;
import co3098.dv61.mbexplorer.domain.ItemLocation;
import co3098.dv61.mbexplorer.domain.ItemText;
import co3098.dv61.mbexplorer.repository.*;
import co3098.dv61.mbexplorer.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class RestAPIController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;

    ////////////////////////////////////////////////////////////////////////
    /////////////////// FOLDER /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    @RequestMapping("/service/create")
    public String createFolder(@RequestParam String email, @RequestParam String folder, @RequestParam(required = false) String parent) {
        Folder f = null;
        Folder p = null;
        String[] parents = null;

        // if parent is not null then process the parents content and create folder based on hierarchy
        if (parent != null){

            // put respective parent folder name in an array, order such as: Documents|Books|Fiction =» Document parent of Books parent of Fiction
            parents = parent.split("\\|");

            // check if these parent folders exist on the database
            String parentPath = "";
            for (String pai : parents){
                parentPath = parentPath + pai;

                // if a parent folder does not exist, return false
                if (userService.getFolder(email, parentPath) == null){
                    return "false";//(false+" - Parent folder "+pai+" does not exist on database. Path: "+parentPath);
                }

                parentPath = parentPath + "|";
            }

            // make sure that the folder with the given name doesnt already exist in the db in the same path
            String folderPath = parent+"|"+folder;

            Folder checkFolder = userService.getFolder(email, folderPath);
            if (checkFolder != null){
                // folder already exists, return false
                return "false";//(false+" - Folder "+folder+" already exists on the database on the same path. Path: "+folderPath);
            }

            // create child folder and add to db and list

            // create instance of new folder
            Folder newFolder = new Folder(folder,new ArrayList<>(),false,folder);

            //// set hierarchy right

            /// for the child folder
            // setup subfolder -- will be null since the folder is the child
            newFolder.setSubfolders(new ArrayList<>());

            // setup path
            newFolder.setPath(folderPath);

            // setup href
            newFolder.setHref("view/folder?path="+folderPath);

            // add folder to db
            userService.addFolder(email, newFolder);

            /// for the parent folder, add newFolder as subfolder

            // get the parent folder with the correct path
            parentPath = parent;
            Folder parentFolder = userService.getFolder(email, parentPath);

            if (parentFolder == null){
                return "false";//false+" Failed to get parentFolder with path: "+parent;
            }

            // update the parent folder's subfolder list
            List<Folder> subfolders = parentFolder.getSubfolders();

            // if subfolders is null then create new arraylist and add the newFolder
            if (subfolders == null){
                // create new arraylist to store subfolder and add the newFolder to the list
                List<Folder> newSubfoldersList = new ArrayList<>();
                newSubfoldersList.add(newFolder);
                parentFolder.setSubfolders(newSubfoldersList);
            }
            else {
                // subfolder list already exists, proceed to add subfolder
                subfolders.add(newFolder);
                parentFolder.setSubfolders(subfolders);
            }

            // update folder to db
            userService.updateFolder(email, parentFolder);

            ////// synchronize subfolders of all the parent folders in order to show the complete tree
            // refresh as necessary
            synchronizeSubfolders(email,parent,parent.split("\\|").length);


        }
        else{
            // if there are no parents, simply add the folder to db with no hierarchy

            // make sure that the folder with the given name doesnt already exist in the db in the same path
            Folder checkFolder = userService.getFolder(email, folder);

            if (checkFolder != null){
                // if already exists on db
                return "false";//(false+" - Folder "+folder+" already exists in the same path! Path: "+folder+" , no parent specified");
            }
            else {
                Folder newFolder = new Folder(folder, new ArrayList<>(), false, folder);
                newFolder.setHref("view/folder?path="+folder);
                userService.addFolder(email, newFolder);
            }
        }

        return "true";//"Successfully created folder "+folder;
    }

    @RequestMapping("/service/delete")
    public String deleteFolder(@RequestParam String email, @RequestParam String folder){

        // first make sure that folder name string isnt null
        if (folder != null){

            // make sure that the folder exists
            if (userService.getFolder(email, folder) != null){

                // get all the folders available on the db
                List<Folder> folders = userRepository.findByEmail(email).getFolders();

                // delete all the folders that start with the given folder path to be deleted
                for (Folder f : folders){
                    if (f.getPath().startsWith(folder)){
                        // delete the respective folder
                        userService.deleteFolder(email,f);
                    }
                }

                // get all items available on the db and delete the ones which start with the respective path
                List<ItemLink> itemLinkList = userRepository.findByEmail(email).getLinkItems();
                for (ItemLink i : itemLinkList){
                    if (i.getPath().startsWith(folder)){
                        userService.deleteItemLink(email,i);
                    }
                }

                List<ItemLocation> itemLocationList = userRepository.findByEmail(email).getLocationItems();
                for (ItemLocation i : itemLocationList){
                    if (i.getPath().startsWith(folder)){
                        userService.deleteitemLocation(email,i);
                    }
                }

                List<ItemText> itemTextList = userRepository.findByEmail(email).getTextItems();
                for (ItemText i : itemTextList){
                    if (i.getPath().startsWith(folder)){
                        userService.deleteItemText(email,i);
                    }
                }

                //// update the subfolder list of the parent
                // refresh two times as the first time there are multiple loops going on and some tasks are left incomplete
                //  for (int j = 0; j < 2; j++) {
                String pathFromBeginning = "";
                String[] splitAll = folder.split("\\|");
                String[] split = Arrays.copyOf(splitAll, splitAll.length - 1);

                for (int i = 0; i < split.length; i++) {

                    // add path
                    pathFromBeginning = pathFromBeginning + split[i];

                    ///// PRINT STATEMENT //////
                    //System.out.println("==================================================================");
                    //System.out.println("Folder path: "+pathFromBeginning);
                    //System.out.println("==================================================================");

                    // get folder instance
                    Folder pFolder = userService.getFolder(email, pathFromBeginning);

                    if (pFolder != null) {
                        // get subfolder list
                        List<Folder> subfolders = pFolder.getSubfolders();

                        if (subfolders != null) {

                            // subfolders list updated
                            List<Folder> updatedSubfolders = new ArrayList<>(subfolders);

                            //System.out.println("Subfolders list is not null!");
                            for (Folder subfolder : subfolders) {
                                //System.out.println("Subfolder path: " + subfolder.getPath() + " -- Path to delete must start with: " + folder);

                                if (subfolder.getPath().equals(folder)) {
                                    // if subfolder matches path of the deleted path
                                    //System.out.println("Matched successfully! Deleted subfolder path: " + subfolder.getPath() + "from subfolders of: " + pathFromBeginning);
                                    updatedSubfolders.remove(subfolder);
                                } else {
                                    // if subfolder doesn't match path of deleted path
                                    // check if it matches a subpath of the deleted path

                                    String subPath = folder;
                                    for (int a = splitAll.length-1; a >=0; a--){
                                        // setup correct path, remove the last element from the path
                                        subPath = subPath.replace(splitAll[a], "");
                                        if (subPath.endsWith("|") && subPath.length()>1){
                                            subPath = subPath.substring(0, subPath.length() - 1);

                                            //System.out.println("- Checking if path: "+subfolder.getPath()+" matches folder path "+ subPath);
                                            if (subfolder.getPath().equals(subPath)){
                                                //System.out.println("Search Path: "+subPath+" successfully matches folder path "+ subfolder.getPath());

                                                // check if its subfolder matches folder path to be deleted
                                                List<Folder> subfoldersOfSubfolder = subfolder.getSubfolders();

                                                if (subfoldersOfSubfolder != null) {
                                                    //System.out.println("Subfolders of subfolder "+subfolder.getPath()+" is not null, begin to search inside the subfolders.");
                                                    List<Folder> updatedSubfoldersOfSubfolder = new ArrayList<>(subfoldersOfSubfolder);
                                                    for (Folder subfolderOfSubfolder : subfoldersOfSubfolder) {
                                                        if (subfolderOfSubfolder.getPath().equals(folder)) {
                                                            //System.out.println("Subfolder of subfolder with path: " + subfolderOfSubfolder.getPath() + " -- matches -- folder path deleted: " + folder);

                                                            // update the updatedSubfoldersOfSubfolder, remove the respective subfolder from the list
                                                            updatedSubfoldersOfSubfolder.remove(subfolderOfSubfolder);

                                                            updatedSubfolders.remove(subfolder);

                                                            // update subfolder list of subfolders
                                                            subfolder.setSubfolders(updatedSubfoldersOfSubfolder);

                                                            updatedSubfolders.add(subfolder);

                                                        }
                                                    }
                                                }
                                                else{
                                                    //System.out.println("Subfolders of subfolder "+subfolder.getPath()+" is null!");
                                                }

                                            }
                                        }
                                        else{
                                            //System.out.println("Reached end of path.");
                                            break;
                                        }
                                    }

                                }
                            }

                            // update database
                            pFolder.setSubfolders(updatedSubfolders);
                            userService.updateFolder(email,pFolder);
                        }

                        // update path for next parent
                        pathFromBeginning = pathFromBeginning + "|";
                    } else {
                        return "false";//"false - Failed to get pFolder with path: " + pathFromBeginning;
                    }
                }
                //}

                ////// update subfolders of all the parent folders so that the database is completely synchronized with the changes
                // refresh as necessary
                synchronizeSubfolders(email,folder,folder.split("\\|").length);

                // deleted successfully
                return "true";

            }
            else{
                // failed to obtain from repo
                return "false";//"false - Folder with path "+folder+" does not exist on the database.";
            }

        }
        else{
            return "false";//"false - No folder to delete, please specify the folder name on the url!";
        }
    }

    @RequestMapping("/service/structure")
    public Folder structure(@RequestParam String email, @RequestParam String folder){

        if (folder != null){
            // get folder with the respective path given
            Folder f = userService.getFolder(email,folder);

            if (f != null){
                // structure json data accordingly

                return f;

            }
            else{
                //return "false - Folder with path "+folder+" not on database!";
                return null;
            }
        }
        else{
            //return "false - No folder to delete, please specify the folder name on the url!";
            return null;
        }
    }

    @RequestMapping("/service/count")
    public Map<String, String> count(@RequestParam String email, @RequestParam String folder){
        // get folder instance
        Folder f = userService.getFolder(email,folder);

        if (f != null){
            // get direct count
            int directCount = f.getSubfolders().size();

            // get indirect count
            int indirectCount = 0;
            List<Folder> allFolders = userRepository.findByEmail(email).getFolders();
            // loop through all the folders in the db
            for (Folder a : allFolders){
                if (a.getPath().startsWith(folder)){
                    // if the folder's path starts with the given folder path, add to indirect count
                    indirectCount++;
                }
            }

            // remove one from the count since it includes the current path
            indirectCount--;

            // map the results accordingly and return
            Map<String, String> results = new LinkedHashMap<>();
            results.put("direct",Integer.toString(directCount));
            results.put("indirect",Integer.toString(indirectCount));

            return results;
        }
        else{
            // failed to get folder instance
            return null;
        }

    }

    @RequestMapping("/service/createStructure")
    public String createStructure(@RequestParam String email, @RequestParam String tree, @RequestParam String root){

        /////////// process tree string ////////////////////////

        // split the tree by [
        List<String> splitTree= new ArrayList<>(Arrays.asList(tree.split("\\[")));

        // remove empty elements on the list
        splitTree.removeAll(Arrays.asList("", null));

        // add opening bracket on all except the first element
        List<String> treeComponents = new ArrayList<>();
        for (int i = 0; i < splitTree.size(); i++){
            // add ] at the beginning of the string
            treeComponents.add("]"+splitTree.get(i));
        }

        // stores list of paths for folder creation
        List<String> paths = new LinkedList<>();

        String previousPath = "";
        String pathTracking = "";
        for (String element : treeComponents){
            // if element has one ]
            if (StringUtils.countMatches(element,"]") <= 1){
                // System.out.println("Less or equal to one occurence at: "+element);
                // remove the ] occurence
                element = element.replace("]", "");

                // previous path
                previousPath = previousPath + pathTracking;

                // add to path tracking
                pathTracking = pathTracking + element;

                // add to list of paths
                paths.add(pathTracking);

                // add | to pathTracking so that it is ready for the next path
                pathTracking = pathTracking + "|";
            }
            else{
                // if more than one occurence of ]
                //   System.out.println("More than one occurence at: "+element);

                //   System.out.println("Previous path: "+previousPath);
                //   System.out.println("Current path: "+pathTracking);

                // get string in between ]...] , if it exists
                if (!(StringUtils.substringBetween(element,"]","]").equals(""))){
                    // get the content in between
                    String inBetween = StringUtils.substringBetween(element,"]","]");

                    // check if the inBetween has | , split it to see
                    List<String> inBetweenSplit = new ArrayList<>(Arrays.asList(inBetween.split("\\|")));

                    // if the size is greater than one, means there are further paths to add from the current path
                    if (inBetweenSplit.size() > 1){
                        for (String pathPortion : inBetweenSplit){
                            paths.add(pathTracking + pathPortion);
                        }
                    }
                    else{
                        if (inBetweenSplit.size() == 1){
                            paths.add(pathTracking + inBetweenSplit.get(0));
                        }
                    }

                    ///// check the rest of the string of element
                    String remainingPortion = element.replace("]"+inBetween+"]","");
                    //    System.out.println("Remaining portion, after removing inBetween: "+remainingPortion);

                    // replace all [ , ] , |
                    String cleanedRemainingPortion = remainingPortion.replaceAll("\\[","").replaceAll("\\]","").replaceAll("\\|", "");

                    //   System.out.println("Cleaned remaining portion: "+ cleanedRemainingPortion);
                    // add as path
                    if (StringUtils.countMatches(remainingPortion,"|") >=1){
                        // System.out.println("Found at least one instance of | on remaining portion. Adding: "+previousPath+cleanedRemainingPortion);
                        // if it has | , use previous path
                        paths.add(previousPath+cleanedRemainingPortion);
                        previousPath = previousPath + cleanedRemainingPortion;
                    }
                    else{
                        //      System.out.println("DID NOT find instance of | on remaining portion. Adding: "+pathTracking+cleanedRemainingPortion);
                        paths.add(pathTracking+cleanedRemainingPortion);
                        pathTracking = pathTracking + cleanedRemainingPortion;
                    }

                }
                else{
                    // dont know if needed
                }
            }
        }

        List<String> completedPaths = new LinkedList<>();
        // add root to each of the paths
        for (String element : paths){
            // cleaning up the path
            if (element.charAt(0) == '|' || element.charAt(0) == '[' || element.charAt(0) == ']'){
                element = element.substring(1);
            }
            if ( element.charAt(element.length()-1) == '|' || element.charAt(element.length()-1) == '[' || element.charAt(element.length()-1) == ']'){
                element = element.substring(0, element.length() - 1);
            }

            // add root to the element path
            element = root + "|" + element;

            // add to list
            completedPaths.add(element);
        }

        // add the root at the beginning of the list
        completedPaths.add(0,root);

        //    for (String element : completedPaths){
        //       System.out.println(element);
        //   }

        ////////// make rest api requests to create the respective folders accordingly ////////
        List<String> apiResults = new ArrayList<>();

        for (String path : completedPaths){
            // split by |
            List<String> splitPaths = new ArrayList<>(Arrays.asList(path.split("\\|")));

            // if it has more than one element, means that the first element will be parent
            if (splitPaths.size() > 1){
                // get folder parameter (last one in the list)
                String folderParameter = splitPaths.get(splitPaths.size()-1);

                //// get parent parameter

                String parentParameter = "";

                // remove last element of the list (remove the folder, so we only have parents in the list)
                splitPaths.remove(splitPaths.size()-1);

                // structure the parentParameter string
                for (String parent : splitPaths){
                    parentParameter = parentParameter + parent + "|";
                }

                // remove the | at the end of the string
                parentParameter = parentParameter.substring(0,parentParameter.length()-1);

                //System.out.println("Parent parameter: "+parentParameter+" Folder parameter: "+folderParameter);

                // create rest api request to create folder
                apiResults.add(createFolder(email,folderParameter,parentParameter));

            }
            else{
                // there is only one element, so no parent parameter
                String folderParameter = splitPaths.get(0);

                //System.out.println("Folder parameter: "+folderParameter);

                // create rest api request to create folder
                apiResults.add(createFolder(email,folderParameter,null));
            }
        }

        // process results
        if (apiResults.stream().allMatch(result -> result.equals("false"))){
            // if all the createFolder api calls returned false, then it failed
            return "false";
        }
        else{
            // if at least one passed then it worked, as in some cases it will return false due to some folders already existing
            return "true";
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////// ITEM - LINK /////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    // Request example: /service/create/itemLink?title="Harry+Potter"&url="http://hogwarts.com"&parentPath=Documents|Books|Fiction
    // parentPath will be used to obtain the respective Folder where the item will be added
    // title will be used so that its unique path will be "Documents|Books|Fiction|Harry+Potter", while the name will be set to Harry Potter. it will be within quotes
    // url will be received in quotes just like the title, so make sure to extract the content inside it and then handle the data
    // the space will be represented by + on the url and will be replaced by " " when setting the title, although on the path it will be kept
    @RequestMapping("/service/create/itemLink")
    public String createItemLink(@RequestParam String email, @RequestParam String title, @RequestParam String url, @RequestParam String parentPath){

        // get folder of which the item will be added
        Folder folder  = userService.getFolder(email, parentPath);

        // make sure that the parent folder is not null
        if (folder != null) {

            // create itemLink

            // make sure that none of the parameters are null
            if (title != null && url != null && parentPath != null) {

                // extract title
                if (title.charAt(0) == '"' && title.charAt(title.length()-1) == '"') {
                    title = title.substring(1, title.length() - 1);
                }

                //System.out.println("Item title: " + title);

                // extract url
                if (url.charAt(0) == '"' && title.charAt(url.length()-1) == '"') {
                    url = url.substring(1, url.length() - 1);
                }

                //System.out.println("Item url: " + url);

                // make unique path for the item (parentpath+title)
                String path = parentPath + "|" + title;

                //System.out.println("Item path: " + path);

                // check if item already exists
                ItemLink existingItem = userService.getItemLink(email, path);

                if (existingItem == null) {

                    ///// item does not already exist in the db, proceed to create

                    // create item
                    ItemLink item = new ItemLink(path, "<b>(Link)</b> "+title, url);
                    item.setHref("view/itemLink?path="+path);

                    // add to item repo
                    userService.addItemLink(email, item);

                    // update folder and add it to repo
                    if (folder.getLinkItems() == null) {
                        // if its null then create new arraylist of itemlinks
                        List<ItemLink> itemLinkList = new ArrayList<>();

                        // add item to it
                        itemLinkList.add(item);

                        // update folder instance with the above list
                        folder.setLinkItems(itemLinkList);

                        // update folder to db
                        userService.updateFolder(email, folder);

                        // synchronize the database tree hierarchy, update all the parent's folders
                        synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                        // return true
                        return "true";
                    } else {
                        // if folder already has itemLinks, add it to the already existing list

                        // get the list
                        List<ItemLink> itemLinkList = folder.getLinkItems();

                        // add item to the list
                        itemLinkList.add(item);

                        // update folder instance with the above list
                        folder.setLinkItems(itemLinkList);

                        // update folder to db
                        userService.updateFolder(email, folder);

                        // synchronize the database tree hierarchy, update all the parent's folders
                        synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                        // return true
                        return "true";
                    }
                } else {
                    // item already exists, return false
                    return "false";
                }

            } else {
                // if any of them are null, return false
                return "false";
            }
        }
        else{
            // parent folder doesnt exist, return false
            return "false";
        }

    }

    @RequestMapping("/service/delete/itemLink")
    public String deleteItemLink(@RequestParam String email, @RequestParam String path){

        // get item link from the path
        ItemLink item = userService.getItemLink(email,path);

        // make sure that the item was successfully retrieved from the db
        if (item != null){

            ////// delete from Item repo ///////////////
            userService.deleteItemLink(email,item);

            ////// delete from Folder repo /////////////

            // get parent folder path
            List<String> splitPath = new ArrayList<>(Arrays.asList(path.split("\\|")));
            splitPath.remove(splitPath.size()-1);

            String parentPath = "";
            for (String pathPortion : splitPath){
                parentPath = parentPath + pathPortion + "|";
            }
            parentPath = parentPath.substring(0,parentPath.length()-1);

            //System.out.println("Parent path of the item to be deleted: "+ parentPath);

            // get parent folder instance from the db
            Folder parentFolder = userService.getFolder(email,parentPath);

            if (parentFolder != null){
                // get list of items
                List<ItemLink> items = parentFolder.getLinkItems();

                List<ItemLink> updatedItems = new ArrayList<>(items);

                // remove all item instances from the list
                for (ItemLink i : items){
                    if (i.getPath().equals(item.getPath())){
                        updatedItems.remove(i);
                    }
                }

                // update items list on the folder instance
                parentFolder.setLinkItems(updatedItems);

                // update folder on db
                userService.updateFolder(email, parentFolder);

                // synchronize folders
                synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                return "true";
            }
            else{
                // failed to get parent folder
                return "false";
            }

        }
        else{
            // failed to get item from db
            return "false";
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////// ITEM - LOCATION /////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    // Request example: /service/create/itemLocation?location="Lisbon"&latitude="372638.23"&longitude="54235.43"&parentPath=Documents|Books|Fiction
    @RequestMapping("/service/create/itemLocation")
    public String createItemLocation(@RequestParam String email, @RequestParam String location, @RequestParam String latitude, @RequestParam String longitude, @RequestParam String parentPath){

        // get folder of which the item will be added
        Folder folder  = userService.getFolder(email,parentPath);

        // make sure that the parent folder is not null
        if (folder != null) {

            // create itemLocation

            // make sure that none of the parameters are null
            if (location != null && latitude != null && longitude != null && parentPath != null) {

                // extract location
                if (location.charAt(0) == '"' && location.charAt(location.length()-1) == '"') {
                    location = location.substring(1, location.length() - 1);
                }

                //System.out.println("Item location: " + location);

                // extract latitude
                if (latitude.charAt(0) == '"' && latitude.charAt(latitude.length()-1) == '"') {
                    latitude = latitude.substring(1, latitude.length() - 1);
                }

                // extract latitude
                if (longitude.charAt(0) == '"' && longitude.charAt(longitude.length()-1) == '"') {
                    longitude = longitude.substring(1, longitude.length() - 1);
                }

                //System.out.println("Item url: " + url);

                // make unique path for the item (parentpath+location)
                String path = parentPath + "|" + location;

                //System.out.println("Item path: " + path);

                // check if item already exists
                ItemLocation existingItem = userService.getitemLocation(email,path);

                if (existingItem == null) {

                    ///// item does not already exist in the db, proceed to create

                    // create item
                    ItemLocation item = new ItemLocation(path,"<b>(Location)</b> "+location,Double.parseDouble(latitude),Double.parseDouble(longitude));
                    item.setHref("view/itemLocation?path="+path);

                    // add to item repo
                    userService.additemLocation(email,item);

                    // update folder and add it to repo
                    if (folder.getLocationItems() == null) {
                        // if its null then create new arraylist of itemlocation
                        List<ItemLocation> itemLocationList = new ArrayList<>();

                        // add item to it
                        itemLocationList.add(item);

                        // update folder instance with the above list
                        folder.setLocationItems(itemLocationList);

                        // update folder to db
                        userService.updateFolder(email,folder);

                        // synchronize the database tree hierarchy, update all the parent's folders
                        synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                        // return true
                        return "true";
                    } else {
                        // if folder already has itemLocations, add it to the already existing list

                        // get the list
                        List<ItemLocation> itemLocationList = folder.getLocationItems();

                        // add item to the list
                        itemLocationList.add(item);

                        // update folder instance with the above list
                        folder.setLocationItems(itemLocationList);

                        // update folder to db
                        userService.updateFolder(email,folder);

                        // synchronize the database tree hierarchy, update all the parent's folders
                        synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                        // return true
                        return "true";
                    }
                } else {
                    // item already exists, return false
                    return "false";
                }

            } else {
                // if any of them are null, return false
                return "false";
            }
        }
        else{
            // parent folder doesnt exist, return false
            return "false";
        }

    }

    @RequestMapping("/service/delete/itemLocation")
    public String deleteItemLocation(@RequestParam String email, @RequestParam String path){
        // get item location from the path
        ItemLocation item = userService.getitemLocation(email,path);

        // make sure that the item was successfully retrieved from the db
        if (item != null){

            ////// delete from Item repo ///////////////
            userService.deleteitemLocation(email,item);

            ////// delete from Folder repo /////////////

            // get parent folder path
            List<String> splitPath = new ArrayList<>(Arrays.asList(path.split("\\|")));
            splitPath.remove(splitPath.size()-1);

            String parentPath = "";
            for (String pathPortion : splitPath){
                parentPath = parentPath + pathPortion + "|";
            }
            parentPath = parentPath.substring(0,parentPath.length()-1);

            //System.out.println("Parent path of the item to be deleted: "+ parentPath);

            // get parent folder instance from the db
            Folder parentFolder = userService.getFolder(email,parentPath);

            if (parentFolder != null){
                // get list of items
                List<ItemLocation> items = parentFolder.getLocationItems();

                List<ItemLocation> updatedItems = new ArrayList<>(items);

                // remove all item instances from the list
                for (ItemLocation i : items){
                    if (i.getPath().equals(item.getPath())){
                        updatedItems.remove(i);
                    }
                }

                // update items list on the folder instance
                parentFolder.setLocationItems(updatedItems);

                // update folder on db
                userService.updateFolder(email,parentFolder);

                // synchronize folders
                synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                return "true";
            }
            else{
                // failed to get parent folder
                return "false";
            }

        }
        else{
            // failed to get item from db
            return "false";
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////// ITEM - TEXT /////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    // Request example: /service/create/itemText?title="My Text"&content="Hey, what's up. This is my text ayy!"&parentPath=Documents|Books|Fiction
    @RequestMapping("/service/create/itemText")
    public String createItemText(@RequestParam String email, @RequestParam String title, @RequestParam String content, @RequestParam String parentPath){

        // get folder of which the item will be added
        Folder folder  = userService.getFolder(email,parentPath);

        // make sure that the parent folder is not null
        if (folder != null) {

            // create itemText

            // make sure that none of the parameters are null
            if (title != null && content != null && parentPath != null) {

                // extract title
                if (title.charAt(0) == '"' && title.charAt(title.length()-1) == '"') {
                    title = title.substring(1, title.length() - 1);
                }

                //System.out.println("Item title: " + title);

                // extract content
                if (content.charAt(0) == '"' && content.charAt(title.length()-1) == '"') {
                    content = content.substring(1, content.length() - 1);
                }

                //System.out.println("Item content: " + content);

                // make unique path for the item (parentpath+title)
                String path = parentPath + "|" + title;

                //System.out.println("Item path: " + path);

                // check if item already exists
                ItemText existingItem = userService.getItemText(email,path);

                if (existingItem == null) {

                    ///// item does not already exist in the db, proceed to create

                    // create item
                    ItemText item = new ItemText(path, "<b>(Text)</b> "+title, content);
                    item.setHref("view/itemText?path="+path);

                    // add to item repo
                    userService.addItemText(email,item);

                    // update folder and add it to repo
                    if (folder.getTextItems() == null) {
                        // if its null then create new arraylist of itemtext
                        List<ItemText> itemTextList = new ArrayList<>();

                        // add item to it
                        itemTextList.add(item);

                        // update folder instance with the above list
                        folder.setTextItems(itemTextList);

                        // update folder to db
                        userService.updateFolder(email,folder);

                        // synchronize the database tree hierarchy, update all the parent's folders
                        synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                        // return true
                        return "true";
                    } else {
                        // if folder already has itemTexts, add it to the already existing list

                        // get the list
                        List<ItemText> itemTextList = folder.getTextItems();

                        // add item to the list
                        itemTextList.add(item);

                        // update folder instance with the above list
                        folder.setTextItems(itemTextList);

                        // update folder to db
                        userService.updateFolder(email,folder);

                        // synchronize the database tree hierarchy, update all the parent's folders
                        synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                        // return true
                        return "true";
                    }
                } else {
                    // item already exists, return false
                    return "false";
                }

            } else {
                // if any of them are null, return false
                return "false";
            }
        }
        else{
            // parent folder doesnt exist, return false
            return "false";
        }

    }

    @RequestMapping("/service/delete/itemText")
    public String deleteItemText(@RequestParam String email, @RequestParam String path){
        // get item text from the path
        ItemText item = userService.getItemText(email,path);

        // make sure that the item was successfully retrieved from the db
        if (item != null){

            ////// delete from Item repo ///////////////
            userService.deleteItemText(email,item);

            ////// delete from Folder repo /////////////

            // get parent folder path
            List<String> splitPath = new ArrayList<>(Arrays.asList(path.split("\\|")));
            splitPath.remove(splitPath.size()-1);

            String parentPath = "";
            for (String pathPortion : splitPath){
                parentPath = parentPath + pathPortion + "|";
            }
            parentPath = parentPath.substring(0,parentPath.length()-1);

            //System.out.println("Parent path of the item to be deleted: "+ parentPath);

            // get parent folder instance from the db
            Folder parentFolder = userService.getFolder(email,parentPath);

            if (parentFolder != null){
                // get list of items
                List<ItemText> items = parentFolder.getTextItems();

                List<ItemText> updatedItems = new ArrayList<>(items);

                // remove all item instances from the list
                for (ItemText i : items){
                    if (i.getPath().equals(item.getPath())){
                        updatedItems.remove(i);
                    }
                }

                // update items list on the folder instance
                parentFolder.setTextItems(updatedItems);

                // update folder on db
                userService.updateFolder(email,parentFolder);

                // synchronize folders
                synchronizeSubfolders(email,parentPath, parentPath.split("\\|").length);

                return "true";
            }
            else{
                // failed to get parent folder
                return "false";
            }

        }
        else{
            // failed to get item from db
            return "false";
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void synchronizeSubfolders(String email, String parentPath, int refreshCount){
        // refresh as many times necessary to make sure all is synchronized properly
        // System.out.println("================ Synchronizing: "+parentPath+"==================================");
        // System.out.println("Refresh count "+ refreshCount);
        for (int j = 0; j < refreshCount; j++) {
            //  System.out.println("Loop index count: "+ j);
            ////// update subfolders of all the parent folders so that the database is completely synchronized with the changes
            String pathFromBeginning = "";
            String[] split = parentPath.split("\\|");

            for (int i = 0; i < split.length; i++) {
                // make sure we are not accessing out of bounds
                if (i + 1 >= split.length) {
                    break;
                }

                // add path
                pathFromBeginning = pathFromBeginning + split[i];

                // get folder instance
                Folder pFolder = userService.getFolder(email,pathFromBeginning);
                String pathOfInstance = pathFromBeginning + "|" + split[i + 1];

                if (pFolder != null) {
                    //System.out.println("Found pFolder! Path: "+pathFromBeginning);
                    //   userService.deleteFolder(pFolder);

                    // get supposed subfolder instance
                    Folder cFolder = userService.getFolder(email,pathOfInstance);

                    if (cFolder != null) {
                        //// add cFolder as subfolder of pFolder

                        // get current subfolders of pFolder
                        List<Folder> pSubfolders = pFolder.getSubfolders();

                        // check if cFolder already exists, and if so, remove it
                        if (pSubfolders.stream().anyMatch(a -> a.getPath().equals(cFolder.getPath()))) {
                            Folder toDelete = pSubfolders.stream().filter(a -> a.getPath().equals(cFolder.getPath())).findFirst().get();
                            pSubfolders.remove(toDelete);
                        }

                        // add cFolder as subfolder
                        pSubfolders.add(cFolder);

                        pFolder.setSubfolders(pSubfolders);

                        // update database with this change
                        userService.updateFolder(email,pFolder);

                        // update path for next parent
                        pathFromBeginning = pathFromBeginning + "|";
                    }
                    else{
                        //System.out.println("Failed to find cFolder! Path: "+pathOfInstance);
                    }

                }
                else{
                    //System.out.println("Failed to find pFolder! Path: "+pathFromBeginning);
                }
            }
        }

    }
}
