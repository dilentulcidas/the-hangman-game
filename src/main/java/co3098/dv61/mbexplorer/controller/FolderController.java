package co3098.dv61.mbexplorer.controller;

import co3098.dv61.mbexplorer.domain.Folder;
import co3098.dv61.mbexplorer.domain.ItemLink;
import co3098.dv61.mbexplorer.domain.ItemLocation;
import co3098.dv61.mbexplorer.domain.ItemText;
import co3098.dv61.mbexplorer.repository.*;
import co3098.dv61.mbexplorer.service.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class FolderController {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    RestAPIController apiRequests;
    @Autowired
    IndexController indexController;


    @RequestMapping(value = "/view/folder", method=RequestMethod.GET)
    public ModelAndView viewFolder(HttpServletRequest request, @RequestParam String path){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // get the folder instance from the database
        Folder folder = userService.getFolder(email, path);

        if (folder != null) {

            // setup subfolders display
            Gson gson = new Gson();
            JsonObject jsonObject = (JsonObject) gson.toJsonTree(folder);

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
                    //System.out.println("Creating new node: ");
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
                indexController.restructureJsonTree(jsonObject.getAsJsonArray("subfolders"));
            }

            // replace the key names according to bootstrap treeview convention
            String treeView = gson.toJson(jsonObject).replace("\\u003cb\\u003e","<b>").replace("\\u003c/b\\u003e","</b>")
                    .replace("\"subfolders\":","\"nodes\":")
                    //.replace("\"path\":","\"href\":")
                    .replace("\"name\":","\"text\":")
                    .replace("\"title\":","\"text\":")
                    .replace("\"location\":","\"text\":");

            //System.out.println(treeView);

            Boolean isRoot;
            if (!path.contains("|")){
                isRoot = true;
            }
            else{
                isRoot = false;
            }

            // display it properly on the correct jsp
            ModelAndView mav = new ModelAndView();
            mav.setViewName("folderDetails");
            mav.addObject("folder", folder);
            mav.addObject("subfoldertree", treeView);
            mav.addObject("isroot", isRoot);

            return mav;
        }
        else{
            // couldn't fetch folder, show toast displaying error
            ModelAndView mav = indexController.index(request);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to get the detailed data of the folder from the database!");
            return mav;
        }
    }

    @RequestMapping(value = "/create/folder", method=RequestMethod.POST)
    public ModelAndView addFolder(HttpServletRequest request, @RequestParam("path") String path, @RequestParam("name") String name){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // make sure that all the parameters received are not null
        if (path.equals("none")){
            // folder to be created at root level

            // call rest api to add the respective folder, null path since it's at root
            String result = apiRequests.createFolder(email, name, null);

            if (result.equals("true")){
                // successful
                ModelAndView mav = viewFolder(request, name);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully added folder "+name);

                return mav;
            }
            else{
                // unsuccessful
                ModelAndView mav = indexController.index(request);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Failed to add folder "+name+". Make sure that there is no other folder with the same name in this directory.");

                return mav;
            }
        }
        else{
            // adding subfolder

            // call rest api to add the respective folder, null path since it's at root
            String result = apiRequests.createFolder(email, name, path);

            if (result.equals("true")){
                // successful
                ModelAndView mav = viewFolder(request, path);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully added subfolder "+name);

                return mav;
            }
            else{
                // unsuccessful
                ModelAndView mav = indexController.index(request);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Failed to add subfolder "+name+". Make sure that there is no other folder with the same name in this directory.");

                return mav;
            }
        }
    }

    @RequestMapping(value = "/edit/folder", method=RequestMethod.POST)
    public ModelAndView editFolder(HttpServletRequest request, @RequestParam("path") String path, @RequestParam("name") String name, @RequestParam("locked") String locked){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        Folder folderToBeUpdated = userService.getFolder(email, path);

        //// folder to be updated found, proceed to update it
        if (folderToBeUpdated != null) {

            // check if folder already exists

            // get parent path
            List<String> splitByList = new ArrayList<>(Arrays.asList(path.split("\\|")));
            splitByList.remove(splitByList.size()-1);
            String parentPath = null;
            if (splitByList.size() > 0){
                parentPath = "";
                for (String parent : splitByList){
                    parentPath = parentPath + parent + "|";
                }
                parentPath = parentPath.substring(0,parentPath.length()-1);
            }

            String updatedFolderPath = parentPath + "|" + name;

            // make sure that renamed folder doesnt already exist
            if (userService.getFolder(email, updatedFolderPath) == null || (userService.getFolder(email, updatedFolderPath) != null && updatedFolderPath.equals(path))) {

                // update its name
                folderToBeUpdated.setName(name);
                userService.updateFolder(email, folderToBeUpdated);

                // store the to be updated folder's path
                String oldPath = path;

                // get the new path
                String[] split = path.split("\\|");
                List<String> splitInList = new ArrayList<>(Arrays.asList(split));
                splitInList.remove(splitInList.size() - 1);

                String pathLoop = "";
                for (String portion : splitInList) {
                    pathLoop = pathLoop + portion + "|";
                }
                String newPath = pathLoop + name;

                // get all folders from the db
                List<Folder> folders = userRepository.findByEmail(email).getFolders();

                // get all items from the db
                List<ItemLink> itemLinks = userRepository.findByEmail(email).getLinkItems();
                List<ItemLocation> itemLocations = userRepository.findByEmail(email).getLocationItems();
                List<ItemText> itemTexts = userRepository.findByEmail(email).getTextItems();

                // get all folders with that respective path from the repo, rename it, set href and set locked status
                for (Folder folder : folders) {
                    // make sure it starts with the old path
                    if (folder.getPath().startsWith(oldPath)) {
                        // delete old version from repo
                        userService.deleteFolder(email, folder);

                        // rename path
                        folder.setPath(folder.getPath().replace(oldPath, newPath));

                        // rename href
                        folder.setHref("view/folder?path=" + folder.getPath());

                        // update locked status
                        folder.setLocked(Boolean.parseBoolean(locked));

                        // add new version to repo
                        userService.addFolder(email, folder);

                    }
                }

                // same for itemLink
                for (ItemLink itemLink : itemLinks) {
                    // make sure it starts with the old path
                    if (itemLink.getPath().startsWith(oldPath)) {
                        // delete old version from repo
                        userService.deleteItemLink(email, itemLink);

                        // rename path
                        itemLink.setPath(itemLink.getPath().replace(oldPath, newPath));

                        // rename href
                        itemLink.setHref("view/itemLink?path=" + itemLink.getPath());

                        // add new version to repo
                        userService.addItemLink(email, itemLink);
                    }
                }

                // same for itemLocation
                for (ItemLocation itemLocation : itemLocations) {
                    // make sure it starts with the old path
                    if (itemLocation.getPath().startsWith(oldPath)) {
                        // delete old version from repo
                        userService.deleteitemLocation(email, itemLocation);

                        // rename path
                        itemLocation.setPath(itemLocation.getPath().replace(oldPath, newPath));

                        // rename href
                        itemLocation.setHref("view/itemLocation?path=" + itemLocation.getPath());

                        // add new version to repo
                        userService.additemLocation(email, itemLocation);
                    }
                }

                // same for itemText
                for (ItemText itemText : itemTexts) {
                    // make sure it starts with the old path
                    if (itemText.getPath().startsWith(oldPath)) {
                        // delete old version from repo
                        userService.deleteItemText(email, itemText);

                        // rename path
                        itemText.setPath(itemText.getPath().replace(oldPath, newPath));

                        // rename href
                        itemText.setHref("view/itemText?path=" + itemText.getPath());

                        // add new version to repo
                        userService.addItemText(email, itemText);
                    }
                }

                //System.out.println("New path: " + newPath);

                Folder folder = userService.getFolder(email, newPath);

                ///// update items

                // update linkItems
                if (folder.getLinkItems() != null){
                    //System.out.println("Found link items to update on folder path: "+folder.getPath());
                    List<ItemLink> linkItems = folder.getLinkItems();

                    for (ItemLink itemLink : linkItems){
                        // rename path
                        itemLink.setPath(folder.getPath()+"|"+itemLink.getTitle().replace("<b>(Link)</b> ", ""));

                        // rename href
                        itemLink.setHref("view/itemLink?path="+itemLink.getPath());

                    }
                }

                // update locationItems
                if (folder.getLocationItems() != null){
                    List<ItemLocation> linkLocations = folder.getLocationItems();

                    for (ItemLocation itemLocation : linkLocations){
                        // rename path
                        itemLocation.setPath(folder.getPath()+"|"+itemLocation.getLocation().replace("<b>(Location)</b> ", ""));

                        // rename href
                        itemLocation.setHref("view/itemLocation?path="+itemLocation.getPath());

                    }
                }

                // update textItems
                if (folder.getTextItems() != null){
                    List<ItemText> linkItems = folder.getTextItems();

                    for (ItemText itemText : linkItems){
                        // rename path
                        itemText.setPath(folder.getPath()+"|"+itemText.getTitle().replace("<b>(Text)</b> ", ""));

                        // rename href
                        itemText.setHref("view/itemText?path="+itemText.getPath());

                    }
                }

                ///// update subfolders

                if (folder.getSubfolders() != null) {
                    List<Folder> copyOfSubfolders = new ArrayList<>(folder.getSubfolders());
                    loopThroughSubfoldersAndUpdate(folder.getSubfolders(), copyOfSubfolders, folder.getLocked(), folder.getPath());
                    folder.setSubfolders(copyOfSubfolders);
                    synchronizeSubfoldersAfterUpdate(email, newPath,oldPath,newPath.split("\\|").length);

                    // update the db
                    userService.updateFolder(email,folder);
                }
                else{
                    // it does not have subfolders, simply synchronize
                    synchronizeSubfoldersAfterUpdate(email, newPath,oldPath,newPath.split("\\|").length);

                    // update the db
                    userService.updateFolder(email,folder);
                }

                // update the subfolders of every folder that has path thats starts with the new path
                List<Folder> allFolders = userRepository.findByEmail(email).getFolders();
                for (Folder a : allFolders){
                    if (a.getPath().startsWith(newPath)){
                        if (a.getSubfolders() != null){
                            List<Folder> copyOfSubfolders = new ArrayList<>(a.getSubfolders());
                            loopThroughSubfoldersAndUpdate(a.getSubfolders(), copyOfSubfolders, Boolean.parseBoolean(locked), a.getPath());
                            a.setSubfolders(copyOfSubfolders);
                            synchronizeSubfoldersAfterUpdate(email, newPath,oldPath,newPath.split("\\|").length);

                            // update the db
                            userService.updateFolder(email, a);
                        }
                    }
                }

                // redirect to same page but updated jsp
                ModelAndView mav = viewFolder(request, newPath);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully updated the folder!");

                return mav;
            }
            else{
                // Folder already exists in the same directory
                ModelAndView mav = viewFolder(request, path);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Folder already exists in the same directory! Try a different name");

                return mav;
            }
        }
        else{
            // Failed to fetch the item from the database!
            // unsuccessful
            ModelAndView mav = viewFolder(request, path);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to fetch the folder from the database!");

            return mav;
        }
    }

    @RequestMapping(value = "/delete/folder", method=RequestMethod.POST)
    public ModelAndView deleteFolder(HttpServletRequest request, @RequestParam("path") String path){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // use the RESTApiController method to delete the respective item
        String result = apiRequests.deleteFolder(email, path);

        List<String> splitList = Arrays.asList(path.split("\\|"));
        String deletedFolderName = splitList.get(splitList.size()-1);

        // get parent path
        List<String> splitByList = new ArrayList<>(Arrays.asList(path.split("\\|")));
        splitByList.remove(splitByList.size()-1);
        String parentPath = null;
        if (splitByList.size() > 0){
            parentPath = "";
            for (String parent : splitByList){
                parentPath = parentPath + parent + "|";
            }
            parentPath = parentPath.substring(0,parentPath.length()-1);
        }

        if (result.equals("true")){
            // successful
            if (parentPath == null) {
                ModelAndView mav = indexController.index(request);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully deleted folder " + deletedFolderName);

                return mav;
            }
            else{
                ModelAndView mav = viewFolder(request, parentPath);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully deleted folder " + deletedFolderName);

                return mav;
            }
        }
        else{
            // unsuccessful
            ModelAndView mav = indexController.index(request);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to delete folder "+deletedFolderName);

            return mav;
        }
    }

    @RequestMapping(value = "/move/folder", method=RequestMethod.POST)
    public ModelAndView moveFolder(HttpServletRequest request, @RequestParam("path") String oldPath, @RequestParam("newPath") String parentPathOfNew){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        if (parentPathOfNew.startsWith(oldPath)){
            // if the folder to be moved is set to move at its child's folder, that is invalid
            ModelAndView mav = viewFolder(request, oldPath);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Not allowed to move the parent folder to its child folder! Do not move within its own folder!");
            return mav;
        }
        else {
            // invalid path if it ends with |
            if (parentPathOfNew.endsWith("|")) {
                ModelAndView mav = viewFolder(request, oldPath);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Invalid path! Make sure it doesn't end with |");

                return mav;
            } else {
                Folder folderToBeUpdated = userService.getFolder(email, oldPath);

                // make sure we successfully fetched the folder to be updated from the db
                if (folderToBeUpdated != null) {

                    // make sure that the new path isnt already in use/ folder doesnt already exist
                    String newPath = parentPathOfNew + "|" + folderToBeUpdated.getName();
                    Folder existingFolder = userService.getFolder(email, newPath);

                    if (existingFolder == null) {

                        // make sure that the new path's parent path already has the parent folders created
                        String pathTrack = "";
                        for (String parent : parentPathOfNew.split("\\|")) {
                            pathTrack = pathTrack + parent;

                            // get the folder
                            Folder parentFolder = userService.getFolder(email, pathTrack);

                            if (parentFolder == null) {
                                // parent folder doesn't exist, return error
                                ModelAndView mav = viewFolder(request, oldPath);
                                mav.addObject("show", "true"); // show toaster when completed
                                mav.addObject("message", "Failed to move folder: the new path described is invalid! Make sure you already created the required folders.");

                                return mav;
                            }

                            // prepare for next parent
                            pathTrack = pathTrack + "|";
                        }

                        // remove old version of folder
                        String deletionResult = deleteFolderWithoutDeletingItems(email,oldPath);

                        if (!deletionResult.equals("true")) {
                            // failed to delete old folder
                            ModelAndView mav = viewFolder(request, oldPath);
                            mav.addObject("show", "true"); // show toaster when completed
                            mav.addObject("message", "Failed to move folder!");

                            return mav;
                        } else {

                            // create new version of folder
                            apiRequests.createFolder(email, folderToBeUpdated.getName(), parentPathOfNew);

                            // get the new version of folder
                            Folder updatedFolder = userService.getFolder(email, newPath);

                            // update its folders,subfolders,items
                            updatedFolder.setSubfolders(folderToBeUpdated.getSubfolders());
                            updatedFolder.setLinkItems(folderToBeUpdated.getLinkItems());
                            updatedFolder.setTextItems(folderToBeUpdated.getTextItems());
                            updatedFolder.setLocationItems(folderToBeUpdated.getLocationItems());

                            userService.updateFolder(email, updatedFolder);

                            //// update the actual items and related folders

                            // get all folders from the db
                            List<Folder> folders = userRepository.findByEmail(email).getFolders();

                            // get all items from the db
                            List<ItemLink> itemLinks = userRepository.findByEmail(email).getLinkItems();
                            List<ItemLocation> itemLocations = userRepository.findByEmail(email).getLocationItems();
                            List<ItemText> itemTexts = userRepository.findByEmail(email).getTextItems();

                            // get all folders with that respective path from the repo, rename it, set href and set locked status
                            for (Folder folder : folders) {
                                // make sure it starts with the old path
                                if (folder.getPath().startsWith(oldPath)) {
                                    // delete old version from repo
                                    userService.deleteFolder(email, folder);

                                    // rename path
                                    folder.setPath(folder.getPath().replace(oldPath, newPath));

                                    // rename href
                                    folder.setHref("view/folder?path=" + folder.getPath());

                                    // add new version to repo
                                    userService.addFolder(email, folder);

                                }
                            }

                            // same for itemLink
                            for (ItemLink itemLink : itemLinks) {
                                // make sure it starts with the old path
                                if (itemLink.getPath().startsWith(oldPath)) {
                                    // delete old version from repo
                                    userService.deleteItemLink(email,itemLink);

                                    // rename path
                                    itemLink.setPath(itemLink.getPath().replace(oldPath, newPath));

                                    // rename href
                                    itemLink.setHref("view/itemLink?path=" + itemLink.getPath());

                                    // add new version to repo
                                    userService.addItemLink(email,itemLink);
                                }
                            }

                            // same for itemLocation
                            for (ItemLocation itemLocation : itemLocations) {
                                // make sure it starts with the old path
                                if (itemLocation.getPath().startsWith(oldPath)) {
                                    // delete old version from repo
                                    userService.deleteitemLocation(email,itemLocation);

                                    // rename path
                                    itemLocation.setPath(itemLocation.getPath().replace(oldPath, newPath));

                                    // rename href
                                    itemLocation.setHref("view/itemLocation?path=" + itemLocation.getPath());

                                    // add new version to repo
                                    userService.additemLocation(email,itemLocation);
                                }
                            }

                            // same for itemText
                            for (ItemText itemText : itemTexts) {
                                // make sure it starts with the old path
                                if (itemText.getPath().startsWith(oldPath)) {
                                    // delete old version from repo
                                    userService.deleteItemText(email,itemText);

                                    // rename path
                                    itemText.setPath(itemText.getPath().replace(oldPath, newPath));

                                    // rename href
                                    itemText.setHref("view/itemText?path=" + itemText.getPath());

                                    // add new version to repo
                                    userService.addItemText(email,itemText);
                                }
                            }

                            ///////////// SYNCHRONIZE /////////////////////////////////////

                            Folder folder = userService.getFolder(email,newPath);

                            ///// update the items data accordingly
                            itemLinks = folder.getLinkItems();
                            itemLocations = folder.getLocationItems();
                            itemTexts = folder.getTextItems();

                            // for itemLink
                            if (itemLinks != null) {
                                for (ItemLink itemLink : itemLinks) {
                                    // make sure it starts with the old path
                                    if (itemLink.getPath().startsWith(oldPath)) {
                                        // delete old version from repo
                                        userService.deleteItemLink(email,itemLink);

                                        // rename path
                                        itemLink.setPath(itemLink.getPath().replace(oldPath, newPath));

                                        // rename href
                                        itemLink.setHref("view/itemLink?path=" + itemLink.getPath());

                                        // add new version to repo
                                        userService.addItemLink(email,itemLink);
                                    }
                                }
                            }

                            // for itemLocation
                            if (itemLocations != null) {
                                for (ItemLocation itemLocation : itemLocations) {
                                    // make sure it starts with the old path
                                    if (itemLocation.getPath().startsWith(oldPath)) {
                                        // delete old version from repo
                                        userService.deleteitemLocation(email,itemLocation);

                                        // rename path
                                        itemLocation.setPath(itemLocation.getPath().replace(oldPath, newPath));

                                        // rename href
                                        itemLocation.setHref("view/itemLocation?path=" + itemLocation.getPath());

                                        // add new version to repo
                                        userService.additemLocation(email,itemLocation);
                                    }
                                }
                            }

                            // for itemText
                            if (itemTexts != null) {
                                for (ItemText itemText : itemTexts) {
                                    // make sure it starts with the old path
                                    if (itemText.getPath().startsWith(oldPath)) {
                                        // delete old version from repo
                                        userService.deleteItemText(email,itemText);

                                        // rename path
                                        itemText.setPath(itemText.getPath().replace(oldPath, newPath));

                                        // rename href
                                        itemText.setHref("view/itemText?path=" + itemText.getPath());

                                        // add new version to repo
                                        userService.addItemText(email,itemText);
                                    }
                                }
                            }

                            if (folder.getSubfolders() != null) {
                                List<Folder> copyOfSubfolders = new ArrayList<>(folder.getSubfolders());
                                folder.setSubfolders(copyOfSubfolders);
                                synchronizeSubfoldersAfterUpdate(email, newPath, oldPath, newPath.split("\\|").length);

                                // update the db
                                userService.updateFolder(email,folder);
                            } else {
                                // it does not have subfolders, simply synchronize
                                synchronizeSubfoldersAfterUpdate(email, newPath, oldPath, newPath.split("\\|").length);

                                // update the db
                                userService.updateFolder(email,folder);
                            }

                            // update the subfolders of every folder that has path thats starts with the new path
                            List<Folder> allFolders = userRepository.findByEmail(email).getFolders();
                            for (Folder a : allFolders) {
                                if (a.getPath().startsWith(newPath)) {
                                    if (a.getSubfolders() != null) {
                                        List<Folder> copyOfSubfolders = new ArrayList<>(a.getSubfolders());
                                        loopThroughSubfoldersAndUpdate(a.getSubfolders(), copyOfSubfolders, a.getLocked(), a.getPath());
                                        a.setSubfolders(copyOfSubfolders);
                                        synchronizeSubfoldersAfterUpdate(email,newPath, oldPath, newPath.split("\\|").length);

                                        // update the db
                                        userService.updateFolder(email,a);
                                    }
                                }
                            }

                            // loop through subfolders one more time from the old path
                            List<String> oldParentPathSplit = new ArrayList<String>(Arrays.asList(oldPath.split("\\|")));
                            oldParentPathSplit.remove(oldParentPathSplit.size() - 1);
                            if (oldParentPathSplit.size() > 0) {
                                // update through the old path's subfolders
                                String pathTracking = "";
                                for (String oldParent : oldParentPathSplit) {
                                    pathTracking = pathTracking + oldParent;

                                    // get the folder
                                    Folder oldParentFolder = userService.getFolder(email,pathTracking);

                                    // update its subfolders
                                    if (oldParentFolder.getSubfolders() != null) {
                                        List<Folder> copyOfSubfolders = new ArrayList<>(oldParentFolder.getSubfolders());
                                        loopThroughSubfoldersAndUpdate(oldParentFolder.getSubfolders(), copyOfSubfolders, oldParentFolder.getLocked(), oldParentFolder.getPath());
                                        oldParentFolder.setSubfolders(copyOfSubfolders);
                                        synchronizeSubfoldersAfterUpdate(email,newPath, oldPath, newPath.split("\\|").length);

                                        // update the db
                                        userService.updateFolder(email,oldParentFolder);
                                    }

                                    // prepare for next parent
                                    pathTracking = pathTracking + "|";
                                }
                            }

                            // redirect to same page but updated jsp
                            ModelAndView mav = viewFolder(request,newPath);
                            mav.addObject("show", "true"); // show toaster when completed
                            mav.addObject("message", "Successfully moved folder to new path!");

                            return mav;
                        }

                    } else {

                        // unsuccessful, folder already exists in the new path
                        ModelAndView mav = viewFolder(request,oldPath);
                        mav.addObject("show", "true"); // show toaster when completed
                        mav.addObject("message", "Failed to move folder: folder already exists in the new path!");

                        return mav;

                    }
                } else {
                    // failed to get the folder to update from the db
                    ModelAndView mav = indexController.index(request);
                    mav.addObject("show", "true"); // show toaster when completed
                    mav.addObject("message", "Failed to fetch the current folder from the database!");

                    return mav;

                }
            }
        }

    }

    // resolve error when clicking on a tree element, giving wrong href link
    @RequestMapping("/view/view/folder")
    public RedirectView redirectFolderViewView(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/folder?path="+path);
        return redirectView;
    }
    @RequestMapping("/view/view/itemLink")
    public RedirectView redirectItemLinkViewView(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLink?path="+path);
        return redirectView;
    }
    @RequestMapping("/view/view/itemLocation")
    public RedirectView redirectItemLocationViewView(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLocation?path="+path);
        return redirectView;
    }
    @RequestMapping("/view/view/itemText")
    public RedirectView redirectItemTextViewView(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemText?path="+path);
        return redirectView;
    }

    @RequestMapping("/create/view/folder")
    public RedirectView redirectCreateViewFolder(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/folder?path="+path);
        return redirectView;
    }
    @RequestMapping("/create/view/itemLink")
    public RedirectView redirectCreateViewItemLink(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLink?path="+path);
        return redirectView;
    }
    @RequestMapping("/create/view/itemLocation")
    public RedirectView redirectCreateViewItemLocation(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLocation?path="+path);
        return redirectView;
    }
    @RequestMapping("/create/view/itemText")
    public RedirectView redirectCreateViewItemText(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemText?path="+path);
        return redirectView;
    }
    @RequestMapping("/delete/view/folder")
    public RedirectView redirectDeleteViewFolder(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/folder?path="+path);
        return redirectView;
    }
    @RequestMapping("/delete/view/itemLink")
    public RedirectView redirectDeleteViewItemLink(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLink?path="+path);
        return redirectView;
    }
    @RequestMapping("/delete/view/itemLocation")
    public RedirectView redirectDeleteViewItemLocation(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLocation?path="+path);
        return redirectView;
    }
    @RequestMapping("/delete/view/itemText")
    public RedirectView redirectDeleteViewItemText(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemText?path="+path);
        return redirectView;
    }
    @RequestMapping("/edit/view/folder")
    public RedirectView redirectEditViewFolder(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/folder?path="+path);
        return redirectView;
    }
    @RequestMapping("/edit/view/itemLink")
    public RedirectView redirectEditViewItemLink(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLink?path="+path);
        return redirectView;
    }
    @RequestMapping("/edit/view/itemLocation")
    public RedirectView redirectEditViewItemLocation(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLocation?path="+path);
        return redirectView;
    }
    @RequestMapping("/edit/view/itemText")
    public RedirectView redirectEditViewItemText(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemText?path="+path);
        return redirectView;
    }
    @RequestMapping("/move/view/folder")
    public RedirectView redirectMoveViewFolder(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/folder?path="+path);
        return redirectView;
    }
    @RequestMapping("/move/view/itemLink")
    public RedirectView redirectMoveViewItemLink(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLink?path="+path);
        return redirectView;
    }
    @RequestMapping("/move/view/itemLocation")
    public RedirectView redirectMoveViewItemLocation(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemLocation?path="+path);
        return redirectView;
    }
    @RequestMapping("/move/view/itemText")
    public RedirectView redirectMoveViewItemText(@RequestParam String path){
        // redirect
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/view/itemText?path="+path);
        return redirectView;
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void loopThroughSubfoldersAndUpdate(List<Folder> subfolders, List<Folder> copyOfSubfolders, Boolean locked, String path){

        //System.out.println("============= RUNNING looopThroughSubfoldersAndUpdate ===============");

        for (int i = 0; i < subfolders.size(); i++){
            //System.out.println("=============  Subfolder path: "+subfolders.get(i).getPath());

            List<Folder> updatedCopyOfSubfolders = new ArrayList<>(copyOfSubfolders);

            // remove old version
            for (Folder copy : copyOfSubfolders){
                if (copy.getPath().equals(subfolders.get(i).getPath())){
                    if (updatedCopyOfSubfolders.remove(copy)){
                        //System.out.println("Removed success!");
                    }
                    else{
                        //System.out.println("Remove fail!");
                    }
                }
            }

            copyOfSubfolders = updatedCopyOfSubfolders;

            //System.out.println("subfolder old path: "+subfolders.get(i).getPath());

            // rename path
            subfolders.get(i).setPath(path+"|"+subfolders.get(i).getName());

            //System.out.println("subfolder new path: "+subfolders.get(i).getPath());

            // rename href
            subfolders.get(i).setHref("view/folder?path="+subfolders.get(i).getPath());

            // update locked status
            subfolders.get(i).setLocked(locked);

            // update linkItems
            if (subfolders.get(i).getLinkItems() != null){
                //System.out.println("Found link items to update on subfolder path: "+subfolders.get(i).getPath());
                List<ItemLink> linkItems = subfolders.get(i).getLinkItems();

                //System.out.println("==========UPDATING LINKITEM =========");
                for (ItemLink itemLink : linkItems){
                    //System.out.println("Old path: "+itemLink.getPath());
                    // rename path
                    itemLink.setPath(subfolders.get(i).getPath()+"|"+itemLink.getTitle().replace("<b>(Link)</b> ", ""));
                    //System.out.println("New path: "+itemLink.getPath());

                    // rename href
                    itemLink.setHref("view/itemLink?path="+itemLink.getPath());

                }
                //System.out.println("========== / UPDATING LINKITEM =========");
                subfolders.get(i).setLinkItems(linkItems);
            }

            // update locationItems
            if (subfolders.get(i).getLocationItems() != null){
                List<ItemLocation> linkLocations = subfolders.get(i).getLocationItems();

                for (ItemLocation itemLocation : linkLocations){
                    // rename path
                    itemLocation.setPath(subfolders.get(i).getPath()+"|"+itemLocation.getLocation().replace("<b>(Location)</b> ", ""));

                    // rename href
                    itemLocation.setHref("view/itemLocation?path="+itemLocation.getPath());

                }

                subfolders.get(i).setLocationItems(linkLocations);
            }

            // update textItems
            if (subfolders.get(i).getTextItems() != null){
                List<ItemText> linkItems = subfolders.get(i).getTextItems();

                for (ItemText itemText : linkItems){
                    // rename path
                    itemText.setPath(subfolders.get(i).getPath()+"|"+itemText.getTitle().replace("<b>(Text)</b> ", ""));

                    // rename href
                    itemText.setHref("view/itemText?path="+itemText.getPath());

                }

                subfolders.get(i).setTextItems(linkItems);
            }

            // add the new version
            copyOfSubfolders.add(subfolders.get(i));

            // go through other subfolders within that subfolder
            if (subfolders.get(i).getSubfolders() != null) {
                loopThroughSubfoldersAndUpdate(subfolders.get(i).getSubfolders(), copyOfSubfolders, subfolders.get(i).getLocked(), subfolders.get(i).getPath());
                //updatedSubfolders.add(subfolder);
                //System.out.println("---== Finished loop and added subfolder to updated subfolders"+" subfolder path: "+subfolder.getPath());
            }
            else{
                //updatedSubfolders.add(subfolder);
                //System.out.println("---== No more subfolders, finished loop and added subfolder to updated subfolders. subfolder path:"+subfolders.get(i).getPath());
            }
        }
    }

    public void synchronizeSubfoldersAfterUpdate(String email, String parentPath, String oldFolderPath, int refreshCount){
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
                Folder pFolder = userService.getFolder(email, pathFromBeginning);
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

                        // check if old folder path exists, and if so, remove it
                        if (pSubfolders.stream().anyMatch(a -> a.getPath().startsWith(oldFolderPath))) {
                            Folder toDelete = pSubfolders.stream().filter(a -> a.getPath().startsWith(oldFolderPath)).findFirst().get();
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

    public String deleteFolderWithoutDeletingItems(String email, @RequestParam String folder){

        // first make sure that folder name string isnt null
        if (folder != null){

            // make sure that the folder exists
            if (userService.getFolder(email,folder) != null){


                // delete the folder with the given path
                userService.deleteFolder(email,userService.getFolder(email,folder));

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
                    Folder pFolder = userService.getFolder(email,pathFromBeginning);

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
                apiRequests.synchronizeSubfolders(email,folder,folder.split("\\|").length);

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


}
