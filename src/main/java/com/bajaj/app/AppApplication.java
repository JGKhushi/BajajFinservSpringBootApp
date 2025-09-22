package com.bajaj.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class AppApplication {
	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}
}

@Component
class StartupRunner implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		// Step 1: Generate webhook
		String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("name", "Khusi Nema");
		requestBody.put("regNo", "0002CB221029");
		requestBody.put("email", "khusinema22@gmail.com");

		ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, requestBody, Map.class);
		String webhook = (String) response.getBody().get("webhook");
		String accessToken = (String) response.getBody().get("accessToken");

		// Step 2: Prepare final SQL query
		String finalQuery = "SELECT \n" +
				"    p.AMOUNT AS SALARY,\n" +
				"    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,\n" +
				"    FLOOR(DATEDIFF(CURDATE(), e.DOB)/365) AS AGE,\n" +
				"    d.DEPARTMENT_NAME\n" +
				"FROM PAYMENTS p\n" +
				"JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID\n" +
				"JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID\n" +
				"WHERE DAY(p.PAYMENT_TIME) <> 1\n" +
				"  AND p.AMOUNT = (\n" +
				"      SELECT MAX(AMOUNT)\n" +
				"      FROM PAYMENTS\n" +
				"      WHERE DAY(PAYMENT_TIME) <> 1\n" +
				"  );";


		// Step 3: Submit final query to webhook
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, String> submitBody = new HashMap<>();
		submitBody.put("finalQuery", finalQuery);

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(submitBody, headers);

		ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhook, entity, String.class);
		System.out.println("Submission Response: " + submitResponse.getBody());
	}
}
