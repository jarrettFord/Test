package io.netty.channel;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract interface ChannelHandler
{
  public abstract void handlerAdded(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
  
  public abstract void handlerRemoved(ChannelHandlerContext paramChannelHandlerContext)
    throws Exception;
  
  public abstract void exceptionCaught(ChannelHandlerContext paramChannelHandlerContext, Throwable paramThrowable)
    throws Exception;
  
  @Inherited
  @Documented
  @Target({java.lang.annotation.ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Sharable {}
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.channel.ChannelHandler
 * JD-Core Version:    0.7.0.1
 */