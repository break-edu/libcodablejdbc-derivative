package me.hysong.libcodablejdbc;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

//public interface CompositionObject {
////    String getPrefix();
//    LinkedHashMap<String, Object> decompose(String asPrefix);
//    void compose(String asPrefix, LinkedHashMap<String, Object> fromMap);
//    String[] compositionKeys();
//}

public abstract class CompositionObject {
    @Getter @Setter private String prefix;
    public abstract LinkedHashMap<String, Object> decompose();
    public abstract void compose(LinkedHashMap<String, Object> fromMap);
    public abstract String[] compositionKeys();
}
