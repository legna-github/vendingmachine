package lc.phx.interview.vending_machine;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App 
{
	private final AtomicInteger availableBalance;
	
	private final Pattern pattern = Pattern.compile("^(\\d+)?(ENTER|CANCEL|MONEY|REPORT|STOCK|DONE)$");

	private final Sale sales;

    public App(List<Product> products) {
		super();
		this.availableBalance = new AtomicInteger(0);
		this.sales = new Sale(new Inventory(products));
	}

    public void start() {
    	try(Scanner sc = new Scanner(System.in)) {
    		while(sc.hasNextLine()) {

    			Matcher matcher = pattern.matcher(sc.nextLine());
    			
    	        if(matcher.find()) {
    	    		try {
						Command.valueOf(matcher.group(2)).execute(this, matcher.group(1));
					}
    	    		catch (EmptyStackException e) {
    	    			System.out.println("Product sold out");
					}
    	    		catch (Exception e) {
						System.out.println("BAD, BAD " + e.getMessage());
					}
    	        }
    	        else {
    	        	System.out.println("Invalid command");
    	        }
    	        System.out.println("Available balance " + availableBalance);

    		}
    	}
		
	}
    
	public static void main( String[] args )
    {

		List<Product> products = 
				createProducts(9, "12", "Snack 12", 27);
		products.addAll(createProducts(5, "13", "Snack 13", 31));
		products.addAll(createProducts(7, "14", "Snack 14", 43));

		new App(products).start();
    }

	private static List<Product> createProducts(int quantity, String id, String description, Integer price) {
		return IntStream.range(0, quantity)
				.mapToObj(idx -> new Product(id, description, price))
				.collect(Collectors.toList());
	}
	
	private enum Command {

		ENTER {
			@Override
			public void execute(App app, String productId) {
				Integer price = app.sales.price(productId);
				if(price > app.availableBalance.get()) {
					System.out.println("Insuficient balance price :" + price + ", balance :" + app.availableBalance.get());
				} 
				else {

					Product product = app.sales.sales(productId);
					app.availableBalance.addAndGet(-product.getPrice());
					System.out.println("Dispensed product " + productId);
				}
			}
		},
		DONE {
			@Override
			public void execute(App app, String ignore) {
				System.out.println(String.format("return balance %d", app.availableBalance.getAndSet(0)));
			}
		},
		CANCEL {
			@Override
			public void execute(App app, String productId) {
				// do nothing
			}
		},
		MONEY {
			@Override
			public void execute(App app, String amount) {
				Objects.requireNonNull(amount, "Missing amount");
				app.availableBalance.addAndGet(Integer.parseInt(amount));				
			}
		},
		REPORT {
			@Override
			public void execute(App app, String ignore) {
				app.sales.report();
			}
		},
		STOCK {
			@Override
			public void execute(App app, String productId) {
				
				List<Product> products = new ArrayList<>(); 

				boolean all = Objects.isNull(productId);
				if(all  || "12".equals(productId)) {
					products.addAll(createProducts(9, "12", "Snack 12", 27));
				}
				if(all  || "13".equals(productId)) {
					products.addAll(createProducts(5, "13", "Snack 13", 31));
				}
				if(all  || "14".equals(productId)) {
					products.addAll(createProducts(7, "14", "Snack 14", 43));
				}
				
				app.sales.stock(products);
			}
		}
		;

		public abstract void execute(App app, String group);
	}
}
