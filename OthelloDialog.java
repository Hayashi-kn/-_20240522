import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class OthelloDialog extends JDialog implements ActionListener{
	Othello othello;
	JLabel label;
	JButton yes;
	JButton no;
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.yes) {
			this.othello.getGiveupmessage(true);
			this.dispose();          //dispose()はちょろっと調べただけなので挙動に注意
		}else if(e.getSource() == this.no) {
			this.othello.getGiveupmessage(false);
			this.dispose();
		}
	}
	
	OthelloDialog(Othello othello){
		super(othello);
		this.othello = othello;
		this.setSize(300, 150);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setLayout(new GridLayout(3, 1));
		
		this.label = new JLabel("本当に投了しますか?");
		this.add(this.label);
		
		
		this.yes = new JButton("はい");
		this.yes.setActionCommand("Yes");
		this.yes.addActionListener(this);
		this.add(this.yes);
		
		this.no = new JButton("いいえ");
		this.no.setActionCommand("No");
		this.no.addActionListener(this);
		this.add(this.no);
		
	}
}
