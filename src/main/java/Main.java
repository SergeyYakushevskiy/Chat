import client.ClientUI;
import utils.Constants;

// Обобщённый класс для презентации проекта. Указывается количество запущенных клиентов в переменной CLIENT_NUMBER
public class Main {

    private static final int CLIENT_NUMBER = 2;

    public static void main(String[] args) {
        for(int i = 1; i <= CLIENT_NUMBER; i ++){
            String title = "Клиент № " + i;
            new ClientUI(title, Constants.host, Constants.port);
        }
    }

}
