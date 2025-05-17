package com.example.AdminService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb+srv://yoyo:yoyo123@admincluster.2eaglmq.mongodb.net/admindb?retryWrites=true&w=majority&appName=AdminCluster"
})
class AdminServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
