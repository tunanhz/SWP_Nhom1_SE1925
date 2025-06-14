package model;
public class InternalAnnouncement {
    private int announcementId;
    private String title;
    private String content;
    private int createdByAdminBusiness;
    private int createdByAdminSystem;
    private String createdAt;

    public InternalAnnouncement() {
    }

    public InternalAnnouncement(int announcementId, String title, String content, 
                               int createdByAdminBusiness, int createdByAdminSystem, String createdAt) {
        this.announcementId = announcementId;
        this.title = title;
        this.content = content;
        this.createdByAdminBusiness = createdByAdminBusiness;
        this.createdByAdminSystem = createdByAdminSystem;
        this.createdAt = createdAt;
    }

    public int getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(int announcementId) {
        this.announcementId = announcementId;
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

    public int getCreatedByAdminBusiness() {
        return createdByAdminBusiness;
    }

    public void setCreatedByAdminBusiness(int createdByAdminBusiness) {
        this.createdByAdminBusiness = createdByAdminBusiness;
    }

    public int getCreatedByAdminSystem() {
        return createdByAdminSystem;
    }

    public void setCreatedByAdminSystem(int createdByAdminSystem) {
        this.createdByAdminSystem = createdByAdminSystem;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}