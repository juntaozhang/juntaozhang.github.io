package com.example;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.roaringbitmap.RoaringBitmap;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;

public class VeryLargeBitmap {

    public static void main(String[] args) throws IOException {
//    RoaringBitmap rb = new RoaringBitmap();
        RoaringBitmap rb = new RoaringBitmap();
//    rb.add(0L, 1L << 32);// the biggest bitmap we can create
        for (int i = 100; i < 200; i++) {
//      if (i % 5 == 0)
            rb.add(i);
        }
        for (int i = 300; i < 400; i++) {
//      if (i % 7 == 0)
            if(i==333)
                rb.add(i);
        }
        for (int i = 400; i < 420; i++) {
//      if (i % 6 == 0)
            rb.add(i);
        }
        for (int i = 18344; i < 18444; i++) {
            rb.add(i);
        }
//    new ImmutableRoaringBitmap(rb.)
        System.out.println("350这个数字之前:" + rb.previousValue(350));
        System.out.println("范围数据个数" + rb.rangeCardinality(411, 420));
        System.out.println("Optimize before:" + rb.getSizeInBytes());
        rb.runOptimize();
        ByteArrayOutputStream b = new ByteArrayOutputStream(rb.serializedSizeInBytes());
        DataOutputStream d = new DataOutputStream(b);
        rb.serialize(d);
        d.close();
        b.close();

        RoaringBitmap ret = new RoaringBitmap();
        try {
            ret.deserialize(ByteBuffer.wrap(b.toByteArray()));
        } catch(IOException ignore) {
        }
        System.out.println("last:" + ret.last());

        System.out.println("序列化大小:"+b.toByteArray().length);
        System.out.println("Optimize after:" + rb.getSizeInBytes());
        System.out.println("first:" + rb.getIntIterator().next());
//    rb.forEach(new IntConsumer(){
//      @Override
//      public void accept(int i) {
//        System.out.println(i);
//      }
//    });
//    System.out.println("middle:" + );
        System.out.println("last:" + rb.getReverseIntIterator().next());
        System.out.println("Cardinality:" + rb.getCardinality());
        System.out.println(((rb.getSizeInBytes() * 50 * 50_0000_0000L) / (1024.0 * 1024.0 * 1024.0)) + "G");

//    System.out.println("memory usage: "+ rb.getSizeInBytes()*1.0/(1L << 32)+" byte per value");
//    if(rb.getLongCardinality() != ( 1L << 32))
//      throw new RuntimeException("bug!");

    }
}
