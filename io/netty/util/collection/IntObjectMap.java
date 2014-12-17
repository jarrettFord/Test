package io.netty.util.collection;

public abstract interface IntObjectMap<V>
{
  public abstract V get(int paramInt);
  
  public abstract V put(int paramInt, V paramV);
  
  public abstract void putAll(IntObjectMap<V> paramIntObjectMap);
  
  public abstract V remove(int paramInt);
  
  public abstract int size();
  
  public abstract boolean isEmpty();
  
  public abstract void clear();
  
  public abstract boolean containsKey(int paramInt);
  
  public abstract boolean containsValue(V paramV);
  
  public abstract Iterable<Entry<V>> entries();
  
  public abstract int[] keys();
  
  public abstract V[] values(Class<V> paramClass);
  
  public static abstract interface Entry<V>
  {
    public abstract int key();
    
    public abstract V value();
    
    public abstract void setValue(V paramV);
  }
}


/* Location:           C:\Users\User\Desktop\spam.jar
 * Qualified Name:     io.netty.util.collection.IntObjectMap
 * JD-Core Version:    0.7.0.1
 */