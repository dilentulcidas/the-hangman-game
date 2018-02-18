package co3098.dv61.mbexplorer.service;

import co3098.dv61.mbexplorer.domain.*;
import co3098.dv61.mbexplorer.repository.FolderRepository;
import co3098.dv61.mbexplorer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    ///////////////////// FOLDER ////////////////////////////////////////////////
    // adds folder to database to FOLDER list
    public void addFolder(String userEmail, Folder folder){

        // get user
        User user = userRepository.findByEmail(userEmail);

        boolean isNull = false;
        if (user == null){
            isNull = true;
        }
        System.out.println("isNull: "+isNull);

        // get current folder list
        List<Folder> userFolders = user.getFolders();

        // remove old version if it has, so that there are no duplicates
        String folderPath = folder.getPath();
        int i = 0;
        boolean found = false;
        for (Folder f : userFolders){
            if (f.getPath().equals(folderPath)){
                found = true;
                break;
            }
            i++;
        }

        if (found) {
            System.out.println("Removing index: "+i);
            userFolders.remove(i);
        }

        // add folder to the list
        userFolders.add(folder);

        // update user instance
        user.setFolders(userFolders);

        // add to repository
        userRepository.save(user);
    }

    // gets the specific folder with the respective path
    public Folder getFolder(String userEmail, String path){
        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current folder list
        List<Folder> userFolders = user.getFolders();

        // get respective folder
        Folder folderReturn = null;
        for (Folder folder : userFolders){
            if (folder.getPath().equals(path)){
                folderReturn = folder;
                break;
            }
        }

        return folderReturn;
    }

    // updates folder to db
    public void updateFolder(String userEmail, Folder folder){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current folder list
        List<Folder> userFolders = user.getFolders();

        // remove respective folder, first get its path then search for it on the list
        String folderPath = folder.getPath();
        int i = 0;
        boolean found = false;
        for (Folder f : userFolders){
            if (f.getPath().equals(folderPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userFolders.remove(i);
        }

        // add folder to the list
        userFolders.add(folder);

        // update user instance
        user.setFolders(userFolders);

        // update repository
        userRepository.save(user);
    }

    // remove folder from db
    public void deleteFolder(String userEmail, Folder folder){
        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current folder list
        List<Folder> userFolders = user.getFolders();

        // remove respective folder, first get its path then search for it on the list
        String folderPath = folder.getPath();
        int i = 0;
        boolean found = false;
        for (Folder f : userFolders){
            if (f.getPath().equals(folderPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userFolders.remove(i);
        }

        // update user instance
        user.setFolders(userFolders);

        // update repo with the deleted folder
        userRepository.save(user);
    }

    //////////////////////////////// ITEM LINK /////////////////////////////////

    // adds itemLink to database to ITEM LINK list
    public void addItemLink(String userEmail, ItemLink itemLink){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current item list
        List<ItemLink> userItems = user.getLinkItems();

        // remove respective item, first get its path then search for it on the list
        String itemPath = itemLink.getPath();
        int i = 0;
        boolean found = false;
        for (ItemLink item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // add item to the list
        userItems.add(itemLink);

        // update user instance
        user.setLinkItems(userItems);

        // add to repository
        userRepository.save(user);
    }

    // gets the specific itemLink with the respective path
    public ItemLink getItemLink(String userEmail, String path){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current items list
        List<ItemLink> userItems = user.getLinkItems();

        // get respective item
        ItemLink itemReturn = null;
        for (ItemLink item : userItems){
            if (item.getPath().equals(path)){
                itemReturn = item;
                break;
            }
        }

        return itemReturn;
    }

    // updates itemLink to db
    public void updateItemLink(String userEmail, ItemLink itemLink){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current item list
        List<ItemLink> userItems = user.getLinkItems();

        // remove respective item, first get its path then search for it on the list
        String itemPath = itemLink.getPath();
        int i = 0;
        boolean found = false;
        for (ItemLink item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // add item to the list
        userItems.add(itemLink);

        // update user instance
        user.setLinkItems(userItems);

        // add to repository
        userRepository.save(user);
    }

    // remove itemLink from db
    public void deleteItemLink(String userEmail, ItemLink itemLink){
        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current items list
        List<ItemLink> userItems = user.getLinkItems();

        // remove respective item
        String itemPath = itemLink.getPath();
        int i = 0;
        boolean found = false;
        for (ItemLink item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // update user instance
        user.setLinkItems(userItems);

        // update repo with the deleted folder
        userRepository.save(user);
    }

    //////////////////////////////// ITEM LOCATION /////////////////////////////////

    // adds itemLocation to database to ITEM LOCATION list
    public void additemLocation(String userEmail, ItemLocation itemLocation){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current item list
        List<ItemLocation> userItems = user.getLocationItems();

        // remove respective item, first get its path then search for it on the list
        String itemPath = itemLocation.getPath();
        int i = 0;
        boolean found = false;
        for (ItemLocation item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // add item to the list
        userItems.add(itemLocation);

        // update user instance
        user.setLocationItems(userItems);

        // add to repository
        userRepository.save(user);
    }

    // gets the specific itemLocation with the respective path
    public ItemLocation getitemLocation(String userEmail, String path){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current items list
        List<ItemLocation> userItems = user.getLocationItems();

        // get respective item
        ItemLocation itemReturn = null;
        for (ItemLocation item : userItems){
            if (item.getPath().equals(path)){
                itemReturn = item;
                break;
            }
        }

        return itemReturn;
    }

    // updates itemLocation to db
    public void updateitemLocation(String userEmail, ItemLocation itemLocation){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current item list
        List<ItemLocation> userItems = user.getLocationItems();

        // remove old version if it has, so that there are no duplicates
        // remove respective item, first get its path then search for it on the list
        String itemPath = itemLocation.getPath();
        int i = 0;
        boolean found = false;
        for (ItemLocation item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // add item to the list
        userItems.add(itemLocation);

        // update user instance
        user.setLocationItems(userItems);

        // add to repository
        userRepository.save(user);
    }

    // remove itemLocation from db
    public void deleteitemLocation(String userEmail, ItemLocation itemLocation){
        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current items list
        List<ItemLocation> userItems = user.getLocationItems();

        // remove respective item
        String itemPath = itemLocation.getPath();
        int i = 0;
        boolean found = false;
        for (ItemLocation item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // update user instance
        user.setLocationItems(userItems);

        // update repo with the deleted folder
        userRepository.save(user);
    }

    //////////////////////////////// ITEM TEXT /////////////////////////////////

    // adds ItemText to database to ITEM TEXT list
    public void addItemText(String userEmail, ItemText itemText){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current item list
        List<ItemText> userItems = user.getTextItems();

        // remove respective item, first get its path then search for it on the list
        String itemPath = itemText.getPath();
        int i = 0;
        boolean found = false;
        for (ItemText item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // add item to the list
        userItems.add(itemText);

        // update user instance
        user.setTextItems(userItems);

        // add to repository
        userRepository.save(user);
    }

    // gets the specific ItemText with the respective path
    public ItemText getItemText(String userEmail, String path){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current items list
        List<ItemText> userItems = user.getTextItems();

        // get respective item
        ItemText itemReturn = null;
        for (ItemText item : userItems){
            if (item.getPath().equals(path)){
                itemReturn = item;
                break;
            }
        }

        return itemReturn;
    }

    // updates ItemText to db
    public void updateItemText(String userEmail, ItemText itemText){

        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current item list
        List<ItemText> userItems = user.getTextItems();

        // remove respective item, first get its path then search for it on the list
        String itemPath = itemText.getPath();
        int i = 0;
        boolean found = false;
        for (ItemText item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // add item to the list
        userItems.add(itemText);

        // update user instance
        user.setTextItems(userItems);

        // add to repository
        userRepository.save(user);
    }

    // remove ItemText from db
    public void deleteItemText(String userEmail, ItemText itemText){
        // get user
        User user = userRepository.findByEmail(userEmail);

        // get current items list
        List<ItemText> userItems = user.getTextItems();

        // remove respective item
        String itemPath = itemText.getPath();
        int i = 0;
        boolean found = false;
        for (ItemText item : userItems){
            if (item.getPath().equals(itemPath)){
                found = true;
                break;
            }
            i++;
        }

        // removing respective folder
        if (found) {
            System.out.println("Removing index: "+i);
            userItems.remove(i);
        }

        // update user instance
        user.setTextItems(userItems);

        // update repo with the deleted folder
        userRepository.save(user);
    }

    //////////// USER /////////////////////////////////////////

    // adding first time user
    public void addUser(String email){
        User user = new User(email, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        userRepository.save(user);
    }

}
