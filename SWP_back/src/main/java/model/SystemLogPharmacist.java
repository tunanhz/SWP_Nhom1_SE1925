package model;
public class SystemLogPharmacist {
    private int logId;
    private int accountPharmacistId;
    private String action;
    private String actionType;
    private String logTime;

    public SystemLogPharmacist() {
    }

    public SystemLogPharmacist(int logId, int accountPharmacistId, String action, String actionType, String logTime) {
        this.logId = logId;
        this.accountPharmacistId = accountPharmacistId;
        this.action = action;
        this.actionType = actionType;
        this.logTime = logTime;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getAccountPharmacistId() {
        return accountPharmacistId;
    }

    public void setAccountPharmacistId(int accountPharmacistId) {
        this.accountPharmacistId = accountPharmacistId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }
}