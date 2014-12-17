package org.spacehq.mc.auth;

public abstract interface ProfileLookupCallback
{
  public abstract void onProfileLookupSucceeded(GameProfile paramGameProfile);
  
  public abstract void onProfileLookupFailed(GameProfile paramGameProfile, Exception paramException);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     org.spacehq.mc.auth.ProfileLookupCallback
 * JD-Core Version:    0.7.0.1
 */