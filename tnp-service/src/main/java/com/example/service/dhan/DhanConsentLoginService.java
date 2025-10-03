package com.example.service.dhan;

import com.example.data.transactional.entities.User;
import com.example.external.dhan.DhanIntegrationService;
import com.example.external.dhan.model.ConsentResponse;
import com.example.service.TotpService;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DhanConsentLoginService {

    private final DhanIntegrationService dhanIntegrationService;
    private final TotpService totpService;

    public String loginAndGetTokenId(User user) {
        log.info("Starting Dhan consent login flow for user: {}", user.getId());

        ConsentResponse consent =
                dhanIntegrationService
                        .generateConsent(
                                user.getDhanClientId(),
                                user.getDhanApiKey(),
                                user.getDhanApiSecret())
                        .getBody();

        if (consent == null || consent.getConsentAppId() == null) {
            throw new IllegalStateException("Failed to generate consent");
        }

        String consentAppId = consent.getConsentAppId();
        log.info("Generated consentAppId: {}", consentAppId);

        String startUrl = dhanIntegrationService.buildConsentLoginUrl(consentAppId);
        log.info("Consent login URL: {}", startUrl);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, 30);

        try {
            driver.get(startUrl);
            logDom(driver, "After loading start URL");

            // Step 1: Submit mobile number
            String mobile = user.getDhanMobileNumber();
            if (mobile == null || mobile.isEmpty()) {
                throw new IllegalStateException("User does not have DHAN_MOBILE_NUMBER configured");
            }
            log.info("Submitting mobile number: {}", mobile);

            WebElement mobileInput =
                    wait.until(
                            ExpectedConditions.elementToBeClickable(
                                    By.cssSelector(
                                            "input[placeholder*='mobile'], input[type='tel']")));
            mobileInput.clear();
            mobileInput.sendKeys(mobile);

            WebElement submitMobileButton =
                    wait.until(
                            ExpectedConditions.elementToBeClickable(
                                    By.cssSelector("button[type='submit']")));
            submitMobileButton.click();
            logDom(driver, "After submitting mobile number");

            // Step 2: Generate TOTP
            String totpSecret = user.getDhanTOTPSecret();
            if (totpSecret == null || totpSecret.isEmpty()) {
                throw new IllegalStateException("User does not have DHAN_TOTP_SECRET configured");
            }
            int[] otpDigits = totpService.generateToken(totpSecret);
            log.info("Generated TOTP digits: {}", toOtpString(otpDigits));

            // Step 3: Fill OTP digits
            fillCodeInput(driver, wait, otpDigits);
            logDom(driver, "After filling OTP digits");

            // Step 4: Click OTP Proceed button
            WebElement otpSubmit = waitForProceedButton(driver, wait);
            otpSubmit.click();
            logDom(driver, "After clicking OTP Proceed button");

            // Step 5: Fill PIN digits (auto-submits)
            int[] pinDigits = {2, 4, 0, 2, 1, 1}; // replace with actual PIN
            fillCodeInput(driver, wait, pinDigits);
            logDom(driver, "After filling PIN digits");

            WebElement continueButton =
                    driver.findElement(By.cssSelector("button.continue")); // adjust selector
            continueButton.click();

            System.out.println("Button PRessed");
            // Step 7: Poll for redirect and extract tokenId
            String tokenId = pollForToken(driver, 30); // wait up to 30s
            if (tokenId == null) {
                throw new IllegalStateException(
                        "Unable to capture tokenId. URL: " + driver.getCurrentUrl());
            }

            log.info("Captured tokenId: {}", tokenId);
            return tokenId;

        } finally {
            driver.quit();
        }
    }

    private void fillCodeInput(WebDriver driver, WebDriverWait wait, int[] digits) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement container =
                wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("code-input")));
        List<WebElement> inputs =
                wait.until(
                        d -> {
                            List<WebElement> els =
                                    container.findElements(By.cssSelector("input[type='tel']"));
                            return els.size() == 6 ? els : null;
                        });

        for (int i = 0; i < 6; i++) {
            WebElement input = inputs.get(i);
            js.executeScript(
                    "arguments[0].value='"
                            + digits[i]
                            + "'; arguments[0].dispatchEvent(new Event('input'));",
                    input);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        // Blur last input to trigger auto-submit
        js.executeScript("arguments[0].blur();", inputs.get(5));
    }

    private String pollForToken(WebDriver driver, int timeoutSeconds) {
        for (int i = 0; i < timeoutSeconds * 2; i++) { // check every 500ms
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            String url = driver.getCurrentUrl();
            if (url.contains("tokenId=")) {
                int idx = url.indexOf("tokenId=");
                int end = url.indexOf("&", idx);
                return end > 0 ? url.substring(idx + 8, end) : url.substring(idx + 8);
            }
        }
        return null;
    }

    private WebElement waitForProceedButton(WebDriver driver, WebDriverWait wait) {
        return wait.until(
                d -> {
                    List<WebElement> buttons = d.findElements(By.tagName("button"));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed()
                                && btn.isEnabled()
                                && btn.getText().toLowerCase().contains("proceed")) {
                            return btn;
                        }
                    }
                    return null;
                });
    }

    private void logDom(WebDriver driver, String step) {
        log.info("DOM snapshot [{}]:\n{}", step, driver.getPageSource());
    }

    private String toOtpString(int[] digits) {
        StringBuilder sb = new StringBuilder();
        for (int d : digits) sb.append(d);
        return sb.length() < 6
                ? String.format("%06d", Integer.parseInt(sb.toString()))
                : sb.toString();
    }
}
