package com.yunjiangzhe.wangwang.printer.a8;

import android.app.Activity;
import android.util.Log;

import com.yunjiangzhe.wangwang.bean.PostGetInfoEntity;
import com.yunjiangzhe.wangwang.match.IPayManager;

public class LianDiA8PayManager implements IPayManager {
    private final String wxPay_sao = "微信扫码支付";
    private final String alPay_sao = "支付宝扫码支付";
    private final String wxPay_qr = "微信二维码支付";
    private final String alPay_qr = "支付宝二维码支付";
    private final String cardPay = "刷卡支付";

    public final static int REQUEST_CODE = 10;//美团支付Activity返回码

    /**
     * 美团机识别码    支付宝
     */
    private final int ERP_PAY_TYPE_ALI = 1;
    /**
     * 美团机识别码    微信
     */
    private final int ERP_PAY_TYPE_WEIXIN = 2;
    /**
     * 美团机识别码     银行卡
     */
    private final int ERP_PAY_TYPE_BANK = 3;
    /**
     * 美团机识别码     apple pay
     */
    private final int ERP_PAY_TYPE_APPLE = 4;

    private int payWay = 0;

    @Override
    public void smartPay(Activity context, String payType, PostGetInfoEntity data)
    {
//        switch (payType)
//        {
//            case cardPay:
//                payWay = ERP_PAY_TYPE_BANK;
//                break;
//            case alPay_qr:
//            case wxPay_qr:
//                break;
//            case alPay_sao:
//                payWay = ERP_PAY_TYPE_ALI;
//                break;
//            case wxPay_sao:
//                payWay = ERP_PAY_TYPE_WEIXIN;
//                break;
//        }

        Log.e("yza","联迪a8支付");
    }

    @Override
    public void payCallBack(Activity context, String callBackData)
    {

    }
}
