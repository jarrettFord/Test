package org.spacehq.packetlib.packet;

import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.io.NetOutput;

public abstract interface Packet
{
  public abstract void read(NetInput paramNetInput)
    throws IOException;
  
  public abstract void write(NetOutput paramNetOutput)
    throws IOException;
  
  public abstract boolean isPriority();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.packet.Packet
 * JD-Core Version:    0.7.0.1
 */