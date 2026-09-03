package service;

import config.SystemConfig;
import model.BusinessType;
import model.Transaction;
import util.FileUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
/**
 * 交易业务服务类
 * 管理交易记录、执行汇款业务、维护流水号
 */
public class AccountService {
    private int transactionCount;//交易次数
    private final List<Transaction> records;//交易记录

    public AccountService() {
        this.records = new ArrayList<>();
        //自动读取文件
        List<Transaction> loadedRecords = FileUtil.loadRecords();
        if (!loadedRecords.isEmpty()) {
            this.records.addAll(loadedRecords);
            this.transactionCount = loadedRecords.size();
        } else {
            this.transactionCount = 0;
        }
    }
    public int getTransactionCount() {
        return transactionCount;
    }
    public List<Transaction> getRecords() {
        return records;
    }
    /**
     * 执行汇款业务
     * 完整流程：计算费用 → 生成流水号 → 构建交易对象 →
     * 追加到内存列表 → 持久化到CSV文件 → 返回交易凭证
     * @param type     业务类型（决定附加费）
     * @param amount   汇款金额（已通过上层校验）
     * @param sender   汇款人姓名
     * @param receiver 收款人姓名
     * @return 成功创建的交易对象；理论上不会返回null（异常由上层catch兜底）
     */
    public Transaction executeRemittance(BusinessType type, BigDecimal amount, String sender, String receiver) {
        BigDecimal fee = FeeCalculator.calculateFee(amount);
        BigDecimal extraFee = type.getExtraFee();
        BigDecimal totalCost =amount.add(fee).add(extraFee);
        transactionCount++;
        String txId = String.format("TX%06d",transactionCount);//生成格式化流水号
        Transaction tx = new Transaction(txId,type,amount,fee,extraFee,sender,receiver);
        records.add(tx);
        FileUtil.saveRecords(records);
        return tx;
    }
}