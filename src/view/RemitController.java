package view;

import model.BusinessType;
import model.Transaction;
import service.AccountService;
import service.FeeCalculator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class RemitController {

    private final AccountService accountService = new AccountService();

    @FXML
    private ComboBox<BusinessType> cbBusinessType;

    @FXML
    private TextField tfAmount;
    @FXML
    private TextField tfSender;
    @FXML
    private TextField tfReceiver;

    @FXML
    private TextArea taMsg;

    @FXML
    private TableView<Transaction> tableRecord;
    @FXML private TableColumn<Transaction,String> colId;
    @FXML private TableColumn<Transaction,String> colTime;
    @FXML private TableColumn<Transaction,String> colType;
    @FXML private TableColumn<Transaction,BigDecimal> colAmount;
    @FXML private TableColumn<Transaction,BigDecimal> colFee;
    @FXML private TableColumn<Transaction,BigDecimal> colExtra;
    @FXML private TableColumn<Transaction,BigDecimal> colTotal;
    @FXML private TableColumn<Transaction,String> colSender;
    @FXML private TableColumn<Transaction,String> colReceiver;

    @FXML
    public void initialize(){
        //下拉框填充业务类型
        cbBusinessType.getItems().addAll(BusinessType.values());
        cbBusinessType.setValue(BusinessType.NORMAL);

        // 下拉框显示中文业务名称
        Callback<ListView<BusinessType>, ListCell<BusinessType>> cellFactory = param -> new ListCell<BusinessType>() {
            @Override
            protected void updateItem(BusinessType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        };
        cbBusinessType.setCellFactory(cellFactory);
        cbBusinessType.setButtonCell(cellFactory.call(null));

        //表格字段绑定
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType().getDisplayName())
        );
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colFee.setCellValueFactory(new PropertyValueFactory<>("fee"));
        colExtra.setCellValueFactory(new PropertyValueFactory<>("extraFee"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colSender.setCellValueFactory(new PropertyValueFactory<>("sender"));
        colReceiver.setCellValueFactory(new PropertyValueFactory<>("receiver"));
    }

    @FXML
    private void doRemit(){
        taMsg.clear();
        BusinessType type = cbBusinessType.getValue();
        String amountStr = tfAmount.getText().trim();
        String sender = tfSender.getText().trim();
        String receiver = tfReceiver.getText().trim();

        // 输入校验
        if(sender.isBlank() || receiver.isBlank()){
            taMsg.setText("错误：汇款人、收款人不能为空！");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        }catch (Exception e){
            taMsg.setText("错误：金额格式不正确！");
            return;
        }

        if(!FeeCalculator.isAmountValid(amount)){
            taMsg.setText("错误：汇款金额必须1~50000元！");
            return;
        }

        // 预先计算费用，用于确认弹窗
        BigDecimal fee = FeeCalculator.calculateFee(amount);
        BigDecimal extraFee = type.getExtraFee();
        BigDecimal totalCost = amount.add(fee).add(extraFee);

        // 汇款确认弹窗
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认汇款");
        confirmAlert.setHeaderText("请核对汇款信息");
        String confirmContent = String.format(
                "业务类型：%s\n汇款金额：%s 元\n基础汇费：%s 元\n附加服务费：%s 元\n总费用：%s 元\n汇款人：%s\n收款人：%s",
                type.getDisplayName(),
                amount.toPlainString(),
                fee.toPlainString(),
                extraFee.toPlainString(),
                totalCost.toPlainString(),
                sender,
                receiver
        );
        confirmAlert.setContentText(confirmContent);
        confirmAlert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if(result.isEmpty() || result.get() != ButtonType.OK){
            taMsg.setText("已取消本次汇款");
            return;
        }

        // 执行汇款
        Transaction tx = accountService.executeRemittance(type,amount,sender,receiver);
        if(tx == null){
            taMsg.setText("汇款执行失败！");
            return;
        }

        // 成功提示：仅显示当前这笔订单
        String successMsg = String.format(
                "✅ 汇款办理成功！\n\n交易编号：%s\n业务类型：%s\n汇款金额：%s 元\n基础汇费：%s 元\n附加服务费：%s 元\n总费用：%s 元\n汇款人：%s\n收款人：%s",
                tx.getId(),
                tx.getType().getDisplayName(),
                tx.getAmount().toPlainString(),
                tx.getFee().toPlainString(),
                tx.getExtraFee().toPlainString(),
                tx.getTotalCost().toPlainString(),
                tx.getSender(),
                tx.getReceiver()
        );
        taMsg.setText(successMsg);

        // 清空输入框
        tfAmount.clear();
        tfSender.clear();
        tfReceiver.clear();

        // ========== 核心修改：表格仅显示刚办理的这一条新订单，不显示全部 ==========
        ObservableList<Transaction> newOrder = FXCollections.observableArrayList(tx);
        tableRecord.setItems(newOrder);
    }

    @FXML
    private void queryRecord(){
        List<Transaction> list = accountService.getRecords();
        ObservableList<Transaction> data = FXCollections.observableArrayList(list);
        tableRecord.setItems(data);
        taMsg.setText("共查询到 "+list.size()+" 条历史交易记录");
    }

    @FXML
    private void showStat(){
        List<Transaction> records = accountService.getRecords();
        if(records.isEmpty()){
            taMsg.setText("暂无交易记录，无法统计");
            return;
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal totalExtra = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        BigDecimal min = new BigDecimal("99999999");

        for(Transaction t : records){
            totalAmount = totalAmount.add(t.getAmount());
            totalFee = totalFee.add(t.getFee());
            totalExtra = totalExtra.add(t.getExtraFee());
            if(t.getAmount().compareTo(max)>0) max = t.getAmount();
            if(t.getAmount().compareTo(min)<0) min = t.getAmount();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("==== 统计汇总 ====\n");
        sb.append("总笔数：").append(records.size()).append("\n");
        sb.append("汇款总金额：").append(totalAmount.toPlainString()).append(" 元\n");
        sb.append("汇费总收入：").append(totalFee.toPlainString()).append(" 元\n");
        sb.append("附加费总收入：").append(totalExtra.toPlainString()).append(" 元\n");
        sb.append("最大单笔：").append(max.toPlainString()).append(" 元\n");
        sb.append("最小单笔：").append(min.toPlainString()).append(" 元");
        taMsg.setText(sb.toString());
    }
}
