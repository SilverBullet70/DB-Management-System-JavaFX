

public enum Color {
	GREEN("\033[0;32m"),
	RESET("\033[0m"),
	RED("\033[0;31m"),
	CYAN("\033[0;36m"),
	YELLOW("\033[0;33m"),
	PURPLE( "\033[1;95m");
	
	public final String value;

	Color(String string) {
		this.value = string;
	}
}
