package model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 交易记录实体类
 * 保存每一笔汇款的全部信息
 */
public class Transaction {
    private String id;
    private String time;
    private BusinessType type;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal extraFee;
    private BigDecimal totalCost;
    private String sender;
    private String receiver;
    // 新建交易构造器：自动生成时间、计算总费用
    public Transaction(String id,BusinessType type, BigDecimal amount, BigDecimal fee, BigDecimal extraFee, String sender, String receiver) {
        this.id = id;
        this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//自动获取时间并格式化
        this.type = type;
        this.amount = amount;
        this.fee = fee;
        this.extraFee = extraFee;
        this.totalCost = amount.add(fee).add(extraFee);
        this.sender = sender;
        this.receiver = receiver;
    }
    // 文件加载构造器：全字符串入参，内部完成类型转换
    public Transaction(String id,String time,String typeStr,String amount,String fee,String extraFee,String totalCost,String sender,String receiver) {
        this.id = id;
        this.time = time;
        this.type = BusinessType.valueOf(typeStr);
        this.amount = new BigDecimal(amount);
        this.fee = new BigDecimal(fee);
        this.extraFee = new BigDecimal(extraFee);
        this.totalCost = new BigDecimal(totalCost);
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getId() {
        return id;
    }
    public String getTime() {
        return time;
    }
    public BusinessType getType() {
        return type;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public BigDecimal getFee() {
        return fee;
    }
    public BigDecimal getExtraFee() {
        return extraFee;
    }
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    public String getSender() {
        return sender;
    }
    public String getReceiver() {
        return receiver;
    }
    @Override
    public String toString(){
        return String.format(
                "|%-6s|%-20s|%-8s|%10.2f|%8.2f|%8.2f|%10.2f|%-6s|%-6s|",
                id,time,type.getDisplayName(),amount,fee,extraFee,totalCost,sender,receiver
        );
    }
}
