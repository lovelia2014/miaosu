package com.miaosu.flux.bills;

import com.miaosu.flux.base.ResultCode;
import com.miaosu.flux.base.ServiceException;
import com.miaosu.flux.bills.enums.BillChannel;
import com.miaosu.flux.bills.enums.BillStatus;
import com.miaosu.flux.bills.enums.BillType;
import com.miaosu.flux.orders.OrderRepository;
import com.miaosu.flux.orders.enums.PayState;
import com.miaosu.flux.system.SerialNoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 账务Service
 * Created by angus on 15/10/4.
 */
@Service
@Transactional(timeout = 10)
public class AcctService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BalanceDao balanceDao;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private SerialNoUtil serialNoUtil;

    /**
     * 账单入账接口
     * @param billId 账单ID
     */
    public void checkin(String billId) {
        // 查找账单
        Bill bill = billRepository.findOne(billId);
        if (bill == null) {
            throw new ServiceException(ResultCode.DATA_NOT_EXISTS);
        }

        // 只允许失败的订单入账
        if (BillStatus.FAILED.equals(bill.getStatus())) {
            int rows = 0;
            switch (bill.getType()) {
            case ADD:
                rows = balanceDao.add(bill.getUsername(), bill.getAmt());
                if (rows == 0) {
                    throw new ServiceException(ResultCode.FAILED, "用户" + bill.getUsername() + "不存在");
                }
                billRepository.updateBillStatus(billId, BillStatus.SUCCESS);
                break;
            case SUBTRACTION:
                rows = balanceDao.subtract(bill.getUsername(), bill.getAmt().abs());
                if (rows == 0) {
                    throw new ServiceException(ResultCode.FAILED, "用户" + bill.getUsername() + "不存在或余额不足");
                }
                billRepository.updateBillStatus(billId, BillStatus.SUCCESS);
                break;
            }
        }
    }

    /**
     * 充值接口
     * @param userName 会员名
     * @param amount 金额
     * @param info 备注信息
     */
    public void recharge(String userName, BigDecimal amount, String info) {
        Assert.isTrue(amount != null && amount.compareTo(BigDecimal.ZERO) >= 0, "amount参数错误");
        String billId = serialNoUtil.genrateBillNo();
        billRepository.save(new Bill(billId, userName, amount, null, BillType.ADD, BillChannel.RECHARGE, info,
                BillStatus.INIT, new Date()));
        int rows = balanceDao.add(userName, amount);
        if (rows <= 0) {
            throw new ServiceException(ResultCode.FAILED, "用户[" + userName + "]不存在");
        }
        billRepository.updateBillStatus(billId, BillStatus.SUCCESS);
    }

    /**
     * 扣款接口
     * @param userName 会员名
     * @param amount 金额
     * @param info 备注信息
     */
    public void deduct(String userName, BigDecimal amount, String info) {
        Assert.isTrue(amount != null && amount.compareTo(BigDecimal.ZERO) >= 0, "amount参数错误");
        Balance balance = balanceRepository.findOne(userName);
        if (balance.getBalance().compareTo(amount) < 0) {
            throw new ServiceException(ResultCode.FAILED, "用户[" + userName + "]余额不足");
        }
        String billId = serialNoUtil.genrateBillNo();
        billRepository.save(new Bill(billId, userName, amount.negate(), null, BillType.SUBTRACTION, BillChannel.OTHERS,
                info, BillStatus.INIT, new Date()));
        int rows = balanceDao.subtract(userName, amount);
        if (rows <= 0) {
            throw new ServiceException(ResultCode.FAILED, "用户[" + userName + "]不存在");
        }
        billRepository.updateBillStatus(billId, BillStatus.SUCCESS);
    }

    /**
     * 订单退款接口
     * @param userName 用户姓名
     * @param billId 流水ID
     * @param orderId 订单编号
     * @return 结果码；0： 成功；1：失败；2：退款单不存在；3：退款单状态异常
     */
    public int refund(String userName, String billId, String orderId) {
        // 查找账单
        Bill bill = billRepository.findOne(billId);
        if (bill == null) {
            return 2;
        }

        switch (bill.getStatus()) {
        case SUCCESS:
            return 0;
        case PROCESS:
            return 3;
        case INIT:
        case FAILED:
            BigDecimal amount = bill.getAmt().abs();
            // 退款
            int rows = balanceDao.add(userName, amount);
            // 刷新订单支付状态为已退款
            if (rows == 0) {
                billRepository.updateBillStatus(billId, BillStatus.FAILED);
                orderRepository.updatePayStatus(orderId, PayState.REFUND_PROCESS, null);
            } else {
                billRepository.updateBillStatus(billId, BillStatus.SUCCESS);
                orderRepository.updatePayStatus(orderId, PayState.REFUNDED, null);
                return 0;
            }
        default:
            return 3;
        }
    }

    /**
     * 订单支付接口
     * @return 结果码；0：成功；1：余额不足；2: 支付单号不存在；3: 支付单状态异常
     */
    public int payment(String userName, String billId, String orderId) {

        // 查找账单
        Bill bill = billRepository.findOne(billId);
        if (bill == null) {
            return 2;
        }

        switch (bill.getStatus()) {
        case SUCCESS:
            return 0;
        case PROCESS:
            return 3;
        case INIT:
        case FAILED:
            BigDecimal amount = bill.getAmt().abs();
            // 扣款
            int rows = balanceDao.subtract(userName, amount);
            // 刷新订单支付状态
            if (rows == 0) {
                billRepository.updateBillStatus(billId, BillStatus.FAILED);
                orderRepository.updatePayStatus(orderId, PayState.FAILED, "余额不足");
                return 1;
            } else {
                billRepository.updateBillStatus(billId, BillStatus.SUCCESS);
                orderRepository.updatePayStatus(orderId, PayState.SUCCESS, null);
                return 0;
            }
        default:
            return 3;
        }
    }
}
