package com.huwl.oracle.kylin_control_win.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;

import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.huwl.oracle.kylinremotecontrol.beans.NetMessage;
import com.huwl.oracle.kylinremotecontrol.beans.Terminal;

public class LoginWindow extends JWindow{
	private static List<ImageIcon> loadings=new ArrayList();
	private static int loadingHeight=59;
	private static  int loadingWidth=new Double(loadingHeight*1.14).intValue();
	private static Terminal terminal;
	private volatile boolean loadingFlag=true;
	static{
		for(int i=0;i<30;i++){
			Image loadingImg=null;
			try {
				loadingImg = ImageIO.read(LoginWindow.class.getClassLoader().getResourceAsStream("imgs/loading"+i+".png"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			loadings.add(new ImageIcon(loadingImg.getScaledInstance(loadingWidth
					, loadingHeight, Image.SCALE_DEFAULT)));
		}
		File f=new File("src/main/resources/terminal.info");
		
		if(f.exists()){
			try {
				ObjectInputStream in=new ObjectInputStream(new FileInputStream(f));
				try {
					terminal=(Terminal) in.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			String uuid=UUID.randomUUID().toString();
			String os=System.getProperty("os.name"); 
	        String name = "";
	        try {
	        	InetAddress addr =InetAddress.getLocalHost();//新建一个InetAddress类
	            name = addr.getHostName().toString();// 获得本机名称
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        terminal=new Terminal(uuid, name, os);
			try {
				ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(f));
				out.writeObject(terminal);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	private JPanel leftPanel,rightPanel;
	private JLabel brandLabel,loadingLable,loadTextLabel,qrcodeLabel;
	private Color colorBack=new Color(50, 51, 56),colorFore=new Color(240,240,240);
	private Socket socket;
	public LoginWindow() {
		try {
			socket = new Socket("27.22.95.52",5544);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Dimension dimension=Toolkit.getDefaultToolkit().getScreenSize();
		int width=500,height=170;
		this.setBounds(new Double((dimension.getWidth()-width)/2).intValue()
				, new Double((dimension.getHeight()-height)/2).intValue()
				, width, height);
		this.setVisible(true);
		this.getContentPane().setBackground(colorBack);
		this.setAlwaysOnTop(true);
		this.setLayout(null);
		
		
		initConponents();
		listen();
		requestLogin();
		
		rePain();
		
	}
	private void listen() {
		new Thread(){
			@Override
			public void run() {
				while(true){
					ObjectInputStream in=null;
					try {
						in=new ObjectInputStream(socket.getInputStream());
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("服务器断开");
						break;
					}
					NetMessage msg=null;
					try {
						msg=(NetMessage) in.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					final NetMessage proxMsg=msg;
					new Thread(){
						public void run() {
							
							if(proxMsg.getForWhat()==NetMessage.PROVIDE_QR_CODE){
								NetMessage netMessage=new NetMessage();
								netMessage.setForWhat(NetMessage.VALIDATE_LOGIN);
								netMessage.getMap().put("terminal", terminal);
								netMessage.getMap().put("date",new Date());
								Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();  
						        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
						        BitMatrix bitMatrix=null;
								try {
									bitMatrix = new MultiFormatWriter().encode(JSONObject.toJSONString(netMessage),  
									        BarcodeFormat.QR_CODE, 164, 164, hints);
								} catch (WriterException e) {
									e.printStackTrace();
								}// 生成矩阵  
						        BufferedImage bimg=MatrixToImageWriter.toBufferedImage(bitMatrix
						        		, new MatrixToImageConfig(0xFF323338,0xFFFAFAFA));
								 
						        loadingFlag=false;
						        rightPanel.remove(loadingLable);
						        rightPanel.remove(loadTextLabel);
						        qrcodeLabel.setIcon(new ImageIcon(bimg));
						        rightPanel.add(qrcodeLabel);
						        LoginWindow.this.rePain();
						        
							}
						};
					}.start();
				}
			}
		}.start();
	}
	private void requestLogin() {
	
		final NetMessage m=new NetMessage();
		m.setForWhat(NetMessage.REQUEST_QR_CODE);
		m.getMap().put("terminal",terminal);
		new Thread(){
			public void run() {
				m.send(socket);
				/*while(true){
					try {
						ObjectInputStream in=new ObjectInputStream(socket.getInputStream());
						NetMessage imgMsg=null;
						try {
							imgMsg=(NetMessage) in.readObject();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}*/
			};
		}.start();
		
	}
	private void initConponents() {
		Image brandIcon=null;
		try {
			brandIcon = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("imgs/brandIcon.png"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  
		brandLabel=new JLabel();
		int iconHeight=80;
		int iconWidth=new Double(iconHeight*1.4).intValue();
		brandLabel.setIcon(new ImageIcon(brandIcon.getScaledInstance(iconWidth, iconHeight, Image.SCALE_DEFAULT)));
		brandLabel.setVisible(true);
		brandLabel.setBounds((300-iconWidth)/2, (170-iconHeight)/2, iconWidth, iconHeight);
		
		
		leftPanel=new JPanel();
		leftPanel.setVisible(true);
		leftPanel.setBounds(0, 0, 300, 170);
		leftPanel.setBackground(colorBack);
		leftPanel.setLayout(null);
		leftPanel.add(brandLabel);
		this.add(leftPanel);
		
		
//		loginBtn=new JLabel("登录");
//		loginBtn.setBounds(0, 0, 100, 30);
		
		loadingLable=new JLabel();
		loadingLable.setVisible(true);
		loadingLable.setBounds((197-loadingWidth)/2, (164-loadingHeight)/2
				, loadingWidth, loadingHeight);
		new Thread(){
			public void run() {
				while(loadingFlag){
					for(int i=0;i<30;i++){
						loadingLable.setIcon(loadings.get(i));
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
		}.start();
		
		loadTextLabel=new JLabel("正在加载");
		loadTextLabel.setVisible(true);
		loadTextLabel.setBounds(0,((164-loadingHeight)/2)+63,197,30);
		loadTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loadTextLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		
		rightPanel=new JPanel();
		rightPanel.setVisible(true);
		rightPanel.setBounds(300, 3, 197, 164);
		rightPanel.setBackground(new Color(250,250,250));
		rightPanel.setLayout(null);
		rightPanel.add(loadingLable);
		rightPanel.add(loadTextLabel);
		this.add(rightPanel);
		
		qrcodeLabel=new JLabel();
		qrcodeLabel.setVisible(true);
		qrcodeLabel.setBounds((197-164)/2, 0, 197, 164);
		
		
		
	}
	private void rePain(){
		this.paint(getGraphics());
		this.paintComponents(getGraphics());
		this.paintAll(getGraphics());
		
	}
	public static void main(String[] args) {
		LoginWindow loginWindow=new LoginWindow();
	}

}
