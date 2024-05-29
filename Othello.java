import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Othello extends JFrame implements MouseListener{
	public static int row = 8;
	private int turn = 0;
	public int[] grids = new int[row*row]; //黒を0,白を1,ボード(緑)を2に設定
	private int[] dx = new int[8];
	public int statecount = 0;
	//public int passcount = 0;
	public int myColor;
	public int rivalColor;
	private boolean emphasis = true;
	private boolean gu = true;
   
	private JTextArea chatArea;
	private JTextField inField;
	private JButton buttonArray[];
	private JButton stop, pass;
	private JButton sendm;
	private JLabel colorLabel; // 色表示用ラベル
	private JLabel turnLabel; // 手番表示用ラベル
	private JLabel chatLabel;
	private JLabel rivalRecord;
	private JLabel timeleftLabel;
	private Clock clock;
	public Container c; // コンテナ
	public static ImageIcon blackIcon, whiteIcon, boardIcon; //アイコン
	
	private Client client; //staticでいいのか確認
	private String myname;
	private String rivalname;
	
	
	
	public Othello(Client client, String myname,String rivalname, int win, int loss, int draw, int myColor, boolean emphasis){
		
		this.client = client;
		client.setMessageHandler(this::handleServerMassage);
		
		this.rivalname = rivalname;  
		this.myname = myname;
		this.emphasis = emphasis;
		this.myColor = myColor;
		this.rivalColor = myColor^1;
		
		
		
		for(int i = 0 ; i < row * row ; i++){
			grids[i] = 2; //初めは石が置かれていない
			int center = row * row / 2;
			grids[center - row / 2 - 1] = 0;
			grids[center + row / 2    ] = 0;
			grids[center - row / 2    ] = 1;
			grids[center + row / 2 - 1] = 1;
		}
		
		dx[0] = 1;
		dx[1] = -1;
		dx[2] = row;
		dx[3] = -row;
		dx[4] = row+1;
		dx[5] = -row-1;
		dx[6] = row-1;
		dx[7] = -row+1;
		
		c = getContentPane();
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpeg");
		c.setLayout(null);
		
		rivalRecord = new JLabel(rivalname + " : " +  win + "勝" + loss + "負" + draw +"引き分け");
		rivalRecord.setHorizontalAlignment(JLabel.CENTER);
		rivalRecord.setBounds(45*2, 20, (row*45)*2, 45*3-45*4/5);
		rivalRecord.setFont(new Font("Font.DIALOG_INPUT", Font.PLAIN, 30));
		c.add(rivalRecord);
		
		buttonArray = new JButton[row * row];
		int i;
		for(i = 0; i < row*row; i++) {
			if(grids[i] == 0) {
				buttonArray[i] = new JButton(blackIcon);
			}else if(grids[i] == 1) {
				buttonArray[i] = new JButton(whiteIcon);
			}else if(grids[i] == 2){
				buttonArray[i] = new JButton(boardIcon);
			}
			c.add(buttonArray[i]);
			
			int x = 45*3 + (i % row) * 45;
			int y = 45*3 + (int) (i / row) * 45;
			buttonArray[i].setBounds(x, y, 45, 45);
			buttonArray[i].addMouseListener(this);
			buttonArray[i].setActionCommand(Integer.toString(i));	
		}
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("ネットワーク対戦型オセロゲーム");
		setSize((row*45 - 50)*3 , (row * 45)*2 );
		c.setLayout(null);//
		
		
		stop = new JButton("投了");//終了ボタンを作成
		c.add(stop); 
		stop.setBounds(0 + 45*3, 45*3 +row * 45 + 30, (row * 45 + 10) / 2, 30);//終了ボタンの境界を設定
		stop.addMouseListener(this);
		stop.setActionCommand("stop");
		
		
		pass = new JButton("パス");//パスボタンを作成
		c.add(pass); 
		pass.setBounds(45*3 + (row * 45 + 10) / 2, 45*3 + row * 45 + 30, (row * 45 + 10 ) / 2, 30);//パスボタンの境界を設定
		pass.addMouseListener(this);
		pass.setActionCommand("pass");
		
		
		if(myColor == 0) {
			colorLabel = new JLabel(myname + " : " + "黒");
		}else if(myColor == 1) {
			colorLabel = new JLabel(myname + " : " + "白");
		}
		colorLabel.setBounds(45*3 , 45*3 + row * 45 + 60 , (row/2) * 45 + 10, 30);//境界を設定
		colorLabel.setHorizontalAlignment(JLabel.CENTER);
		c.add(colorLabel);
		
		
		if(turn == myColor) {
			turnLabel = new JLabel("あなたの番です");
		}else {
			turnLabel = new JLabel("相手の番です");
		}
		turnLabel.setBounds(45*3 + (row*45)/2, 45*3 + row * 45 + 60, (row/2) * 45 + 10, 30);//境界を設定
		turnLabel.setHorizontalAlignment(JLabel.CENTER);
		c.add(turnLabel);
		
		
		chatLabel = new JLabel("チャット欄");
		chatLabel.setBounds(45*3 + 30 + (row * 45 + 10) , 45*3+0, (row * 45 + 10)*4 / 5, 30);
		chatLabel.setHorizontalAlignment(JLabel.CENTER);
		c.add(chatLabel);
		
		
		JScrollPane sp = new JScrollPane();
		chatArea = new JTextArea("テスト");
		Color color = new Color(0,204,0);     //手動で設定しています
		chatArea.setBackground(color);
		sp.setViewportView(chatArea);
		sp.setBounds(45*3 + 30 + (row * 45 + 10), 45*3+30, (row * 45 + 10)*4 / 5, ((row-2) * 45 + 10));
		c.add(sp);
		
		
		inField = new JTextField(16);
		inField.setBounds(45*3 + 30 + (row * 45 + 10), 45*3 + 30 + ((row-2) * 45 + 10), (row * 45 + 10) * 4 / 5 - 60, 30);
		c.add(inField);
		
		
		sendm = new JButton("送信"); //チャットの送信ボタンを作成
		sendm.setBounds(45*3 + 30 + (row * 45 + 10) + (row * 45 + 10)*4/5  - 60,45*3 +  30 + ((row-2) * 45 + 10), 60, 30);
		sendm.addMouseListener(this);
		sendm.setActionCommand("sendm");
		c.add(sendm);         
		
		
		timeleftLabel = new JLabel("残り時間");
		timeleftLabel.setBounds(45*3 + 30 + (row * 45 + 10), 45*3 +  30 + ((row-2) * 45 + 10) + 30, (row*45+10)*4/5, 30);
		timeleftLabel.setHorizontalAlignment(JLabel.CENTER);
		c.add(timeleftLabel);
		
		
		if(turn == myColor) {
			clock = new Clock(this,false);
		}else {
			clock = new Clock(this,true);
		}
		clock.setBounds(45*3 + 30 + (row * 45 + 10), 45*3 +  30 + ((row-2) * 45 + 10) + 30 + 30, (row*45+10)*4/5, 40);
		clock.setHorizontalAlignment(JLabel.CENTER);
		clock.setFont(new Font("Font.DIALOG_INPUT", Font.ITALIC, 30));
		c.add(clock);
		
		if(emphasis == true) {
			this.judge(rivalColor, myColor);
		}
	}
	
	
	private void dispWait() {
		int i;
		for(i = 0; i < row*row; i++) {
			this.c.remove(buttonArray[i]);
		}
		c.revalidate();
		
		buttonArray = new JButton[row * row];
		
		for(i = 0; i < row*row; i++) {
			if(grids[i] == 0) {
				buttonArray[i] = new JButton(blackIcon);
			}else if(grids[i] == 1) {
				buttonArray[i] = new JButton(whiteIcon);
			}else if(grids[i] == 2){
				buttonArray[i] = new JButton(boardIcon);
			}
			
			this.c.add(buttonArray[i]);
			
			int x = 45*3+(i % row) * 45;
			int y = 45*3+(int) (i / row) * 45;
			buttonArray[i].setBounds(x, y, 45, 45);
			buttonArray[i].addMouseListener(this);
			buttonArray[i].setActionCommand(Integer.toString(i));
			
		}
		
		c.revalidate();
	}
	
	private void setClock(boolean reset) {
		this.c.remove(clock);
		this.c.revalidate();
		clock = new Clock(this,reset); //時計を再設定
		clock.setBounds(45*3 + 30 + (row * 45 + 10), 45*3 +  30 + ((row-2) * 45 + 10) + 30 + 30, (row*45+10)*4/5, 40);
		clock.setHorizontalAlignment(JLabel.CENTER);
		clock.setFont(new Font("Font.DIALOG_INPUT", Font.ITALIC, 30));
		c.add(clock);
		this.c.revalidate();
	}
	
	
	
	
	
	private boolean checkEdge(int num, int dx) {//numがオセロ盤のフチの外にいるか判定。numには現在の場所positionの値は絶対に来ない。
		
		if((num % row == 0 && (num + dx) %row == 7) || (num % row == 7 && (num + dx)%row == 0 )) {
			return true;
		}else {
			return false;
		}
		
	}
	
	private boolean putStone(int position, int rColor, int mColor) {
		int i,j,k;
		int x,y;
		boolean is = false;
		if(grids[position] != 2) {
			return false;
		}
		for(i = 0; i < 8; i++) {
			x = position + dx[i];
			if((0<=x && x < row*row ) && !checkEdge(position,dx[i]) && !checkEdge(x,dx[i]) && this.grids[x] == rColor) {
				for(j = 2; j <= 7; j++) {          //j <= 7でいいのか要考察↓
					y = position + j*dx[i];
					if(0 <= y && y < row*row) {
						if(this.grids[y] == mColor) {
							this.grids[position] = mColor;
							is = true;
							for(k = j - 1; k >= 1; k--) {
								this.grids[position + k*dx[i]] = mColor;
								System.out.println(this.grids[position + k*dx[i]]);
							}
							break;
						}else if(this.grids[y] == 2) {
							break;
						}
						                  
						if(checkEdge(y, dx[i])) {
							break;
						}
						
					}else {
						break;
					}
				}
			}
		}
		for(int z = 0; z < row*row; z++) {
			System.out.printf(this.grids[z]+ " ");
			if(z%row == row-1) {
				System.out.println();
			}
		}
		dispWait();
		
		if(is == true) {
			sendRecord();
			clock.resetTime();
			setClock(true);
			return true;
		}else {
			judge(rivalColor, myColor);
			return false;
		}
		
	}
	
	
	
	private boolean judge(int rColor, int mColor) {                  //本当に置けない状況か判断
		int i,j,k;
		int x,y;
		for(i = 0; i < row*row; i++) {
			if(this.grids[i] == 2) {
				for(j = 0; j < 8; j++) {
					x = i + dx[j];
					if((0 <= x && x < row*row) && !checkEdge(i,dx[j]) && !checkEdge(x,dx[j]) && this.grids[x] == rColor) {
						for(k = 2; k <= 7; k++) {  //本当にk <= 7か検討
							y = i + k*dx[j];
							if((0 <= y && y < row*row)) {
								if(this.grids[y] == mColor) {
									if(emphasis == true) {
										buttonArray[j].setBackground(Color.ORANGE);
									}else {
										return false;
									}
								}else if(this.grids[y] == 2) {
									break;
								}
							}
							
							if(checkEdge(y, dx[j])) {
								break;                    //ここらへん後でしっかり確認
							}
						}
					}
				}
			}
		}
		
		return true;
	}
	
	
	private void handleServerMassage(String message) {         //受信用メソッド、自動的に動作する(はず)
		int i;
		String str;
		String[] parts = message.split("",2);
		System.out.println("Received message: " + message);
		String sgc = "sendGridSuccess ";
		String scms = "sendChatMessageSuccess";
		String fpds = "fetchPassDataSuccess";
		String fsds = "fetchStopDataSUccess";
        if (message.startsWith(sgc)) {
        	str = parts[1];
        	for(i = 0; i < row*row; i++) {
        		this.grids[i] = str.charAt(i);
        	}
        	if(str.charAt(i) == 4) {  //4はあいてが投了の意味
        		checkWinner(2);
        	}else if(str.charAt(i) == 2) { //2は自分も相手もパスした、の意味
        		checkWinner(0);            //通常の終了の仕方
        	}else if(str.charAt(i) == 1) {
        		statecount = 1;
        		dispWait();
        		this.chatArea.setText(this.chatArea.getText() + "\n" + "システム:パスされました");
            	turn = myColor;
            	turnLabel.setText("あなたの番です");
            	setClock(false);
        	}else {
        		statecount = 0;
        		dispWait();
            	turn = myColor;
            	turnLabel.setText("あなたの番です");
            	setClock(false);
        	}
            //System.out.println(message);
            
        } else if (message.startsWith("fetchBoardDataFail")) {
            //JOptionPane.showMessageDialog(loginFrame, "Login Failed", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (message.startsWith(scms)) {
        	str = parts[1];
			this.chatArea.setText(this.chatArea.getText() + "\n" + rivalname +  ":" + str);
            //*System.out.println(message);
            
        } else if (message.startsWith("signup failed")) {
            //JOptionPane.showMessageDialog(loginFrame, "Signup Failed", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (message.startsWith(fpds)) {
        	
        	
        } else if (message.startsWith(fsds)) {
        	
        	
        }
        
	}
	
	public void checkWinner(int stop) {
		Result result = new Result(this, grids, stop,ModalityType.APPLICATION_MODAL);
		result.setLocation(this.getLocation().x + (this.getWidth() - result.getWidth()) / 2, this.getLocation().y + this.getHeight()/4);
		result.setVisible(true);
		
	}
	
	
	
	private void sendMessage(String msg) {  //新規メッセージを送信
		client.sendMessage("sendChatMessageSuccess " + msg);	//fetchChatDataSuccess等を""内に追加
	}
	
	private void setMessage(String username, String msg) { //チャット欄にメッセージをセット
		this.chatArea.setText(this.chatArea.getText() + "\n" + username +  ":" + msg);
		sendMessage(msg);
	}
	private void sendRecord() {
		//盤面データは8×8=64桁を文字列で送る
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < row*row; i++) {
			sb.append(String.valueOf(grids[i]));
		}
		sb.append(String.valueOf(statecount));
		turn = rivalColor;
		client.sendMessage("sendGridSuccess " + sb.toString()); //fetchBoardDataSuccess等入れる
		turnLabel.setText("相手の番です");
	}
	
	void getGiveupmessage(boolean TF) {
		gu = TF;
	}
	
	public void mouseClicked(MouseEvent e) {
		JButton button = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
		String command = button.getActionCommand();
		
		
		if(command.equals("stop")) {
			OthelloDialog othellodialog = new OthelloDialog(this, ModalityType.APPLICATION_MODAL);
			othellodialog.setLocation(this.getLocation().x + (this.getWidth() - othellodialog.getWidth()) / 2, this.getLocation().y + (this.getHeight() - othellodialog.getHeight())/2);
			othellodialog.setVisible(true);
			if(gu == true) {
				statecount = 4;
				sendRecord(); 
				checkWinner(1);
				//giveup();
				
			}
		}else if(command.equals("pass")) {
			if(judge(rivalColor,myColor) == true) {
				statecount++;
				sendRecord();
			}else {
				AblePut ableput = new AblePut(this, ModalityType.APPLICATION_MODAL);
				ableput.setLocation(this.getLocation().x + (this.getWidth() - ableput.getWidth()) / 2, this.getLocation().y + (this.getHeight() - ableput.getHeight())/2);
				ableput.setVisible(true);
				
			}
		}else if(command.equals("sendm")) {
			setMessage(myname ,inField.getText());      
		}else {
			if(putStone(Integer.parseInt(command), rivalColor, myColor) == false) {
				CantPut cantput = new CantPut(this,ModalityType.APPLICATION_MODAL);
				cantput.setLocation(this.getLocation().x + (this.getWidth() - cantput.getWidth()) / 2, this.getLocation().y + (this.getHeight() - cantput.getHeight())/2);
				cantput.setVisible(true);
			}else {
				statecount = 0;
				sendRecord();
			}
		}
	}
	
	public void mouseEntered(MouseEvent e) {
		/*JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
		String command = theButton.getActionCommand();
		
		if(Character.isDigit(command.charAt(0)) == true) {
			dispWait();
		}*/
		
	}
	
	public void mouseExited(MouseEvent e) {
		/*JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
		String command = theButton.getActionCommand();
		
		if(Character.isDigit(command.charAt(0)) == true) {
			dispWait();
		}*/
	}
	
	public void mousePressed(MouseEvent e) {
		
	}
	
	public void mouseReleased(MouseEvent e) {
		
	}
	
	
	
	/*public static void main(String[] args) {
		//Client client = new Client();//レコードクラスからクライアントのインスタンスをもらう、
		Othello othello = new Othello(client);
		othello.setVisible(true);
		
		
	}*/

}


