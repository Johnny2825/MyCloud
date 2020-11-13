public class Authentication extends AbstractMessage{
    private String login;
    private String password;
    private boolean status;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean getStatus() {
        return status;
    }

    public Authentication(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Authentication(boolean status) {
        this.status= status;
    }
}
