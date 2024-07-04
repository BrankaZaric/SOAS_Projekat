package util.exceptions;

public class NoDataFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoDataFoundException() {
		
	}
	
	public NoDataFoundException(String message) {
//		super(fineTuneMessage(message));
		super(message);
	}
	
//	private static String fineTuneMessage(String message) {
//		String[] decomposedMessage = message.split("\"");
//		return decomposedMessage[1];
//	}

}
