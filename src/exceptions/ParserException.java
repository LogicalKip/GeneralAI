package exceptions;

public class ParserException extends RuntimeException {
	private static final long serialVersionUID = 26154604934233329L;

	public ParserException(String msg) {
		super(msg);
	}
}