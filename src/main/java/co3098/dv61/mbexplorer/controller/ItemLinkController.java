package co3098.dv61.mbexplorer.controller;

import co3098.dv61.mbexplorer.domain.Folder;
import co3098.dv61.mbexplorer.domain.ItemLink;
import co3098.dv61.mbexplorer.repository.ItemLinkRepository;
import co3098.dv61.mbexplorer.repository.UserRepository;
import co3098.dv61.mbexplorer.service.UserService;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.html.HTMLTableCaptionElement;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class ItemLinkController {

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    RestAPIController apiRequests;
    @Autowired
    IndexController indexController;
    @Autowired
    FolderController folderController;

    @RequestMapping(value = "/view/itemLink", method=RequestMethod.GET)
    public ModelAndView viewLinkItem(HttpServletRequest request, @RequestParam String path){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // get the item link instance from the database
        ItemLink item = userService.getItemLink(email,path);

        // parent path
        String[] splitParent = path.split("\\|");
        List<String> parents = new ArrayList<>(Arrays.asList(splitParent));
        parents.remove(parents.size()-1);

        String parentPath = "";

        for (String parent : parents){
            parentPath = parentPath + parent + "|";
        }
        parentPath = parentPath.substring(0,parentPath.length()-1);

        // get parent folder
        Folder parentFolder = userService.getFolder(email,parentPath);

        if (item != null) {

            // format item name as to removing the "(Link) " at the beginning
            if (item.getTitle().startsWith(("<b>(Link)</b> "))) {
                String updatedTitle = item.getTitle().replace("<b>(Link)</b> ", "");
                item.setTitle(updatedTitle);
            }

            // if the url starts with " and ends with "
            if (item.getUrl().startsWith("\"") && item.getUrl().endsWith("\"")){
                // get url
                String itemUrl = item.getUrl();

                // remove first and last substring
                if (itemUrl.length() > 2) {
                    itemUrl = itemUrl.substring(1,itemUrl.length()-1);
                    item.setUrl(itemUrl);
                }
            }

            // make sure that url starts with http://
            if (!item.getUrl().startsWith("http://") && !item.getUrl().startsWith("https://")) {
                item.setUrl("http://" + item.getUrl());
            }

            // display it properly on the correct jsp
            ModelAndView mav = new ModelAndView();
            mav.setViewName("itemLinkDetails");
            mav.addObject("item", item);
            mav.addObject("folder",parentFolder);
            return mav;
        }
        else{
            // couldn't fetch item, redirect to error page
            ModelAndView mav = indexController.index(request);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to get the detailed data of the item from the database!");
            return mav;
        }
    }

    @RequestMapping(value = "/create/itemLink", method=RequestMethod.POST)
    public ModelAndView addLinkItem(HttpServletRequest request, @RequestParam("path") String parentPath, @RequestParam("title") String title, @RequestParam("url") String url){
        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // make api request and store its result
        String result = apiRequests.createItemLink(email,title,url,parentPath);

        String newPath = parentPath + "|" + title;

        if (result.equals("true")){
            // successful
            ModelAndView mav = viewLinkItem(request,newPath);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Successfully added link item "+title);

            return mav;
        }
        else{
            // unsuccessful
            ModelAndView mav = folderController.viewFolder(request,parentPath);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to add link item "+title+". Make sure that there is no other item with the same name in this directory.");

            return mav;
        }
    }

    @RequestMapping(value = "/edit/itemLink", method=RequestMethod.POST)
    public ModelAndView editLinkItem(HttpServletRequest request, @RequestParam("title") String title, @RequestParam("url") String url, @RequestParam("path") String path){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // get item link with that respective path from the repo
        ItemLink itemToBeUpdated = userService.getItemLink(email,path);

        //// item to be updated found, proceed to update it
        if (itemToBeUpdated != null) {
            // get new path (in order to return the view with that link) and parent path
            String[] split = path.split("\\|");
            List<String> splitInList = new ArrayList<>(Arrays.asList(split));
            splitInList.remove(splitInList.size() - 1);

            String pathLoop = "";
            for (String portion : splitInList) {
                pathLoop = pathLoop + portion + "|";
            }
            String newPath = pathLoop + title;
            String parentPath = pathLoop.substring(0, pathLoop.length() - 1);

            //System.out.println("New path: " + newPath + " parentPath: " + parentPath);

            // make api requests to remove the old item and add the new updated item
            //System.out.println("Path of the item to be deleted: " + path);
            apiRequests.deleteItemLink(email,path);
            apiRequests.createItemLink(email,title, url, parentPath);

            // redirect to same page but updated jsp
            ModelAndView mav = viewLinkItem(request,newPath);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Successfully updated the item!");

            return mav;
        }
        else{
            // Failed to fetch the item from the database!
            // unsuccessful
            ModelAndView mav = viewLinkItem(request,path);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to fetch the item from the database!");

            return mav;
        }

    }

    @RequestMapping(value = "/delete/itemLink", method=RequestMethod.POST)
    public ModelAndView deleteLinkItem(HttpServletRequest request, @RequestParam("path") String path){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // use the RESTApiController method to delete the respective item
        String result = apiRequests.deleteItemLink(email,path);

        List<String> splitList = Arrays.asList(path.split("\\|"));
        String deletedItemTitle = splitList.get(splitList.size()-1);

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

            if (parentPath != null){
                // successful
                ModelAndView mav = folderController.viewFolder(request,parentPath);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully deleted "+deletedItemTitle);

                return mav;
            }
            else{
                // successful - parent path empty
                ModelAndView mav = indexController.index(request);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully deleted "+deletedItemTitle);

                return mav;
            }
        }
        else{
            // unsuccessful
            ModelAndView mav = indexController.index(request);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to delete "+deletedItemTitle);

            return mav;
        }
    }

    @RequestMapping(value = "/move/itemLink", method=RequestMethod.POST)
    public ModelAndView moveLinkItem(HttpServletRequest request, @RequestParam("path") String path, @RequestParam("newPath") String newParentPath){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // get current item (item to be moved)
        ItemLink itemToMove = userService.getItemLink(email,path);
        String itemName = itemToMove.getTitle().replace("<b>(Link)</b> ","");
        String newItemPath = newParentPath+"|"+itemName;

        // get parent path of the item to be moved
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

        // invalid path if it ends with |
        if (newParentPath.endsWith("|")){
            ModelAndView mav = folderController.viewFolder(request,parentPath);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Invalid path! Make sure it doesn't end with |");

            return mav;
        }
        else {

            // make sure that it was fetched successfully from the db
            if (itemToMove != null) {

                // check if the new path has the parent folders already created
                String[] parents = newParentPath.split("\\|");
                String pathTracked = "";
                for (String parent : parents) {
                    pathTracked = pathTracked + parent;

                    // check if folder exists
                    Folder parentFolder = userService.getFolder(email,pathTracked);

                    if (parentFolder == null) {
                        // parent folder doesnt exist, return error
                        ModelAndView mav = folderController.viewFolder(request,parentPath);
                        mav.addObject("show", "true"); // show toaster when completed
                        mav.addObject("message", "Failed to move item: one of the parent folders does not exist!");

                        return mav;
                    }

                    // accomodate for next parent
                    pathTracked = pathTracked + "|";
                }

                // check if an item with the same name already exists in the new path
                ItemLink alreadyExists = userService.getItemLink(email,newItemPath);

                if (alreadyExists != null) {
                    // item with the same namealready exists, return error
                    ModelAndView mav = folderController.viewFolder(request,parentPath);
                    mav.addObject("show", "true"); // show toaster when completed
                    mav.addObject("message", "Failed to move item: link item with the same name already exists in the mentioned parent path!");

                    return mav;
                }

                //// proceed to move item

                // delete old item
                apiRequests.deleteItemLink(email,path);

                // add the same item to the new path
                itemToMove.setPath(parentPath + "|" + newItemPath);
                apiRequests.createItemLink(email,itemName, itemToMove.getUrl(), newParentPath);

                ModelAndView mav = folderController.viewFolder(request,newParentPath);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully moved item!");

                return mav;

            } else {
                // failed to fetch from the db

                ModelAndView mav = folderController.viewFolder(request,parentPath);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Failed to fetch item to be moved from the database!");

                return mav;
            }
        }
    }

}
