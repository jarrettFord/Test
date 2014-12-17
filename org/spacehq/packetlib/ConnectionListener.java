package org.spacehq.packetlib;

public abstract interface ConnectionListener
{
  public abstract String getHost();
  
  public abstract int getPort();
  
  public abstract boolean isListening();
  
  public abstract void close();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.packetlib.ConnectionListener
 * JD-Core Version:    0.7.0.1
 */