class Clock extends JLabel{
	Othello othello;
	private int min = 3;
	int i;
	boolean stop = false;
	
	Clock(Othello othello, boolean reset){
		this.othello = othello;
		Timer t = new Timer();
		t.schedule(new ClockTask(reset), 1000);
	}
	
	void setTime() {
		for(i = min*60; i >= 0;i--) {
			this.setText(i/60 + ":" + String.format("%02d", i%60));
			if(stop == true) {
				break;
			}
			try {
				Thread.sleep(1000);
			}catch(Exception e) {
				
			}
		}
		if(stop != true) {
			
			othello.checkWinner(1);
		}
	}
	
	void resetTime() {
		stop = true;
		this.setText("99:99");
	}
	
	class ClockTask extends TimerTask{
		boolean reset;
		
		ClockTask(boolean reset){
			this.reset = reset;
		}
		
		public void run() {
			if(reset == false) {
				setTime();
			}else {
				resetTime();
			}
		}
	}
}

class CantPut extends JDialog {
	Othello othello;
	JLabel label;
	
	CantPut(Othello othello,ModalityType mt){
		super(othello,mt);
		this.othello = othello;
		this.setSize(300, 150);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setLayout(new GridLayout(3, 1));
		
		this.label = new JLabel("ここには置けません。");
		this.add(this.label);
	}
}


