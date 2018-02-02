package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.datatypes.Session;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Alex on 4/28/2015.
 */
public class TemporaryDB {
    private static TemporaryDB _ins = null;
    private final static Logger LOGGER = Logger.getLogger("TemporaryDB");

    // extending the client class just for DB purpose.
    private class ExtendedClient {
        public Client client;
        // awaiting packets.
        public List<Packet> packetQueue;
        // last time it polled.
        public Long timestamp;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null)
                return false;
            if(o.getClass() == Client.class) {
                Client client = (Client) o;
                return this.client.getId() == client.getId();
            }
            if(o.getClass() == getClass()) {
                ExtendedClient client = (ExtendedClient) o;
                return this.client.getId() == client.client.getId();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return client.getId();
        }
    }

    private Set<ExtendedClient> _clients;
    private LinkedList<Session> _sessions;

    private TemporaryDB(){
        _clients = new HashSet<>();
        _sessions = new LinkedList<>();
    }

    public static TemporaryDB getInstance(){
        if(_ins == null)
            _ins = new TemporaryDB();
        return _ins;
    }

    // *****************
    // CLIENTS FUNCTIONS
    // *****************

    private ExtendedClient findExtendedClient(Client c) {
        for(ExtendedClient ec: _clients) {
            if(ec.client.equals(c))
                return ec;
        }
        return null;
    }

    public Client findClientById(int clientId) {
        for(ExtendedClient ec: _clients) {
            if(ec.client.getId() == clientId)
                return ec.client;
        }
        return null;
    }

    public Client[] getAllClients() {
        if(_clients.size() == 0)
            return new Client[0];
        LinkedList<Client> clone = new LinkedList<Client>();
        for(ExtendedClient c: _clients)
            clone.add(c.client);
        return clone.toArray(new Client[0]);
    }

    synchronized public void addAndReplaceClient(Client client){
        ExtendedClient existingClient = findExtendedClient(client);
        ExtendedClient newClient = new ExtendedClient();
        newClient.client = client;

        if(_clients.contains(client)) {// we are replacing client.
            newClient.packetQueue = existingClient.packetQueue;
            newClient.timestamp = existingClient.timestamp;
            _clients.remove(client);
        } else { // new client
            newClient.packetQueue = new LinkedList<>();

        }
        _clients.add(newClient);
    }

    synchronized public void removeClient(Client client){
        if(_clients.contains(client)) {
            _clients.remove(client);
        }

        // TODO: optimize. should be removed.
        for(Session session: _sessions){
            if(session.getClientList().contains(client))
                session.getClientList().remove(client);
        }
    }

    public void setTimestamp(Client client, long timestamp) {
        findExtendedClient(client).timestamp = timestamp;
    }

    public long getTimestamp(Client client) {
        return findExtendedClient(client).timestamp;
    }

    public List<Packet> getQueue(Client client) {
        return findExtendedClient(client).packetQueue;
    }

    public void clearQueue(Client client) {
        findExtendedClient(client).packetQueue.clear();
    }

    //******************
    // SESSION FUNCTIONS
    //******************

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

    // **********
    // CLEANING
    // **********

    public void removeOldQueues(){
        long currTime = System.currentTimeMillis();
        for(ExtendedClient c: _clients){
            if((currTime - c.timestamp) > Settings._TIME_TO_DELETE_CLIENT_MILLIS) {
                removeClient(c.client);
                LOGGER.info("removing client because time has passed since he polled");
            }
        }
    }
}
