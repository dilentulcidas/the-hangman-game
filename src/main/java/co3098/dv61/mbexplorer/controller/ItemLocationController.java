package co3098.dv61.mbexplorer.controller;

import co3098.dv61.mbexplorer.domain.Folder;
import co3098.dv61.mbexplorer.domain.ItemLocation;
import co3098.dv61.mbexplorer.repository.UserRepository;
import co3098.dv61.mbexplorer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class ItemLocationController {

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

    @RequestMapping(value = "/view/itemLocation", method=RequestMethod.GET)
    public ModelAndView viewLocationItem(HttpServletRequest request, @RequestParam String path){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // get the item link instance from the database
        ItemLocation item = userService.getitemLocation(email,path);

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

            // format item name as to removing the "(Location) " at the beginning
            if (item.getLocation().startsWith(("<b>(Location)</b> "))) {
                String updatedLocation = item.getLocation().replace("<b>(Location)</b> ", "");
                item.setLocation(updatedLocation);
            }

            // display it properly on the correct jsp
            ModelAndView mav = new ModelAndView();
            mav.setViewName("itemLocationDetails");
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

    @RequestMapping(value = "/create/itemLocation", method=RequestMethod.POST)
    public ModelAndView addLocationItem(HttpServletRequest request, @RequestParam("path") String parentPath, @RequestParam("location") String location,
                                        @RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // make api request and store its result
        String result = apiRequests.createItemLocation(email,location,latitude,longitude,parentPath);

        String newPath = parentPath + "|" + location;

        if (result.equals("true")){
            // successful
            ModelAndView mav = viewLocationItem(request,newPath);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Successfully added location item "+location);

            return mav;
        }
        else{
            // unsuccessful
            ModelAndView mav = folderController.viewFolder(request,parentPath);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to add location item "+location+". Make sure that there is no other item with the same name in this directory.");

            return mav;
        }
    }

    @RequestMapping(value = "/edit/itemLocation", method=RequestMethod.POST)
    public ModelAndView editLocationItem(HttpServletRequest request, @RequestParam("location") String location,
                                         @RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude,
                                         @RequestParam("path") String path){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // get item link with that respective path from the repo
        ItemLocation itemToBeUpdated = userService.getitemLocation(email,path);

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
            String newPath = pathLoop + location;
            String parentPath = pathLoop.substring(0, pathLoop.length() - 1);

            //System.out.println("New path: " + newPath + " parentPath: " + parentPath);

            // make api requests to remove the old item and add the new updated item
            //System.out.println("Path of the item to be deleted: " + path);
            apiRequests.deleteItemLocation(email,path);
            apiRequests.createItemLocation(email,location,latitude,longitude,parentPath);

            // redirect to same page but updated jsp
            ModelAndView mav = viewLocationItem(request, newPath);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Successfully updated the item!");

            return mav;
        }
        else{
            // Failed to fetch the item from the database!
            // unsuccessful
            ModelAndView mav = viewLocationItem(request, path);
            mav.addObject("show", "true"); // show toaster when completed
            mav.addObject("message", "Failed to fetch the item from the database!");

            return mav;
        }

    }

    @RequestMapping(value = "/delete/itemLocation", method=RequestMethod.POST)
    public ModelAndView deleteLocationItem(HttpServletRequest request, @RequestParam("path") String path){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // use the RESTApiController method to delete the respective item
        String result = apiRequests.deleteItemLocation(email,path);

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

    @RequestMapping(value = "/move/itemLocation", method=RequestMethod.POST)
    public ModelAndView moveLocationItem(HttpServletRequest request, @RequestParam("path") String path, @RequestParam("newPath") String newParentPath){

        // get email from session
        String email = (String) request.getSession().getAttribute("email");

        // get current item (item to be moved)
        ItemLocation itemToMove = userService.getitemLocation(email, path);
        String itemName = itemToMove.getLocation().replace("<b>(Location)</b> ","");
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
            ModelAndView mav = folderController.viewFolder(request, parentPath);
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
                    Folder parentFolder = userService.getFolder(email, pathTracked);

                    if (parentFolder == null) {
                        // parent folder doesnt exist, return error
                        ModelAndView mav = folderController.viewFolder(request, parentPath);
                        mav.addObject("show", "true"); // show toaster when completed
                        mav.addObject("message", "Failed to move item: one of the parent folders does not exist!");

                        return mav;
                    }

                    // accomodate for next parent
                    pathTracked = pathTracked + "|";
                }

                // check if an item with the same name already exists in the new path
                ItemLocation alreadyExists = userService.getitemLocation(email, newItemPath);

                if (alreadyExists != null) {
                    // item with the same namealready exists, return error
                    ModelAndView mav = folderController.viewFolder(request, parentPath);
                    mav.addObject("show", "true"); // show toaster when completed
                    mav.addObject("message", "Failed to move item: location item with the same name already exists in the mentioned parent path!");

                    return mav;
                }

                //// proceed to move item

                // delete old item
                apiRequests.deleteItemLocation(email, path);

                // add the same item to the new path
                itemToMove.setPath(parentPath + "|" + newItemPath);
                apiRequests.createItemLocation(email, itemName, Double.toString(itemToMove.getLatitude()), Double.toString(itemToMove.getLongitude()), newParentPath);

                ModelAndView mav = folderController.viewFolder(request, newParentPath);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Successfully moved item!");

                return mav;

            } else {
                // failed to fetch from the db

                ModelAndView mav = folderController.viewFolder(request, parentPath);
                mav.addObject("show", "true"); // show toaster when completed
                mav.addObject("message", "Failed to fetch item to be moved from the database!");

                return mav;
            }
        }
    }

}
