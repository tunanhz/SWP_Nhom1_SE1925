public class SystemLogPatient {
    private int logId;
    private int accountPatientId;
    private String action;
    private String actionType;
    private String logTime;

    public SystemLogPatient() {
    }

    public SystemLogPatient(int logId, int accountPatientId, String action, String actionType, String logTime) {
        this.logId = logId;
        this.accountPatientId = accountPatientId;
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

    public int getAccountPatientId() {
        return accountPatientId;
    }

    public void setAccountPatientId(int accountPatientId) {
        this.accountPatientId = accountPatientId;
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