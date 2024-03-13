package csc435.app;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Set;

public class Worker implements Runnable {
    private IndexStore store;
    private Dispatcher server;
    private ZContext context;


    public Worker(IndexStore store,Dispatcher server, ZContext context) {
        this.store = store;
        this.server = server;
        this.context = context;
    }
    
    @Override
    public void run() {
        ZMQ.Socket socket = context.createSocket(ZMQ.REP);
        socket.connect("inproc://workers");
        try{
            while(true) {
                byte[] buffer = socket.recv(0);
                Data req = (Data) Util.deserialize(buffer);
                //String message = new String(buffer, ZMQ.CHARSET);
                ConnectionType data = req.getConnectionType();
                if(data.equals(ConnectionType.CONN)){
                    if(req.isStatus()){
                        GlobalIndex.connectedClients.add(req.getClientId());
                    }else{
                        GlobalIndex.connectedClients.remove(req.getClientId());
                    }
                    req.setResponse(true);
                    //System.out.println("sending to client");
                    socket.send(Util.serialize(req), 0);
                    //socket.close();
                } else  if(data.equals(ConnectionType.INDEX)) {
                    GlobalIndex.globalIndex.put(req.getFilename(),req.getWordCount());
                    req.setIndexed(true);
                    socket.send(Util.serialize(req), 0);
                }
                else  if(data.equals(ConnectionType.SEARCH)) {
                    Set<String> words =req.getWords();
                    if(words.size()==1){
                        String word=null;
                        for(String s:words) {
                            word = s;
                        }
                        Set<String> ans =store.lookupIndex(word);
                        req.setResponse(true);
                        req.setResult(ans);
                        socket.send(Util.serialize(req), 0);
                    }else{
                        String str[]=new String[2]; int i=0;
                        for(String s:words){
                            str[i]=s;
                            i++;
                        }
                        req.setResponse(true);
                        Set<String> ans =store.lookupIndex(str[0],str[1]);
                        req.setResult(ans);
                        socket.send(Util.serialize(req), 0);
                    }
                }
                else {
                    System.out.println("unrecogonized request");
                }

                /*if (message.compareTo("quit") == 0) {
                    break;
                }

                if (message.compareTo("search") == 0) {
                    message = "2+2=4";
                    socket.send(message.getBytes(ZMQ.CHARSET), 0);
                    continue;
                }

                if (message.compareTo("multiplication") == 0) {
                    message = "2x2=4";
                    socket.send(message.getBytes(ZMQ.CHARSET), 0);
                    continue;
                }

                message = "???";
                socket.send(message.getBytes(ZMQ.CHARSET), 0);*/
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        server.workerTerminate();
        socket.close();
    }
}
