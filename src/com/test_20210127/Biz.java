package com.test_20210127;

import java.util.concurrent.atomic.AtomicInteger;

public class Biz {

    public static int count = 100000;
    public static AtomicInteger atomicInteger = new AtomicInteger(100000);
    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {
            new Thread(() ->{
                for (int j = 0; j < 10000; j++) {
                    synchronized (Biz.class) {
                        count --;
                    }
                    atomicInteger.getAndDecrement();

                }
                System.out.println("当前thread执行结束！");
            }).start();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("count:: " + count);
        System.out.println("atomicInteger:: " + atomicInteger.get());
    }
}
