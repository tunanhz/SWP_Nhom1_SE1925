public class SystemLogStaff {
    private int logId;
    private int accountStaffId;
    private String action;
    private String actionType;
    private String logTime;

    public SystemLogStaff() {
    }

    public SystemLogStaff(int logId, int accountStaffId, String action, String actionType, String logTime) {
        this.logId = logId;
        this.accountStaffId = accountStaffId;
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

    public int getAccountStaffId() {
        return accountStaffId;
    }

    public void setAccountStaffId(int accountStaffId) {
        this.accountStaffId = accountStaffId;
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