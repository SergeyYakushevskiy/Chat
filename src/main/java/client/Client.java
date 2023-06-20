package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

// ������� ����� �������. �������� �� ����������� � �������, ����������� GUI � �������� ���������
public class Client{
    private IGUI IGUI;
    private String userName;
    private String host;
    private int port;
    private PrintWriter out;

    // ��� �������� ������� ��������� ���� �������, ���� � �������� ����������, ������� ����� ��������� ���������
    // (� �������� ��� �������� � �������)
    public Client(String host, int port, IGUI IGUI){
        this.host = host;
        this.port = port;
        this.IGUI = IGUI;
        this.userName = this.IGUI.getUserName();
    }

    public void execute() throws IOException, InterruptedException {
        // ������ ����������� � �������, ������ ��������� ����� ��� ���������� ��������� ��������� �� ������� �
        // ��������� ����� ��� �������� ��������� � out
            Socket socket = new Socket(this.host, this.port);
            ReadThread read = new ReadThread(socket, IGUI);
            read.setDaemon(true);
            read.start();
            out = new PrintWriter(socket.getOutputStream(), true);
            // �� ������ ���������� ��� ������� - ��� ����
            Thread.sleep(3000);
            out.println(userName);
    }
    // ����� �������� ���������. � ��������� ������������ ��� ������������, ������� ��� ��������, � ����� ��� ������������ �������
    public void sendMessage(String message){
        message = userName + ": " + message;
        out.println(message);
    }

    // ��� ��������
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
