package org.spacehq.packetlib.packet;

import java.io.IOException;
import org.spacehq.packetlib.io.NetInput;
import org.spacehq.packetlib.io.NetOutput;

public abstract interface PacketHeader
{
  public abstract boolean isLengthVariable();
  
  public abstract int getLengthSize();
  
  public abstract int getLengthSize(int paramInt);
  
  public abstract int readLength(NetInput paramNetInput, int paramInt)
    throws IOException;
  
  public abstract void writeLength(NetOutput paramNetOutput, int paramInt)
    throws IOException;
  
  public abstract int readPacketId(NetInput paramNetInput)
    throws IOException;
  
  public abstract void writePacketId(NetOutput paramNetOutput, int paramInt)
    throws IOException;
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.packet.PacketHeader
 * JD-Core Version:    0.7.0.1
 */