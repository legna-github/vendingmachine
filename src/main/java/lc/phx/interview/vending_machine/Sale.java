package lc.phx.interview.vending_machine;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Sale {

	private final Inventory inventory;

	private final Inventory salesRegister;
	
	public Sale(Inventory inventory) {
		super();
		this.inventory = inventory;
		this.salesRegister = new Inventory();
	}

	public List<Product> sales(String productId, int quantity) {
		return IntStream.range(0, quantity).mapToObj(idx -> {
			Product sold = inventory.removed(productId);
			salesRegister.stock(Arrays.asList(sold));
			System.out.println("Dispensed product " + productId);
			return sold;
		})
		.collect(Collectors.toList());
	}

	public void report() {
		salesRegister.report();
	}

}
