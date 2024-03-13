package csc435.app;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ClientSideEngine {
    private ZMQ.Socket socket;
    private String ip;
    private int port;
    private static boolean isConnected;
    private String clientId;

    public boolean getIsConnected(){
        return isConnected;
    }

    public ClientSideEngine() {}

    static Set<String> foldersAccessed = new HashSet<>();

    public ZMQ.Socket getSocket(ZContext context) {
        ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
        socket.connect("tcp://" + ip + ":" + port);
        return socket;
    }

    public void indexFiles(String rawFile, String datasetNo ) throws Exception {
        if(isConnected){
            File input = new File(rawFile);
            if (input.exists() && input.isDirectory()) {
                File[] files = input.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        if (foldersAccessed.contains(file.getAbsolutePath())) continue;
                        indexFile(file.getAbsolutePath(), file.getName(), datasetNo);
                    }
                }
            }
        }
    }

    public void indexFile(String fileName, String name, String datasetNo) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        foldersAccessed.add(datasetNo+name);
        String line;
        HashMap<String, Integer> wordCount = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            line = line.replace("\r", "");
            line = line.replace("\\s+", "");
            line = line.replace("[^\\w\\s", "");
            line = line.replaceAll("[^a-zA-Z0-9 ]", "");
            String[] str = line.split(" ");
            for (String s : str) {
                if(s.equals("")) continue;
                if(wordCount.containsKey(s)){
                    wordCount.put(s, wordCount.get(s) + 1);
                }
                else{
                    wordCount.put(s, 1);
                }
            }
        }
        Data indexData = new Data();
        indexData.setFilename(datasetNo+name);
        indexData.setWordCount(wordCount);
        indexData.setConnectionType(ConnectionType.INDEX);
        ZContext context = new ZContext(1);
        socket=getSocket(context);
        socket.send(Util.serialize(indexData), 0);
        if(!(Util.deserialize(socket.recv(0))).isIndexed()){
            System.out.println("Indexing failed for "+fileName);
        }
        socket.close();
    }

    public void searchFiles(Set<String> words) throws Exception{
        if(isConnected){
            Data searchData = new Data();
            searchData.setWords(words);
            searchData.setConnectionType(ConnectionType.SEARCH);
            ZContext context = new ZContext(1);
            socket = getSocket(context);
            socket.send(Util.serialize(searchData), 0);
            Data response = Util.deserialize(socket.recv(0));
            if (!response.isResponse()) {
                System.out.println("Search operation failed");
            }
            socket.close();
            response.getResult().stream().forEach(System.out::println);
        }else{
            System.out.println("not connected to any server");
        }


    }

    public void openConnection(String ip, String port)   {
        try{
            ZContext context = new ZContext(1);
            this.ip = ip;
            this.port = Integer.parseInt(port);
            socket = getSocket(context);
            Data connectionData = new Data();
            connectionData.setRequest(true);
            connectionData.setStatus(true);
            connectionData.setConnectionType(ConnectionType.CONN);
            this.clientId = ClientAppInterface.hostname+":"+ClientAppInterface.port;
            connectionData.setClientId(this.clientId);
            socket.send(Util.serialize(connectionData), 0);
            //System.out.println("waiting for response");
            Data data = Util.deserialize(socket.recv(0));
            if (data.isResponse()) {
                this.isConnected = true;
                System.out.println("connected to " + ip + " at port " + port);
            } else {
                System.out.println("connection failed");
            }
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public void closeConnection() throws Exception {
        if(isConnected) {
            ZContext context = new ZContext(1);
            socket = getSocket(context);
            Data connectionData = new Data();
            connectionData.setRequest(true);
            connectionData.setStatus(false);
            connectionData.setClientId(clientId);
            connectionData.setConnectionType(ConnectionType.CONN);
            socket.send(Util.serialize(connectionData), 0);
            if ((Util.deserialize(socket.recv(0))).isResponse()) {
                System.out.println("disconnected from " + ip + ": " + port);
            } else {
                System.out.println("connection failed");
            }
            socket.close();
        }
        System.exit(0);
    }
}
