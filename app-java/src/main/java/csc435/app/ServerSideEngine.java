package csc435.app;

public class ServerSideEngine {
    private IndexStore store;

    public ServerSideEngine(IndexStore store) {
        this.store = store;
    }

    public void initialize(String hostname, String port) {
        Dispatcher dispatcher = new Dispatcher(store,hostname, port, 4 );
        new Thread(dispatcher).start();
    }

    public void spawnWorker() {
    }

    public void shutdown() {

    }

    public void list() {
        for(String s: GlobalIndex.connectedClients){
            System.out.println(s);
        }
    }
}
