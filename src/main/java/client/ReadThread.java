package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

// ����� - ����� ��� ���������� ��������� � �������
// �������� ��� ������� �������, ����� ��� ��������� ������ ����� ��������� �� ������� � ������� ��� �� GUI
public class ReadThread extends Thread{
    private Socket socket;
    private Scanner in;
    private IGUI IGUI;
    // ��� �������� ����������� socket - �����, ������ ����� ��������� � �������� GUI, ���� ��� ��������� ��������
    public ReadThread(Socket socket, IGUI igui){
        this.socket = socket;
        this.IGUI = igui;
    }

    // �������������� ������� ����� ������ � ���, ����� �� ����� ������ ���-�� ���������
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
