package com.github.zeldazach.binghamtonrover.util;

import java.util.concurrent.atomic.AtomicLong;

public class ResetTimer {

    private Runnable good_runnable;
    private Runnable bad_runnable;

    private long timeout;
    private AtomicLong last_time = new AtomicLong(0);

    private ResetThread thread;

    public ResetTimer(Runnable rg, Runnable rb, long t) {
        good_runnable = rg;
        bad_runnable = rb;
        timeout = t;

        thread = new ResetThread();
        thread.setDaemon(true);
        thread.start();
    }

    public void trigger() {
        last_time.set(System.currentTimeMillis());
            good_runnable.run();
    }

    private class ResetThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (System.currentTimeMillis() - last_time.get() >= timeout) {
                    bad_runnable.run();
                }

                try
                {
                    Thread.sleep(5);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
