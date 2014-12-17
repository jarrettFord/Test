package org.spacehq.packetlib.event.session;

public class SessionAdapter
  implements SessionListener
{
  public void packetReceived(PacketReceivedEvent event) {}
  
  public void packetSent(PacketSentEvent event) {}
  
  public void connected(ConnectedEvent event) {}
  
  public void disconnecting(DisconnectingEvent event) {}
  
  public void disconnected(DisconnectedEvent event) {}
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.session.SessionAdapter
 * JD-Core Version:    0.7.0.1
 */