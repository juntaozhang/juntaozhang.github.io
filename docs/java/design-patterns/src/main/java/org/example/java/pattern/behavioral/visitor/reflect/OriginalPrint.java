package org.example.java.pattern.behavioral.visitor.reflect;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Juntao.Zhang on 2015/1/12.
 */
public class OriginalPrint {
  public static void messyPrintCollection(Collection collection) {
    Iterator iterator = collection.iterator();
    while (iterator.hasNext()) {
      Object o = iterator.next();
      if (o instanceof Collection)
        messyPrintCollection((Collection)o);
      else if (o instanceof String)
        System.out.println("'"+o.toString()+"'");
      else if (o instanceof Float)
        System.out.println(o.toString()+"f");
      else
        System.out.println(o.toString());
    }
  }
  public static void main(String[] args) {
    List list = Lists.newArrayList();
    List subList = Lists.newArrayList();
    subList.add(2.0F);
    subList.add("this is strings");
    list.add(1.0F);
    list.add("this is a string");
    list.add(subList);
    messyPrintCollection(list);
  }
}
