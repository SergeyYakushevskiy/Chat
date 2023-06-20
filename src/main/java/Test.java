import java.nio.charset.Charset;

public class Test {

    public static void main(String[] args) {
        byte[] bytes = "în".getBytes(Charset.forName("Windows-1251"));
        for(byte ch : bytes)
            System.out.println(String.format("%8s", Integer.toBinaryString(ch & 0xFF)).replace(' ', '0'));
    }

}