class AblePut extends JDialog{
	Othello othello;
	JLabel label;
	
	AblePut(Othello othello,ModalityType mt){
		super(othello,mt);
		this.othello = othello;
		this.setSize(300, 150);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setLayout(new GridLayout(3, 1));
		
		this.label = new JLabel("まだ置けます。パスできません。");
		this.add(this.label);
	}
	
}

class Result extends JDialog implements ActionListener{
	Othello othello;
	JLabel winnerlabel;
	JLabel countlabel;
	Container c;
	public int grids[] = new int[Othello.row*Othello.row];
	JButton buttonArray[];
	JButton next;
	String str;
	int i;
	int stop;
	public int black = 0;
	public int white = 0;
	public int winner = 0;
	
	Result(Othello othello, int[] grids, int stop, ModalityType mt){
		super(othello, mt);
		this.othello = othello;
		this.stop = stop;
		
		for(i = 0; i < Othello.row*Othello.row; i++) {
			this.grids[i] = grids[i];
		}
		
		this.setSize((Othello.row + 4)*45 , (Othello.row + 4)*45 + 50);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		

		c = getContentPane();
		buttonArray = new JButton[Othello.row * Othello.row];
		//int i;
		
		for(i = 0; i < Othello.row*Othello.row; i++) {
			if(this.grids[i] == 0) {
				buttonArray[i] = new JButton(Othello.blackIcon);
			}else if(this.grids[i] == 1) {
				buttonArray[i] = new JButton(Othello.whiteIcon);
			}else if(this.grids[i] == 2){
				buttonArray[i] = new JButton(Othello.boardIcon);
			}
			c.add(buttonArray[i]);
			
			int x = 45*2 + (i % Othello.row) * 45;
			int y = 45*1 + (int) (i / Othello.row) * 45;
			buttonArray[i].setBounds(x, y, 45, 45);
		}
		
		
		c.setLayout(null);
		
		
		
		
		for(i = 0; i < Othello.row*Othello.row; i++) {
			if(grids[i] == 0) {
				black++;
			}else if(grids[i] == 1) {
				white++;
			}
		}
		
		if(black > white) {
			winner = 0;
		}else if(black < white) {
			winner = 1;
		}else {
			winner = 2;
		}
		
		if(stop == 0) { //通常
			if(winner == 2) {
				str = "引き分け";
			}else if(winner == othello.myColor) {
				str = "あなたの勝ち!";
			}else {
				str = "相手の勝ち";
			}
		}else if(stop == 1){ //自分が投了/時間切れした場合
			str = "相手の勝ち";
		}else {        //相手が投了した場合
			str = "あなたの勝ち";
		}
		
		this.winnerlabel = new JLabel(str);
		winnerlabel.setHorizontalAlignment(JLabel.CENTER);
		winnerlabel.setBounds(45*2,Othello.row*45+30 * 45*1, Othello.row*45, 30);
		c.add(this.winnerlabel);
		
		this.countlabel = new JLabel("黒 : " + black + ", 白 : " + white);
		countlabel.setHorizontalAlignment(JLabel.CENTER);
		countlabel.setBounds(45*2,Othello.row*45+30 + 45*1 + 30, Othello.row*45, 30);
		c.add(this.countlabel);
		
		this.next = new JButton("マッチング画面に戻る");
		c.add(next); 
		this.next.setBounds(0 + 45*2 + 45*2, Othello.row * 45+30 + 45*1 + 60, (Othello.row * 45) / 2, 30);//終了ボタンの境界を設定
		this.next.addActionListener(this);
		this.next.setActionCommand("stop");
		
	}
	
	public void actionPerformed(ActionEvent e) {
		this.dispose();
		othello.dispose();
	}
	
}
