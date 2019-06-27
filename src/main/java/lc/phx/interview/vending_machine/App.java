package lc.phx.interview.vending_machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App 
{
	private final AtomicInteger availableBalance;
	
	private final Pattern pattern = 
			Pattern.compile("^(\\d+)?(ENTER|CANCEL|MONEY|REPORT|STOCK|DONE)$");

	private final Inventory inventory;

	private final Sale sales;

    public App() {
		this(Collections.emptyList());
	}

	public App(List<Product> products) {
		super();
		this.availableBalance = new AtomicInteger(0);
		this.inventory = new Inventory(products);
		this.sales = new Sale(inventory);
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

		new App().start();
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
			public void execute(App app, String report) {
				Report.execute(app, Report.findByCode(report));
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
	
	private enum Report {
		SALES("01") {
			@Override
			void execute(App app) {
				System.out.println("Sales report");
				app.sales.report();
			}
		},
		INVENTORY("02") {
			@Override
			void execute(App app) {
				System.out.println("Inventory report");
				app.inventory.report();
			}
		}
		;
		private String code;
		
		private Report(String code) {
			this.code = code;
		}

		public static Set<Report> findByCode(String code) {
			EnumSet<Report> result = EnumSet.allOf(Report.class)
			.stream()
			.filter(report -> Objects.equals(code, report.code))
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(Report.class)));
			if(result.isEmpty()) {
				return EnumSet.allOf(Report.class);
			}
			return result;
		}

		abstract void execute(App app);

		public static void execute(App app, Set<Report> reports) {
			reports.forEach(report -> report.execute(app));
		}
	}
}
