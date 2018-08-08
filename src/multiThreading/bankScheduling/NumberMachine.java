package multiThreading.bankScheduling;

public class NumberMachine {
	
	private NumberMachine(){}
	private static NumberMachine instance = new NumberMachine();
	public static NumberMachine getInstance(){
		return instance;
	}
	
	private NumberManager commonManager = new NumberManager();
	private NumberManager expressManager = new NumberManager();
	private NumberManager vipManager = new NumberManager();
	public NumberManager getCommonManager() {
		return commonManager;
	}
	public NumberManager getExpressManager() {
		return expressManager;
	}
	public NumberManager getVipManager() {
		return vipManager;
	}
	
}
