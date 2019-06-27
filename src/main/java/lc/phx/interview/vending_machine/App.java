package lc.phx.interview.vending_machine;

import java.util.EmptyStackException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App 
{
	
	private final Pattern pattern = 
			Pattern.compile("^(\\d+)?(ENTER|CANCEL|MONEY|REPORT|STOCK|DONE)(\\d+)?$");

	private final Inventory inventory;

	private final Sale sales;

	private int runningBalance;

    public App() {
		super();
		this.runningBalance = 0;
		this.inventory = new Inventory();
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
    	        System.out.println("Available balance " + runningBalance);

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
			public void execute(App app, String productId, String quantity) {
				Objects.requireNonNull(productId, "Missing productId");
				// .getProduct(productId) check that we carry the product
				int price = new ProductCatalog().getProduct(productId).getPrice();
				// .check(productId) check that we have the product in stock
				int inStock = app.inventory.check(productId);
				// quantity = 1 by default
				int cnt = Integer.parseInt(Objects.nonNull(quantity) ? quantity: "1" );
				if(price * cnt > app.runningBalance) {
					System.out.println("Insuficient balance, cost :" + (price * cnt) + ", balance :" + app.runningBalance);
				}
				else if(cnt > inStock) {
					System.out.println("Insuficient stock, we have : " + inStock);
				}
				else {

					app.sales.sales(productId, cnt).forEach(product -> {
						app.runningBalance -= product.getPrice();
					});
				}
			}
		},
		DONE {
			@Override
			public void execute(App app, String arg1, String arg2) {
				System.out.println(String.format("return balance %d", app.runningBalance));
				app.runningBalance = 0;
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
				app.runningBalance += Integer.parseInt(amount);				
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
				app.inventory.stock(createProductStock(productId, quantity));
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
//				return createProducts(
//						Integer.parseInt(Objects.nonNull(quantity) ? quantity: "10" ), 
//						productCatalog.getProduct(productId).getId());
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
