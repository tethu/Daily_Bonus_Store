package com.example.db_store;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import android.util.Log;

public class Queue_rssi {
	int lenght;
	int rssi;
	int check;
	int state;
	int counter=0;
	Queue_rssi(int check, int state) {
		this.check = check;
		this.state = state;
	}

	Queue<Integer> qe = new LinkedList<Integer>();

	public boolean Queue_function(int new_rssi) {
		rssi = new_rssi;
		int i = 0;// �X��-�P�_60 3��
		boolean in_flag = false;
		qe.add(rssi);
		Log.d("����", qe.size() + "");

		Iterator it = qe.iterator();
		if(counter<6)
			counter++;
		if(counter==6){
			it.next();
			it.remove();
		}
		while (it.hasNext()) {
			if (state == 0) {			//�s�u
				Integer iteratorValue = (Integer) it.next();
				if (iteratorValue >= check) {
					i = i + 1;
					if (i == 2) {
						in_flag = true;
					}
				}
			} else if (state == 1) {	//���_
				Integer iteratorValue = (Integer) it.next();
				if (iteratorValue <= check) {
					i = i + 1;
					if (i == 5) {
						in_flag = true;
					}
				}
			}
		}
		return in_flag;

	}

}
