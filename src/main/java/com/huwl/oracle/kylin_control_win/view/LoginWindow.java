package com.huwl.oracle.kylin_control_win.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class LoginWindow extends JWindow{
	private JPanel leftPanel,rightPanel;
	private JLabel brandLabel;
	private Color colorBack=new Color(50, 51, 56);
	private JLabel loginBtn;
	public LoginWindow() {
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
		
		this.paint(getGraphics());
		this.paintAll(getGraphics());
		this.paintComponents(getGraphics());
		
		
		
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
		
		
		loginBtn=new JLabel("登录");
		loginBtn.setBounds(0, 0, 100, 30);
		
		
		
		rightPanel=new JPanel();
		rightPanel.setVisible(true);
		rightPanel.setBounds(300, 3, 197, 164);
		rightPanel.setBackground(new Color(250,250,250));
		this.add(rightPanel);
		
		
	}
	public static void main(String[] args) {
		LoginWindow loginWindow=new LoginWindow();
	}

}
