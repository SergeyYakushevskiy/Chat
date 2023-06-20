package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

// Главный класс клиента. Отвечает за подключение к серверу, подключение GUI и отправку сообщений
public class Client{
    private IGUI IGUI;
    private String userName;
    private String host;
    private int port;
    private PrintWriter out;

    // При создании клиента указывает хост сервера, порт и оболочка интерфейса, который умеет считывать сообщения
    // (с оболочки или принятый с сервера)
    public Client(String host, int port, IGUI IGUI){
        this.host = host;
        this.port = port;
        this.IGUI = IGUI;
        this.userName = this.IGUI.getUserName();
    }

    public void execute() throws IOException, InterruptedException {
        // создаём подключение к серверу, создаём отдельный поток для постоянной прослушки сообщенйи от сервера и
        // указываем метод для отправки сообщений в out
            Socket socket = new Socket(this.host, this.port);
            ReadThread read = new ReadThread(socket, IGUI);
            read.setDaemon(true);
            read.start();
            out = new PrintWriter(socket.getOutputStream(), true);
            // На старте отправляем имя клиента - имя окна
            Thread.sleep(3000);
            out.println(userName);
    }
    // метод отправки сообщения. К сообщению прибавляется имя пользователя, который его отправил, а после оно отправляется серверу
    public void sendMessage(String message){
        message = userName + ": " + message;
        out.println(message);
    }

    // для проверки
  /* public static void main(String[] args) {
        try {
            Socket client = new Socket(Constants.host, Constants.port);
            Scanner in = new Scanner(client.getInputStream());
            Scanner outScan = new Scanner(System.in);
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            while(in.hasNextLine()){
                System.out.println(in.nextLine());
                out.println(outScan.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
