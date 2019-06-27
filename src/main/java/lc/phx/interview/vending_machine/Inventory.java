package lc.phx.interview.vending_machine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class Inventory {

	private static final int MAX_CAPACITY = 10;

	private final ProductCatalog catalog; 

	private final Map<String, MyStack> inventory;
	
	public Inventory() {
		this(Collections.emptyList());
	}

	public Inventory(Collection<Product> products) {
		super();
		this.catalog = new ProductCatalog();
		this.inventory = new HashMap<>();
		stock(products);
	}
	
	public void stock(Collection<Product> products) {
		products.forEach(product -> inventory.computeIfAbsent(catalog.getProduct(product.getId()).getId(), k -> new MyStack()).add(product));
	}

	public Product sales(String productId) {
		Objects.requireNonNull(inventory.get(productId), "Product is not on inventory  : " + productId);
		return inventory.get(productId).pop();
	}

	public Integer price(String productId) {
		Objects.requireNonNull(inventory.get(productId), "Product is not on inventory : " + productId);
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
	
	private class MyStack extends Stack<Product>{

		private static final long serialVersionUID = -3451085333802155894L;

		public synchronized boolean add(Product product) {
			if(size() < MAX_CAPACITY) {
				return super.add(product);
			}
			return false;
		}
		
	}
}
