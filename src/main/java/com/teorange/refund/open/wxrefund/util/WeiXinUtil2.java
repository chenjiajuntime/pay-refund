package com.teorange.refund.open.wxrefund.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class WeiXinUtil2 {
	/**
	 * 服务号相关信息
	 */
	public  static String appid = "";// 公众账号ID
	public  static String mch_id = "";// 商户号
	public  static String device_info = "";// 设备号
	public String nonce_str = "";// 随机字符串
	public String sign = "";// 签名
	public String body = "订单退款";// 商品描述
	public String detail = "";// 商品详情
	public String attach = "";// 附加数据
	public String out_trade_no = "";// 商户订单号
	public final static String fee_type = "CNY";// 货币类型
	public String total_fee = "";// 总金额
	public String spbill_create_ip = "";// 终端IP
	public String time_start = "";// 交易起始时间
	public String time_expire = "";// 交易结束时间
	public String goods_tag = "";// 商品标记
	public final static String notify_url = "http://xx.com/api/refund/refundBack_002"; //回调地址
	public final static String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder"; // 微信支付统一接口(POST)
	public final static String UNIFIED_QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery"; // 微信订单查询接口(POST)
	public final static String trade_type = "JSAPI";// 交易类型
	public String product_id = "";// 商品ID
	public String openid = "";// 用户标识

	public String prepayid = "";// 预付订单号
	public String packageStr = "Sign=WXPay";// 扩展字段

	public  static String key = "xx";
	private SortedMap<Object, Object> parameters;	// 统一下单参数
	private SortedMap<Object, Object> payParameters;// 调用支付接口参数
	private SortedMap<Object, Object> queryParameters;// 调用订单查询接口参数

	/**
	 * 创建统一下单接口
	 * 
	 * @return
	 */
	public SortedMap<Object, Object> createParameters() {
		this.parameters = new TreeMap<Object, Object>();
		this.parameters.put("appid", appid);
		this.parameters.put("mch_id", mch_id);
		this.parameters.put("device_info", device_info);
		this.parameters.put("nonce_str", nonce_str);
		this.parameters.put("sign", sign);
		this.parameters.put("body", body);
		this.parameters.put("detail", detail);
		this.parameters.put("attach", attach);
		this.parameters.put("out_trade_no", out_trade_no);
		this.parameters.put("fee_type", fee_type);
		this.parameters.put("total_fee", total_fee);
		this.parameters.put("spbill_create_ip", spbill_create_ip);
		this.parameters.put("time_start", time_start);
		this.parameters.put("time_expire", time_expire);
		this.parameters.put("goods_tag", goods_tag);
		this.parameters.put("notify_url", notify_url);
		this.parameters.put("trade_type", trade_type);
		this.parameters.put("product_id", product_id);
		this.parameters.put("openid", openid);
		return this.parameters;
	}

	/**
	 * 调用支付接口参数
	 * 
	 * @return
	 */
	public SortedMap<Object, Object> createPayParameters() {
		this.payParameters = new TreeMap<Object, Object>();
		this.payParameters.put("appid", appid);
		this.payParameters.put("partnerid", mch_id);
		this.payParameters.put("prepayid", prepayid);
		this.payParameters.put("package", packageStr);
		this.payParameters.put("noncestr", getNonceStr());
		this.payParameters.put("timestamp", time_start);
		this.payParameters.put("sign", sign);
		return this.payParameters;
	}

	/**
	 * 调用订单查询接口参数
	 * 
	 * @return
	 */
	public SortedMap<Object, Object> createQueryParameters() {
		this.queryParameters = new TreeMap<Object, Object>();
		this.queryParameters.put("appid", appid);
		this.queryParameters.put("mch_id", mch_id);
		this.queryParameters.put("out_trade_no", out_trade_no);
		this.queryParameters.put("nonce_str", nonce_str);
		this.queryParameters.put("sign", sign);
		return this.queryParameters;
	}

	/**
	 * 获取当前时间 yyyyMMddHHmmss
	 * 
	 * @return String
	 */
	public static String getCurrTime() {
		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHH");
		String s = outFormat.format(now);
		return s;
	}

	/**
	 * 取出一个指定长度大小的随机正整数.
	 * 
	 *            ,length小于11
	 * @return 返回生成的随机数。
	 */
	public static int buildRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}
	
	public static String getNonceStr() {
		Random random = new Random();
		return MD5Utils4WX.MD5Encode(String.valueOf(random.nextInt(10000)), "UTF-8");
	}
	
	public static String CreateNoncestr() {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String res = "";
		for (int i = 0; i < 16; i++) {
			Random rd = new Random();
			res += chars.charAt(rd.nextInt(chars.length() - 1));
		}
		return res;
	}

	/**
	 * @Description：sign签名
	 * @param characterEncoding
	 *            编码格式
	 * @param parameters
	 *            请求参数
	 * @return
	 */
	public static String createSign(String characterEncoding, SortedMap<Object, Object> parameters) {
		StringBuffer sb = new StringBuffer();
		Set<Entry<Object, Object>> es = parameters.entrySet();
		Iterator<Entry<Object, Object>> it = es.iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = (Entry<Object, Object>) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + key);
		String sign = MD5Utils4WX.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
		parameters.put("sign", sign);
		return sign;
	}

	/**
	 * 是否财付通签名,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 *
	 * @return boolean
	 */
	public boolean verifySign() {
		StringBuffer sb = new StringBuffer();
		Set<Entry<Object, Object>> es = this.parameters.entrySet();
		Iterator<Entry<Object, Object>> it = es.iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = (Entry<Object, Object>) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}

		sb.append("key=" + key);

		// 算出摘要
		String sign = MD5Utils4WX.MD5Encode(sb.toString(), "utf-8").toLowerCase();

		String tenpaySign = this.getParameter("sign").toLowerCase();

		return tenpaySign.equals(sign);
	}

	/**
	 * @Description：将请求参数转换为xml格式的string
	 * @param parameters
	 *            请求参数
	 * @return
	 */
	public static String getRequestXml(SortedMap<Object, Object> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set<Entry<Object, Object>> es = parameters.entrySet();
		Iterator<Entry<Object, Object>> it = es.iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = (Entry<Object, Object>) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"appkey".equals(k)) {
				if (k.equals("key") || k.equals("sign"))
					sb.append("<" + k + ">" + v + "</" + k + ">\n");
				else
					sb.append("<" + k + ">" + v + "</" + k + ">\n");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}

	/**
	 * 解析xml,返回第一级元素键值对。如果第一级元素有子节点，则此节点的值是子节点的xml数据。
	 *
	 * @param strxml
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Map doXMLParse(String strxml) throws JDOMException, IOException {
		if (null == strxml || "".equals(strxml)) {
			return null;
		}

		Map<String, String> m = new HashMap<String, String>();
		InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		Element root = doc.getRootElement();
		List list = root.getChildren();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Element e = (Element) it.next();
			String k = e.getName();
			String v = "";
			List children = e.getChildren();
			if (children.isEmpty()) {
				v = e.getTextNormalize();
			} else {
				v = XMLUtil.getChildrenText(children);
			}

			m.put(k, v);
		}

		// 关闭流
		in.close();

		return m;
	}
public static void main(String[] args) throws JDOMException, IOException {
	String xxx = "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[您没有APP支付权限]]></return_msg></xml>";
	Map resMap = doXMLParse(xxx);
}
	/**
	 * 获取子结点的xml
	 *
	 * @param children
	 * @return String
	 */
	public static String getChildrenText(List children) {
		StringBuffer sb = new StringBuffer();
		if (!children.isEmpty()) {
			Iterator it = children.iterator();
			while (it.hasNext()) {
				Element e = (Element) it.next();
				String name = e.getName();
				String value = e.getTextNormalize();
				List list = e.getChildren();
				sb.append("<" + name + ">");
				if (!list.isEmpty()) {
					sb.append(XMLUtil.getChildrenText(list));
				}
				sb.append(value);
				sb.append("</" + name + ">");
			}
		}

		return sb.toString();
	}

	/**
	 * 获取xml编码字符集
	 *
	 * @param strxml
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static String getXMLEncoding(String strxml) throws JDOMException, IOException {
		InputStream in = new ByteArrayInputStream(strxml.getBytes());
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		in.close();
		return (String) ((Document) doc).getProperty("encoding");
	}

	public String getNonce_str() {
		return nonce_str;
	}

	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}

	public String getOut_trade_no() {
		return out_trade_no;
	}

	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}

	public String getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(String total_fee) {
		this.total_fee = total_fee;
	}

	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}

	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}

	public String getTime_start() {
		return time_start;
	}

	public void setTime_start(String time_start) {
		this.time_start = time_start;
	}

	public String getTime_expire() {
		return time_expire;
	}

	public void setTime_expire(String time_expire) {
		this.time_expire = time_expire;
	}

	public String getGoods_tag() {
		return goods_tag;
	}

	public void setGoods_tag(String goods_tag) {
		this.goods_tag = goods_tag;
	}

	public String getProduct_id() {
		return product_id;
	}

	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	/**
	 * 获取参数值
	 * 
	 * @param parameter
	 *            参数名称
	 * @return String
	 */
	public String getParameter(String parameter) {
		String s = (String) this.parameters.get(parameter);
		return (null == s) ? "" : s;
	}

	/**
	 * 设置参数值
	 * 
	 * @param parameter
	 *            参数名称
	 * @param parameterValue
	 *            参数值
	 */
	public void setParameter(String parameter, String parameterValue) {
		String v = "";
		if (null != parameterValue) {
			v = parameterValue.trim();
		}
		this.parameters.put(parameter, v);
	}

	public SortedMap<Object, Object> getParameters() {
		return parameters;
	}

	public void setParameters(SortedMap<Object, Object> parameters) {
		this.parameters = parameters;
	}

	public SortedMap<Object, Object> getPayParameters() {
		return payParameters;
	}

	public void setPayParameters(SortedMap<Object, Object> payParameters) {
		this.payParameters = payParameters;
	}

	public SortedMap<Object, Object> getQueryParameters() {
		return queryParameters;
	}

	public void setQueryParameters(SortedMap<Object, Object> queryParameters) {
		this.queryParameters = queryParameters;
	}

	public String getPrepayid() {
		return prepayid;
	}

	public void setPrepayid(String prepayid) {
		this.prepayid = prepayid;
	}

	public String getPackageStr() {
		return packageStr;
	}

	public void setPackageStr(String packageStr) {
		this.packageStr = packageStr;
	}
}
