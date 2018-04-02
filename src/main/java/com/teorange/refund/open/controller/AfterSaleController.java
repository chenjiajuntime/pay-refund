package com.teorange.refund.open.controller;

import com.teorange.refund.open.alirefund.config.AlipayConfig;
import com.teorange.refund.open.alirefund.util.AlipaySubmit;
import com.teorange.refund.open.alirefund.util.UtilDate;
import com.teorange.refund.open.util.StringUtil;
import com.teorange.refund.open.wxrefund.config.Configure;
import com.teorange.refund.open.wxrefund.config.Configure2;
import com.teorange.refund.open.wxrefund.util.WeiXinUtil;
import com.teorange.refund.open.wxrefund.util.WeiXinUtil2;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * created by cjj
 */
@RestController("/xx")
public class AfterSaleController {

    public void operatorRefund() throws Exception{

        //1、调起支付宝原路返还
        String tradeno = StringUtil.trim("获取支付宝交易号");
        String payAmount = StringUtil.trim("获取支付金额");
        String refundMoney = StringUtil.trim("退款金额");
        String refundId = StringUtil.trim("平台退款单id");
        //退款批次号
        String batch_no = UtilDate.getOrderNum();
        String aliRefund = aliRefund(tradeno, refundMoney, refundId, batch_no);

        //2、调起微信支付原路返还
        String weixinType = "";
        long numberCount = 1;//退款笔数
        String out_refund_no = generatePayNO();//商户退款单号
        String result = "";
        Map<String, String> resultData = new HashMap<String, String>();
        if (weixinType.equals("app支付")) {

            result = doRefundByWX(refundMoney, tradeno, numberCount, out_refund_no, payAmount);
            //解析xml文件
            resultData = WeiXinUtil.doXMLParse(result);

        } else if (weixinType.equals("公众号支付")) {

            result = doRefundByWX2(refundMoney, tradeno, numberCount, out_refund_no, payAmount);
            //解析xml文件
            resultData = WeiXinUtil2.doXMLParse(result);
        }
    }

    /**
     * 退款操作（请求）
     * @return
     * @throws Exception
     * trade_no 支付宝返回的交易号
     * money  退款金额
     */
    public String aliRefund(String trade_no, String money, String refundId, String batch_no) throws Exception {

        //服务器异步通知页面路径
        String notify_url;
        String sHtmlText = "";
        try {
            Map<String, String> sParaTemp = new HashMap<String, String>();

            //退款详细数据，必填，格式（支付宝交易号^退款金额^备注），多笔请用#隔开
            String detail_data = trade_no + "^" + money + "^退款";
            sParaTemp.put("detail_data", detail_data);
            sParaTemp.put("service", AlipayConfig.service);//
            sParaTemp.put("partner", AlipayConfig.partner);//合作者id
            sParaTemp.put("_input_charset", AlipayConfig.input_charset);//字符集格式
            sParaTemp.put("notify_url", AlipayConfig.notify_url);//回调地址
            sParaTemp.put("seller_email", "siigee@163.com");//账号
            sParaTemp.put("refund_date", UtilDate.getDateFormatter());//退款时间
            sParaTemp.put("batch_no", batch_no);//批次号，必填，格式：当天日期[8位]+序列号[3至24位]，如：201603081000001
            sParaTemp.put("batch_num", "1"); //退款笔数，必填，参数detail_data的值中，“#”字符出现的数量加1，最大支持1000笔（即“#”字符出现的数量999个）
            sHtmlText = AlipaySubmit.buildRequest(sParaTemp, "get", "确认");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sHtmlText;
    }

    /**
     * 商户退款单号
     *
     * @return
     */
    private String generatePayNO() {
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.getTimeInMillis()) + createMerchantOrderId(6);
    }

