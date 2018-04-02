package com.teorange.refund.open.alirefund.controller;


import com.teorange.refund.open.alirefund.util.AlipayNotify;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController("/回调地址")
public class AlipayRefundActionMultiController {

	/**
	 * 支付宝原路退款回调方法
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public String query(HttpServletRequest request, HttpServletResponse response) throws Exception {

		request.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();

		//获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
		}

		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		//批次号
		String batch_no = new String(request.getParameter("batch_no").getBytes("ISO-8859-1"), "UTF-8");
		//批量退款数据中转账成功的笔数
		String success_num = new String(request.getParameter("success_num").getBytes("ISO-8859-1"), "UTF-8");
		//批量退款数据中的详细信息
		String result_details = new String(request.getParameter("result_details").getBytes("ISO-8859-1"), "UTF-8");
		try {
			//验证成功
			if (AlipayNotify.verify(params)) {
				//截取SUCCESS
				String result = result_details.substring(result_details.length() - 7, result_details.length());
				//截取退款金额
				String refundMoney = result_details.substring(28, result_details.length() - 7);
				//截取交易号
				String tradeno = result_details.substring(0, 28);
				//判断退款是否成功
				if (result.toString().equals("SUCCESS")) {

					// TODO 业务操作
				}
				//请不要修改或删除
				out.print("success");

			} else {//验证失败
				out.print("fail");
			}

		} catch (Exception e) {
			out.println("fail");
		} finally {
			out.close();
		}
		return null;
	}

}
