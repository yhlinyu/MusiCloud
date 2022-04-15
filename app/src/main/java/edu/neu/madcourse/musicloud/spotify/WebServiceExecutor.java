package edu.neu.madcourse.musicloud.spotify;

import java.util.concurrent.Executor;

public class WebServiceExecutor implements Executor {
    @Override
    public void execute(Runnable runnable) {
        // Starts a new thread for each task
        new Thread(runnable).start();
    }
}

