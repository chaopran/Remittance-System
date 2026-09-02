package util;

import java.math.BigDecimal;
import java.util.Scanner;
/**
 * 控制台输入工具类
 * 封装所有键盘输入，完成基础格式校验
 */
public class InputUtil {
    private static final Scanner scanner = new Scanner(System.in);
    private InputUtil() {}
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();// 去除输入字符串两端的空格
    }
    // 读取用户输入的整数
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("输入无效，请输入纯数字！");
            }
        }
    }
    // 读取用户输入的正整数
    public static BigDecimal readPositiveBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                BigDecimal val = new BigDecimal(scanner.nextLine().trim());
                if (val.compareTo(BigDecimal.ZERO) > 0) {
                    return val;
                }
                System.out.println("金额必须大于0！");
            } catch (NumberFormatException e) {
                System.out.println("输入无效，请输入正确的金额（如：100.50）！");
            }
        }
    }
}
