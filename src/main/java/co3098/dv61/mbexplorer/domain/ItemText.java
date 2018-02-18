package co3098.dv61.mbexplorer.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

public class ItemText {

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ItemText(String path, String title, String content) {
        this.path = path;
        this.title = title;
        this.content = content;
    }

    public ItemText(String path, String title, String content, String href) {
        this.path = path;
        this.title = title;
        this.content = content;
        this.href = href;
    }

    @Id
    String path; // will have directory path + title. ex: "Documents|Books|<title>"
    @JsonProperty("name")
    String title;
    @JsonIgnore
    String content;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @JsonIgnore
    String href;

    public ItemText(){

    }
}
