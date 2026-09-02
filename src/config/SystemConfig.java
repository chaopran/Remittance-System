package config;

import java.math.BigDecimal;
/**
 * 系统金额配置类，包含金额的最小值、最大值、手续费率、最小手续费、最大手续费和数据文件路径等常量。
 */
public class SystemConfig {
    public static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(1.0);
    public static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(50000.0);

    public static final BigDecimal FEE_RATE = BigDecimal.valueOf(0.01);
    public static final BigDecimal MIN_FEE = BigDecimal.valueOf(2.0);
    public static final BigDecimal MAX_FEE = BigDecimal.valueOf(50.0);

    public static final String DATA_FILE_PATH = "records.csv";

    private SystemConfig() {
    }//禁止实例化
}
