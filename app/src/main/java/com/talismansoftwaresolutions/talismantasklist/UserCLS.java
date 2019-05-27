package com.talismansoftwaresolutions.talismantasklist;

public class UserCLS {

    private int userID;
    private int serverUserID;
    private String userName;
    private String userPassword;
    private String userEMail;

    public UserCLS() {}

    public UserCLS(String username) {
        this.userName = username;
    }


    //Getters and setters
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }


    public int getserverUserID() {
        return serverUserID;
    }

    public void setserverUserID(int serverUserID) {
        this.serverUserID = serverUserID;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserEMail() {
        return userEMail;
    }

    public void setUserEMail(String userEMail) {
        this.userEMail = userEMail;
    }

    public String toString() {
        return ("User name: " + userName + " User ID: " + userID + " Password: " + userPassword +
                " Server ID: " + serverUserID + " Email: " + userEMail);
    }

}