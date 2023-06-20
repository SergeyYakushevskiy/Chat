package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

// класс - поток для считывания сообщений с сервера
// создаётся для каждого клиента, чтобы тот постоянно ожидал новое сообщение от сервера и выводил его на GUI
public class ReadThread extends Thread{
    private Socket socket;
    private Scanner in;
    private IGUI IGUI;
    // При создании указывается socket - канал, откуда ждать сообщения и оболочка GUI, куда эти сообщения выводить
    public ReadThread(Socket socket, IGUI igui){
        this.socket = socket;
        this.IGUI = igui;
    }

    // инициализируем входной канал данных и ждём, когда по этому каналу что-то передадут
    @Override
    public void run(){
        try {
            in = new Scanner(socket.getInputStream());
            while (in.hasNextLine()){
                    IGUI.acceptMessage(in.nextLine());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
