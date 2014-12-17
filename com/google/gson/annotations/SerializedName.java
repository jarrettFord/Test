package com.google.gson.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
public @interface SerializedName
{
  String value();
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     com.google.gson.annotations.SerializedName
 * JD-Core Version:    0.7.0.1
 */