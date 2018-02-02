package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.datatypes.Session;
import com.alar.cellowar.shared.messaging.MessageCompression;
import com.alar.cellowar.shared.messaging.MessageResponseSession;

import java.util.*;

public class MatchingPool {
    private static MatchingPool _ins = null;

    private boolean _isSearching = false;
    private UUID _searchedSessionId = null;
    private Client _searchingClient = null;

    private MatchingPool() {

    }

    public static MatchingPool getInstance() {
        if(_ins == null)
            _ins = new MatchingPool();
        return _ins;
    }

    /**
     * main function of the MatchingPool
     *
     * makes matches for 2 people.
     * first one calls joinPool, recives session id which claims it is waiting for match.
     * the second one calls joinPoool receives the same session with match stats which initiate a game start.
     */
    synchronized public Session joinPool(Client c) {
        if(c == null)
            return null;
        Session session;
        if(!_isSearching) {
            // first one trying to join.
            _isSearching = true;
            _searchedSessionId = UUID.randomUUID();
            _searchingClient = c;
            List<Client> clientList = new LinkedList<>();
            clientList.add(c);
            HashMap<Integer, Integer> clientOrder = new HashMap<>();
            clientOrder.put(c.getId(), 1);

            session = new Session(_searchedSessionId, clientList, null, clientOrder);
            session.setIsSearching(true);

            TemporaryDB.getInstance().addAndReplaceSession(session);

            return session;

        } else {
            // second one joining. there is a match.
            List<Client> clientList = new LinkedList<>();
            clientList.add(c);
            if(_searchingClient != null)
                clientList.add(_searchingClient);

            HashMap<Integer, Integer> clientOrder = new HashMap<>();
            clientOrder.put(c.getId(), 1);
            if(_searchingClient != null)
                clientOrder.put(_searchingClient.getId(), 2);

            session = new Session(_searchedSessionId, clientList, null, clientOrder);
            session.setIsSearching(false);

            TemporaryDB.getInstance().addAndReplaceSession(session);

            _searchingClient = null;
            _searchedSessionId = null;
            _isSearching = false;

            return session;
        }
    }

    // for debugging. allow single client to make a game.
    public void setSingleJoin() {
        _isSearching = true;
        _searchedSessionId = UUID.randomUUID();
    }
}
