package org.kramerlab.bmad.scripts;

public class BitManipulation {
    public static void main(String... args) throws Throwable{
        byte b1 = (byte) 8;
        String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
        for (int i = 0; i < s1.length(); i++) {
            System.out.print(s1.charAt(i)); // 10000001
            if (s1.charAt(i) == '1') {
                System.out.println("= one");
            } else {
                System.out.println("= zero");
            }
        }
    }
}
