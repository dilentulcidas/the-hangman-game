package co3098.dv61.mbexplorer.service;

import co3098.dv61.mbexplorer.domain.Folder;
import co3098.dv61.mbexplorer.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FolderService {

    @Autowired
    FolderRepository folderRepository;

    // adds folder to database to FOLDER table
    public void addFolder(Folder folder){

        // add to repository
        folderRepository.save(folder);
    }

    // gets the specific folder with the respective path
    public Folder getFolder(String path){
        return folderRepository.findByPath(path);
    }

    // updates folder to db
    public void updateFolder(Folder folder){

        // add to repository the new version
        folderRepository.save(folder);
    }

    // remove folder from db
    public void deleteFolder(Folder folder){
        // remove from repo
        folderRepository.delete(folder);
    }

    // add folder name to path, given the current path
    public void addToPath(String path, String folderName){
        path = path+"|"+folderName;
    }

    // remove folder name to path, given the current path
    public void removeFromPath(String path, String folderName){
        List<String> list = Arrays.asList(path.split("\\|"));
        if (list.contains(folderName)) {
            list.remove(folderName);
            for (String element : list){
                path = path + element + "|";
            }

            if (!path.equals("")){
                path = path.substring(0,path.length()-1);
            }
        }
    }

    public boolean folderNameIsInPath(String path, String folderName){
        List<String> list = Arrays.asList(path.split("\\|"));

        if (list.contains(folderName)) {
            return true;
        }
        else{
            return false;
        }
    }

    public String convertArrayToPath(String[] list){
        String to_return = "";
        for (String element : list){
            to_return = to_return + element + "|";
        }

        if (!to_return.equals("")){
            to_return = to_return.substring(0, to_return.length() - 1);
        }

        return to_return;
    }
}
