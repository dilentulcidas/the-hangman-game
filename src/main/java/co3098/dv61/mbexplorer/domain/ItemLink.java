package co3098.dv61.mbexplorer.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;


public class ItemLink {

	public ItemLink(String path, String title, String url) {
		this.path = path;
		this.title = title;
		this.url = url;
	}

	public ItemLink(String path, String title, String url, String href) {
		this.path = path;
		this.title = title;
		this.url = url;
		this.href = href;
	}

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Id @JsonInclude
    String path; // will have directory path + title. ex: "Documents|Books|<title>"
	@JsonProperty("name")
	String title;
	@JsonIgnore
    String url;

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	@JsonIgnore
	String href;

	public ItemLink(){

	}


}
