package org.spacehq.mc.protocol.data.status.handler;

import org.spacehq.packetlib.Session;

public abstract interface ServerPingTimeHandler
{
  public abstract void handle(Session paramSession, long paramLong);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.status.handler.ServerPingTimeHandler
 * JD-Core Version:    0.7.0.1
 */