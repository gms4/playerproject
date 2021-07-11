package main;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.DefaultListModel;

public class player {

	/* Precisamos de um lock pra lidar com a lista de m�sicas, para
	 * que nada seja executado de forma precipitada.
	 * Tamb�m precisamos de uma forma de agrupar a lista de m�sicas
	 * e as suas espectivas dura��es. vou usar list model, visto que ele j� colabora com o display.
	 * Vou usar tamb�m um boolean pra me auxiliar em saber se tem outra
	 * thread usando a lista de m�sicas, porque se sim esperaremos a
	 * opera��o acabar. Para sinalizar a��es dentro da lista de m�sica, 
	 * vou criar uma vari�vel da categoria concurrent.condition.
	 */

	/* Primeira entrega:
	 * add (done)
	 * remove (done)
	 * listar pra mostrar que add e remove funcionam (done)
	 */

	public Lock lock = new ReentrantLock();
	public boolean taken = false; //outra thread "pegou" a lista por enquanto
	public DefaultListModel<String> lengthlist = new DefaultListModel<>();
	public DefaultListModel<String> playlist = new DefaultListModel<>();
	public Condition action = lock.newCondition();

	//thread 1: adicionar m�sica � lista

	class addThread extends Thread {
		String title;
		String length;

		//construtor
		public addThread(String title, String length) {
			this.length = length;
			this.title = title;
		}

		public void run() {
			try {

				lock.lock();

				while (taken) action.await(); //lista em uso, espere

				taken = true; //lista em uso no processo de adi��o
				playlist.addElement(this.title);
				lengthlist.addElement(this.length);
				taken = false; //acabou opera��o
				action.signalAll(); //agora podemos liberar todas as threads na espera, al�m de liberar o lock

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}

	//thread 2: remo��o, que � bem parecida com a thread de adicionar

	class removeThread extends Thread {
		int mscPos; //pra remover, procuraremos a m�sica pela posi��o

		//construtor
		public removeThread (int removeMsc) {
			this.mscPos = removeMsc;
		}

		public void run() {
			try {
				lock.lock();

				while (taken) action.await();

				taken = true;
				playlist.remove(this.mscPos);
				lengthlist.remove(this.mscPos);
				taken = false;
				action.signalAll();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				lock.unlock();
			}		
		}
	}

	//thread 3: list, pra mostrar quais m�sicas est�o no player

	public class list extends Thread {

		public void run() {
			try {
				lock.lock();

				while (taken) action.await();

				taken = true;

				for (int i = 0; i < playlist.size(); i++) { //passar pela lista imprimindo as m�sicas
					System.out.println(playlist.get(i));
				}

				taken = false;
				action.signalAll();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}

	public void initAdd(String msc, String length) {
		Thread add = new Thread(new addThread(msc, length));
		add.start();
		//new addThread(msc, length).start();
	}

	public void initRemove(int pos) {
		Thread remove = new Thread(new removeThread(pos));
		remove.start();
	}


}

