package com.ligl.common.applet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Created by liguoliang on 2017/7/10 0010.
 */
public class SimpleNotepad implements ActionListener {

    private Frame frame;
    private FileDialog fd_load;
    private FileDialog fd_save;
    private TextArea ta;
    private String file = "";
    private MenuItem save;
    private RandomAccessFile raf;
    private FileChannel fci;
    private FileLock flock;
    private CharsetEncoder encoder;
    private CharsetDecoder decoder;
    public static void main( String args[]) {
        new SimpleNotepad().init();
    }
    public void init(){
        frame = new Frame("My Notepad");
        MenuBar mb = new MenuBar();
        Menu file = new Menu("文件");
        Menu help = new Menu("帮助");
        MenuItem open = new MenuItem("打开");
        save = new MenuItem("保存");
        save.setEnabled(false);
        file.add(open);
        file.add(save);
        mb.add(file);
        mb.add(help);
        frame.setMenuBar(mb);
        ta = new TextArea();
        frame.add(ta,"Center");
        open.addActionListener(this);
        save.addActionListener(this);
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
        frame.setSize(600,400);
        frame.setLocation(300,100);
        frame.setVisible( true);
        fd_load = new FileDialog(frame,"打开文件",FileDialog.LOAD);
        fd_save = new FileDialog(frame,"保存文件",FileDialog.SAVE);
        Charset charset = Charset.forName(System.getProperty("file.encoding"));
        encoder = charset.newEncoder();
        decoder = charset.newDecoder();
    }
    public void actionPerformed(ActionEvent e){
        String s = e.getActionCommand();
        if(s.equals("打开")){
            fd_load.setVisible(true);
            String d = fd_load.getDirectory();
            String f = fd_load.getFile();
            if((d != null) && (f != null)){
                String destfile = d + f;
                if(destfile.equals(file)){
                    return;
                }else{
                    this.closeFile();
                    file = destfile;
                    this.loadFile();
                }
            }
        }else if(s.equals("保存")){
            this.saveFile();
        }
    }
    public void loadFile(){
        try{
            raf = new RandomAccessFile(file,"rw");
            fci = raf.getChannel();
            flock = fci.tryLock();
            if(flock == null){
                ta.setText("");
                JOptionPane.showMessageDialog(null,
                        "文件正在使用中，无法以独占的方式打开！",
                        "错误提示", JOptionPane.ERROR_MESSAGE);
                file = "";
                raf.close();
                raf = null;
            }else{
                int length = (int)fci.size();
                ByteBuffer bb = ByteBuffer.allocate(length);
                fci.read(bb);
                bb.flip();
                CharBuffer cb = decoder.decode(bb);
                ta.setText(cb.toString());
                frame.setTitle("My Notepad - " + file);
                save.setEnabled(true);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void saveFile(){
        String content = ta.getText();
        try{
            CharBuffer cb = CharBuffer.wrap(content.toCharArray());
            ByteBuffer bb = encoder.encode(cb);
            raf.setLength(0);
            fci.write(bb);
            fci.force(true);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void closeFile(){
        try{
            if(flock != null){
                flock.release();
            }
            if(raf != null){
                raf.close();
            }
            file = "";
            frame.setTitle("My Notepad");
            save.setEnabled(false);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
