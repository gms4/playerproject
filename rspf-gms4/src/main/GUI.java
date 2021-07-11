package main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUI extends player implements ActionListener, ListSelectionListener, WindowConstants {

	private JButton addButton;
	private JButton removeButton;
	private JList setlist; //lista de músicas
	private JFrame front; //interação com o usuário
	int mscPos; int mscLength;

	/* Setando aqui os botões necessários e 
	 * estruturas básicas pra o front funcionar
	 */

	public GUI() {

		addButton = new JButton("Add");
		addButton.addActionListener(this);
		addButton.setActionCommand("add");
		addButton.setBounds(445, 120, 80, 40);

		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);
		removeButton.setActionCommand("remove");
		removeButton.setBounds(30, 120, 80, 40);

		setlist = new JList();
		setlist.setModel(playlist);
		setlist.addListSelectionListener(this);

		//possibilidade de ver as músicas com scrolling
		JScrollPane set = new JScrollPane(setlist);
		set.setViewportView(setlist);
		set.setBounds(30, 170, 495, 130);

		//ativando layout
		/*JPanel layout = new JPanel();
		layout.setLayout(null);
		layout.add(addButton);
		layout.add(removeButton);
		layout.add(set);*/
		
		//testei panel e table, ainda decidindo qual vou usar

		JTable layout = new JTable();
		layout.setLayout(null);
		layout.add(addButton);
		layout.add(removeButton);
		layout.add(set);
		Color c = new Color(0, 0, 0);
		layout.setBackground(c);

		//agora, o front
		front = new JFrame();
		front.add(layout);
		front.setTitle("Eattron Player: 2021 chegou e agora eu sou cringe");
		front.setSize(570, 400);
		front.setDefaultCloseOperation(EXIT_ON_CLOSE);
		//front.setBackground(c);
		front.setVisible(true);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GUI();
	}

	//banco de ações
	//aqui vai ter todas as ações que forem clicadas pelo usuário, e como elas funcionarão
	//o optionpane do java swing já coloca umas janelinhas de input
	@Override
	public void actionPerformed(ActionEvent ae) {
		// TODO Auto-generated method stub
		String action = ae.getActionCommand();
		if (action.equals("add")) {

			String artist = JOptionPane.showInputDialog(front, "Artist", "Who's the Artist?", -1);
			String music = JOptionPane.showInputDialog(front, "Music", "What's the song's called?", -1);
			String length = JOptionPane.showInputDialog(front, "Length", "And how long is it?", -1);
			initAdd(artist + " - " + music, length);

		} else if (action.equals("remove")) {
			//tem que ver se tem algo selecionado pra remover
			if(isntEmpty()) {
				initRemove(mscPos);
				//coloquei a thread pra esperar um pouco depois da exclusão
				try {
					Thread.sleep(2);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}

	private boolean isntEmpty() {
		// TODO Auto-generated method stub
		//se não tiver nada selecionado, retorna null e não roda o remove
		if (setlist.getSelectedValue() != null) return true;
		else return false;
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
        if (isntEmpty()) {
        	mscPos = setlist.getSelectedIndex();
        }
	}
}

