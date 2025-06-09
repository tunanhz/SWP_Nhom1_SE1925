public class ResultsOfParaclinicalServices {
    private int resultId;
    private int serviceOrderItemId;
    private String resultDescription;
    private String createdAt;

    public ResultsOfParaclinicalServices() {
    }

    public ResultsOfParaclinicalServices(int resultId, int serviceOrderItemId, 
                                        String resultDescription, String createdAt) {
        this.resultId = resultId;
        this.serviceOrderItemId = serviceOrderItemId;
        this.resultDescription = resultDescription;
        this.createdAt = createdAt;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getServiceOrderItemId() {
        return serviceOrderItemId;
    }

    public void setServiceOrderItemId(int serviceOrderItemId) {
        this.serviceOrderItemId = serviceOrderItemId;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}