package com.bptn.feedApp;

import org.h2.tools.Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FeedAppApplicationTests {

	@MockBean
	Server server;

	@DisplayName("Demo Test")
	@Test
	void contextLoads() {
	}
}
