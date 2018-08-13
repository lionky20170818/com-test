package com.ligl.common.utils;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 封装了一些采用HttpClient发送HTTP请求的方法
 * 
 * @see 本工具所采用的是HttpComponents-Client-4.2.1
 * @version v1.3
 * @history v1.0-->新建<code>sendGetRequest(String,String)</code>和
 *          <code>sendPostRequest(String,Map<String,String>,String,String)</code>
 *          方法
 * @history v1.1-->新增
 *          <code>sendPostSSLRequest(String,Map<String,String>,String,String)</code>
 *          方法,用于发送HTTPS的POST请求
 * @history v1.2-->新增
 *          <code>sendPostRequest(String,String,boolean,String,String)</code>
 *          方法,用于发送HTTP协议报文体为任意字符串的POST请求
 * @history v1.3-->新增<code>java.net.HttpURLConnection</code>实现的
 *          <code>sendPostRequestByJava()</code>方法
 */
public class HttpClientUtil {

    private HttpClientUtil() {

    }

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * 发送HTTP_GET请求
     * 
     * @see 该方法会自动关闭连接,释放资源
     * @param requestURL 请求地址(含参数)
     * @param decodeCharset 解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
     * @return 远程主机响应正文
     */
    public static String sendGetRequest(String reqURL, String decodeCharset) throws Exception {

        long responseLength = 0; // 响应长度
        String responseContent = null; // 响应内容
        HttpClient httpClient = new DefaultHttpClient(); // 创建默认的httpClient实例
        HttpGet httpGet = new HttpGet(reqURL); // 创建org.apache.http.client.methods.HttpGet
        try {
            HttpResponse response = httpClient.execute(httpGet); // 执行GET请求
            HttpEntity entity = response.getEntity(); // 获取响应实体
            if (null != entity) {
                responseLength = entity.getContentLength();
                responseContent = EntityUtils.toString(entity, decodeCharset == null ? "UTF-8" : decodeCharset);
                EntityUtils.consume(entity); // Consume response content
            }
        } catch (ClientProtocolException e) {
            log.error("该异常通常是协议错误导致,比如构造HttpGet对象时传入的协议不对(将'http'写成'htp')或者服务器端返回的内容不符合HTTP协议要求等,堆栈信息如下", e);
            throw new Exception("协议错误");
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            log.error("该异常通常是网络原因引起的,如HTTP服务器未启动等,堆栈信息如下", e);
            throw new Exception("网络原因");
        } finally {
            httpClient.getConnectionManager().shutdown(); // 关闭连接,释放资源
        }
        return responseContent;
    }

    public static Map<String, Object> getUserBaseInfo(String username) {
        final String user_url = "http://user.qbao.com" + "/api/get/user/";
        String securityKey = "dd44db9bf8d04a3eb9b2837fb44e2333";
        String sign = "username=" + username + "&securityKey=" + securityKey;
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        sign = encoder.encodePassword(sign, securityKey);
        try {

            String json = sendGetRequest(user_url + username + "/" + sign + ".html", null);

            //            String json = HttpConnector.get(user_url + username + "/" + sign
            //                    + ".html", null);
            log.info("根据用户：{} ,获取用户基本信息接口，返回 :  {}", username, json);
            Map<String, Object> map = JSON.<Map<String, Object>> parseObject(json, Map.class);
            String code = (String) map.get("code");
            if ("1".equals(code)) {// 用户存在
                json = (String) map.get("data");
                Map<String, Object> map2 = JSON.<Map<String, Object>> parseObject(json, Map.class);
                return map2;
            }
        } catch (Exception e) {
            log.error("根据用户：{} ,获取用户基本信息接口，错误：{}", username, e.getMessage(), e);
        }
        return null;
    }

    public static void main(String[] args) {


           	String url="http://192.168.0.43:80/cif/enterpriseservice/register.html";
           	String sendData="password=aaaaaaa&businessCertificateType=1&businessCertificateNo=bb1654544887545564aa&email=2356566565@qq.com&businessRegistrationNo=15646449749797&enterpriseName=资邦金服&registerAddress=杨高南路759号";
        			String json=sendPostRequest(url, sendData, true);
        			System.out.println(json);

    }

    /**
     * 发送HTTP_POST请求
     * 
     * @see 该方法为<code>sendPostRequest(String,String,boolean,String,String)</code>
     *      的简化方法
     * @see 该方法在对请求数据的编码和响应数据的解码时,所采用的字符集均为UTF-8
     * @see 当<code>isEncoder=true</code>时,其会自动对<code>sendData</code>中的[中文][|][
     *      ]等特殊字符进行<code>URLEncoder.encode(string,"UTF-8")</code>
     * @param isEncoder 用于指明请求数据是否需要UTF-8编码,true为需要
     */
    public static String sendPostRequest(String reqURL, String sendData, boolean isEncoder) {

        return sendPostRequest(reqURL, sendData, isEncoder, null, null);
    }

