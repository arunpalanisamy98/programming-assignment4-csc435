package csc435.app;


import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;

public class Dispatcher implements Runnable {
    private ServerSideEngine engine;
    private String address;
    private String port;
    private int numWorkers;
    private int numTerminatedWorkers;

    private IndexStore store;
    private ZContext context;
    private ZMQ.Socket routerSocket;
    private ZMQ.Socket dealerSocket;

    public Dispatcher(IndexStore store,String address, String port, int numWorkers) {
        this.store = store;
        this.address = address;
        this.port = port;
        this.numWorkers = numWorkers;
        numTerminatedWorkers = 0;
    }


    public Dispatcher(ServerSideEngine engine) {
        this.engine = engine;
    }
    
    @Override
    public void run() {
        ArrayList<Thread> threads = new ArrayList<Thread>();

        // ZMQ context initialized with 4 IO threads
        context = new ZContext(4);

        // Create ZMQ router and dealer sockets
        routerSocket = context.createSocket(ZMQ.ROUTER);
        dealerSocket = context.createSocket(ZMQ.DEALER);

        // Bind the router socket to the server listening address and port
        // Bind the dealer socket to worker internal communcation channel
        routerSocket.bind("tcp://" + address + ":" + port);
        dealerSocket.bind("inproc://workers");

        for (int i = 0; i < numWorkers; i++) {
            Worker worker = new Worker(store,this, context);
            Thread thread = new Thread(worker);
            thread.start();
            threads.add(thread);
        }

        ZMQ.proxy(routerSocket, dealerSocket, null);

        try {
            for (int i = 0; i < numWorkers; i++) {
                threads.get(i).join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        routerSocket.close();
        dealerSocket.close();
        context.close();
    }

    public synchronized void workerTerminate() {
        numTerminatedWorkers++;

        // Stop after two workers terminated
        if (numTerminatedWorkers >= 2) {
            context.destroy();
        }
    }
}
