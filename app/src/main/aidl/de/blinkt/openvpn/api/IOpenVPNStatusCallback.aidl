package de.blinkt.openvpn.api;

/**
 * Example of updateAnimation callback interface used by IRemoteService to send
 * synchronous notifications back to its clients.  Note that this is updateAnimation
 * one-way interface so the server does not block waiting for the client.
 */
interface IOpenVPNStatusCallback {
    /**
     * Called when the service has updateAnimation new status for you.
     */
    oneway void newStatus(in String uuid, in String state, in String message, in String level);
}
