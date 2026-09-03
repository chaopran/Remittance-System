package util;

import config.SystemConfig;
import model.Transaction;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
/**
 * CSV文件读写工具
 * 完成交易记录持久化保存与加载
 */
public class FileUtil {
    private FileUtil(){}

    public static void saveRecords(List<Transaction> records){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SystemConfig.DATA_FILE_PATH))) {
            for (Transaction tx : records) {
                String line = String.join(",",
                        tx.getId(), tx.getTime(), tx.getType().name(),
                        tx.getAmount().toPlainString(), tx.getFee().toPlainString(),
                        tx.getExtraFee().toPlainString(), tx.getTotalCost().toPlainString(),
                        tx.getSender(), tx.getReceiver()
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            // 仅打印警告不抛出异常，避免因IO问题中断主业务流程
            System.err.println("[系统警告] 交易记录保存失败：" + e.getMessage());
        }
    }
    /**
     * 从CSV文件加载交易记录列表
     * 文件不存在时返回空列表（非null），调用方无需额外判空。
     * 遇到格式不合规的行会静默跳过，保证已正确写入的数据不受影响。
     * @return 交易记录列表，文件不存在或读取失败时返回空列表
     */
    public static List<Transaction> loadRecords() {
        List<Transaction> records = new ArrayList<>();
        File file = new File(SystemConfig.DATA_FILE_PATH);
        if (!file.exists()) return records;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                //只有长度为9才进行解析
                if (parts.length == 9) {
                    records.add(new Transaction(
                            parts[0], parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7], parts[8]
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("[系统警告] 交易记录加载失败：" + e.getMessage());
        }
        return records;
    }
}
