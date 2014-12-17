package io.netty.handler.codec.http;

import java.util.Set;

public abstract interface Cookie
  extends Comparable<Cookie>
{
  public abstract String getName();
  
  public abstract String getValue();
  
  public abstract void setValue(String paramString);
  
  public abstract String getDomain();
  
  public abstract void setDomain(String paramString);
  
  public abstract String getPath();
  
  public abstract void setPath(String paramString);
  
  public abstract String getComment();
  
  public abstract void setComment(String paramString);
  
  public abstract long getMaxAge();
  
  public abstract void setMaxAge(long paramLong);
  
  public abstract int getVersion();
  
  public abstract void setVersion(int paramInt);
  
  public abstract boolean isSecure();
  
  public abstract void setSecure(boolean paramBoolean);
  
  public abstract boolean isHttpOnly();
  
  public abstract void setHttpOnly(boolean paramBoolean);
  
  public abstract String getCommentUrl();
  
  public abstract void setCommentUrl(String paramString);
  
  public abstract boolean isDiscard();
  
  public abstract void setDiscard(boolean paramBoolean);
  
  public abstract Set<Integer> getPorts();
  
  public abstract void setPorts(int... paramVarArgs);
  
  public abstract void setPorts(Iterable<Integer> paramIterable);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.Cookie
 * JD-Core Version:    0.7.0.1
 */