    /**
     * 发送HTTP_POST请求
     * 
     * @see 该方法会自动关闭连接,释放资源
     * @see 当<code>isEncoder=true</code>时,其会自动对<code>sendData</code>中的[中文][|][
     *      ]等特殊字符进行<code>URLEncoder.encode(string,encodeCharset)</code>
     * @param reqURL 请求地址
     * @param sendData
     *            请求参数,若有多个参数则应拼接成param11=value11?m22=value22?m33=value33的形式后,
     *            传入该参数中
     * @param isEncoder 请求数据是否需要encodeCharset编码,true为需要
     * @param encodeCharset 编码字符集,编码请求数据时用之,其为null时默认采用UTF-8解码
     * @param decodeCharset 解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
     * @return 远程主机响应正文
     */
    public static String sendPostRequest(String reqURL, String sendData, boolean isEncoder, String encodeCharset,
                                         String decodeCharset) {

        String responseContent = null;
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(reqURL);
        // httpPost.setHeader(HTTP.CONTENT_TYPE,
        // "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        try {
            if (isEncoder) {
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                for (String str : sendData.split("&")) {
                    formParams.add(new BasicNameValuePair(str.substring(0, str.indexOf("=")), str.substring(str
                        .indexOf("=") + 1)));
                }
                httpPost.setEntity(new StringEntity(URLEncodedUtils.format(formParams, encodeCharset == null ? "UTF-8"
                    : encodeCharset)));
            } else {
                httpPost.setEntity(new StringEntity(sendData));
            }

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                responseContent = EntityUtils.toString(entity, decodeCharset == null ? "UTF-8" : decodeCharset);
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            log.error("与[" + reqURL + "]通信过程中发生异常,堆栈信息如下", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }
    

    /**
     * 发送HTTP_POST请求
     * 
     * @see 该方法会自动关闭连接,释放资源
     * @see 该方法会自动对<code>params</code>中的[中文][|][ ]等特殊字符进行
     *      <code>URLEncoder.encode(string,encodeCharset)</code>
     * @param reqURL 请求地址
     * @param params 请求参数
     * @param encodeCharset 编码字符集,编码请求数据时用之,其为null时默认采用UTF-8解码
     * @param decodeCharset 解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
     * @return 远程主机响应正文
     * @throws Exception
     */
    public static String sendPostRequest(String reqURL, Map<String, String> params, String encodeCharset,
                                         String decodeCharset) throws Exception {
        String responseContent = null;
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(reqURL);

        List<NameValuePair> formParams = new ArrayList<NameValuePair>(); // 创建参数队列

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, encodeCharset == null ? "UTF-8" : encodeCharset));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                responseContent = EntityUtils.toString(entity, decodeCharset == null ? "UTF-8" : decodeCharset);
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            log.error("与[" + reqURL + "]通信过程中发生异常,堆栈信息如下", e);
            throw new Exception(e.getMessage());
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }

    public static String sendPostRequest(String reqURL, Map<String, String> params) throws Exception {

        return sendPostRequest(reqURL, params, null, null);
    }

    /**
     * 发送HTTPS_POST请求
     * 
     * @see 该方法为<code>sendPostSSLRequest(String,Map<String,String>,String,String)</code>
     *      方法的简化方法
     * @see 该方法在对请求数据的编码和响应数据的解码时,所采用的字符集均为UTF-8
     * @see 该方法会自动对<code>params</code>中的[中文][|][ ]等特殊字符进行
     *      <code>URLEncoder.encode(string,"UTF-8")</code>
     */
    public static String sendPostSSLRequest(String reqURL, Map<String, String> params) {

        return sendPostSSLRequest(reqURL, params, null, null);
    }

    /**
     * 发送HTTPS_POST请求
     * 
     * @see 该方法会自动关闭连接,释放资源
     * @see 该方法会自动对<code>params</code>中的[中文][|][ ]等特殊字符进行
     *      <code>URLEncoder.encode(string,encodeCharset)</code>
     * @param reqURL 请求地址
     * @param params 请求参数
     * @param encodeCharset 编码字符集,编码请求数据时用之,其为null时默认采用UTF-8解码
     * @param decodeCharset 解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
     * @return 远程主机响应正文
     */
    public static String sendPostSSLRequest(String reqURL, Map<String, String> params, String encodeCharset,
                                            String decodeCharset) {

        String responseContent = "";
        HttpClient httpClient = new DefaultHttpClient();
        X509TrustManager xtm = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            public X509Certificate[] getAcceptedIssuers() {

                return null;
            }
        };
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { xtm }, null);
            SSLSocketFactory socketFactory = new SSLSocketFactory(ctx);
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, socketFactory));

            HttpPost httpPost = new HttpPost(reqURL);
            List<NameValuePair> formParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(formParams, encodeCharset == null ? "UTF-8" : encodeCharset));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                responseContent = EntityUtils.toString(entity, decodeCharset == null ? "UTF-8" : decodeCharset);
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            log.error("与[" + reqURL + "]通信过程中发生异常,堆栈信息为", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }

    /**
     * 发送HTTP_POST请求
     * 
     * @see 若发送的<code>params</code>中含有中文,记得按照双方约定的字符集将中文
     *      <code>URLEncoder.encode(string,encodeCharset)</code>
     * @see 本方法默认的连接超时时间为30秒,默认的读取超时时间为30秒
     * @param reqURL 请求地址
     * @param params 发送到远程主机的正文数据,其数据类型为
     *            <code>java.util.Map<String, String></code>
     * @return 远程主机响应正文`HTTP状态码,如<code>"SUCCESS`200"</code><br>
     *         若通信过程中发生异常则返回"Failed`HTTP状态码",如<code>"Failed`500"</code>
     */
    public static String sendPostRequestByJava(String reqURL, Map<String, String> params) {

        StringBuilder sendData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sendData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (sendData.length() > 0) {
            sendData.setLength(sendData.length() - 1); // 删除最后一个&符号
        }
        return sendPostRequestByJava(reqURL, sendData.toString());
    }

    /**
     * 发送HTTP_POST请求
     * 
     * @see 若发送的<code>sendData</code>中含有中文,记得按照双方约定的字符集将中文
     *      <code>URLEncoder.encode(string,encodeCharset)</code>
     * @see 本方法默认的连接超时时间为30秒,默认的读取超时时间为30秒
     * @param reqURL 请求地址
     * @param sendData 发送到远程主机的正文数据
     * @return 远程主机响应正文`HTTP状态码,如<code>"SUCCESS`200"</code><br>
     *         若通信过程中发生异常则返回"Failed`HTTP状态码",如<code>"Failed`500"</code>
     */
    public static String sendPostRequestByJava(String reqURL, String sendData) {

        HttpURLConnection httpURLConnection = null;
        OutputStream out = null; // 写
        InputStream in = null; // 读
        int httpStatusCode = 0; // 远程主机响应的HTTP状态码
        try {
            URL sendUrl = new URL(reqURL);
            httpURLConnection = (HttpURLConnection) sendUrl.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true); // 指示应用程序要将数据写入URL连接,其值默认为false
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setConnectTimeout(30000); // 30秒连接超时
            httpURLConnection.setReadTimeout(30000); // 30秒读取超时

            out = httpURLConnection.getOutputStream();
            out.write(sendData.toString().getBytes());

            // 清空缓冲区,发送数据
            out.flush();

            // 获取HTTP状态码
            httpStatusCode = httpURLConnection.getResponseCode();

            // 该方法只能获取到[HTTP/1.0 200 OK]中的[OK]
            // 若对方响应的正文放在了返回报文的最后一行,则该方法获取不到正文,而只能获取到[OK],稍显遗憾
            // respData = httpURLConnection.getResponseMessage();

            // //处理返回结果
            // BufferedReader br = new BufferedReader(new
            // InputStreamReader(httpURLConnection.getInputStream()));
            // String row = null;
            // String respData = "";
            // if((row=br.readLine()) != null){
            // //readLine()方法在读到换行[\n]或回车[\r]时,即认为该行已终止
            // respData = row; //HTTP协议POST方式的最后一行数据为正文数据
            // }
            // br.close();

            in = httpURLConnection.getInputStream();
            byte[] byteDatas = new byte[in.available()];
            in.read(byteDatas);
            return new String(byteDatas) + "`" + httpStatusCode;
        } catch (Exception e) {
            log.error(e.getMessage());
            return "Failed`" + httpStatusCode;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    log.error("关闭输出流时发生异常,堆栈信息如下", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error("关闭输入流时发生异常,堆栈信息如下", e);
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
                httpURLConnection = null;
            }
        }
    }

    public static String doPost(List<NameValuePair> paramList, String url, String charset) throws IOException {

        HttpClient httpClient = new DefaultHttpClient();
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 30000);
        // httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,
        // charset);
        HttpPost post = new HttpPost(url);
        if (paramList != null && paramList.size() > 0) {
            post.setEntity(new UrlEncodedFormEntity(paramList, charset));
        }
        // post.getParams().setParameter("http.protocol.content-charset",charset);
        // post.getParams().setParameter(HTTP.CHARSET_PARAM, charset);
        // post.getParams().setParameter(HTTP.DEFAULT_PROTOCOL_CHARSET,charset);
        // 设置成了默认的恢复策略，在发生异常时候将自动重试3次，在这里你也可以设置成自定义的恢复策略
        // post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new
        // DefaultHttpMethodRetryHandler());
        // httpClient.getParams().setParameter(CoreProtocolPNames., arg1)
        try {
            HttpResponse response = httpClient.execute(post);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw e;
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public static String doPost(String reqUrl, Map<String, String> parameters, String recvEncoding) throws IOException {

        HttpURLConnection url_con = null;
        // String responseContent = null;
        StringBuilder sb = new StringBuilder();
        String vchartset = recvEncoding == "" ? "UTF-8" : recvEncoding;
        try {
            StringBuffer params = new StringBuffer();
            for (Iterator<?> iter = parameters.entrySet().iterator(); iter.hasNext();) {
                Entry<?, ?> element = (Entry<?, ?>) iter.next();
                params.append(element.getKey().toString());
                params.append("=");
                params.append(URLEncoder.encode(element.getValue().toString(), vchartset));
                params.append("&");
            }

            if (params.length() > 0) {
                params = params.deleteCharAt(params.length() - 1);
            }

            URL url = new URL(reqUrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("POST");
            url_con.setConnectTimeout(5000);
            url_con.setReadTimeout(10000);
            url_con.setDoOutput(true);
            byte[] b = params.toString().getBytes();
            url_con.getOutputStream().write(b, 0, b.length);
            url_con.getOutputStream().flush();
            url_con.getOutputStream().close();

            int code = url_con.getResponseCode();
            if (code != 200) {
                return "ERROR" + code;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(url_con.getInputStream()));
            String s = in.readLine();
            while (s != null) {
                sb.append(s);
                s = in.readLine();
            }
            /*
             * InputStream in = url_con.getInputStream(); byte[] echo = new
             * byte[10 * 1024]; int len = in.read(echo); responseContent = (new
             * String(echo, 0, len)).trim(); int code =
             * url_con.getResponseCode(); if (code != 200) { responseContent =
             * "ERROR" + code; }
             */

        } catch (IOException e) {
            throw e;
            // System.out.println("网络故障:"+ e.toString());
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }
        // return responseContent;
        return sb.toString();
    }

    /**
     * doPost:post请求   默认字符集为UTF-8. <br/>
     *
     * @param reqUrl
     * @param parameters
     * @return
     * @throws IOException
     */
    public static String doPost(String reqUrl, Map<String, String> parameters) throws IOException {
    	return doPost(reqUrl, parameters, "UTF-8");
    }
    
    /**
     * doPost:http Json字符串post请求. <br/>
     *
     * @param reqURL
     * @param json
     * @param charset
     * @return
     */
    public static String doPost(String reqURL,String json, String charset){
    	String responseContent = null;
    	HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(reqURL);
    	try {
    		charset = charset == null ? "UTF-8" : charset;
	    	StringEntity s = new StringEntity(json.toString());
	    	s.setContentEncoding(charset);
	    	s.setContentType("application/json");
	    	post.setEntity(s);
	    	 
	    	HttpResponse response = httpClient.execute(post);
	        HttpEntity entity = response.getEntity();
	        if (null != entity) {
	            responseContent = EntityUtils.toString(entity, charset);
	            EntityUtils.consume(entity);
	        } 
    	} catch (Exception e) {
            log.error("与[" + reqURL + "]通信过程中发生异常,堆栈信息如下", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
	}
    
    public static String sendPostRequest(String reqURL, String sendData) throws Exception {

        String responseContent = null;
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(reqURL);

        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
        try {
            httpPost.setEntity(new StringEntity(sendData,"UTF-8"));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                responseContent = EntityUtils.toString(entity, "UTF-8" );
                EntityUtils.consume(entity);
            }
        } catch (Exception e) {
            log.error("与[" + reqURL + "]通信过程中发生异常,堆栈信息如下", e);
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }
    
    /**
     * doPost:http Json字符串post请求, 字符编码UTF-8 <br/>
     *
     * @param reqURL
     * @param json
     * @return
     */
    public static String doPost(String reqURL,String json){
    	return doPost(reqURL, json, "UTF-8");
    }
}
