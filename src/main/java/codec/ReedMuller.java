package codec;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


// класс кодировки сообщений кодом Рида-Маллера RM(1, 7). Выбран не случайно, т.к. k будет равен 8, что соответствует байту данных
public class ReedMuller {
    private static int r = 1;
    private static int m = 7;
    private static int n;
    private static int k;
    private static int d;
    private static int t;
    private static byte[][] G;
    private static HashMap<Integer, byte[]> iTable = new HashMap<>();
    // Как только есть хотя бы 1 клиент, который будет пользоваться кодом, задаются основные параметры и вычисляются
    // необходимые структуры
    static {
        n = (int) Math.pow(2, m);
        findK();
        t = (int) (Math.pow(2, m - r - 1));
        d = (int) Math.pow(2, m - r);
        createG();
        createITable();
        System.out.println("n = " + n);
        System.out.println("k = " + k);
        System.out.println("d = " + d);
    }
    // метод создания матрицы G;
    private static void createG(){
        // Создание матрицы - частный случай. Подходит только для RM первого порядка
        G = new byte[k][n];
        // первая строка - все 1
        Arrays.fill(G[0], (byte) 1);
        // далее вычисляются все двоичные комбинации чисел от 0 до n, каждый бит этого числа
        // записывается в соответствующую ячейку матрицы G
        for(int num = 0; num < n; num++)
            for(int sw = 0; sw < m; sw++){
                byte bit = (byte)((num >> sw) & 1);
                G[sw + 1][num] = bit;
            }
        System.out.println();
    }
    // метод создания таблицы информационных и кодовых слов. Хранятся в словаре, где ключ - информационное слово,
    // значение - кодовая последовательность
    private static void createITable(){
        for(int i = 0; i < Math.pow(2, k); i++){
            byte[] encodedWord = new byte[n];
            for(int c = 0; c < k; c++){
                byte bit = (byte)((i >> (k - c - 1)) & 1);
                if(bit == 1)
                    for (int nIndex = 0; nIndex < n; nIndex++)
                        encodedWord[nIndex] ^= G[c][nIndex];
            }
            iTable.put(i, encodedWord);
        }
    }
    // метод закодирования входящего текста
    public String encode(String text){
        // каждый символ - это байт данных. Конвертируем текст в последовательность байт
        byte[] bytes = convertToByte(text);
        StringBuilder output = new StringBuilder();
        for(byte ch : bytes){
            int index = ch & 0xFF;
            // находим кодовое слово, зная - что информационное слово - это входной байт символа
            byte[] encodedWord = iTable.get(index);
            byte[] noisedWord = makeNoise(encodedWord.clone());
            for (byte c : noisedWord)
                output.append(c);
            output.append(" ");
            }
        // записывпем кодовые последовательности в виде строки через пробел
        System.out.println(output.toString().trim());
        return output.toString().trim();
    }
    // метод раскодирования входящего текста
    public String decode(String text){
        // Все символы разделены пробелом при передаче. Компонуем их в массив строк
        // Конвертируем слова из строкового типа в числовой
        byte[][] words = convertToWords(text.split(" "));
        // Отнимаем от всех нулей в словах единицу
        for(int i = 0; i < words.length; i++)
            words[i] = replace(words[i], 0, -1);
        // последовательность байт, в которую будем записывать раскодированные символы
        byte[] output = new byte[words.length];

        for(int wIndex = 0; wIndex < words.length; wIndex ++){
            // на старте берём текущее слово
            byte[] current = words[wIndex];
            // создаём последовательность векторов вида (1, х), которые путём произведения Кронекера
            // дадут кодовое слово без ошибки
            byte[][] vectors = new byte[m][2];
            // создаём цикл из m шагов для вычисления каждого x
            for(int order = 0; order < m; order ++){
                vectors[order][0] = 1;
                byte[][] S = new byte[2][current.length / 2];
                // делим последовательность на 2 строки: чётную и нечётную
                for(int index = 0; index < current.length; index++){
                    if(index % 2 == 0)
                        S[0][index / 2] = current[index];
                    else
                        S[1][index / 2] = current[index];
                }
                // вычисляем для каждого вектора столбца сумму его значений и их разницу. Сумму записываем вверх, разницу - вниз
                int sum = 0, dif = 0;
                for(int i = 0; i < S[0].length; i++){
                    S[0][i] = (byte) (S[0][i] + S[1][i]);
                    // т.к. мы уже нашли сумму, то чтобы найти разность, надо от суммы дважды отнять нижний элемент
                    S[1][i] = (byte) (S[0][i] - 2 * S[1][i]);
                    sum += Math.abs(S[0][i]);
                    dif += Math.abs(S[1][i]);
                }
                // если сумма больше, то следующей проверяющей строкой будет верхняя, а данный x будет равен 1. Иначе -
                // нижняя и -1
                current = (sum > dif) ? S[0] : S[1];
                vectors[order][1] = (byte) ((sum > dif) ? 1 : -1);
            }
            // находим кодовое слово произведением Кронекера
            byte[] word = kroneckerProduct(vectors[0], vectors[1]);
            for(int i = 2; i < vectors.length; i++)
                word = kroneckerProduct(word, vectors[i]);
            // меняем -1 на 0 в последовательности
            word = replace(word, -1, 0);
            /*StringBuilder out = new StringBuilder();
            for(byte ch: word)
                out.append(ch);
            System.out.println(out);*/
            // находим информационное слово, зная кодовое
            output[wIndex] = findI(word);
        }
        // конвертируем последовательность байт в строку
        return convertToString(output);
    }
    // метод поиска информационного слова по кодовому
    private byte findI(byte[] word){
        // перебираем все кодовые слова и сравниваем их с искомым. Если слова совпали, то из этой строки извлекаем ключ -
        // информационное слово
        for (Map.Entry<Integer, byte[]> entry : iTable.entrySet()) {
            if(Arrays.equals(word, entry.getValue())) {
                return (byte)(int)entry.getKey();
            }
        }
        // если не находим, то возвращаем 0
        return 0;
    }
    // метод произведения Кронекера между двумя матрицами
    private byte[] kroneckerProduct(byte[] A, byte[] B){
        byte[] output = new byte[A.length * B.length];
        for(int i = 0; i < B.length; i++)
            for (int j = 0; j < A.length; j++)
                output[i * A.length + j] = (byte) (A[j] * B[i]);
        return output;
    }
    // метод конвертирования слов из строкового представления в числовой
    private byte[][] convertToWords(String[] words){
        byte[][] output = new byte[words.length][n];
        for(int i = 0; i < words.length; i++)
            for(int j = 0; j < n; j++)
                output[i][j] = Byte.parseByte(words[i].substring(j, j + 1));

        return output;
    }
    // метод для внедрения ошибочного бита в слуйчаный символ двоичной последовательности
    private byte[] makeNoise(byte[] word){
        // вычисляем случайную позицию ошибки в диапазоне от 0 до n
        int pos = (int)(Math.random() * n);
   //     System.out.println(pos);
        // меняем бит в этой позиции на противоположный
        word[pos] ^= 1;
        return word;
    }
    //метод для поиска параметра k кода
    private static void findK(){
        // по формуле, к - сумма всех сочетаний при i = от 0 до r из m по i
        k = 1;
        for(int i = 1; i <= r; i++)
            k += comb(m, i);
    }
    // функция замены нулей в полседовательности на -1
    private byte[] replace(byte[] seq, int from, int to){
        byte[] replaced = new byte[seq.length];
        for(int i = 0; i < seq.length; i++)
            replaced[i] = (seq[i] == from) ? (byte) to : seq[i];
        return replaced;
    }
    // функция конвертирования строки в массив байт. кодировка - windows-1251
    private byte[] convertToByte(String input){
        return input.getBytes(Charset.forName("Windows-1251"));
    }
    // функция конвертирования массива байт в строку
    private String convertToString(byte[] bytes){
        return new String(bytes, Charset.forName("Windows-1251"));
    }
    // функция для вычисления сочетаний из n по k
    private static int comb(int n, int k){
        return fact(n) / (fact(k) * fact(n - k));
    }
    // функция вычисления факториала
    private static int fact(int n){
        int res = 1;
        for(int i = 2; i <= n; i++) {
            res *= i;
        }
        return res;
    }


 /* public static void main(String[] args) {
        ReedMuller code = new ReedMuller();
        System.out.println(code.decode(code.encode("a")));
    }*/
}
