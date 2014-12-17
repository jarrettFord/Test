package org.spacehq.packetlib;

public abstract interface SessionFactory
{
  public abstract Session createClientSession(Client paramClient);
  
  public abstract ConnectionListener createServerListener(Server paramServer);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.SessionFactory
 * JD-Core Version:    0.7.0.1
 */