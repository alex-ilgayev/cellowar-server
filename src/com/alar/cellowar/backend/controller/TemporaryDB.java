package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Session;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alex on 4/28/2015.
 */
public class TemporaryDB {
    private static TemporaryDB _ins = null;

    private LinkedList<Client> _clients;
    private LinkedList<Session> _sessions;

    // pool of waiting to find partners.
    private LinkedList<Client> _poolClients;

    private TemporaryDB(){
        _clients = new LinkedList<Client>();
        _sessions = new LinkedList<Session>();
        _poolClients = new LinkedList<>();
    }

    public static TemporaryDB getInstance(){
        if(_ins == null)
            _ins = new TemporaryDB();
        return _ins;
    }

    public Client[] getAllClients() {
        if(_clients.size() == 0)
            return new Client[0];
        LinkedList<Client> clone = new LinkedList<Client>();
        for(Client c: _clients)
            clone.add(c);
        return clone.toArray(new Client[0]);
    }

    public Client[] getPlayingClients(){
        if(_clients.size() == 0)
            return new Client[0];
        LinkedList<Client> clone = new LinkedList<Client>();
        for(Client c: _clients){
            if(c.getCurrSessionId() != null)
                clone.add(c);
        }
        return clone.toArray(new Client[0]);
    }

    public void addAndReplaceJoinPool(Client client) {
        if(_poolClients.contains(client)){
            _poolClients.remove(client);
        }
        _poolClients.add(client);
    }

    public void removeFromJoinPool(Client client) {
        if(_poolClients.contains(client)) {
            _poolClients.remove(client);
        }
    }

    /**
     * finds match of clients who joined the pool according to the number of people specified.
     * the function removed the clients from the waiting list and send them.
     * returns null if none found
     */
    public List<Client> findJoinPoolMatch(int num) {
        int size = _poolClients.size();
        if(size < num) {
            return null;
        }
        List<Client> ret = new LinkedList<>();
        for(int i=0; i<num; i++) {
            Client c = _poolClients.remove(0);
            ret.add(0, c);
        }
        return ret;
    }

    public void addAndReplaceClient(Client client){
        if(_clients.contains(client)){
            _clients.remove(client);
        }
        _clients.add(client);
    }

    public void removeClient(Client client){
        if(_poolClients.contains(client)) {
            _poolClients.remove(client);
        }

        if(_clients.contains(client)) {
            _clients.remove(client);
        }

        for(Session session: _sessions){
            if(session.getClientList().contains(client))
                session.getClientList().remove(client);
        }
    }

    public Client findClient(int clientId){
        for(Client client: _clients)
            if(client.getId() == clientId)
                return client;
        return null;
    }

    public void addAndReplaceSession(Session session){
        if(_sessions.contains(session)){
            _sessions.remove(session);
        }
        _sessions.add(session);
    }

    public void removeSession(Session session){
        if(_sessions.contains(session)) {
            _sessions.remove(session);
        }
    }

    public Session findSession(UUID sessionId){
        for(Session session: _sessions) {
            if (session.getSessionId().equals(sessionId))
                return session;
        }
        return null;
    }

    public Session[] getAllSessions(){
        if(_sessions.size() == 0)
            return new Session[0];
        return _sessions.toArray(new Session[0]);
    }

//    /**
//     * Search for specific client out of client pool.
//     *
//     * @param clientId
//     * @return return the client, or null otherwise.
//     */
//    public Client findClient(int clientId){
//        for(Client client: _clients){
//            if(client.getId() == clientId)
//                return client;
//        }
//        return null;
//    }
}
