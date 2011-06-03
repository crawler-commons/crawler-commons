package crawlercommons.fetcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Payload implements Map<String, Object> {

    private Map<String, Object> _data;
    
    public Payload() {
        _data = new HashMap<String, Object>();
    }

    public void clear() {
        _data.clear();
    }

    public boolean containsKey(Object key) {
        return _data.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return _data.containsValue(value);
    }

    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return _data.entrySet();
    }

    public boolean equals(Object o) {
        return _data.equals(o);
    }

    public Object get(Object key) {
        return _data.get(key);
    }

    public int hashCode() {
        return _data.hashCode();
    }

    public boolean isEmpty() {
        return _data.isEmpty();
    }

    public Set<String> keySet() {
        return _data.keySet();
    }

    public Object put(String key, Object value) {
        return _data.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        _data.putAll(m);
    }

    public Object remove(Object key) {
        return _data.remove(key);
    }

    public int size() {
        return _data.size();
    }

    public Collection<Object> values() {
        return _data.values();
    }
    
    
}
