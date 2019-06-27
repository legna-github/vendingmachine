package lc.phx.interview.vending_machine;

import java.util.Objects;

public class Product {

	private final String id;
	private final String description;
	private final Integer price;

	public Product(String id, String description, Integer price) {
		super();
		Objects.requireNonNull(this.id = id, "Missing product id");
		Objects.requireNonNull(this.description = description, "Missing product description");
		Objects.requireNonNull(this.price = price, "Missing product price ");
	}

	public String getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public Integer getPrice() {
		return price;
	}
	
}
