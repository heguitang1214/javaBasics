package multiThreading.bankScheduling;

import java.util.ArrayList;
import java.util.List;

public class NumberManager {
	private int lastNumber = 0;
	private List queueNumbers = new ArrayList();
	
	public synchronized Integer generateNewNumber(){
		queueNumbers.add(++lastNumber);
		return lastNumber;
	}
	
	public synchronized Integer fetchNumber(){
		if(queueNumbers.size()>0){
			return (Integer)queueNumbers.remove(0);
		}else{
			return null;
		}
	}
}
