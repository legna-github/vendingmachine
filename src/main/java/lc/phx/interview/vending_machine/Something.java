package lc.phx.interview.vending_machine;

import java.util.EmptyStackException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Something {

	private final Inventory inventory;
	private final Sale sales;
	private int runningBalance;

	public Something() {
		super();
		this.runningBalance = 0;
		this.inventory = new Inventory();
		this.sales = new Sale(inventory);
	}

	public int execute(String command, String prefix, String suffix) {
		try {
			Command.valueOf(command).execute(this, prefix, suffix);
		}
		catch (EmptyStackException e) {
			System.out.println("Product sold out");
		}
		catch (Exception e) {
			System.out.println("BAD, BAD " + e.getMessage());
		}
		return runningBalance;
		
	}

	private enum Command {
	
			ENTER {
				@Override
				public void execute(Something app, String productId, String quantity) {
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
				public void execute(Something app, String arg1, String arg2) {
					System.out.println(String.format("return balance %d", app.runningBalance));
					app.runningBalance = 0;
				}
			},
			CANCEL {
				@Override
				public void execute(Something app, String productId, String ignore) {
					// do nothing
				}
			},
			MONEY {
				@Override
				public void execute(Something app, String amount, String ignore) {
					Objects.requireNonNull(amount, "Missing amount");
					app.runningBalance += Integer.parseInt(amount);				
				}
			},
			REPORT {
				@Override
				public void execute(Something app, String report, String ignore) {
					Report.execute(app, Report.findByCode(report, EnumSet.allOf(Report.class)));
				}
			},
			STOCK {
				private static final int MAX_CAPACITY = 10;

				@Override
				public void execute(Something app, String productId, String quantity) {
					app.inventory.stock(createProductStock(productId, quantity));
				}
	
				private List<Product> createProductStock(String productId, String quantity) {
					if(Objects.isNull(productId)) {
						return stock();
					}
					return stock(productId, quantity);
				}

				private List<Product> stock(String productId, String quantity) {
					//				return createProducts(
					//						Integer.parseInt(Objects.nonNull(quantity) ? quantity: "10" ), 
					//						productCatalog.getProduct(productId).getId());
									String id = new ProductCatalog().getProduct(productId).getId();
									int qty = Integer.parseInt(Objects.requireNonNull(quantity, "quantity is required"));
									return createProducts(qty, id);
				}

				private List<Product> stock() {
					return new ProductCatalog().getProducts()
					.stream()
					.map(Product::getId)
					.map(id -> createProducts(MAX_CAPACITY, id))
					.flatMap(List::stream)
					.collect(Collectors.toList());
				}
				
				private List<Product> createProducts(int quantity, String id) {
					ProductCatalog productCatalog = new ProductCatalog();
					return IntStream.range(0, quantity)
							.mapToObj(idx -> productCatalog.getProduct(id))
							.collect(Collectors.toList());
				}
			}
			;
	
			public abstract void execute(Something app, String arg1, String arg2);
		}

	private enum Report {
		SALES("01") {
			@Override
			void execute(Something app) {
				System.out.println("Sales report");
				app.sales.report();
			}
		},
		INVENTORY("02") {
			@Override
			void execute(Something app) {
				System.out.println("Inventory report");
				app.inventory.report();
			}
		}
		;
		private String code;
		
		private Report(String code) {
			this.code = code;
		}
	
		abstract void execute(Something app);
	
		public static void execute(Something app, Set<Report> reports) {
			reports.forEach(report -> report.execute(app));
		}
		
		public static Set<Report> findByCode(String code, Set<Report> defaultValue) {

			Set<Report> result = EnumSet.allOf(Report.class)
			.stream()
			.filter(report -> Objects.equals(code, report.code))
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(Report.class)));
			
			Predicate<Set<Report>> isEmpty = Set<Report>::isEmpty;
			return Optional.of(result).filter(isEmpty.negate()).orElse(defaultValue);
		}

	}

}
