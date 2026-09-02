package model;

import java.math.BigDecimal;
/**
 * 汇款业务类型枚举
 */
public enum BusinessType {
    NORMAL("普通汇款", BigDecimal.valueOf(0.0)),//得到一个BigDecimal对象
    ELECTRONIC("电子汇款", BigDecimal.valueOf(0.0)),
    URGENT("加急汇款", BigDecimal.valueOf(5.0)),
    EXPRESS("特急汇款", BigDecimal.valueOf(10.0));

    private final String displayName;
    private final BigDecimal extraFee;

    BusinessType(String displayName, BigDecimal extraFee) {
        this.displayName = displayName;
        this.extraFee = extraFee;
    }
    public String getDisplayName() {
        return displayName;
    }
    public BigDecimal getExtraFee() {
        return extraFee;
    }
    public static BusinessType fromIndex(int index){
        BusinessType[] values = values();//全部枚举常量，存到数组中返回
        if (index < 1 || index >values.length){
            return null;
        }
        return values[index-1];
    }
}
