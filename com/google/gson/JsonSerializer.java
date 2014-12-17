package com.google.gson;

import java.lang.reflect.Type;

public abstract interface JsonSerializer<T>
{
  public abstract JsonElement serialize(T paramT, Type paramType, JsonSerializationContext paramJsonSerializationContext);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.JsonSerializer
 * JD-Core Version:    0.7.0.1
 */