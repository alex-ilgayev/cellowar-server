package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Client;
import com.alar.cellowar.shared.datatypes.Packet;
import com.alar.cellowar.shared.datatypes.Session;
import com.alar.cellowar.shared.messaging.MessageCompression;
import com.alar.cellowar.shared.messaging.MessageResponseSession;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Alex on 4/28/2015.
 *
 * Holds a message queue for each client.
 * The client will be polling the server for available messages any moment.
 */
public class MessageQueues {
    private final static Logger LOGGER = Logger.getLogger("MessageQueues");

    // map from client id to relevant client queue.
    private Hashtable<Integer, LinkedList<Packet>> _queues;
    // map from client id to the last time it polled the queue (which means activity).
    private Hashtable<Integer, Long> _timeStampMap;

    private static MessageQueues _ins = null;

    private MessageQueues(){
        _queues = new Hashtable<>();
        _timeStampMap = new Hashtable<>();
    }

    private void removeClient(int clientId){

    }

    public static MessageQueues getInstance(){
        if(_ins == null)
            _ins = new MessageQueues();
        return _ins;
    }

    public boolean isClientExist(Client client) {
        return _queues.containsKey(client.getId());
    }

    // if client exists, leaves his packets in the queue.
    public void addAndReplaceClient(Client client){
        if(_queues.containsKey(client.getId())){
            TemporaryDB.getInstance().addAndReplaceClient(client);
        }
        else {
            TemporaryDB.getInstance().addAndReplaceClient(client);
            LinkedList<Packet> packetList = new LinkedList<Packet>();
            _queues.put(client.getId(), packetList);
        }
    }

    public void addPacket(Client client, Packet packet){
        if(!_queues.containsKey(client.getId())) {
            LOGGER.severe(ErrorStrings.SERVER_ERROR_PACKET_FROM_UNRECOGNIZED_USER);
            return;
        }
        _queues.get(client.getId()).addLast(packet);
    }

    /**
     * Getting unread packets for the client.
     * The packets oldest to the newest.
     * also adding session message to the queue.
     *
     * @param client
     * @return New awaiting packets.
     */
    public Packet[] getAwaitingPackets(Client client, UUID id){
        _timeStampMap.put(client.getId(), System.currentTimeMillis());
        if(_queues.containsKey(client.getId())){
            // adding session message to the user.
            UUID clientSessionId = null;
            Session serverSession = null;
            if(((clientSessionId = client.getCurrSessionId()) != null)
                    && ((serverSession = TemporaryDB.getInstance().findSession(clientSessionId)) != null)) { // user is part of active session.
                MessageResponseSession returnMsg = new MessageResponseSession();
                returnMsg.responseClient = client;
                returnMsg.responseId = id;
                returnMsg.activeSession = serverSession;

                Packet p = new Packet();
                p.date = System.currentTimeMillis();
                p.payload = Base64.getEncoder().encodeToString(MessageCompression.getInstance().compress(returnMsg));
                MessageQueues.getInstance().addPacket(client, p);
            }

            Packet[] packets = _queues.get(client.getId()).toArray(new Packet[0]);
            _queues.get(client.getId()).clear();
            return packets;
        }
        else {
            return new Packet[0];
        }
    }

    public void removeOldQueues(){
        long currTime = System.currentTimeMillis();
        for(Iterator it = _queues.entrySet().iterator(); it.hasNext();){
            Map.Entry pair = (Map.Entry) it.next();
            int clientId = (Integer) pair.getKey();
            if(!_timeStampMap.containsKey(clientId)){
                it.remove();
                Client client = TemporaryDB.getInstance().findClient(clientId);
                if(client != null)
                    TemporaryDB.getInstance().removeClient(client);
                LOGGER.info("removed client through old queue removing");
            } else {
                if((currTime - _timeStampMap.get(clientId)) > Settings._TIME_TO_DELETE_CLIENT_MILLIS) {
                    _timeStampMap.remove(clientId);
                    it.remove();
                    Client client = TemporaryDB.getInstance().findClient(clientId);
                    if(client != null)
                        TemporaryDB.getInstance().removeClient(client);
                    LOGGER.info("removed client through old queue removing");
                }
            }
        }
    }
}
