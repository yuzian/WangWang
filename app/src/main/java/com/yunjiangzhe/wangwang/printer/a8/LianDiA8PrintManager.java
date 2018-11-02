package com.yunjiangzhe.wangwang.printer.a8;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;

import com.qiyu.ble.BLE;
import com.qiyu.ble.PrintUtils;
import com.qiyu.share.Share;
import com.qiyu.util.CopyObjectUtil;
import com.qiyu.util.DateUtils;
import com.qiyu.util.L;
import com.qiyu.util.NumberFormat;
import com.qiyu.util.PackageDetailUtils;
import com.qiyu.util.StringUtil;
import com.yunjiangzhe.wangwang.R;
import com.yunjiangzhe.wangwang.base.AppEnumHelp;
import com.yunjiangzhe.wangwang.bean.ServiceTextBean;
import com.yunjiangzhe.wangwang.match.IPrintManager;
import com.yunjiangzhe.wangwang.response.bean.OrderDetail;
import com.yunjiangzhe.wangwang.response.bean.OrderMain;
import com.yunjiangzhe.wangwang.response.bean.PayWayBean;
import com.yunjiangzhe.wangwang.response.bean.PreferentialDetail;
import com.yunjiangzhe.wangwang.response.bean.QRCodeBean;
import com.yunjiangzhe.wangwang.response.data.OrderCollectData;
import com.yunjiangzhe.wangwang.response.data.OrderSummaryData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.qiyu.util.App.getStr;
import static com.qiyu.util.NumberFormat.dTs;

public class LianDiA8PrintManager extends IPrintManager {
    private int length_1 = 20;
    private int length_2 = 6;
    private int length_3 = 10;
    public final static int REQUEST_CODE_PRINT_ONE = 100;

    public final static int REQUEST_CODE_PRINT_TWO = 101;

    private boolean isFrist = true;  //是否是打印第一联

    private static LianDiA8PrintManager instance;
    private static final int Restaurant_Name_FontSize = 2;//设置打印餐馆名称的字体大小
    private static final int DESK_Num_FontSize = 5;//设置排队号码的字体大小
    private static final String MTPackageName = "com.sankuai.poscashier";
    private static final String MTPrintName = "com.sankuai.poscashier.activity.PrintActivity";

    public static LianDiA8PrintManager getInstance()
    {
        if (instance == null)
        {
            synchronized (LianDiA8PrintManager.class)
            {
                if (instance == null)
                {
                    instance = new LianDiA8PrintManager();
                }
            }
        }
        return instance;
    }


