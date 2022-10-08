package domain;

public class CyberpunkGamePOJO {
	private String   name;
	private Integer  price;
	private Boolean  available;
	private Double   version;
	private String[] functions;

	public String getName() {
		return name;
	}

	public int getPrice() {
		return price;
	}

	public boolean isAvailable() {return available;}

	public double getVersion() {
		return version;
	}

	public String[] getFunctions() {
		return functions;
	}
}