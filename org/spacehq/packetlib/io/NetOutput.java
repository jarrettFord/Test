package org.spacehq.packetlib.io;

import java.io.IOException;

public abstract interface NetOutput
{
  public abstract void writeBoolean(boolean paramBoolean)
    throws IOException;
  
  public abstract void writeByte(int paramInt)
    throws IOException;
  
  public abstract void writeShort(int paramInt)
    throws IOException;
  
  public abstract void writeChar(int paramInt)
    throws IOException;
  
  public abstract void writeInt(int paramInt)
    throws IOException;
  
  public abstract void writeVarInt(int paramInt)
    throws IOException;
  
  public abstract void writeLong(long paramLong)
    throws IOException;
  
  public abstract void writeFloat(float paramFloat)
    throws IOException;
  
  public abstract void writeDouble(double paramDouble)
    throws IOException;
  
  public abstract void writePrefixedBytes(byte[] paramArrayOfByte)
    throws IOException;
  
  public abstract void writeBytes(byte[] paramArrayOfByte)
    throws IOException;
  
  public abstract void writeBytes(byte[] paramArrayOfByte, int paramInt)
    throws IOException;
  
  public abstract void writeString(String paramString)
    throws IOException;
  
  public abstract void flush()
    throws IOException;
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.io.NetOutput
 * JD-Core Version:    0.7.0.1
 */