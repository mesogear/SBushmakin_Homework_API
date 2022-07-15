package data;

public class LoginUnsuccessful {
    private String error;

    public LoginUnsuccessful() {}

    public LoginUnsuccessful(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
