package org.spacehq.mc.protocol.data.status.handler;

import org.spacehq.mc.protocol.data.status.ServerStatusInfo;
import org.spacehq.packetlib.Session;

public abstract interface ServerInfoBuilder
{
  public abstract ServerStatusInfo buildInfo(Session paramSession);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.protocol.data.status.handler.ServerInfoBuilder
 * JD-Core Version:    0.7.0.1
 */