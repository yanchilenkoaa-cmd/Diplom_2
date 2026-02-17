package models;

public class UserCredentials {
    private String email;
    private String password;
    private String name;

    public UserCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserCredentials(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // Геттеры
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
}
