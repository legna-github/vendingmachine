package lc.phx.interview.vending_machine;

import java.util.Arrays;

public class Sale {

	private final Inventory inventory;

	private final Inventory salesRegister;
	
	public Sale(Inventory inventory) {
		super();
		this.inventory = inventory;
		this.salesRegister = new Inventory();
	}

	public Product sales(String productId) {
		Product sold = inventory.removed(productId);
		salesRegister.stock(Arrays.asList(sold));
		return sold;
	}

	public void report() {
		salesRegister.report();
	}

}
