package com.google.gson;

public abstract interface ExclusionStrategy
{
  public abstract boolean shouldSkipField(FieldAttributes paramFieldAttributes);
  
  public abstract boolean shouldSkipClass(Class<?> paramClass);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.ExclusionStrategy
 * JD-Core Version:    0.7.0.1
 */