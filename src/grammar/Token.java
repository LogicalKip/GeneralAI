package grammar;

public class Token {

	public enum TokenType {
		EPSILON,
		COMMON_WORD,
		QUESTION_MARK
	}

	public final TokenType token;
	public final String sequence;

	public Token(TokenType token, String sequence)
	{
		super();
		this.token = token;
		this.sequence = sequence;
	}
}