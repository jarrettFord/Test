package io.netty.handler.codec.http.multipart;

public abstract interface FileUpload
  extends HttpData
{
  public abstract String getFilename();
  
  public abstract void setFilename(String paramString);
  
  public abstract void setContentType(String paramString);
  
  public abstract String getContentType();
  
  public abstract void setContentTransferEncoding(String paramString);
  
  public abstract String getContentTransferEncoding();
  
  public abstract FileUpload copy();
  
  public abstract FileUpload duplicate();
  
  public abstract FileUpload retain();
  
  public abstract FileUpload retain(int paramInt);
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.handler.codec.http.multipart.FileUpload
 * JD-Core Version:    0.7.0.1
 */