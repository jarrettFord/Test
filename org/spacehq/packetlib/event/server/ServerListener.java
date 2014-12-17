package org.spacehq.packetlib.event.server;

public abstract interface ServerListener
{
  public abstract void serverBound(ServerBoundEvent paramServerBoundEvent);
  
  public abstract void serverClosing(ServerClosingEvent paramServerClosingEvent);
  
  public abstract void serverClosed(ServerClosedEvent paramServerClosedEvent);
  
  public abstract void sessionAdded(SessionAddedEvent paramSessionAddedEvent);
  
  public abstract void sessionRemoved(SessionRemovedEvent paramSessionRemovedEvent);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.event.server.ServerListener
 * JD-Core Version:    0.7.0.1
 */