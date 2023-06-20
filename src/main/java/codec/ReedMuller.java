package codec;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


// ����� ��������� ��������� ����� ����-������� RM(1, 7). ������ �� ��������, �.�. k ����� ����� 8, ��� ������������� ����� ������
public class ReedMuller {
    private static int r = 1;
    private static int m = 7;
    private static int n;
    private static int k;
    private static int d;
    private static int t;
    private static byte[][] G;
    private static HashMap<Integer, byte[]> iTable = new HashMap<>();
    // ��� ������ ���� ���� �� 1 ������, ������� ����� ������������ �����, �������� �������� ��������� � �����������
    // ����������� ���������
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
    // ����� �������� ������� G;
    private static void createG(){
        // �������� ������� - ������� ������. �������� ������ ��� RM ������� �������
        G = new byte[k][n];
        // ������ ������ - ��� 1
        Arrays.fill(G[0], (byte) 1);
        // ����� ����������� ��� �������� ���������� ����� �� 0 �� n, ������ ��� ����� �����
        // ������������ � ��������������� ������ ������� G
        for(int num = 0; num < n; num++)
            for(int sw = 0; sw < m; sw++){
                byte bit = (byte)((num >> sw) & 1);
                G[sw + 1][num] = bit;
            }
        System.out.println();
    }
    // ����� �������� ������� �������������� � ������� ����. �������� � �������, ��� ���� - �������������� �����,
    // �������� - ������� ������������������
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
    // ����� ������������� ��������� ������
    public String encode(String text){
        // ������ ������ - ��� ���� ������. ������������ ����� � ������������������ ����
        byte[] bytes = convertToByte(text);
        StringBuilder output = new StringBuilder();
        for(byte ch : bytes){
            int index = ch & 0xFF;
            // ������� ������� �����, ���� - ��� �������������� ����� - ��� ������� ���� �������
            byte[] encodedWord = iTable.get(index);
            byte[] noisedWord = makeNoise(encodedWord.clone());
            for (byte c : noisedWord)
                output.append(c);
            output.append(" ");
            }
        // ���������� ������� ������������������ � ���� ������ ����� ������
        System.out.println(output.toString().trim());
        return output.toString().trim();
    }
    // ����� �������������� ��������� ������
    public String decode(String text){
        // ��� ������� ��������� �������� ��� ��������. ��������� �� � ������ �����
        // ������������ ����� �� ���������� ���� � ��������
        byte[][] words = convertToWords(text.split(" "));
        // �������� �� ���� ����� � ������ �������
        for(int i = 0; i < words.length; i++)
            words[i] = replace(words[i], 0, -1);
        // ������������������ ����, � ������� ����� ���������� ��������������� �������
        byte[] output = new byte[words.length];

        for(int wIndex = 0; wIndex < words.length; wIndex ++){
            // �� ������ ���� ������� �����
            byte[] current = words[wIndex];
            // ������ ������������������ �������� ���� (1, �), ������� ���� ������������ ���������
            // ����� ������� ����� ��� ������
            byte[][] vectors = new byte[m][2];
            // ������ ���� �� m ����� ��� ���������� ������� x
            for(int order = 0; order < m; order ++){
                vectors[order][0] = 1;
                byte[][] S = new byte[2][current.length / 2];
                // ����� ������������������ �� 2 ������: ������ � ��������
                for(int index = 0; index < current.length; index++){
                    if(index % 2 == 0)
                        S[0][index / 2] = current[index];
                    else
                        S[1][index / 2] = current[index];
                }
                // ��������� ��� ������� ������� ������� ����� ��� �������� � �� �������. ����� ���������� �����, ������� - ����
                int sum = 0, dif = 0;
                for(int i = 0; i < S[0].length; i++){
                    S[0][i] = (byte) (S[0][i] + S[1][i]);
                    // �.�. �� ��� ����� �����, �� ����� ����� ��������, ���� �� ����� ������ ������ ������ �������
                    S[1][i] = (byte) (S[0][i] - 2 * S[1][i]);
                    sum += Math.abs(S[0][i]);
                    dif += Math.abs(S[1][i]);
                }
                // ���� ����� ������, �� ��������� ����������� ������� ����� �������, � ������ x ����� ����� 1. ����� -
                // ������ � -1
                current = (sum > dif) ? S[0] : S[1];
                vectors[order][1] = (byte) ((sum > dif) ? 1 : -1);
            }
            // ������� ������� ����� ������������� ���������
            byte[] word = kroneckerProduct(vectors[0], vectors[1]);
            for(int i = 2; i < vectors.length; i++)
                word = kroneckerProduct(word, vectors[i]);
            // ������ -1 �� 0 � ������������������
            word = replace(word, -1, 0);
            /*StringBuilder out = new StringBuilder();
            for(byte ch: word)
                out.append(ch);
            System.out.println(out);*/
            // ������� �������������� �����, ���� �������
            output[wIndex] = findI(word);
        }
        // ������������ ������������������ ���� � ������
        return convertToString(output);
    }
    // ����� ������ ��������������� ����� �� ��������
    private byte findI(byte[] word){
        // ���������� ��� ������� ����� � ���������� �� � �������. ���� ����� �������, �� �� ���� ������ ��������� ���� -
        // �������������� �����
        for (Map.Entry<Integer, byte[]> entry : iTable.entrySet()) {
            if(Arrays.equals(word, entry.getValue())) {
                return (byte)(int)entry.getKey();
            }
        }
        // ���� �� �������, �� ���������� 0
        return 0;
    }
    // ����� ������������ ��������� ����� ����� ���������
    private byte[] kroneckerProduct(byte[] A, byte[] B){
        byte[] output = new byte[A.length * B.length];
        for(int i = 0; i < B.length; i++)
            for (int j = 0; j < A.length; j++)
                output[i * A.length + j] = (byte) (A[j] * B[i]);
        return output;
    }
    // ����� ��������������� ���� �� ���������� ������������� � ��������
    private byte[][] convertToWords(String[] words){
        byte[][] output = new byte[words.length][n];
        for(int i = 0; i < words.length; i++)
            for(int j = 0; j < n; j++)
                output[i][j] = Byte.parseByte(words[i].substring(j, j + 1));

        return output;
    }
    // ����� ��� ��������� ���������� ���� � ��������� ������ �������� ������������������
    private byte[] makeNoise(byte[] word){
        // ��������� ��������� ������� ������ � ��������� �� 0 �� n
        int pos = (int)(Math.random() * n);
   //     System.out.println(pos);
        // ������ ��� � ���� ������� �� ���������������
        word[pos] ^= 1;
        return word;
    }
    //����� ��� ������ ��������� k ����
    private static void findK(){
        // �� �������, � - ����� ���� ��������� ��� i = �� 0 �� r �� m �� i
        k = 1;
        for(int i = 1; i <= r; i++)
            k += comb(m, i);
    }
    // ������� ������ ����� � ������������������ �� -1
    private byte[] replace(byte[] seq, int from, int to){
        byte[] replaced = new byte[seq.length];
        for(int i = 0; i < seq.length; i++)
            replaced[i] = (seq[i] == from) ? (byte) to : seq[i];
        return replaced;
    }
    // ������� ��������������� ������ � ������ ����. ��������� - windows-1251
    private byte[] convertToByte(String input){
        return input.getBytes(Charset.forName("Windows-1251"));
    }
    // ������� ��������������� ������� ���� � ������
    private String convertToString(byte[] bytes){
        return new String(bytes, Charset.forName("Windows-1251"));
    }
    // ������� ��� ���������� ��������� �� n �� k
    private static int comb(int n, int k){
        return fact(n) / (fact(k) * fact(n - k));
    }
    // ������� ���������� ����������
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
