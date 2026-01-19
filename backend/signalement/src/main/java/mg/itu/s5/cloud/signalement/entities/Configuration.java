package mg.itu.s5.cloud.signalement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "configurations")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Configuration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "key", length = 100)
    private String key;

    @Column(name = "value", length = 100)
    private String value;

    @Column(name = "type", length = 100)
    private String type;

    public Configuration() {}

    public Configuration(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
