package lc.phx.interview.vending_machine;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class App {

	private Something something = new Something();

	private final Pattern pattern = Pattern.compile("^(\\d+)?(ENTER|CANCEL|MONEY|REPORT|STOCK|DONE)(\\d+)?$");

	public App() {
		super();
	}

	public void start() {
		try (Scanner sc = new Scanner(System.in)) {
			while (sc.hasNextLine()) {

				Matcher matcher = pattern.matcher(sc.nextLine());

				if (matcher.find()) {
					System.out.println("Available balance "
							+ something.execute(matcher.group(2), matcher.group(1), matcher.group(3)));
				} else {
					System.out.println("Invalid command");
				}

			}
		}

	}

	public static void main(String[] args) {

		IntStream.range(0, 5).forEach(i -> System.out.println("Happy Birthday " + (i == 3 ? "dear NAME" : "to you")));

		new App().start();
	}
}
