package ua.nprblm.bookexchange.models;

public class Users {
    private String username;
    private String number;
    private String password;
    private String image;
    private String tg;
    private String fb;

    public Users()
    {

    }

    public Users(String username, String number, String password) {
        this.username = username;
        this.number = number;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTg() {
        return tg;
    }

    public void setTg(String tg) {
        this.tg = tg;
    }

    public String getFb() {
        return fb;
    }

    public void setFb(String fb) {
        this.fb = fb;
    }
}
