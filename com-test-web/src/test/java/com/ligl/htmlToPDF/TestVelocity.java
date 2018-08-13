package com.ligl.htmlToPDF;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.Date;
import java.util.Random;

/**
 * Created by liguoliang on 2017/8/16.
 * http://blog.csdn.net/clementad/article/details/51819647
 */
public class TestVelocity {

    public static void main(String[] args) throws Exception {
        //初始化并取得Velocity引擎
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        //取得velocity的模版
        Template t = ve.getTemplate("com-test-web/src/webapp/hello.vm", "UTF-8");
        //velocity 在给路劲时会比较麻烦
        //取得velocity的上下文context
        VelocityContext context = new VelocityContext();
        //往vm中写入信息
        context.put("serialNumber", "20171019001A");
        context.put("accountName", "张三");
        context.put("openingBank", "工商银行");
        context.put("accountNumber", "4123123123123");
        context.put("seller", "李四");
        context.put("legalRepresentative", "王五");
        context.put("phone", "13098123123");
        context.put("date", (new Date()).toString());
        StringWriter writer = new StringWriter();
        //把数据填入上下文
        t.merge(context, writer);
        //输出流
        String out = writer.toString();
        System.out.println(out);

        int max = 1000;
        int min = 1;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        //生成pdf
        String path = "E://MyHtml" + s + ".html";
        //将数据写入生成html文件
        PrintStream printStream = new PrintStream(new FileOutputStream(path));
        try {
            printStream.println(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            printStream.close();
        }

        //html转pdf
        String inputFile = path;

        String url = new File(inputFile).toURI().toURL().toString();
        String outputFile = "e://test2.pdf";
        System.out.println(url);
        OutputStream os = new FileOutputStream(outputFile);
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(url);
        try {
            // 解决中文支持问题
            ITextFontResolver fontResolver = renderer.getFontResolver();
            fontResolver.addFont("C:/Windows/Fonts/SIMSUN.TTC",
                    BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            // 解决图片的相对路径问题
            // renderer.getSharedContext().setBaseURL("file:/D:/z/temp/");
            renderer.layout();
            renderer.createPDF(os);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            os.close();
            //删除指定文件
            System.out.println(path);
            File file = new File(path);
            file.delete();
        }

    }


}
