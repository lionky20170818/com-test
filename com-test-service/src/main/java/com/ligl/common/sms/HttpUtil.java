package com.ligl.common.sms;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTPClient
 * @author zhushanshan
 * 2017年10月16日 下午2:33:15
 */
@Slf4j
public class HttpUtil {
	private HttpUtil(){

	}
    public static String postForm(String url,Map<String,Object> param) {
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost(url);
        // 创建参数队列
        List<BasicNameValuePair> formparams = new ArrayList<>();
        for(Map.Entry<String,Object> entry:param.entrySet()){
        	String key=entry.getKey();
			Object value = entry.getValue();
			String paramValue=value==null?"":value.toString();
			formparams.add(new BasicNameValuePair(key,paramValue));
		}
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams, CommonConstants.DEFAULT_ENCODING);
            httppost.setEntity(uefEntity);
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return  EntityUtils.toString(entity, CommonConstants.DEFAULT_ENCODING);
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
			log.error("异常信息",e);
        } catch (UnsupportedEncodingException e1) {
			log.error("异常信息",e1);
        } catch (IOException e) {
			log.error("异常信息",e);
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
				log.error("异常信息",e);
            }
        }
        return null;
    }
    
    public static String postForm(String url,Map<String,Object> param, String token) {
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = null;
        try {
        if(url.startsWith("https:")){
        	httpclient = createSSLInsecureClient();
        }else{
        	httpclient = HttpClients.createDefault();
        }
        log.info("token:",token);
        // 创建httppost
        HttpPost httppost = new HttpPost(url);
        // 创建参数队列
        List<BasicNameValuePair> formparams = new ArrayList<>();
		for(Map.Entry<String,Object> entry:param.entrySet()){
			Object value = entry.getValue();
			String key=entry.getKey();
			String paramValue=value==null?"":value.toString();
			formparams.add(new BasicNameValuePair(key,paramValue));
		}
        UrlEncodedFormEntity uefEntity;
       
            uefEntity = new UrlEncodedFormEntity(formparams, CommonConstants.DEFAULT_ENCODING);
            httppost.setEntity(uefEntity);
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return  EntityUtils.toString(entity, CommonConstants.DEFAULT_ENCODING);
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
           log.error("异常信息",e);
        } finally {
            // 关闭连接,释放资源
            try {
            	if(httpclient!=null){
					httpclient.close();
				}
            } catch (IOException e) {
				log.error("异常信息",e);
            }
        }
        return null;
    }
    
    
    /**
     * 创建 SSL连接
     * @return
     * @throws GeneralSecurityException
     */
    private static CloseableHttpClient createSSLInsecureClient() throws GeneralSecurityException {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}

				@Override
				public void verify(String host, SSLSocket ssl) throws IOException {
					//do something
				}

				@Override
				public void verify(String host, X509Certificate cert) throws SSLException {
					//do something
				}

				@Override
				public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
					//do something
				}

			});

			return HttpClients.custom().setSSLSocketFactory(sslsf).build();

		} catch (GeneralSecurityException e) {
			throw e;
		}
    }
    
}
