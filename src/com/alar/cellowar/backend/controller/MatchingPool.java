package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.datatypes.Session;
import com.alar.cellowar.shared.messaging.MessageCompression;
import com.alar.cellowar.shared.messaging.MessageResponseSession;

import java.util.*;

public class MatchingPool {
    private static MatchingPool _ins = null;

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

        Session [] allSessions = TemporaryDB.getInstance().getAllSessions();

        // searching for already seeking player.
        for(Session s: allSessions) {
            if(s.getIsSearching()) {
                // match.
                // creating a new session.
                List<Client> clientList = s.getClientList();
                clientList.add(c);

                HashMap<Integer, Integer> clientOrder = new HashMap<>();
                int i=1;
                for(Client client: clientList) {
                    clientOrder.put(client.getId(), i);
                    i++;
                }

                s.setClientList(clientList);
                s.setClientOrder(clientOrder);
                s.setIsSearching(false);
                s.setGameData(BoardGenerator.createNewBoard());

                return s;
            }
        }

        // no candidate, creting session waiting for join.
        Session session;

        List<Client> clientList = new LinkedList<>();
        clientList.add(c);
        HashMap<Integer, Integer> clientOrder = new HashMap<>();
        clientOrder.put(c.getId(), 1);

        session = new Session(UUID.randomUUID(), clientList, null, clientOrder);
        session.setIsSearching(true);

        TemporaryDB.getInstance().addAndReplaceSession(session);

        return session;
    }
}
