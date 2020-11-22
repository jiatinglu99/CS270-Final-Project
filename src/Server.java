import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.*;

public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(5677);
            Map<String, AbstractMap.SimpleEntry<Integer,List<ServerThread>>> roomList=new HashMap<String, AbstractMap.SimpleEntry<Integer,List<ServerThread>>>();
            List<ServerThread> threads = new ArrayList<ServerThread>();
            ServerThread.roomList = roomList;
            ServerThread.threads = threads;
            
            while (true) {
                Socket s=ss.accept();
                System.out.println("Connection from "+s.getInetAddress());
                threads.add(new ServerThread(s));
            }
        } catch (IOException ignored){
            System.out.println(ignored.getMessage());
        }
    }
}

class ServerThread extends Thread{
    PrintWriter pw;
    BufferedReader br;
    static Map<String, AbstractMap.SimpleEntry<Integer,List<ServerThread>>> roomList;
    static List<ServerThread> threads;
    String username;
    String roomName;
    Integer roomGoal;
    List<ServerThread> roomMembers;

    ServerThread(Socket s) throws IOException {
        pw = new PrintWriter(s.getOutputStream(),true);
        pw.println("Connected!");
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.start();
    }

    public Boolean createRoom(String rn){
        roomName = rn;
        roomGoal = ThreadLocalRandom.current().nextInt(1000);
        roomMembers = new ArrayList<ServerThread>();
        roomMembers.add(this);
        roomList.put(roomName, new AbstractMap.SimpleEntry<>(roomGoal, roomMembers));
        return true;
    }

    public Boolean joinRoom(String rn){
        AbstractMap.SimpleEntry<Integer,List<ServerThread>> room = roomList.get(rn);
        if (room == null) return false;
        roomName = rn;
        roomGoal = room.getKey();
        roomMembers = room.getValue();
        roomMembers.add(this);
        roomList.put(roomName, new AbstractMap.SimpleEntry<>(roomGoal, roomMembers));
        return true;
    }

    public Boolean exitRoom(String rn){
        AbstractMap.SimpleEntry<Integer,List<ServerThread>> room = roomList.get(rn);
        if (room == null) return false;
        roomName = null;
        roomGoal = null;
        roomMembers = null;
        roomMembers.remove(this);
        return true;
    }

    String extract(String data){
        String[] arr = data.split("!");
        return arr[1];
    }

    public void run(){
        while(true){
            try {
                String line=br.readLine();
                System.out.println(line);
                if (line.contains("TryLogin!")){
                    // TODO
                }
                else if (line.contains("TryRegister!")){
                    // TODO
                }
                else if (line.contains("Create!")){
                    // TODO
                }
                else if (line.contains("Guest!")){
                    username = extract(line);
                    // TODO
                }
                else if (line.contains("Join!")){
                    // TODO
                }
                else if (line.contains("Exit!")){
                    // TODO
                }
                else if (line.contains("Guess!")){
                    Integer guess;
                    try{
                        guess=Integer.parseInt(extract(line));
                    }
                    catch(NumberFormatException nfe){
                        pw.println("");
                        continue;
                    }
                    if (guess>roomGoal){
                        roomMembers.forEach(s->s.broadcast("Someone!"+Integer.toString(guess)+"!TOOBIG"));
                    } 
                    else if (guess<roomGoal){
                        roomMembers.forEach(s->s.broadcast("Someone!"+Integer.toString(guess)+"!TOOSMALL"));
                    } else{
                        roomMembers.forEach(s->s.broadcast("Someone!"+Integer.toString(guess)+"!EQUAL"));
                        roomMembers.forEach(s->s.exitRoom(roomName));
                    }
                }
            } catch (SocketException se){
                break;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(String message){
        System.out.println(message);
        pw.println(message);
    }
}