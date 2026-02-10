package mg.itu.s5.cloud.signalement.dto;


public class NotificationRequest {
    private String title;
    private String body;
    private String token; 
    private String topic;

    public NotificationRequest() {
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    } 

    
}
