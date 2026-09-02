import model.BusinessType;
import model.Transaction;
import service.AccountService;
import service.FeeCalculator;
import util.InputUtil;
import java.math.BigDecimal;
import java.util.List;

public class Main {
    private static final AccountService accountService = new AccountService();
    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("                        电子汇兑系统                       ");
        System.out.println("========================================================");
        System.out.println();
        boolean running = true;
        while (running) {
            try {
                showMenu();
                String choice = InputUtil.readLine("请选择操作：");
                switch (choice) {
                    case "1": doRemittance();break;
                    case "2": queryRecords();break;
                    case "3": showStatistics();break;
                    case "0": System.out.println("\n感谢使用电子汇兑系统，再见！");
                        running = false;
                        break;
                    default:
                        System.out.println("输入无效，请重新选择！");
                }
            } catch (Exception e) {
                // 全局异常捕获，防止程序因为意外错误直接崩溃
                System.err.println("\n[系统异常] 发生未知错误：" + e.getMessage());
                System.out.println("请重新操作。\n");
            }
        }
    }
    private static void showMenu() {
        System.out.println("--------------------------------------------------------");
        System.out.println("  1. 办理汇款   2. 查询交易记录   3. 统计汇总   0. 退出系统");
        System.out.println("--------------------------------------------------------");
    }
    private static void doRemittance() {
        System.out.println("\n========== 办理汇款 ==========");
        BusinessType[] types = BusinessType.values();//全部枚举常量，存到数组中返回
        System.out.println("请选择业务类型：");
        for (int i = 0; i < types.length; i++) {
            String extra = types[i].getExtraFee().compareTo(BigDecimal.ZERO) > 0
                    ? "（附加服务费：" + types[i].getExtraFee() + "元）" : "";
            System.out.println("  " + (i + 1) + ". " + types[i].getDisplayName() + extra);
        }
        int typeIndex = InputUtil.readInt("请输入编号（1-4）：");
        BusinessType type = BusinessType.fromIndex(typeIndex);
        if (type == null) {
            System.out.println("无效的业务类型！");
            return;
        }
        BigDecimal amount = InputUtil.readPositiveBigDecimal("请输入汇款金额（1~50000元）：");
        if (!FeeCalculator.isAmountValid(amount)) {
            System.out.println("汇款金额必须在 1.0 ~ 50000.0 元之间！");
            return;
        }
        String sender = InputUtil.readLine("请输入汇款人姓名：");
        String receiver = InputUtil.readLine("请输入收款人姓名：");
        BigDecimal fee = FeeCalculator.calculateFee(amount);
        BigDecimal extraFee = type.getExtraFee();
        BigDecimal totalCost = amount.add(fee).add(extraFee);
        System.out.println("\n---------- 交易确认 ----------");
        System.out.println("  业务类型：" + type.getDisplayName());
        System.out.printf("  汇款金额：%s 元%n", amount.toPlainString());
        System.out.printf("  汇    费：%s 元%n", fee.toPlainString());
        if (extraFee.compareTo(BigDecimal.ZERO) > 0) {
            System.out.printf("  附加服务费：%s 元%n", extraFee.toPlainString());
        }
        System.out.printf("  总费用：%s 元%n", totalCost.toPlainString());
        System.out.println("  汇 款 人：" + sender);
        System.out.println("  收 款 人：" + receiver);
        System.out.println("-------------------------------");
        String confirm = InputUtil.readLine("确认办理？（y/n）：");
        if (!"y".equalsIgnoreCase(confirm)) {
            System.out.println("已取消交易。");
            return;
        }
        Transaction tx = accountService.executeRemittance(type, amount, sender, receiver);
        if(tx == null){
            System.out.println("汇款执行失败！");
            return;
        }
        System.out.println("\n交易成功！");
        System.out.println("  交易编号：" + tx.getId());
    }
    private static void queryRecords() {
        System.out.println("\n========== 交易记录 ==========");
        List<Transaction> records = accountService.getRecords();
        if (records.isEmpty()) {
            System.out.println("暂无交易记录。");
            return;
        }
        System.out.println(String.format("| %-6s | %-20s | %-8s | %10s | %8s | %8s | %10s | %-6s | %-6s |",
                "编号", "时间", "类型", "汇款金额", "汇费", "附加费", "总费用", "汇款人", "收款人"));
        System.out.println(new String(new char[120]).replace('\0', '-'));
        for (Transaction tx : records) System.out.println(tx);
        System.out.println("共 " + records.size() + " 条记录。");
    }
    private static void showStatistics() {
        System.out.println("\n========== 统计汇总 ==========");
        List<Transaction> records = accountService.getRecords();
        if (records.isEmpty()) {
            System.out.println("暂无交易记录，无法统计。");
            return;
        }
        BigDecimal totalAmount = BigDecimal.ZERO, totalFee = BigDecimal.ZERO, totalExtra = BigDecimal.ZERO;
        BigDecimal maxAmount = BigDecimal.ZERO, minAmount = new BigDecimal("999999999");
        for (Transaction tx : records) {
            totalAmount = totalAmount.add(tx.getAmount());
            totalFee = totalFee.add(tx.getFee());
            totalExtra = totalExtra.add(tx.getExtraFee());
            if (tx.getAmount().compareTo(maxAmount) > 0) maxAmount = tx.getAmount();
            if (tx.getAmount().compareTo(minAmount) < 0) minAmount = tx.getAmount();
        }
        System.out.println("  交易总笔数：" + records.size());
        System.out.printf("  汇款总金额：%s 元%n", totalAmount.toPlainString());
        System.out.printf("  汇费总收入：%s 元%n", totalFee.toPlainString());
        System.out.printf("  附加费总收入：%s 元%n", totalExtra.toPlainString());
        System.out.printf("  最大单笔金额：%s 元%n", maxAmount.toPlainString());
        System.out.printf("  最小单笔金额：%s 元%n", minAmount.toPlainString());
    }
}