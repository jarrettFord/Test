package org.spacehq.packetlib.event.server;

public class ServerAdapter
  implements ServerListener
{
  public void serverBound(ServerBoundEvent event) {}
  
  public void serverClosing(ServerClosingEvent event) {}
  
  public void serverClosed(ServerClosedEvent event) {}
  
  public void sessionAdded(SessionAddedEvent event) {}
  
  public void sessionRemoved(SessionRemovedEvent event) {}
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.server.ServerAdapter
 * JD-Core Version:    0.7.0.1
 */