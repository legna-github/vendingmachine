package lc.phx.interview.vending_machine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Sale {

	private final Inventory inventory;

	private final Inventory salesRegister;
	
	public Sale(Inventory inventory) {
		super();
		this.inventory = inventory;
		this.salesRegister = new Inventory(Collections.emptyList());
	}

	public Product sales(String productId) {
		Product sold = inventory.sales(productId);
		salesRegister.stock(Arrays.asList(sold));
		return sold;
	}

	public Integer price(String productId) {
		return inventory.price(productId);
	}

	public void stock(Collection<Product> products) {
		inventory.stock(products);
	}
	
	public void report() {
		System.out.println("Inventory report");
		inventory.report();
		System.out.println();
		System.out.println("Sales report");
		salesRegister.report();
	}

}
