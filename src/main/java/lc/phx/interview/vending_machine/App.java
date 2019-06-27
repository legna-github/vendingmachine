package lc.phx.interview.vending_machine;

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
			Pattern.compile("^(\\d+)?(ENTER|CANCEL|MONEY|REPORT|STOCK|DONE)(\\d+)?$");

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
						Command.valueOf(matcher.group(2)).execute(this, matcher.group(1), matcher.group(3));
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

	private static List<Product> createProducts(int quantity, String id) {
		ProductCatalog productCatalog = new ProductCatalog();
		return IntStream.range(0, quantity)
				.mapToObj(idx -> productCatalog.getProduct(id))
				.collect(Collectors.toList());
	}
	
	private enum Command {

		ENTER {
			@Override
			public void execute(App app, String productId, String ignore) {
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
			public void execute(App app, String arg1, String arg2) {
				System.out.println(String.format("return balance %d", app.availableBalance.getAndSet(0)));
			}
		},
		CANCEL {
			@Override
			public void execute(App app, String productId, String ignore) {
				// do nothing
			}
		},
		MONEY {
			@Override
			public void execute(App app, String amount, String ignore) {
				Objects.requireNonNull(amount, "Missing amount");
				app.availableBalance.addAndGet(Integer.parseInt(amount));				
			}
		},
		REPORT {
			@Override
			public void execute(App app, String report, String ignore) {
				Report.execute(app, Report.findByCode(report, EnumSet.allOf(Report.class)));
			}
		},
		STOCK {
			@Override
			public void execute(App app, String productId, String quantity) {
				app.sales.stock(createProductStock(productId, quantity));
			}

			private List<Product> createProductStock(String productId, String quantity) {
				ProductCatalog productCatalog = new ProductCatalog();
				if(Objects.isNull(productId)) {
					return productCatalog.getProducts()
					.stream()
					.map(Product::getId)
					.map(id -> createProducts(15, id))
					.flatMap(List::stream)
					.collect(Collectors.toList());
				}
				return createProducts(
						Integer.parseInt(Objects.requireNonNull(quantity, "quantity is required")), 
						productCatalog.getProduct(productId).getId());
			}
		}
		;

		public abstract void execute(App app, String arg1, String arg2);
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

		public static Set<Report> findByCode(String code, Set<Report> defaultValue) {
			EnumSet<Report> result = EnumSet.allOf(Report.class)
			.stream()
			.filter(report -> Objects.equals(code, report.code))
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(Report.class)));
			if(!result.isEmpty()) {
				return result;
			}
			return defaultValue;
		}

		abstract void execute(App app);

		public static void execute(App app, Set<Report> reports) {
			reports.forEach(report -> report.execute(app));
		}
	}
}
