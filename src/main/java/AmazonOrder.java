import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class AmazonOrder {
	public static void main(String[] args) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments(
				"user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
		WebDriver driver = new ChromeDriver(options);

		Random rand = new Random();
		int delay = rand.nextInt(3000) + 1000;

		System.setProperty("webdriver.chrome.driver", "C:\\webdriver\\chromedriver-win64\\chromedriver.exe");
		
		final String email = "YOUR EMAIL HERE";
		final String password = "YOUR PASSWORD HERE";	
		
		final By freeShippingFilter = By.xpath("//*[@id='s-refinements']/div[2]/ul/li/span/a");
		final By newFilter = By.xpath("//span[text()='New']");
		final By loginButton = By.id("nav-link-accountList");


		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

		try {
			Thread.sleep(delay);
			driver.get("https://www.amazon.eg/");

			wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton)).click();

			WebElement emailInputElement = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='email']")));
			emailInputElement.sendKeys(email);
			driver.findElement(By.id("continue")).click();

			WebElement passwordInputElement = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_password")));
			passwordInputElement.sendKeys(password);
			driver.findElement(By.id("signInSubmit")).click();

			WebElement allBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-hamburger-menu")));
			allBtn.click();

			WebElement seeAllBtn = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//*[@id='hmenu-content']/ul[1]/li[14]/a[1]")));
			seeAllBtn.click();

			WebElement videoGamesBtn = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//*[@id='hmenu-content']/ul[1]/ul/li[11]/a")));
			videoGamesBtn.click();
			WebElement allVideoGamesBtn = wait.until(
					ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='hmenu-content']/ul[32]/li[3]/a")));
			allVideoGamesBtn.click();

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("s-refinements")));
			driver.findElement(freeShippingFilter).click();
			driver.findElement(newFilter).click();

			WebElement sortBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("a-autoid-0")));
			sortBtn.click();
			driver.findElement(By.xpath("//option[text()='Price: High to Low']")).click();
			wait.until(
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot div.s-result-item")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot")));

			// Add products under 15k EGP
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot")));

			boolean hasNextPage = true;

			// Create a LinkedList to keep track of added items
			LinkedList<String> addedItems = new LinkedList<>();

			while (hasNextPage) {
				// Wait for the main slot to be visible
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot")));

				// Locate price elements
				List<WebElement> priceElements = driver.findElements(By.cssSelector("span.a-price-whole"));

				// Iterate through prices and print or interact with them
				for (WebElement priceElement : priceElements) {
					String wholePrice = priceElement.getText().replace(",", ""); // Remove commas from the price

					// Check for the fractional part (if exists)
					WebElement fractionElement = priceElement
							.findElement(By.xpath("following-sibling::span[@class='a-price-fraction']"));
					String fractionalPrice = (fractionElement != null) ? fractionElement.getText() : "00";

					// Combine whole and fractional parts
					String fullPrice = wholePrice + "." + fractionalPrice;

					// Convert to double and check if it's below 15,000 EGP
					double price = Double.parseDouble(fullPrice);
					if (price < 15000) {
						// Get a unique identifier for the item
						String itemIdentifier = priceElement
								.findElement(By.xpath("ancestor::div[contains(@class, 's-result-item')]//h2")).getText()
								+ "-" + fullPrice;

						// Check if the item has already been added
						if (!addedItems.contains(itemIdentifier)) {
							// Locate the Add to Cart button for the current priceElement
							List<WebElement> addToCartButtons = priceElement.findElements(By.xpath(
									"./following::div[@data-csa-c-action-name='addToCart']//button[contains(text(), 'Add to cart')]"));

							// Check if the Add to Cart button exists
							if (!addToCartButtons.isEmpty()) {
								WebElement addToCartButton = addToCartButtons.get(0);

								// Wait for the button to be clickable
								wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));

								// Scroll into view
								((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
										addToCartButton);

								// Click the button
								addToCartButton.click();
								// Add the item identifier to the LinkedList
								addedItems.add(itemIdentifier);
							} else {
								System.out.println("No 'Add to Cart' button for: " + itemIdentifier);
							}
						} else {
							System.out.println("Already added: " + itemIdentifier);
						}
					}
				}

				// Check for the presence of the "Next" button
				try {
					WebElement nextButton = driver.findElement(By.className("s-pagination-next"));
					if (nextButton.isDisplayed()) {
						nextButton.click(); // Click the "Next" button
						Thread.sleep(3000); // Wait for the next page to load
					} else {
						hasNextPage = false; // No more pages
					}
				} catch (NoSuchElementException e) {
					hasNextPage = false; // No "Next" button found
				}
				System.out.println("All products have been added to the cart successfully.");

				driver.get("https://www.amazon.eg/gp/cart/view.html?ref_=nav_cart");
				WebElement proceedToCheckoutButton = wait
						.until(ExpectedConditions.visibilityOfElementLocated(By.name("proceedToRetailCheckout")));
				proceedToCheckoutButton.click();

				// Add address
				WebElement addAddressButton = wait
						.until(ExpectedConditions.visibilityOfElementLocated(By.id("address-book-entry-0")));
				addAddressButton.click();

				// Choose cash as payment method
				WebElement paymentMethodRadio = wait.until(ExpectedConditions
						.visibilityOfElementLocated(By.xpath("//span[contains(text(), 'Cash on Delivery')]")));
				paymentMethodRadio.click();

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			driver.quit();
		}
	}
}
