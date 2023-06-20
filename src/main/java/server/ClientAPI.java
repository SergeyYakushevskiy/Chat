package server;

import codec.ReedMuller;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

// класс дл€ обработки новых клиентов на стороне сервреа
public class ClientAPI extends Thread{
    private Server server;
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    // при подключении нового клиента создаЄтс€ его "объект - консультант", который хранит socket - канал с
    // этим клиентом и сервер, с которым работает
    public ClientAPI(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
    }


    // метод отправки клиенту сообщени€
    public void sendMessage(String message){
        out.println(message);
    }

    @Override

    // при подключении клиента есть socket - канал между клиентом и сервером, и два вложенных канала дл€ отправки и
    // получени€ данных
    public void run(){
        try {
            // отслеживаем их
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            // запрос имени пользовател€
            String name = in.nextLine();
            String text = "";

            // пока пользователь не пришлЄт сообщение типа "exit", ждать от него нового сообщени€.
            // когда оно придЄт, сделать рассылку всем клиентом с сообщением, которое отправил данный клиент
            while(!text.equals("exit")){
                if(in.hasNextLine()) {
                    text = in.nextLine();
                    server.broadcast(text);
                }
            }
            // ≈сли клиент отключилс€, то уведомить всех о его уходе и закрыть socket - канал с этим клиентом
            server.broadcast("ѕока пока, " + name);
            Thread.sleep(1000);
            socket.close();
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
