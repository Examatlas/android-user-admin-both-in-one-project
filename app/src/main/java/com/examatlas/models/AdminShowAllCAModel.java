package com.examatlas.models;

public class AdminShowAllCAModel {
    String caID,imageURL,title,keyword,content,tags,totalRows,totalPages,currentPage;

    public AdminShowAllCAModel(String caID,String imageURL, String title, String keyword, String content, String tags,String totalRows,String totalPages,String currentPage) {
        this.caID = caID;
        this.imageURL = imageURL;
        this.title = title;
        this.keyword = keyword;
        this.content = content;
        this.tags = tags;
        this.totalRows = totalRows;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(String totalRows) {
        this.totalRows = totalRows;
    }

    public String getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(String totalPages) {
        this.totalPages = totalPages;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public String getCaID() {
        return caID;
    }

    public void setCaID(String caID) {
        this.caID = caID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
