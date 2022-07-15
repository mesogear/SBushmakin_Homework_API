package data;

public class LoginSuccessful {
    private String token;

    public LoginSuccessful() {}

    public LoginSuccessful(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
