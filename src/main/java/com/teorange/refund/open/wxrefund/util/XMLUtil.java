package com.teorange.refund.open.wxrefund.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * created by cjj
 */
public class XMLUtil {
    public XMLUtil() {
    }

    public static Map<Object, Object> doXMLParse(String strxml) throws JDOMException, IOException {
        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
        if (null != strxml && !"".equals(strxml)) {
            Map<Object, Object> m = new HashMap();
            InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(in);
            Element root = doc.getRootElement();
            List<?> list = root.getChildren();

            String k;
            String v;
            for(Iterator it = list.iterator(); it.hasNext(); m.put(k, v)) {
                Element e = (Element)it.next();
                k = e.getName();
                v = "";
                List<?> children = e.getChildren();
                if (children.isEmpty()) {
                    v = e.getTextNormalize();
                } else {
                    v = getChildrenText(children);
                }
            }

            in.close();
            return m;
        } else {
            return null;
        }
    }

    public static String getChildrenText(List<?> children) {
        StringBuffer sb = new StringBuffer();
        if (!children.isEmpty()) {
            Iterator it = children.iterator();

            while(it.hasNext()) {
                Element e = (Element)it.next();
                String name = e.getName();
                String value = e.getTextNormalize();
                List<?> list = e.getChildren();
                sb.append("<" + name + ">");
                if (!list.isEmpty()) {
                    sb.append(getChildrenText(list));
                }

                sb.append(value);
                sb.append("</" + name + ">");
            }
        }

        return sb.toString();
    }

    public static String getXmlToString(HttpServletRequest request) {
        Format format = Format.getPrettyFormat();

        try {
            Document doc = (new SAXBuilder()).build(request.getInputStream());
            format.setEncoding("gb2312");
            XMLOutputter xmlout = new XMLOutputter(format);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            xmlout.output(doc, bo);
            String xmlStr = bo.toString();
            return xmlStr;
        } catch (IOException var6) {
            var6.printStackTrace();
        } catch (JDOMException var7) {
            var7.printStackTrace();
        }

        return "";
    }

    public static String converter(Map<Object, Object> dataMap) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<xml>");
        Set<Object> objSet = dataMap.keySet();
        Iterator i$ = objSet.iterator();

        while(i$.hasNext()) {
            Object key = i$.next();
            if (key != null) {
                strBuilder.append("\n");
                strBuilder.append("<").append(key.toString()).append(">");
                Object value = dataMap.get(key);
                strBuilder.append(coverter(value));
                strBuilder.append("</").append(key.toString()).append(">");
            }
        }

        strBuilder.append("</xml>");
        return strBuilder.toString();
    }

    public static String converter(String dataStr) {
        StringReader sr = new StringReader(dataStr);
        InputSource is = new InputSource(sr);

        try {
            Document doc = (new SAXBuilder()).build(is);
            Format format = Format.getPrettyFormat();
            format.setEncoding("gb2312");
            XMLOutputter xmlout = new XMLOutputter(format);
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            xmlout.output(doc, bo);
            String xmlStr = bo.toString();
            return xmlStr;
        } catch (JDOMException var8) {
            var8.printStackTrace();
        } catch (IOException var9) {
            var9.printStackTrace();
        }

        return "";
    }

    public static String coverter(Object[] objects) {
        StringBuilder strBuilder = new StringBuilder();
        Object[] arr$ = objects;
        int len$ = objects.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Object obj = arr$[i$];
            strBuilder.append("<item className=").append(obj.getClass().getName()).append(">\n");
            strBuilder.append(coverter(obj));
            strBuilder.append("</item>\n");
        }

        return strBuilder.toString();
    }

    public static String coverter(Collection<?> objects) {
        StringBuilder strBuilder = new StringBuilder();
        Iterator i$ = objects.iterator();

        while(i$.hasNext()) {
            Object obj = i$.next();
            strBuilder.append("<item className=").append(obj.getClass().getName()).append(">\n");
            strBuilder.append(coverter(obj));
            strBuilder.append("</item>\n");
        }

        return strBuilder.toString();
    }

    public static String coverter(Object object) {
        if (object instanceof Object[]) {
            return coverter((Object[])((Object[])object));
        } else if (object instanceof Collection) {
            return coverter((Collection)object);
        } else {
            StringBuilder strBuilder = new StringBuilder();
            if (isObject(object)) {
                Class<? extends Object> clz = object.getClass();
                Field[] fields = clz.getDeclaredFields();
                Field[] arr$ = fields;
                int len$ = fields.length;

                for(int i$ = 0; i$ < len$; ++i$) {
                    Field field = arr$[i$];
                    field.setAccessible(true);
                    if (field != null) {
                        String fieldName = field.getName();
                        Object value = null;

                        try {
                            value = field.get(object);
                        } catch (IllegalArgumentException var11) {
                            continue;
                        } catch (IllegalAccessException var12) {
                            continue;
                        }

                        strBuilder.append("<").append(fieldName).append(" className=\"").append(value.getClass().getName()).append("\">");
                        if (isObject(value)) {
                            strBuilder.append(coverter(value));
                        } else if (value == null) {
                            strBuilder.append("null");
                        } else {
                            strBuilder.append(value.toString() + "");
                        }

                        strBuilder.append("</").append(fieldName).append(">");
                    }
                }
            } else if (object == null) {
                strBuilder.append("null");
            } else {
                strBuilder.append(object.toString() + "");
            }

            return strBuilder.toString();
        }
    }

    private static boolean isObject(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof String) {
            return false;
        } else if (obj instanceof Integer) {
            return false;
        } else if (obj instanceof Double) {
            return false;
        } else if (obj instanceof Float) {
            return false;
        } else if (obj instanceof Byte) {
            return false;
        } else if (obj instanceof Long) {
            return false;
        } else if (obj instanceof Character) {
            return false;
        } else if (obj instanceof Short) {
            return false;
        } else {
            return !(obj instanceof Boolean);
        }
    }

    public static void main(String[] args) throws JDOMException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        sb.append("    <OpenId><![CDATA[111222]]></OpenId>");
        sb.append("    <AppId><![CDATA[wwwwb4f85f3a797777]]></AppId>");
        sb.append("    <IsSubscribe>1</IsSubscribe>");
        sb.append("    <ProductId><![CDATA[777111666]]></ProductId>");
        sb.append("    <TimeStamp> 1369743908</TimeStamp>");
        sb.append("    <NonceStr><![CDATA[YvMZOX28YQkoU1i4NdOnlXB1]]></NonceStr>");
        sb.append("    <AppSignature><![CDATA[a9274e4032a0fec8285f147730d88400392acb9e]]></AppSignature>");
        sb.append("    <SignMethod><![CDATA[sha1]]></SignMethod>");
        sb.append("</xml>");
        String xml = sb.toString();
        Map<Object, Object> temp = doXMLParse(xml);
        System.out.println(temp.get("OpenId"));
        System.out.println(temp.get("AppId"));
        System.out.println(temp.get("IsSubscribe"));
        System.out.println(temp.get("ProductId"));
        System.out.println(temp.get("TimeStamp"));
        System.out.println(temp.get("NonceStr"));
        System.out.println(temp.get("AppSignature"));
        System.out.println(temp.get("SignMethod"));
    }
}
