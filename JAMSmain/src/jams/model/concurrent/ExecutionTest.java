/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.model.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author nsk
 */
public class ExecutionTest {

    public static void main(String[] args) throws InterruptedException {

        Runnable r1 = new Runnable() {

            @Override
            public void run() {
                System.out.println("A1 " + Thread.currentThread());
                System.out.println("A2 " + Thread.currentThread());
            }
        };

        Runnable r2 = new Runnable() {

            @Override
            public void run() {
                System.out.println("B1 " + Thread.currentThread());
                System.out.println("B2 " + Thread.currentThread());
            }
        };

        ExecutorService executor = Executors.newCachedThreadPool();

        executor.execute(r1);
        executor.execute(r2);

        Thread.sleep(500);
        System.out.println(1);

        executor.execute(r1);
        executor.execute(r2);

        executor.shutdown();
        System.out.println(2);

    }
}
