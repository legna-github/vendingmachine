package lc.phx.interview.vending_machine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class Inventory {

	private static final int MAX_CAPACITY = 10;

	private final ProductCatalog catalog; 

	private final Map<String, MyStack> stock;
	
	public Inventory() {
		super();
		this.catalog = new ProductCatalog();
		this.stock = new HashMap<>();
	}
	
	public void stock(Collection<Product> products) {
		products.forEach(product -> stock.computeIfAbsent(catalog.getProduct(product.getId()).getId(), k -> new MyStack()).add(product));
	}

	public Product removed(String productId) {
		Objects.requireNonNull(stock.get(productId), "Product is not on stock  : " + productId);
		return stock.get(productId).pop();
	}

	public void report() {
		AtomicInteger grandTotal = new AtomicInteger(0);
		stock.forEach((productId, products) ->  {
			String description = "depleted";
			int value = 0;
			int price = 0;
			int size = products.size();
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
