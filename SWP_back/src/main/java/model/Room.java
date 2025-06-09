public class Room {
    private int roomId;
    private String roomName;
    private String department;
    private String status;

    public Room() {
    }

    public Room(int roomId, String roomName, String department, String status) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.department = department;
        this.status = status;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}