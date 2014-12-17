package com.google.gson;

import java.lang.reflect.Field;

public abstract interface FieldNamingStrategy
{
  public abstract String translateName(Field paramField);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.FieldNamingStrategy
 * JD-Core Version:    0.7.0.1
 */