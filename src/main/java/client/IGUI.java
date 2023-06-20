package client;

// общий шаблон любой пользовательской оболочки. Для нормальной работы она должна отправлять имя пользователя и уметь
// принимать сообщения
public interface IGUI {
    String getUserName();
    void acceptMessage(String text);
}
