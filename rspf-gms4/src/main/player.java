package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.DefaultListModel;

public class player {

	/* Precisamos de um lock pra lidar com a lista de m�sicas, para
	 * que nada seja executado de forma precipitada.
	 * Tamb�m precisamos de uma forma de agrupar a lista de m�sicas
	 * e as suas respectivas dura��es. vou usar list model, visto que ele j� colabora com o display.
	 * Vou usar tamb�m um boolean pra me auxiliar em saber se tem outra
	 * thread usando a lista de m�sicas, porque se sim esperaremos a
	 * opera��o acabar. Para sinalizar a��es dentro da lista de m�sica, 
	 * vou criar uma vari�vel da categoria concurrent.condition.
	 */

	/* 
	 * Primeira entrega:
	 * add (done)
	 * remove (done)
	 * listar pra mostrar que add e remove funcionam (done)
	 * play/pause (done)
	 * 
	 * Segunda entrega:
	 * play/pause (done)
	 * next/previous (done)
	 * GUI (done)
	 * 
	 * Terceira entrega:
	 * shuffle (done)
	 * 
	 */

	public Lock lock = new ReentrantLock();
	public boolean taken = false; //outra thread "pegou" a lista por enquanto
	public int position = 0;
	public DefaultListModel<String> playlist = new DefaultListModel<>();
	public List<Integer> mscPosOrder = new ArrayList<>(); //listar pra guardar as posi��es (next/previous)
	public Condition action = lock.newCondition();
	public boolean shuffleOn = false;

	//thread 1: adicionar m�sica � lista

	class addThread extends Thread {
		String title;

		//construtor
		public addThread(String title) {
			this.title = title;
		}

		public void run() {
			try {

				lock.lock();

				while (taken) action.await(); //lista em uso, espere

				taken = true; //lista em uso no processo de adi��o
				playlist.addElement(this.title);
				mscPosOrder.add(playlist.getSize() - 1);
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

	//thread 3: thread que capta a posi��o da m�sica para fazer o next and previous
	//ela funciona como thread mas eu quero o id atualizado no return
	public int positionThread (boolean aux, int index) {

		try {

			lock.lock();

			while (taken) action.await();

			taken = true;

			if (aux) {
				position = mscPosOrder.indexOf(index);
			} else {
				//nova posi��o vai ser o resto da soma da (nova posi��o + tamanho da lista + index atual)/tamanho da lista
				position = (position + mscPosOrder.size() + index) % mscPosOrder.size();
			}

			taken = false;
			action.signalAll();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();
		}

		return mscPosOrder.get(position);
	}

	/* Thread 4: thread que inicializa a fun��o aleat�rio do player.
	 * Reutilizaremos a thread para obter as posi��es necess�rias para fazer com que o shuffle funcione corretamente.
	 */
	class shuffleThread extends Thread {

		@Override public void run() {

			int psc = mscPosOrder.get(position);

			try {
				
				lock.lock();
				
				while (taken) action.await();
				
				taken = true;
				
				//se t� ligado, � pra desligar e listar a playlist de forma sequencial
				if (shuffleOn) {
					shuffleOn = false;
					mscPosOrder.clear();
					for (int i = 0; i < playlist.size(); i++) {
						mscPosOrder.add(i);
					}
					//se t� desligado, � pra ligar e listar a playlist de forma aleat�ria
				} else {
					shuffleOn = true;
					//fun��o para randomizar a ordem dos itens na lista 
					Collections.shuffle(mscPosOrder);
				}
				
				//printando pra provar o aleat�rio
				System.out.println(mscPosOrder);
				
				taken = false;
				action.signalAll();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
			
			//precisamos inicializar a thread de posi��o novamente
			positionThread(true, psc);

		}
	}
    
	//fun��es para iniciar as threads
	
	public void initAdd(String msc) {
		Thread add = new Thread(new addThread(msc));
		add.start();
	}

	public void initRemove(int pos) {
		Thread remove = new Thread(new removeThread(pos));
		remove.start();
	}

	public void initShuffle() {
       Thread shuffle = new Thread(new shuffleThread());
       shuffle.start();
	}

}

