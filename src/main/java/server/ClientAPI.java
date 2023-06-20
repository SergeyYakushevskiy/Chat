package server;

import codec.ReedMuller;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

// ����� ��� ��������� ����� �������� �� ������� �������
public class ClientAPI extends Thread{
    private Server server;
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    // ��� ����������� ������ ������� �������� ��� "������ - �����������", ������� ������ socket - ����� �
    // ���� �������� � ������, � ������� ��������
    public ClientAPI(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
    }


    // ����� �������� ������� ���������
    public void sendMessage(String message){
        out.println(message);
    }

    @Override

    // ��� ����������� ������� ���� socket - ����� ����� �������� � ��������, � ��� ��������� ������ ��� �������� �
    // ��������� ������
    public void run(){
        try {
            // ����������� ��
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            // ������ ����� ������������
            String name = in.nextLine();
            String text = "";

            // ���� ������������ �� ������ ��������� ���� "exit", ����� �� ���� ������ ���������.
            // ����� ��� �����, ������� �������� ���� �������� � ����������, ������� �������� ������ ������
            while(!text.equals("exit")){
                if(in.hasNextLine()) {
                    text = in.nextLine();
                    server.broadcast(text);
                }
            }
            // ���� ������ ����������, �� ��������� ���� � ��� ����� � ������� socket - ����� � ���� ��������
            server.broadcast("���� ����, " + name);
            Thread.sleep(1000);
            socket.close();
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