    /**
     * 排队打印
     *
     * @param activity
     * @param restaurantName 餐馆名
     * @param number         当前号码
     * @param wait           等待人数
     */
    @Override
    public void printListNumber(Activity activity, String restaurantName, String number, int wait)
    {
        showDialog(activity);
        //设置打印JSONArray数据
        JSONArray arr = new JSONArray();
        try
        {
            //打印餐馆名
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName
                    (restaurantName)));
            //打印前半内容
            String content1 = "-------------------------------";
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content1));//分割线

            String content2 = "您的排队号码为:";
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content2));

            //打印号码
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            String numStr = "";
            int numSpace = 8 - number.length() + 2;
            for (int i = 0; i < numSpace; i++)
            {
                numStr += " ";
            }
            numStr += number + "号";
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", DESK_Num_FontSize).put("content", numStr));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));

            //打印后半内容
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content1));//分割线

            String content4 = "您的前面还有 " + wait + " 人在等待";
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content4));

            String content5 = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm");
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content5));

            String content6 = "请耐心留意叫号  过号作废";
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content6));

            for (int i = 0; i < 5; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MTPackageName, MTPrintName));
            intent.putExtra("lines", arr.toString());
            activity.startActivity(intent);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 加菜单打印 -- 蓝牙打印机
     *
     * @param activity
     * @param isShowDialog
     * @param mOrderMain   订单基本信息
     * @param printTimes   打印次数
     */
    @Override
    public void printAddOrder(Activity activity, boolean isShowDialog, OrderMain mOrderMain, int printTimes)
    {
        if (isShowDialog)
        {
            showDialog(activity);
        }

        OrderMain orderMain = (OrderMain) CopyObjectUtil.copy(mOrderMain);

        printOrderOrAddOrder(orderMain, printTimes, 1);
  /*       BLE.get().init().connectDeviceByAddr();
       for (int i = 1; i <= printTimes; i++)
            printAddOrderDataForBlue(orderMain);*/

        //        JSONArray arr = new JSONArray();
        //        for (int i = 1; i <= printTimes; i++)
        //        {
        //            //设置打印JSONArray数据
        //            printAddOrderData(arr, orderMain);
        //        }
        //        Intent intent = new Intent();
        //        intent.setComponent(new ComponentName(MTPackageName, MTPrintName));
        //        intent.putExtra("lines", arr.toString());
        //        activity.startActivityForResult(intent, REQUEST_CODE_PRINT_TWO);

    }

    /**
     * 打印加菜单模板 -- 蓝牙
     */
    private void printAddOrderDataForBlue(OrderMain mOrderMain, PrintUtils printUtils)
    {
        OrderMain orderMain = (OrderMain) CopyObjectUtil.copy(mOrderMain);
        //将订单详情替换成含套餐明细的详情
        List<OrderDetail> printData = PackageDetailUtils.getShowDetails(orderMain.getOrderDetailModelList());
        orderMain.setOrderDetailModelList(printData);

        List<OrderDetail> orderDetails = new ArrayList<>();
        orderDetails.clear();
        orderDetails.addAll(orderMain.getOrderDetailModelList());
        boolean isChange = false;// 是否更换桌号和订单尾号打印的位置
        if (Share.get().getIsCall() == AppEnumHelp.IS_CALL_1.getValue())
        {
            isChange = true;
        }
        //打印餐馆名
        String restaurantName = (TextUtils.isEmpty(orderMain.getRestaurantName()) ? "旗鱼点餐" : orderMain.getRestaurantName());

        printUtils.selectCommand(PrintUtils.ALIGN_CENTER);
        printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
        printUtils.printText(restaurantName);

        printUtils.selectCommand(PrintUtils.NORMAL);

        printUtils.printText("\n\n加菜单\n\n");
        printUtils.selectCommand(PrintUtils.NORMAL);
        printUtils.selectCommand(PrintUtils.ALIGN_LEFT);

        String desk = "";

        if (!TextUtils.isEmpty(orderMain.getMainDesk()) && !orderMain.getMainDesk().equals("null") && !orderMain.getMainDesk().equals("0"))
        {
            desk = "桌号: " + orderMain.getMainDesk();
        }

        String userName = "下单员: " + orderMain.getCreaterName();

        if (isChange)
        {
            printUtils.printText(PrintUtils.get().printTwoData(32, userName, "订单尾号: " + orderMain.getTailNo()) + "\n");
        }
        else
        {
            int totalLen = StringUtil.getLengthForInputStr(userName + desk);// 下单员 和 桌号 的字符总长度
            if (totalLen > 30)
            {
                printUtils.printText(userName + "\n");
                printUtils.printText(desk + "\n");
            }
            else
            {
                printUtils.printText(PrintUtils.get().printTwoData(32, userName, desk) + "\n");
            }
        }
        //分割线
        String content = "--------------------------------\n";
        printUtils.printText(content);

        String personNumber = "";

        if (orderMain.getMainGuests() > 0)
        {
            personNumber = "用餐人数: " + orderMain.getMainGuests();
        }

        if (isChange)
        {
            if (!TextUtils.isEmpty(desk) && !TextUtils.isEmpty(personNumber))
            {
                if (!TextUtils.isEmpty(desk) && !TextUtils.isEmpty(personNumber))
                {
                    int totalLen = StringUtil.getLengthForInputStr(desk + personNumber);
                    if (totalLen > 30)
                    {
                        printUtils.printText(desk + "\n");
                        printUtils.printText(personNumber + "\n");
                    }
                    else
                    {
                        printUtils.printText(PrintUtils.get().printTwoData(32, desk, personNumber) + "\n");
                    }
                }
            }
            else if (TextUtils.isEmpty(desk) && TextUtils.isEmpty(personNumber))
            {
                // 不作处理，两个都为空此行不打印
            }
            else
            {
                printUtils.printText(desk + personNumber + "\n");
            }
        }
        else
        {
            printUtils.printText(PrintUtils.get().printTwoData(32, "订单尾号: " + orderMain.getTailNo(), personNumber) + "\n");
        }

        //没有桌号 也没有用餐人数 直接打印
        printUtils.printText("下单时间: " + DateUtils.formatDateTime(orderMain.getCreateAt(), "yyyy/MM/dd HH:mm") + "\n");

        if (orderMain.getMainStatus() == 2)
        {
            printUtils.printText("付款时间: " + DateUtils.formatDateTime(orderMain.getPayTime(), "yyyy/MM/dd HH:mm") + "\n");
        }

        if (!TextUtils.isEmpty(orderMain.getPackageRemark()))
        {
            printUtils.printText("属性: " + orderMain.getMainRemark() + "\n");
        }
        printUtils.printText(content);//分割线

        printUtils.printText("品名              数量     总价\n");

        // 打印菜品列表
        for (OrderDetail bean : orderDetails)
        {
            printXiaoFeiOrJieZhangDan(printUtils, bean, 1); // 加菜单整单打印，使用大号字体打印菜品列表（即传值mainStatus不等于2）
        }

        printUtils.selectCommand(PrintUtils.NORMAL);
        printUtils.selectCommand(PrintUtils.ALIGN_LEFT);

        if (orderMain.getMainStatus() != 2) // 打印消费单的 备注、菜品数量、合计
        {
            printUtils.selectCommand(PrintUtils.NORMAL);
            printUtils.printText(content);//分割线

            if (!TextUtils.isEmpty(orderMain.getMainRemark()))
            {
                printUtils.printText("订单备注: " + orderMain.getMainRemark() + "\n");
            }

            printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
            printUtils.printText("菜品数量: " + orderMain.getFoodCountTotal() + "\n");
            printUtils.printText("金额合计: " + dTs(orderMain.getOriginalMoney()) + "\n");
        }

        printUtils.selectCommand(PrintUtils.NORMAL);
        printUtils.printText("\n\n\n\n\n\n");
    }

    //加菜单菜品列表
    private void printAddOrderFoodsForBlue(List<OrderDetail> orderDetailModelList, PrintUtils printUtils)
    {
        for (OrderDetail bean : orderDetailModelList)
        {
            printUtils.printText(printSingleFood(length_1, length_2, length_3, bean) + "\n");
        }
    }


    /**
     * 订单打印 -- 蓝牙打印机
     *
     * @param activity
     * @param isShowDialog
     * @param orderMain    订单详情
     * @param printTimes   打印次数
     */
    @Override
    public void printOrder(Activity activity, boolean isShowDialog, OrderMain orderMain, int printTimes)
    {
        if (isShowDialog)
        {
            showDialog(activity);
        }

        printOrderOrAddOrder(orderMain, printTimes, 2);
    }

    /** 美团打印 */
    private void printOrderOrAddOrder(OrderMain orderMain, int printTimes, int orderOrAddOrder)
    {
        Observable.just("").subscribeOn(Schedulers.newThread()).flatMap(new Func1<String, Observable<Boolean>>()
        {
            @Override
            public Observable<Boolean> call(String s)
            {
                return Observable.just(BLE.get().init().connectDeviceByAddr());
            }
        }).unsubscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(ss ->
        {
            if (ss)
            {
                try
                {
                    OutputStream stream = BLE.get().mtBtSocket.getOutputStream();
                    //                    PrintUtils printUtils = new PrintUtils();
                    PrintUtils printUtils = PrintUtils.get();
                    printUtils.setOutputStream(stream);

                    if (orderOrAddOrder == 2)
                    // 打印
                    {
                        for (int i = 1; i <= printTimes; i++)
                        {
                            printOrderDataForBlue(orderMain, printUtils);
                        }
                    }
                    else if (orderOrAddOrder == 1)
                    {
                        for (int i = 1; i <= printTimes; i++)
                        {
                            printAddOrderDataForBlue(orderMain, printUtils);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {

            }

        });
    }

//    /** 蓝牙打印机打印订单 */
//    public void printOrder(OrderMain orderMain, int printTimes, PrintUtils printUtils)
//    {
//        for (int i = 1; i <= printTimes; i++)
//        {
//            printOrderDataForBlue(orderMain, printUtils);
//        }
//    }

    /**
     * 合并订单打印
     */
    @Override
    public void printOrders(Activity activity, boolean isShowDialog, List<OrderMain> data)
    {
        L.e("合并订单合并订单合并订单合并订单合并订单合并订单合并订单合并订单合并订单");
        if (isShowDialog)
        {
            showDialog(activity);
        }
        for (int i = 0; i < data.size(); i++)
        {
            printOrderOrAddOrder(data.get(i), 1, 2);
        }
    }

    /**
     * 订单打印模板
     */
    private void printOrderDataForBlue(OrderMain mOrderMain, PrintUtils printUtils)
    {
        OrderMain orderMain = (OrderMain) CopyObjectUtil.copy(mOrderMain);
        //将订单详情替换成含套餐明细的详情
        List<OrderDetail> printData = PackageDetailUtils.getShowDetails(orderMain.getOrderDetailModelList());
        orderMain.setOrderDetailModelList(printData);

        List<OrderDetail> orderDetails = new ArrayList<>();
        orderDetails.clear();
        orderDetails.addAll(orderMain.getOrderDetailModelList());
        boolean isChange = false;// 是否更换桌号和订单尾号打印的位置
        if (Share.get().getIsCall() == AppEnumHelp.IS_CALL_1.getValue())
        {
            isChange = true;
        }
        //打印餐馆名
        String restaurantName = (TextUtils.isEmpty(orderMain.getRestaurantName()) ? "旗鱼点餐" : orderMain.getRestaurantName());

        printUtils.selectCommand(PrintUtils.ALIGN_CENTER);
        printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
        printUtils.printText(restaurantName);

        printUtils.selectCommand(PrintUtils.NORMAL);

        if (orderMain.getMainStatus() == 2)
        {
            if (orderMain.getReverseNumber() == 0)// 0 为正常订单，1 为反结账订单
            {
                printUtils.printText("\n\n结账单\n\n");
            }
            else
            {
                printUtils.printText("\n\n结账单（反）\n\n");
            }
        }
        else
        {
            if (orderMain.getReverseNumber() == 0)// 0 为正常订单，1 为反结账订单
            {
                printUtils.printText("\n\n消费单\n\n");
            }
            else
            {
                printUtils.printText("\n\n消费单（反）\n\n");
            }
        }
        printUtils.selectCommand(PrintUtils.NORMAL);
        printUtils.selectCommand(PrintUtils.ALIGN_LEFT);

        String desk = "";

        if (!TextUtils.isEmpty(orderMain.getMainDesk()) && !orderMain.getMainDesk().equals("null") && !orderMain.getMainDesk().equals("0"))
        {
            desk = "桌号: " + orderMain.getMainDesk();
        }

        String userName;
        if (orderMain.getMainStatus() == 2)
        {
            userName = getStr(R.string.cashier_person) + ": " + orderMain.getCashierName();
        }
        else
        {
            userName = getStr(R.string.confirm_man) + orderMain.getCreaterName();
        }

        if (isChange)
        {
            printUtils.printText(PrintUtils.get().printTwoData(32, userName, "订单尾号: " + orderMain.getTailNo()) + "\n");
        }
        else
        {
            int totalLen = StringUtil.getLengthForInputStr(userName + desk);// 下单员 和 桌号 的字符总长度
            if (totalLen > 30)
            {
                printUtils.printText(userName + "\n");
                printUtils.printText(desk + "\n");
            }
            else
            {
                printUtils.printText(PrintUtils.get().printTwoData(32, userName, desk) + "\n");
            }
        }
        //分割线
        String content = "--------------------------------\n";
        printUtils.printText(content);

        String personNumber = "";

        if (orderMain.getMainGuests() > 0)
        {
            personNumber = "用餐人数: " + orderMain.getMainGuests();
        }

        if (isChange)
        {
            if (!TextUtils.isEmpty(desk) && !TextUtils.isEmpty(personNumber))
            {
                int totalLen = StringUtil.getLengthForInputStr(desk + personNumber);
                if (totalLen > 30)
                {
                    printUtils.printText(desk + "\n");
                    printUtils.printText(personNumber + "\n");
                }
                else
                {
                    printUtils.printText(PrintUtils.get().printTwoData(32, desk, personNumber) + "\n");
                }
            }
            else if (TextUtils.isEmpty(desk) && TextUtils.isEmpty(personNumber))
            {
                // 不作处理，两个都为空此行不打印
            }
            else
            {
                printUtils.printText(desk + personNumber + "\n");
            }
        }
        else
        {
            printUtils.printText(PrintUtils.get().printTwoData(32, "订单尾号: " + orderMain.getTailNo(), personNumber) + "\n");
        }

        //没有桌号 也没有用餐人数 直接打印
        printUtils.printText("下单时间: " + DateUtils.formatDateTime(orderMain.getCreateAt(), "yyyy/MM/dd HH:mm") + "\n");

        if (orderMain.getMainStatus() == 2)
        {
            printUtils.printText("付款时间: " + DateUtils.formatDateTime(orderMain.getPayTime(), "yyyy/MM/dd HH:mm") + "\n");
        }

        if (!TextUtils.isEmpty(orderMain.getPackageRemark()))
        {
            printUtils.printText("属性: " + orderMain.getMainRemark() + "\n");
        }
        printUtils.printText(content);//分割线

        printUtils.printText("品名              数量     总价\n");

        printFoodsForBlue(orderDetails, printUtils, orderMain.getMainStatus());// 打印菜品列表
        printUtils.selectCommand(PrintUtils.NORMAL);
        printAddFoodsForBlue(orderDetails, printUtils, orderMain.getMainStatus());// 打印加菜列表

        printUtils.selectCommand(PrintUtils.NORMAL);
        printUtils.selectCommand(PrintUtils.ALIGN_LEFT);

        // 打印餐位费
        List<OrderDetail> details = createFeeForDetails(orderMain);

        if (!details.isEmpty())
        {
            printUtils.selectCommand(PrintUtils.NORMAL);
            printUtils.printText(content);//分割线

            for (OrderDetail detail : details)
            {
                String rightText = PrintUtils.get().printTwoData(13, NumberFormat.dTs3(detail.getDetailCount()), NumberFormat.dTs(detail.getTotalPrice()));
                printUtils.printText(PrintUtils.get().printTwoData(32, detail.getFoodName(), rightText) + "\n");
            }
        }

        if (orderMain.getMainStatus() != 2) // 打印消费单的 备注、菜品数量、合计
        {
            printUtils.selectCommand(PrintUtils.NORMAL);
            printUtils.printText(content);//分割线

            if (!TextUtils.isEmpty(orderMain.getMainRemark()))
            {
                printUtils.printText("订单备注: " + orderMain.getMainRemark() + "\n");
            }

            printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
            printUtils.printText("菜品数量: " + orderMain.getFoodCountTotal() + "\n");
            printUtils.printText("金额合计: " + dTs(orderMain.getOriginalMoney()) + "\n");
        }
        else // 打印结账单
        {
            printUtils.selectCommand(PrintUtils.NORMAL);
            if (!TextUtils.isEmpty(orderMain.getMainRemark()))
            {
                printUtils.printText(content);//分割线
                printUtils.printText("订单备注: " + orderMain.getMainRemark() + "\n");
            }

            // 打印优惠信息
            if (orderMain.getDiscountsTotal() > 0)
            {
                printUtils.printText("------------优惠明细------------\n\n");//分割线

                List<PreferentialDetail> detailList = orderMain.getPreferentialList();

                if (null != detailList && detailList.size() > 0)
                {
                    for (PreferentialDetail detail : detailList)
                    {
                        printUtils.printText(detail.getPreferentialName() + ": -￥" + dTs(detail.getPreferentialMoney()) + "\n");
                    }
                }
            }

            // 打印结算明细
            printUtils.printText("------------结算明细------------\n\n");//分割线
            printUtils.printText("菜品数量: " + orderMain.getFoodCountTotal() + "\n");
            printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
            printUtils.printText("金额合计: " + orderMain.getOriginalMoney() + "\n");
            if (orderMain.getDiscountsTotal() > 0)
            {
                printUtils.printText("优惠合计: " + orderMain.getDiscountsTotal() + "\n");
            }
            printUtils.printText("实收金额: " + dTs(orderMain.getReceivedMoney()) + "\n");

            printUtils.selectCommand(PrintUtils.NORMAL);
            if (!TextUtils.isEmpty(orderMain.getPayType()) && orderMain.getReverseNumber() <= 0)
            {
                printUtils.printText("支付方式: " + orderMain.getPayType() + "\n");
            }

            List<PayWayBean> payWayDetail = orderMain.getPayWayDetail();
            if (null != payWayDetail && payWayDetail.size() > 0)
            {
                for (PayWayBean detail : payWayDetail)
                {
                    String payWay = detail.getPayWay() + "";
                    if (payWay.equals("现金支付"))
                    {
                        payWay = "现金收款";
                    }

                    printUtils.printText(payWay + ": " + dTs(detail.getPayMoney()) + "\n");


                    if (detail.getOddChange() > 0)
                    {
                        printUtils.printText("现金找零: " + dTs(detail.getOddChange()) + "\n");
                    }
                }
                printUtils.printText("\n");// 隔一行
            }
        }

        printUtils.selectCommand(PrintUtils.NORMAL);

        String inscribed = Share.get().getPrintInscribed();//落款
        if (!TextUtils.isEmpty(inscribed))
        {
            printUtils.printText("\n\n");

            printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);

            printUtils.printText(inscribed + "\n");
        }

        printUtils.selectCommand(PrintUtils.ALIGN_CENTER);
        if (!TextUtils.isEmpty(orderMain.getQrCodeStr()))
        {
//            printUtils.selectCommand(PrintUtils.ALIGN_CENTER);
            if (orderMain.getMainStatus() == 2)
            {
                printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
                printUtils.printText("\n" + getStr(R.string.sao_get_more_service2) + "\n");

                printUtils.selectCommand(PrintUtils.NORMAL);
                printUtils.printText(getStr(R.string.record_parking_coupons) + "\n");
            }
            else
            {
                printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
                printUtils.printText("\n" + getStr(R.string.sao_get_more_service) + "\n");
                printUtils.selectCommand(PrintUtils.NORMAL);
                printUtils.printText(getStr(R.string.payment_service_coupons) + "\n");
            }
            try
            {
                printUtils.qrCode(orderMain.getQrCodeStr());
                printUtils.selectCommand(PrintUtils.NORMAL);
                printUtils.printText("技术支持by旗鱼点餐\n");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        printUtils.selectCommand(PrintUtils.NORMAL);
        printUtils.printText("\n");
        printUtils.printText("-------分割线,请沿此线撕开------\n");
        printUtils.printText("  \n");
        printUtils.printText("  \n");
        printUtils.printText("  \n");
        printUtils.printText("  \n");
    }

    //打印菜品数据明细 -- 蓝牙打印机
    private void printFoodsForBlue(List<OrderDetail> orderDetails, PrintUtils printUtils, int mainStatus)
    {
        L.e("xxxxxxxxxxxxxxsize=" + orderDetails.size() + "");
        for (OrderDetail orderDetail : orderDetails)
        {
            //  mainStatus; //订单状态(1=初始|2=支付成功|8=餐后支付已确认|9=餐后支付已取消) @mock=1（1 、8 、9未支付 2：已支付）
            // addState;//0普通 1加菜 2退菜

            // 打印消费单 mainStatus!=2    // 打印结账单 mainStatus==2
            if (orderDetail.getAddState() == 0 || orderDetail.getAddState() == 2)
            {
                printXiaoFeiOrJieZhangDan(printUtils, orderDetail, mainStatus);
            }

        }
    }

    private void printXiaoFeiOrJieZhangDan(PrintUtils printUtils, OrderDetail orderDetail, int mainStatus)
    {
        //判断菜品是不是套餐的详情菜品
        boolean isPackageFood = false;
        if (orderDetail.getId() == 0 && !(orderDetail.getFoodName().equals(getStr(R.string.table_fee))) && !(orderDetail.getFoodName().equals(getStr(R.string
                .service_fee))) && !(orderDetail.getFoodName().equals(getStr(R.string.packaging_fee))))
        {
            isPackageFood = true;
        }

        // 添加菜名
        String foodName = orderDetail.getFoodName();

        String price = "";
        if (isPackageFood)
        {
            price = getStr(R.string.package_detail_tip);
        }
        else
        {
            price = NumberFormat.dTs(orderDetail.getTotalPrice());
        }

        // 1.先判断是不是称重计量，如果是，菜品一行，数量总价另起一行
        String count = "";

        if (mainStatus == 2) // 结账单 ---- 菜品列表采用正常字体
        {
            printUtils.selectCommand(PrintUtils.NORMAL);
            if (orderDetail.getUnitType() == 2)//1=普通|2=称重计量|3=份量)
            {
                count = NumberFormat.dTs(orderDetail.getDetailCount()) + orderDetail.getUnitName();
                int totalLen = StringUtil.getLengthForInputStr(price + count);

                if (totalLen > 11 || StringUtil.getLengthForInputStr(foodName) > 19)
                {
                    printUtils.printText(foodName + "\n");

                    String rightText = PrintUtils.get().printTwoData(13, count, price);
                    printUtils.printText(PrintUtils.get().printTwoData(32, "", rightText) + "\n");
                }
                else
                {
                    String rightText = PrintUtils.get().printTwoData(13, count, price);
                    printUtils.printText(PrintUtils.get().printTwoData(32, foodName, rightText) + "\n");
                }
            }
            else // 普通菜品
            {
                count = NumberFormat.dTs3(orderDetail.getDetailCount());

                int totalLen = StringUtil.getLengthForInputStr(foodName + price + count);
                if (totalLen > 30 || StringUtil.getLengthForInputStr(foodName) > 19)
                {
                    printUtils.printText(foodName + "\n");
                    String rightText = PrintUtils.get().printTwoData(13, count, price);
                    printUtils.printText(rightText + "\n");
                }
                else
                {
                    String rightText = PrintUtils.get().printTwoData(13, count, price);
                    printUtils.printText(PrintUtils.get().printTwoData(32, foodName, rightText) + "\n");
                }
            }
        }
        else // 消费单 ---- 菜品列表采用大号字体
        {
            printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
            if (orderDetail.getUnitType() == 2)//1=普通|2=称重计量|3=份量)
            {
                count = NumberFormat.dTs(orderDetail.getDetailCount()) + orderDetail.getUnitName();
                int totalLen = StringUtil.getLengthForInputStr(price + count);

                if (totalLen > 11 || StringUtil.getLengthForInputStr(foodName) > 19)
                {
                    printUtils.printText(foodName + "\n");

                    String rightText = PrintUtils.get().printTwoData(13, count, price);
                    printUtils.printText(PrintUtils.get().printTwoData(32, "", rightText) + "\n");
                }
                else
                {
                    String rightText = PrintUtils.get().printTwoData(13, count, price);
                    printUtils.printText(PrintUtils.get().printTwoData(32, foodName, rightText) + "\n");
                }
            }
            else // 普通菜品
            {
                count = NumberFormat.dTs3(orderDetail.getDetailCount());

                int totalLen = StringUtil.getLengthForInputStr(foodName + price + count);
                if (totalLen > 30 || StringUtil.getLengthForInputStr(foodName) > 19)
                {
                    printUtils.printText(foodName + "\n");
                    String rightText = PrintUtils.get().printTwoData(13, count, price);
                    printUtils.printText(rightText + "\n");
                }
                else
                {
                    String rightText = PrintUtils.get().printTwoData(13, count, price);
                    printUtils.printText(PrintUtils.get().printTwoData(32, foodName, rightText) + "\n");
                }
            }
        }


        // 添加备注
        String foodCategory = "";
        printUtils.selectCommand(PrintUtils.NORMAL);
        if (isPackageFood)
        {
            if (orderDetail.getUnitName().equals("null"))
            {
                foodCategory = "";
            }
            else
            {
                foodCategory = TextUtils.isEmpty(orderDetail.getUnitName()) ? "" : orderDetail.getUnitName() + " "; //用于取套餐菜品的类型 普通为空 份量菜为对应的份量
            }

            if (!TextUtils.isEmpty(orderDetail.getDetailRemark()))
            {
                foodCategory += orderDetail.getDetailRemark();
            }
        }
        else
        {
            if (orderDetail.getAddState() == 2)
            {
                foodCategory = getStr(R.string.cancel_food);
            }
            else
            {
                // 添加分量
                if (!TextUtils.isEmpty(orderDetail.getFoodSpecName()))
                {
                    foodCategory += orderDetail.getFoodSpecName();
                }

                // 添加配菜
                if (!TextUtils.isEmpty(orderDetail.getFoodGarnishName()))
                {
                    if (!TextUtils.isEmpty(foodCategory))
                    {
                        foodCategory += ";";
                    }
                    foodCategory += orderDetail.getFoodGarnishName();
                }

                // 添加备注（做法）
                if (!TextUtils.isEmpty(orderDetail.getFoodCategory()))
                {
                    if (!TextUtils.isEmpty(foodCategory))
                    {
                        foodCategory += ";";
                    }
                    foodCategory += orderDetail.getFoodCategory();
                }

                // 添加备注（菜品备注）
                if (!TextUtils.isEmpty(orderDetail.getDetailRemark()))
                {
                    if (!TextUtils.isEmpty(foodCategory))
                    {
                        foodCategory += ";";
                    }
                    foodCategory += orderDetail.getDetailRemark();
                }

                // 添加外卖或者外带
                if (orderDetail.getFoodWay() == 2)
                {
                    foodCategory += getStr(R.string.take_out);
                }
                else if (orderDetail.getFoodWay() == 3)
                {
                    foodCategory += getStr(R.string.take_out_2);
                }
            }
        }

        if (!TextUtils.isEmpty(foodCategory.trim()))
        {
//            objs.add(createPrintFormat(8, "【" + foodCategory + "】"));
            printUtils.printText("【" + foodCategory + "】" + "\n");
        }
    }

    //打印加菜数据明细 -- 蓝牙打印
    private void printAddFoodsForBlue(List<OrderDetail> orderDetails, PrintUtils printUtils, int mainStatus)
    {
        boolean isPrint = false;
        for (OrderDetail orderDetail : orderDetails)
        {
            if (orderDetail.getAddState() != 1)
            {
                continue;
            }
            // 如果是加菜
            if (!isPrint)
            {
                printUtils.printText("--------------加菜-------------\n");
                isPrint = true;
            }

            printXiaoFeiOrJieZhangDan(printUtils, orderDetail, mainStatus);
        }
    }

    /**
     * 汇总打印
     *
     * @param activity
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param orderSummaryData 汇总数据
     * @param selectedWay      汇总包含的收银方式  (1=自由收银|2=定额收银|3=订单收银)（逗号隔开）判断里面是否包含1,2,3,来识别打印内容
     */

    @Override
    public void printCollect(Activity activity, Date startTime, Date endTime, OrderSummaryData orderSummaryData, String selectedWay)
    {
        showDialog(activity);
        JSONArray arr = new JSONArray();
        try
        {
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName
                    ("经营小票")));

            //打印前半内容
            String content = "-------------------------------";
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "店铺名称:" + Share.get().getRestaurantName()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "汇总日期:" + DateUtils.format(startTime,
                    "yyyy/MM/dd" + " HH:mm")));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "-        " + DateUtils.format(endTime,
                    "yyyy/MM/dd HH:mm")));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "共计 : " + DateUtils.getDay(startTime, endTime)));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));

            // 汇总订单
            compositeData(arr, selectedWay, orderSummaryData);//收银明细数据

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "店长确认 : " + "_________(签字或盖章)"));


            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "打印时间 : " + DateUtils.format(new Date(),
                    "yyyy/MM/dd HH:mm")));
            for (int i = 0; i < 3; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "-------分割线,请沿此线撕开------"));

            for (int i = 0; i < 3; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MTPackageName, MTPrintName));
            intent.putExtra("lines", arr.toString());
            activity.startActivityForResult(intent, REQUEST_CODE_PRINT_TWO);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }


    /**
     * 交班打印
     *
     * @param activity
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param orderSummaryData 汇总数据
     * @param selectedWay      汇总包含的收银方式  (1=自由收银|2=定额收银|3=订单收银)（逗号隔开）判断里面是否包含1,2,3,来识别打印内容
     */
    @Override
    public void printShiftData(Activity activity, Date startTime, Date endTime, OrderSummaryData orderSummaryData, String selectedWay)
    {
        showDialog(activity);
        JSONArray arr = new JSONArray();
        try
        {
            //打印餐馆名
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName(Share
                    .get().getRestaurantName())));

            arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printRestaurantName("交班单")));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "交班时间"));
            //打印前半内容
            String content = "-------------------------------";
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "起:     " + DateUtils.format(startTime,
                    "yyyy/MM/dd HH:mm")));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "止:      " + DateUtils.format(endTime,
                    "yyyy/MM/dd HH:mm")));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));

            // 汇总订单
            compositeData(arr, selectedWay, orderSummaryData);//收银明细数据


            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "打印时间 : " + DateUtils.format(new Date(),
                    "yyyy/MM/dd HH:mm")));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "店长 : " + orderSummaryData.getUserName()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "收银员确认 : " + "_____________"));
            for (int i = 0; i < 8; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MTPackageName, MTPrintName));
            intent.putExtra("lines", arr.toString());
            activity.startActivityForResult(intent, REQUEST_CODE_PRINT_TWO);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

    }


    /**
     * 自由收银打印
     *
     * @param activity
     * @param orderCollectBean 自由收银订单明细
     * @param isShowDialog
     */
    @Override
    public void printChargeFreeOrder(Activity activity, OrderCollectData.OrderCollectBean orderCollectBean, boolean isShowDialog)
    {

        if (isShowDialog)
        {
            showDialog(activity);
        }
        //        createChargeFreeOrder(activity, orderCollectBean);
        printChargeFreeOrderForBlue(orderCollectBean);

    }

    private void printChargeFreeOrderForBlue(OrderCollectData.OrderCollectBean orderCollectBean)
    {
        Observable.just("").subscribeOn(Schedulers.newThread()).flatMap(new Func1<String, Observable<Boolean>>()
        {
            @Override
            public Observable<Boolean> call(String s)
            {
                return Observable.just(BLE.get().init().connectDeviceByAddr());
            }
        }).unsubscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(ss ->
        {
            if (ss)
            {
                try
                {
                    OutputStream stream = BLE.get().mtBtSocket.getOutputStream();
                    //                    PrintUtils printUtils = new PrintUtils();
                    PrintUtils printUtils = PrintUtils.get();
                    printUtils.setOutputStream(stream);

                    printChargeFreeOrderForBlue(orderCollectBean, printUtils);
                    printChargeFreeOrderForBlue(orderCollectBean, printUtils);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        });
    }

    private void printChargeFreeOrderForBlue(OrderCollectData.OrderCollectBean orderCollectBean, PrintUtils printUtils)
    {
        //打印餐馆名
        String restaurantName = (TextUtils.isEmpty(orderCollectBean.getRestaurantName()) ? "旗鱼点餐" : orderCollectBean.getRestaurantName());

        printUtils.selectCommand(PrintUtils.ALIGN_CENTER);
        printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
        printUtils.printText(getStringOutLine(restaurantName, 28) + "\n");

        printUtils.selectCommand(PrintUtils.NORMAL);

//        printUtils.printText("\n自由收银单\n\n");
        printUtils.printText(printUtils.printTwoData("\n" + "自由收银单", "收银员 " + ": " + (TextUtils.isEmpty(orderCollectBean.getCashierName()) ? Share.get()
                .getUserName() : orderCollectBean.getCashierName()) + "\n"));
        printUtils.selectCommand(PrintUtils.ALIGN_LEFT);
        //分割线
        String content = "--------------------------------\n";
        printUtils.printText(content);

        printUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT);
        String orderNo = orderCollectBean.getOrderNo();//订单号
        printUtils.printText("订单尾号" + ": " + (orderNo.substring(orderNo.length() - 4, orderNo.length())) + "\n");
        printUtils.printText("订单金额" + ": ￥" + dTs(orderCollectBean.getOriginalMoney()) + "\n");
        printUtils.printText("优惠金额" + ": -￥" + dTs(orderCollectBean.getDiscountMoney()) + "\n");
        printUtils.printText("实收金额" + ": ￥" + dTs(orderCollectBean.getPayMoney()) + "\n");

        printUtils.selectCommand(PrintUtils.NORMAL);
//        printUtils.printText(content);
//        printUtils.printText("优惠方式" + " : " + (TextUtils.isEmpty(orderCollectBean.getGiftReturnName()) ? "无" : orderCollectBean.getGiftReturnName()) + "\n");
//        printUtils.printText("收款方式" + " : " + orderCollectBean.getPayType() + "\n");
//
//        double cashPayMoney = orderCollectBean.getCashPayMoney();
//        if (cashPayMoney > 0)
//        {
//            printUtils.printText("实收现金" + " : ￥" + cashPayMoney + "\n");
//            printUtils.printText("现金找零" + " : ￥" + orderCollectBean.getOddChange() + "\n");
//        }


        // 打印优惠信息
        List<PreferentialDetail> detailList = orderCollectBean.getPreferentialList();

        if (null != detailList && detailList.size() > 0)
        {
            printUtils.printText("------------优惠明细------------\n\n");//分割线

            for (PreferentialDetail detail : detailList)
            {
                printUtils.printText(detail.getPreferentialName() + ": -￥" + dTs(detail.getPreferentialMoney()) + "\n");
            }
        }

        // 打印结算明细
        printUtils.printText("------------结算明细------------\n\n");//分割线

        printUtils.printText("收款方式" + ": " + orderCollectBean.getPayType() + "\n");

        List<PayWayBean> payWayDetail = orderCollectBean.getPayWayDetail();
        if (null != payWayDetail && payWayDetail.size() > 0)
        {
            for (PayWayBean detail : payWayDetail)
            {
                String payWay = detail.getPayWay() + "";
                if (payWay.equals("现金支付"))
                {
                    payWay = "现金收款";
                }
                printUtils.printText(payWay + ": " + dTs(detail.getPayMoney()) + "\n");
                if (detail.getOddChange() > 0)
                {
                    printUtils.printText("现金找零: " + dTs(detail.getOddChange()) + "\n");
                }
            }
        }

        printUtils.printText("付款时间" + ": " + DateUtils.format(orderCollectBean.getPayTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
        printUtils.printText("收银备注: " + (TextUtils.isEmpty(orderCollectBean.getCollectMoneyRemark()) ? "无" : orderCollectBean.getCollectMoneyRemark()) + "\n");
        printUtils.printText(content);
//        printUtils.printText("付款人签名 :" + "\n\n\n");

        if (isFrist)
        {
            printUtils.printText("付款人签名:           (商户存根)\n\n\n");
        }
        else
        {
            printUtils.printText("                    (客户存根)\n\n\n");
        }

        printUtils.printText("-------分割线,请沿此线撕开------\n\n\n\n");
        isFrist = !isFrist;
    }

    /**
     * 设置区二维码打印
     *
     * @param activity
     * @param qrCodeBean
     * @param isShowDialog
     */
    @Override
    public void printQRCode(Activity activity, QRCodeBean qrCodeBean, boolean isShowDialog)
    {
        if (isShowDialog)
        {
            showDialog(activity);
        }
        JSONArray arr = new JSONArray();
        try
        {
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName
                    (qrCodeBean.getQrcodeName())));// 餐馆名称

            arr.put(new JSONObject().put("type", "qrcode").put("offset", 0).put("height", 395).put("content", qrCodeBean.getQrcodeUrl()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName
                    (qrCodeBean.getQrcodeMark())));// 餐馆落款
            for (int i = 0; i < 4; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MTPackageName, MTPrintName));
            intent.putExtra("lines", arr.toString());
            activity.startActivityForResult(intent, REQUEST_CODE_PRINT_TWO);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 发票二维码打印
     *
     * @param activity
     * @param qrCodeBean
     * @param isShowDialog
     */
    @Override
    public void printQRCodeForInvoice(Activity activity, QRCodeBean qrCodeBean, boolean isShowDialog)
    {
        if (isShowDialog)
        {
            showDialog(activity);
        }
        JSONArray arr = new JSONArray();
        try
        {
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName
                    (qrCodeBean.getQrcodeName())));// 餐馆名称
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printRestaurantName(getStr(R.string
                    .restaurant_artifact))));// 用餐服务神器？
            //            mPrintManager.setPrnText(makeStrCenter(getStr(R.string.restaurant_artifact)).toString(), config);

            arr.put(new JSONObject().put("type", "qrcode").put("offset", 0).put("height", 395).put("content", qrCodeBean.getQrcodeUrl()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printRestaurantName(qrCodeBean.getQrcodeMark())))
            ;// 餐馆落款
            for (int i = 0; i < 4; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MTPackageName, MTPrintName));
            intent.putExtra("lines", arr.toString());
            activity.startActivityForResult(intent, REQUEST_CODE_PRINT_TWO);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 呼叫服务打印
     *
     * @param activity
     * @param serviceTextBean
     * @param isShowDialog
     */
    @Override
    public void printServiceText(Activity activity, ServiceTextBean serviceTextBean, boolean isShowDialog)
    {
        if (isShowDialog)
        {
            showDialog(activity);
        }
        JSONArray arr = new JSONArray();
        try
        {
            String title, msg, printTime;
            title = getStr(R.string.server_text);
            msg = "\n" + serviceTextBean.getServiceText() + "\n\n";
            printTime = getStr(R.string.print_time) + DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName(title)
            ));// “呼叫服务”标题
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", msg));// 服务信息
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printTime));// 打印时间
            for (int i = 0; i < 4; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }
            //开始打印
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(MTPackageName, MTPrintName));
            intent.putExtra("lines", arr.toString());
            activity.startActivityForResult(intent, REQUEST_CODE_PRINT_TWO);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            L.e("printer异常" + e.toString());
        }
    }


    /**
     * 订单打印模板
     *
     * @param arr       打印JSONArray拼接的数据
     * @param orderMain 点单数据
     */
    private void printOrderData(JSONArray arr, OrderMain orderMain)
    {
        List<OrderDetail> orderDetails = new ArrayList<>();
        orderDetails.clear();
        orderDetails.addAll(orderMain.getOrderDetailModelList());
        boolean isChange = false;// 是否更换桌号和订单尾号打印的位置
        if (Share.get().getIsCall() == AppEnumHelp.IS_CALL_1.getValue())
        {
            isChange = true;
        }

        try
        {
            //打印餐馆名
            String restaurantName = (TextUtils.isEmpty(orderMain.getRestaurantName()) ? "旗鱼点餐" : orderMain.getRestaurantName());
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName
                    (restaurantName)));

            arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));

            if (orderMain.getMainStatus() == 2)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printRestaurantName("结账单")));
            }
            else
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printRestaurantName("消费单")));
            }

            arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", (isChange ? ("          "
                    + "订单尾号:" + orderMain.getTailNo()) : ("            桌号:" + orderMain.getMainDesk()))));
            //打印前半内容
            String content = "-------------------------------";
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));//分割线

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "用餐人数:" + orderMain.getMainGuests() + (isChange ?
                    (" " + "" + "   桌号:" + orderMain.getMainDesk()) : ("    订单尾号:" + orderMain.getTailNo()))));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "下单员:" + orderMain.getCreaterName()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "下单时间:" + DateUtils.formatDateTime(orderMain
                    .getCreateAt(), "yyyy/MM/dd HH:mm")));

            if (orderMain.getMainStatus() == 2)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "付款时间:" + DateUtils.formatDateTime(orderMain
                        .getPayTime(), "yyyy/MM/dd HH:mm")));
            }

            if (!TextUtils.isEmpty(orderMain.getPackageRemark()))
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "属性:" + orderMain.getMainRemark()));
            }
            arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));//分割线

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "品名      " + "          数量    总价"));

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));//分割线

            printFoods(arr, orderDetails, orderMain.getMainStatus());// 打印菜品列表

            printAddFoods(arr, orderDetails);// 打印加菜列表


            // 打印餐位费
            List<OrderDetail> details = createFeeForDetails(orderMain);
            if (!details.isEmpty())
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));//分割线
                printFoods(arr, details, orderMain.getMainStatus());
            }

            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", content));//分割线
            if (!TextUtils.isEmpty(orderMain.getMainRemark()))
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "备注 :" + " " + orderMain.getMainRemark()));
            }

            double count = getTotalCount(orderDetails);// 数量合计
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", "数量合计 : " + orderMain
                    .getFoodCountTotal()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", "金额合计 : " + dTs(orderMain
                    .getOriginalMoney())));
            if (orderMain.getSpecialMoney() > 0 && orderMain.getMainStatus() == 2)
            {
                if (0 != orderMain.getGiftReturnId())
                {
                    arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", "优惠折扣 : " +
                            orderMain.getGiftReturnName()));
                }
                else
                {
                    arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", "优惠折扣 : " + "会员优惠"));
                }
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", "优惠金额 : " + dTs
                        (orderMain.getSpecialMoney())));
            }
            if (0.0 < orderMain.getClearMoney() && orderMain.getMainStatus() == 2)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", "抹零金额 : " + dTs
                        (orderMain.getClearMoney())));
            }
            if (orderMain.getMainStatus() == 2)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", "实收金额 : " + dTs
                        (orderMain.getPaidUpAmount())));
            }
            if (!TextUtils.isEmpty(orderMain.getPayType()) && orderMain.getMainStatus() == 2)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", "支付方式 : " + orderMain
                        .getPayType()));
            }
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "打印时间 : " + DateUtils.format(new Date(),
                    "yyyy/MM/dd HH:mm")));
            String inscribed = Share.get().getPrintInscribed();//落款
            if (!TextUtils.isEmpty(inscribed))
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", Restaurant_Name_FontSize).put("content", printRestaurantName
                        (inscribed)));
            }

            for (int i = 0; i < 3; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "-------分割线,请沿此线撕开------"));

            for (int i = 0; i < 5; i++)
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 1).put("fontScale", 1).put("content", ""));
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
            L.e("订单数据出错");
        }

    }

    /**
     * 打印餐馆名  居中
     *
     * @param restaurantName
     */
    private String printRestaurantName(String restaurantName)
    {
        //需要手动计算居中设置的长度
        String nameStr = "", tempStr;
        if (!TextUtils.isEmpty(restaurantName))
        {
            int titleSpace = restaurantName.length() % 16;//将餐馆名对16取余
            if (titleSpace == 0)                        //餐馆名为16字符的倍数
            {
                nameStr = restaurantName + "\n";
            }
            else
            {
                if (restaurantName.length() < 16)         //餐馆名小于16字符
                {
                    for (int i = 0; i < 16 - restaurantName.length(); i++)
                    {
                        nameStr += " ";
                    }
                    nameStr += restaurantName + "\n";
                }
                else                                    //餐馆名大于16字符，且不为倍数
                {
                    nameStr = restaurantName.substring(0, restaurantName.length() - titleSpace - 2);
                    tempStr = restaurantName.substring(restaurantName.length() - titleSpace - 2);
                    for (int i = 0; i < 14 - titleSpace; i++)
                    {
                        nameStr += " ";
                    }
                    nameStr += tempStr + "\n";
                }
            }
        }

        return nameStr;
    }

    //打印菜品数据明细
    private void printFoods(JSONArray arr, List<OrderDetail> orderDetails, int mainStatus)
    {
        for (OrderDetail orderDetail : orderDetails)
        {
            //  mainStatus; //订单状态(1=初始|2=支付成功|8=餐后支付已确认|9=餐后支付已取消) @mock=1（1 、8 、9未支付 2：已支付）
            // addState;//0普通 1加菜 2退菜
            if (orderDetail.getAddState() == 1 || (mainStatus == 2 && orderDetail.getAddState() == 2))
            {
                continue;
            }
            try
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printSingleFood(length_1, length_2, length_3,
                        orderDetail)));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }

    }

    //加菜单菜品列表
    private void printAddOrderFoods(JSONArray arr, List<OrderDetail> orderDetailModelList)
    {
        for (OrderDetail bean : orderDetailModelList)
        {
            try
            {
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printSingleFood(length_1, length_2, length_3,
                        bean)));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
    }


    //打印加菜数据明细
    private void printAddFoods(JSONArray arr, List<OrderDetail> orderDetails)
    {
        boolean isPrint = false;
        for (OrderDetail orderDetail : orderDetails)
        {
            try
            {
                if (orderDetail.getAddState() != 1)
                {
                    continue;
                }
                // 如果是加菜
                if (!isPrint)
                {
                    arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "--------------加菜-------------"));
                    isPrint = true;
                }
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", printSingleFood(length_1, length_2, length_3,
                        orderDetail)));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }


    /**
     * 汇总打印中对过长餐馆名的处理
     *
     * @param srcStr   原名称
     * @param seprator 需要插入的字符
     * @param count    间隔几个字符加插入字符
     * @return 处理后的字符串
     */
    //    private String setRestaurantName(String srcStr, String seprator, int count)
    //    {
    //        StringBuffer sb = new StringBuffer(srcStr);
    //        int index = count;
    //        while (sb.length() > count && index < sb.length() - 1)
    //        {
    //            sb.insert(index, seprator);
    //            index += count + seprator.length();
    //        }
    //        return sb.toString();
    //    }


    /**
     * 组合汇总订单明细数据
     */
    private void compositeData(JSONArray arr, String selectedWay, OrderSummaryData orderSummaryData)
    {
        try
        {
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单汇总"));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单总份数:" + orderSummaryData.getTotalNumber()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单已支付份数:" + orderSummaryData.getPaidNumber()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单未支付份数:" + orderSummaryData.getUnpaidNumber()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单总金额:" + orderSummaryData.getTotalMoney()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "已付款金额:" + orderSummaryData.getPaidMoney()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "未付款金额:" + orderSummaryData.getUnpaidMoney()));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "收银明细(实收金额)"));
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));

            if (selectedWay.contains("3"))
            {//订单收银
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单收银"));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单总份数:" + orderSummaryData
                        .getOrderTotalNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单已支付份数:" + orderSummaryData
                        .getOrderPaidNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单未支付份数:" + orderSummaryData
                        .getOrderUnpaidNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单总金额:" + orderSummaryData
                        .getOrderTotalMoney()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单已付金额:" + orderSummaryData
                        .getOrderPaidMoney()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单未付金额:" + orderSummaryData
                        .getOrderUnpaidMoney()));

                printNoZero("微信支付:", orderSummaryData.getOrderWeChatMoney(), arr);
                printNoZero("支付宝支付:", orderSummaryData.getOrderAliMoney(), arr);
                printNoZero("刷卡支付:", orderSummaryData.getOrderCardMoney(), arr);
                printNoZero("POS机扫码支付:", orderSummaryData.getOrderPostMoney(), arr);
                printNoZero("现金支付:", orderSummaryData.getOrderCashMoney(), arr);
                printNoZero("赠送免单:", orderSummaryData.getOrderFreeMoney(), arr);
                printNoZero("存票会员支付:", orderSummaryData.getOrderCunPiaoMoney(), arr);
                printNoZero("个人微信(记录):", orderSummaryData.getOrderWechatRecordMoney(), arr);
                printNoZero("个人支付宝(记录):", orderSummaryData.getOrderAliRecordMoney(), arr);
                printNoZero("好哒支付(记录):", orderSummaryData.getOrderHdRecordMoney(), arr);
                printNoZero("会员储值卡(记录):", orderSummaryData.getOrderMemberRecordMoney(), arr);
                printNoZero("美团点评(记录):", orderSummaryData.getOrderMtdpRecordMoney(), arr);
                printNoZero("代金券支付(记录):", orderSummaryData.getOrderVoucherRecordMoney(), arr);
                printNoZero("挂账月结(记录):", orderSummaryData.getOrderMonthlyRecordMoney(), arr);
                printNoZero("免费赠送(记录):", orderSummaryData.getOrderFreeRecordMoney(), arr);
                printNoZero("银联刷卡(记录):", orderSummaryData.getOrderUnionpayCardRecordMoney(), arr);
                printNoZero(getStr(R.string.clear_record) + " : ", orderSummaryData.getOrderClearTotalMoney(), arr);
                printNoZero(getStr(R.string.reduction_record) + " : ", orderSummaryData.getOrderReductionSpecialMoney(), arr);
                printNoZero(getStr(R.string.discount_record) + " : ", orderSummaryData.getOrderDiscountSpecialMoney(), arr);
                printNoZero("口碑单品券优惠: ", orderSummaryData.getOrderKoubeiSpecialMoney(), arr);
                printNoZero("会员卡优惠: ", orderSummaryData.getOrderMembershipDiscount(), arr);
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }
            if (selectedWay.contains("1"))
            {//自由收银
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "自由收银"));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单总份数:" + orderSummaryData
                        .getFreeCashTotalNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单已支付份数:" + orderSummaryData
                        .getFreePaidNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单未支付份数:" + orderSummaryData
                        .getFreeUnpaidNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单总金额:" + orderSummaryData
                        .getFreeCashTotalMoney()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单已付金额:" + orderSummaryData
                        .getFreeCashPaidMoney()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单未付金额:" + orderSummaryData
                        .getFreeCashUnpaidMoney()));

                printNoZero("微信支付:", orderSummaryData.getFreeCashWeChatMoney(), arr);
                printNoZero("支付宝支付:", orderSummaryData.getFreeCashAliMoney(), arr);
                printNoZero("POS机扫码支付:", orderSummaryData.getFreePostMoney(), arr);
                printNoZero("现金支付:", orderSummaryData.getFreeCashCashMoney(), arr);
                printNoZero("刷卡支付:", orderSummaryData.getFreeCashCardMoney(), arr);
                printNoZero(getStr(R.string.reduction_record) + " : ", orderSummaryData.getFreeReductionSpecialMoney(), arr);
                printNoZero(getStr(R.string.discount_record) + " : ", orderSummaryData.getFreeDiscountSpecialMoney(), arr);
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }
            if (selectedWay.contains("2"))
            {//定额收银
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "定额收银"));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单总份数:" + orderSummaryData
                        .getQuotaTotalNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单已支付份数:" + orderSummaryData
                        .getQuotaPaidNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单未支付份数:" + orderSummaryData
                        .getQuotaUnpaidNumber()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单总金额:" + orderSummaryData
                        .getQuotaTotalMoney()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单已付金额:" + orderSummaryData
                        .getQuotaPaidMoney()));
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", "订单未付金额:" + orderSummaryData
                        .getQuotaUnpaidMoney()));

                printNoZero("微信支付:", orderSummaryData.getQuotaWeChatMoney(), arr);
                printNoZero("支付宝支付:", orderSummaryData.getQuotaAliMoney(), arr);
                printNoZero("POS机扫码支付:", orderSummaryData.getQuotaPostMoney(), arr);
                printNoZero("刷卡支付:", orderSummaryData.getQuotaCardMoney(), arr);
                printNoZero("现金支付:", orderSummaryData.getQuotaCashMoney(), arr);
                arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", ""));
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 打印值不为0的项
     *
     * @param head
     * @param value
     */
    private void printNoZero(String head, double value, JSONArray arr) throws JSONException
    {
        if (0 != value)
        {
            arr.put(new JSONObject().put("offset", 0).put("fontType", 2).put("fontScale", 1).put("content", head + value));
        }
    }
}
