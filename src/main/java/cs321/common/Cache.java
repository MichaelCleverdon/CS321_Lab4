package cs321.common;

import java.util.LinkedList;

public class Cache<T>{
    private int maxSize;
    private int currentSize = 0;
    LinkedList<T> cache;
    
    public Cache(int size){
        this.maxSize = size;
        cache = new LinkedList<T>();
    }
    
    public T getObject(int index){
    	T obj = cache.get(index);
    	moveToTop(obj);
        return obj;
    }

    public T addObject(T object){
    	T obj = null;
        if(currentSize == maxSize){
            obj = removeObject(maxSize-1);
        }
        if (cache.contains(object)) {
    		moveToTop(object);
    	} else {
    		cache.add(0, object);
    		currentSize++;
    	}
        
        return obj;
        
    }

    public T removeObject(int index){
    	T object = cache.get(index);
        cache.remove(index);
        currentSize--;
        return object;
    }

    public T removeObject(T object){
        cache.remove(object);
        currentSize--;
        return object;
    }

    public void clearCache(){
        cache = new LinkedList<T>();
        currentSize = 0;
    }

    public void moveToTop(T object){
        removeObject(object);
        addObject(object);
    }
    
    @SuppressWarnings("unchecked")
	public LinkedList<T> getCacheObjects(){
    	return (LinkedList<T>) cache.clone();
    }
    
    public int getCurrentSize() {
    	return currentSize;
    }
}