    /**
     * 生成订单号
     *
     * @param code_len
     * @return
     */
    public static String createMerchantOrderId(int code_len) {
        int count = 0;
        char str[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        while (count < code_len) {
            int i = Math.abs(r.nextInt(10));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }

    /**
     * 发送微信退款请求---app支付原路退款
     * @param money
     * @param bankSerialNumber
     * @param numberCount
     * @param no
     * @param sumAmout
     * @return
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private String doRefundByWX(String money, String bankSerialNumber, long numberCount, String no, String sumAmout) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, IOException {
        String result = "";
        String appid = WeiXinUtil.appid;//应用ID
        String mch_id = WeiXinUtil.mch_id;//商户号
        String nonce_str = WeiXinUtil.CreateNoncestr();//随机字符串
        String transaction_id = bankSerialNumber;//微信订单号
        String out_refund_no = no;//商户退款单号
        Double total_fee = 0d;
        try {
            total_fee = StringUtil.getDouble(sumAmout);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //总金额以分为单位
        long totalAmount = new BigDecimal(total_fee * 100d).longValue();
        Double refund_fee = Double.parseDouble(money);
        //退款金额以分为单位
        long Amount = new BigDecimal(refund_fee * 100d).longValue();
        //操作员帐号, 默认为商户号
        String op_user_id = WeiXinUtil.mch_id;

        //(1)签名算法
        SortedMap<Object, Object> SortedMap = new TreeMap<Object, Object>();
        SortedMap.put("appid", appid);
        SortedMap.put("mch_id", mch_id);
        SortedMap.put("nonce_str", nonce_str);
        SortedMap.put("transaction_id", transaction_id);
        SortedMap.put("out_refund_no", out_refund_no);
        SortedMap.put("total_fee", String.valueOf(totalAmount));
        SortedMap.put("refund_fee", String.valueOf(Amount));
        SortedMap.put("op_user_id", op_user_id);

        String sign = WeiXinUtil.createSign("UTF-8", SortedMap);
        //获取最终待发送的数据
        String requestXml = WeiXinUtil.getRequestXml(SortedMap);

        //(2)建立连接并发送数据
        result = WeixinSendPost(requestXml);
        return result;
    }

    /**
     * 公众号支付原路退款
     * @param money
     * @param bankSerialNumber
     * @param numberCount
     * @param no
     * @param sumAmout
     * @return
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private String doRefundByWX2(String money, String bankSerialNumber, long numberCount, String no, String sumAmout) throws UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, IOException {
        String result = "";
        String appid = WeiXinUtil2.appid;//应用ID
        String mch_id = WeiXinUtil2.mch_id;//商户号
        String nonce_str = WeiXinUtil2.CreateNoncestr();//随机字符串
        String transaction_id = bankSerialNumber;//微信订单号
        String out_refund_no = no;//商户退款单号
        Double total_fee = 0d;
        try {
            total_fee = StringUtil.getDouble(sumAmout);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long totalAmount = new BigDecimal(total_fee * 100d).longValue();//总金额以分为单位
        Double refund_fee = Double.parseDouble(money);
        long Amount = new BigDecimal(refund_fee * 100d).longValue();//退款金额以分为单位
        String op_user_id = WeiXinUtil2.mch_id;//操作员帐号, 默认为商户号

        //(1)签名算法
        SortedMap<Object, Object> SortedMap = new TreeMap<Object, Object>();
        SortedMap.put("appid", appid);
        SortedMap.put("mch_id", mch_id);
        SortedMap.put("nonce_str", nonce_str);
        SortedMap.put("transaction_id", transaction_id);
        SortedMap.put("out_refund_no", out_refund_no);
        SortedMap.put("total_fee", String.valueOf(totalAmount));
        SortedMap.put("refund_fee", String.valueOf(Amount));
        SortedMap.put("op_user_id", op_user_id);

        String sign = WeiXinUtil2.createSign("UTF-8", SortedMap);
        //获取最终待发送的数据
        String requestXml = WeiXinUtil2.getRequestXml(SortedMap);

        //(2)建立连接并发送数据
        result = WeixinSendPost2(requestXml);
        return result;
    }

    public String WeixinSendPost(Object xmlObj) throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException {
        String result = "";
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream instream = new FileInputStream(new File(Configure.getCertLocalPath()));
        try {
            keyStore.load(instream, Configure.getCertPassword().toCharArray());
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, Configure.getCertPassword().toCharArray()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {

            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");
            @SuppressWarnings("deprecation")
            HttpEntity xmlData = new StringEntity((String) xmlObj, "text/xml", "iso-8859-1");
            httpPost.setEntity(xmlData);

            System.out.println("executing request" + httpPost.getRequestLine());

            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity, "UTF-8");
                System.out.println(response.getStatusLine());
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return result;
    }

    public String WeixinSendPost2(Object xmlObj) throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException {
        String result = "";
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        FileInputStream instream = new FileInputStream(new File(Configure2.getCertLocalPath()));
        try {
            keyStore.load(instream, Configure2.getCertPassword().toCharArray());
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, Configure2.getCertPassword().toCharArray()).build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {

            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");
            @SuppressWarnings("deprecation")
            HttpEntity xmlData = new StringEntity((String) xmlObj, "text/xml", "iso-8859-1");
            httpPost.setEntity(xmlData);

            System.out.println("executing request" + httpPost.getRequestLine());

            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity, "UTF-8");
                System.out.println(response.getStatusLine());
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        return result;
    }
}
