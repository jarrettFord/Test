package org.spacehq.packetlib.io;

import java.io.IOException;

public abstract interface NetInput
{
  public abstract boolean readBoolean()
    throws IOException;
  
  public abstract byte readByte()
    throws IOException;
  
  public abstract int readUnsignedByte()
    throws IOException;
  
  public abstract short readShort()
    throws IOException;
  
  public abstract int readUnsignedShort()
    throws IOException;
  
  public abstract char readChar()
    throws IOException;
  
  public abstract int readInt()
    throws IOException;
  
  public abstract int readVarInt()
    throws IOException;
  
  public abstract long readLong()
    throws IOException;
  
  public abstract float readFloat()
    throws IOException;
  
  public abstract double readDouble()
    throws IOException;
  
  public abstract byte[] readPrefixedBytes()
    throws IOException;
  
  public abstract byte[] readBytes(int paramInt)
    throws IOException;
  
  public abstract int readBytes(byte[] paramArrayOfByte)
    throws IOException;
  
  public abstract int readBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException;
  
  public abstract String readString()
    throws IOException;
  
  public abstract int available()
    throws IOException;
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.io.NetInput
 * JD-Core Version:    0.7.0.1
 */