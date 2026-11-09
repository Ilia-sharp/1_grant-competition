package academy.tochkavhoda.competition.domain.models;

import java.util.Objects;

public abstract class User {
    private String firstName;
    private String lastName;
    private String login;
    private String password;

    protected User(String firstName, String lastName, String login, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Логин уникален в системе, поэтому пользователи сравниваются по логину
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(login, ((User) o).login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }
}
