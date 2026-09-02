package service;

import config.SystemConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
/**
 * 汇费计算工具类
 * 实现汇费计算、汇款金额范围校验
 */
public class FeeCalculator {
    private FeeCalculator(){
    }
    public static BigDecimal calculateFee(BigDecimal amount){
        BigDecimal fee = amount.multiply(SystemConfig.FEE_RATE);
        // 保留2位，四舍五入
        fee = fee.setScale(2, RoundingMode.HALF_UP);
        if(fee.compareTo(SystemConfig.MIN_FEE)<0){
            fee = SystemConfig.MIN_FEE;
        }
        if(fee.compareTo(SystemConfig.MAX_FEE)>0){
            fee = SystemConfig.MAX_FEE;
        }
        return fee;
    }
    public static boolean isAmountValid(BigDecimal amount){
        return amount.compareTo(SystemConfig.MIN_AMOUNT)>=0 && amount.compareTo(SystemConfig.MAX_AMOUNT)<=0;
    }
}
