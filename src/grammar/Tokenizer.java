package grammar;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.ParserException;
import grammar.Token.TokenType;

public class Tokenizer {
	private class TokenInfo	{
		public final Pattern regex;
		public final TokenType token;

		public TokenInfo(Pattern regex, TokenType token) {
			super();
			this.regex = regex;
			this.token = token;
		}
	}

	private LinkedList<TokenInfo> tokenInfos;
	private LinkedList<Token> tokens;
	

	public Tokenizer() {
		tokenInfos = new LinkedList<TokenInfo>();
		tokens = new LinkedList<Token>();
	}

	public void add(String regex, TokenType id)	{
		tokenInfos.add(new TokenInfo(Pattern.compile("^("+regex+")"), id));
	}

	public void tokenize(String str) {
		String s = str.trim();
		tokens.clear();
		while (!s.equals("")) {
			boolean match = false;
			for (TokenInfo info : tokenInfos) {
				Matcher m = info.regex.matcher(s);
				if (m.find()) {
					match = true;
					String tok = m.group().trim();
					s = m.replaceFirst("").trim();
					tokens.add(new Token(info.token, tok));
					break;
				}
			}
			if (!match) {
				throw new ParserException("Unexpected character in input: "+s);
			}
		}
	}

	public LinkedList<Token> getTokens() {
		return tokens;
	}

}
