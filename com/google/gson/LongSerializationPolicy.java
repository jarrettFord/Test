package com.google.gson;

public enum LongSerializationPolicy
{
  DEFAULT,  STRING;
  
  private LongSerializationPolicy() {}
  
  public abstract JsonElement serialize(Long paramLong);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.LongSerializationPolicy
 * JD-Core Version:    0.7.0.1
 */