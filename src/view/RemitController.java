package view;

import model.BusinessType;
import model.Transaction;
import service.AccountService;
import service.FeeCalculator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import java.math.BigDecimal;
import java.util.List;

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

        Transaction tx = accountService.executeRemittance(type,amount,sender,receiver);
        if(tx == null){
            taMsg.setText("汇款执行失败！");
            return;
        }
        taMsg.setText("✅汇款成功！交易编号："+tx.getId());
        tfAmount.clear();
        tfSender.clear();
        tfReceiver.clear();
        queryRecord();
    }

    @FXML
    private void queryRecord(){
        List<Transaction> list = accountService.getRecords();
        ObservableList<Transaction> data = FXCollections.observableArrayList(list);
        tableRecord.setItems(data);
        taMsg.setText("共 "+list.size()+" 条交易记录");
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
        BigDecimal min = new BigDecimal("999999999");

        for(Transaction t : records){
            totalAmount = totalAmount.add(t.getAmount());
            totalFee = totalFee.add(t.getFee());
            totalExtra = totalExtra.add(t.getExtraFee());
            if(t.getAmount().compareTo(max)>0) max = t.getAmount();
            if(t.getAmount().compareTo(min)<0) min = t.getAmount();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("====统计汇总====\n");
        sb.append("总笔数：").append(records.size()).append("\n");
        sb.append("汇款总金额：").append(totalAmount.toPlainString()).append("\n");
        sb.append("汇费总收入：").append(totalFee.toPlainString()).append("\n");
        sb.append("附加费总收入：").append(totalExtra.toPlainString()).append("\n");
        sb.append("最大单笔：").append(max.toPlainString()).append("\n");
        sb.append("最小单笔：").append(min.toPlainString());
        taMsg.setText(sb.toString());
    }
}
