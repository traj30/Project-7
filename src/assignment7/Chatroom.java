package assignment7;

import java.util.ArrayList;

/**
 * Created by jake on 11/28/16.
 */

public class Chatroom extends Thread{
    String name;
    ArrayList<Client> clients;

    public Chatroom() {
        name = "";
    }
    public Chatroom(String name) {
        this.name = name;
        clients = new ArrayList<Client>();
    }

    public Chatroom(ArrayList<Client> clients) {
        this.clients = clients;
    }

    public Chatroom(String name, ArrayList<Client> clients) {
        this.name = name;
        this.clients = clients;
    }

    public void changeName(String name)
    {
        this.name = name;
    }

    public ArrayList<Client> getClients(){
        return clients;
    }

    public void run(){

    }

    public String toString(){
        return name;
    }
}
