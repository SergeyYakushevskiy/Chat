package client;

import codec.ReedMuller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;


// класс оболочки клиента. Отвечает за графический интерфейс
public class ClientUI extends JFrame implements IGUI{
    private Client client;
    private JMenuBar menuBar;
    private JPanel chatPanel;
    private JTextArea encodedChatArea;
    private JTextArea decodedChatArea;
    private JTextField inputField;
    private ReedMuller code = new ReedMuller();
    private JButton sendMessageButton;

    // при создании указывается имя пользователя, хост и порт. Создаётся окно, инициализируются компоненты,
    // создаётся подключение к серверу
    public ClientUI(String title, String host, int port) {
        super(title);
        this.setDefaultLookAndFeelDecorated(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(640, 480);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        this.setComponents();
        this.setVisible(true);
        try {
            client = new Client(host, port, this);
            client.execute();
        }catch (IOException | InterruptedException ex){
            JOptionPane.showMessageDialog(this, "Не удалось подключиться: " + ex.getMessage());
            this.dispose();
        }
    }
    // отправляем клиенту имя пользователя
    public String getUserName(){
        return this.getTitle();
    }
    // устанавливаем компоненты
    private void setComponents(){
        // меню
        this.menuBar = new JMenuBar();

        JMenu file = new JMenu("Файл");
        JMenuItem exit = new JMenuItem("Выход");
        exit.addActionListener(e -> {
            this.dispose();
        });
        file.add(exit);


        JMenu view = new JMenu("Вид");
        JCheckBoxMenuItem encoded = new JCheckBoxMenuItem("Закодировано");
        encoded.addItemListener(e -> {
            JCheckBoxMenuItem chatState = (JCheckBoxMenuItem) e.getSource();
            if(chatState.getState()) {
                CardLayout layout = (CardLayout) this.chatPanel.getLayout();
                layout.show(this.chatPanel, "encoded");
            }else {
                CardLayout layout = (CardLayout) this.chatPanel.getLayout();
                layout.show(this.chatPanel, "decoded");
            }
        });
        view.add(encoded);

        this.menuBar.add(file);
        this.menuBar.add(view);

        // главную панель, где показываются чаты
        this.chatPanel = new JPanel(new CardLayout());

        this.encodedChatArea = new JTextArea(25, 25);
        this.encodedChatArea.setText("Закодированный чат\n");
        this.encodedChatArea.setEditable(false);

        this.decodedChatArea = new JTextArea(25, 25);
        this.decodedChatArea.setText("Раскодированный чат\n");
        this.decodedChatArea.setEditable(false);

        chatPanel.add(new JScrollPane(this.decodedChatArea), "decoded");
        chatPanel.add(new JScrollPane(this.encodedChatArea), "encoded");

        // панель ввода данных с кнопкой "Отправить"
        JPanel inputPanel = new JPanel(new FlowLayout());

        this.inputField = new JTextField(25);
        this.inputField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER) {
                    ClientUI.this.sendMessage();
                }
            }
            @Override
            public void keyPressed(KeyEvent e) {

            }
            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        this.sendMessageButton = new JButton("Отправить");
        this.sendMessageButton.addActionListener(e -> {
            this.sendMessage();
        });

        inputPanel.add(this.inputField);
        inputPanel.add(this.sendMessageButton);

        this.add(this.menuBar, BorderLayout.NORTH);
        this.add(this.chatPanel, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);
    }
    // метод принятия сообщения. При получении сообщения оно делится на имя пользователя и текст.
    // Имя пользователя не меняется, а текст декодируется
    @Override
    public void acceptMessage(String text){
        encodedChatArea.setEditable(true);
        decodedChatArea.setEditable(true);
        try{
            encodedChatArea.append(text + "\n");
            encodedChatArea.setCaretPosition(encodedChatArea.getDocument().getLength());
            String userName = text.substring(0, text.indexOf(":") + 1);
            String decoded = code.decode(text.substring(text.indexOf(": ") + 2));
            decodedChatArea.append(userName + decoded + "\n");
            decodedChatArea.setCaretPosition(decodedChatArea.getDocument().getLength());
        }catch (StringIndexOutOfBoundsException ex){
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        encodedChatArea.setEditable(false);
        decodedChatArea.setEditable(false);
    }

    // метод отправки сообщений. Сообщение считывается с экрана и отсылается клиенту для отправки.
    // Поле ввода очищается
    public void sendMessage(){
        String message = inputField.getText();
        String encodedMessage = code.encode(message);
        client.sendMessage(encodedMessage);
        inputField.setText("");
    }
}
