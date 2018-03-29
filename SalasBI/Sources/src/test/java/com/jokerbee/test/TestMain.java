package com.jokerbee.test;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.concurrent.TimeUnit;

public class TestMain {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        testOneFuture();
    }

    /**
     * 当单个调用时, 会使用 future 的 handler;
     */
    private static void testOneFuture() {
        Future<String> future1 = Future.future();
        future1.setHandler(res -> {
            if (res.succeeded()) {
                System.out.println("f1 success");
            } else {
                System.out.println("f1 failed");
            }
        });

        try {
            TimeUnit.SECONDS.sleep(1);
            future1.fail("ss");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当 CompositeFuture.all() 时, 会将 futures 的 handler 重新设置一遍, 所以之前设置的 handler 全都失效了.
     */
    private static void testCompositeFuture() {
        Future<String> future1 = Future.future();
        future1.setHandler(res -> {
            if (res.succeeded()) {
                System.out.println("f1 success");
            } else {
                System.out.println("f1 failed");
            }
        });
        Future<String> future2 = Future.future();
        future2.setHandler(res -> {
            if (res.succeeded()) {
                System.out.println("f2 success");
            } else {
                System.out.println("f2 failed");
            }
        });
        Future<String> future3 = Future.future();
        future3.setHandler(res -> {
            if (res.succeeded()) {
                System.out.println("f3 success");
            } else {
                System.out.println("f3 failed");
            }
        });
        Future<String> future4 = Future.future();
        future4.setHandler(res -> {
            if (res.succeeded()) {
                System.out.println("f4 success");
            } else {
                System.out.println("f4 failed");
            }
        });

        CompositeFuture.all(future1, future2, future3, future4).setHandler(res -> {
            if (res.succeeded()) {
                System.out.println("composite all success");
            } else {
                System.out.println("composite all failed");
            }
        });

        try {
            TimeUnit.SECONDS.sleep(1);
            future1.complete();
            TimeUnit.SECONDS.sleep(2);
            future2.complete();
            TimeUnit.SECONDS.sleep(1);
            future3.complete();
            future4.failed();
            TimeUnit.SECONDS.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
