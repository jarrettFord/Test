package org.spacehq.packetlib.event.session;

public abstract interface SessionListener
{
  public abstract void packetReceived(PacketReceivedEvent paramPacketReceivedEvent);
  
  public abstract void packetSent(PacketSentEvent paramPacketSentEvent);
  
  public abstract void connected(ConnectedEvent paramConnectedEvent);
  
  public abstract void disconnecting(DisconnectingEvent paramDisconnectingEvent);
  
  public abstract void disconnected(DisconnectedEvent paramDisconnectedEvent);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.session.SessionListener
 * JD-Core Version:    0.7.0.1
 */