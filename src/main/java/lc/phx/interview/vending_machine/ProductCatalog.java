package lc.phx.interview.vending_machine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductCatalog {

	private final Map<String, Product> catalog;
	
	public ProductCatalog() {
		super();
		this.catalog = createProductCatalog();
	}

	private Map<String, Product> createProductCatalog() {
		Map<String, Product> map = new HashMap<>();
		map.put("12", new Product("12", "Snack 12", 27));
		map.put("13", new Product("13", "Snack 13", 31));
		map.put("14", new Product("14", "Snack 14", 43));
		map.put("15", new Product("15", "Snack 15", 19));
		return Collections.unmodifiableMap(map);
	}

	public Product getProduct(String productId) {
		return Objects.requireNonNull(catalog.get(productId), "Unknown product : " + productId).copy();
	}

	public Collection<Product> getProducts() {
		return catalog.values();
	}

}
