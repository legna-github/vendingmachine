package lc.phx.interview.vending_machine;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Inventory {

	private final Map<String, Stack<Product>> inventory;
	
	public Inventory() {
		this(Collections.emptyList());
	}

	public Inventory(Collection<Product> products) {
		super();
		this.inventory = 
				products.stream()
				.collect(Collectors.groupingBy(Product::getId, Collectors.toCollection(Stack::new)));
	}
	
	public void stock(Collection<Product> products) {
		products.forEach(product -> inventory.computeIfAbsent(product.getId(), k -> new Stack<Product>()).add(product));
	}

	public Product sales(String productId) {
		Objects.requireNonNull(inventory.get(productId), "Unknown product");
		return inventory.get(productId).pop();
	}

	public Integer price(String productId) {
		Objects.requireNonNull(inventory.get(productId), "Unknown product");
		return inventory.get(productId).peek().getPrice();
	}
	
	public void report() {
		AtomicInteger grandTotal = new AtomicInteger(0);
		inventory.forEach((productId, products) ->  {
			int size = products.size();
			String description = "depleted";
			int value = 0;
			int price = 0;
			if(size > 0) {
				description = products.peek().getDescription();
				price = products.peek().getPrice();
				value = products.stream().map(Product::getPrice).reduce(0, Integer::sum);
			}
			grandTotal.addAndGet(value);
			System.out.println(String.format("%10s,%25s,%5s,%5s,%5s", productId, description, size, price, value));
		});
		System.out.println("Grand Total:" + grandTotal.get());
	}
}
