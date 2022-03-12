package com.revsni.stuff;

public class ShutDownHookThread extends Thread {
    @Override
    public void run() {
       // ***write your code here to handle any shutdown request
        System.out.println("Shut Down Hook Called");
        super.run();
    }
}
