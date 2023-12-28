package com.myproject.productservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.assertions.Assertions;
import com.myproject.productservice.dto.ProductRequest;
import com.myproject.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest//加载 Spring Boot 应用程序上下文
@Testcontainers //使用 Testcontainers 框架创建和管理 Docker 容器
@AutoConfigureMockMvc //自动配置 MockMvc
class ProductServiceApplicationTests {
	// 使用 Testcontainers 创建一个 MongoDB 容器
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	// 自动注入 MockMvc，用于模拟 HTTP 请求
	@Autowired
	private MockMvc mockMvc;

	// 自动注入 ObjectMapper，用于序列化和反序列化 JSON
	@Autowired
	private ObjectMapper objectMapper;

	// 自动注入 ProductRepository，用于与 MongoDB 进行交互
	@Autowired
	private ProductRepository productRepository;

	// 在测试运行前设置动态属性，将 MongoDB 的连接信息注入到 Spring Boot 上下文
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	// 测试：创建一个产品
	@Test
	void shouldCreateProduct() throws Exception {
		// 准备产品请求数据
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);

		// 使用 MockMvc 模拟 HTTP POST 请求，发送产品请求数据
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString))
				.andExpect(status().isCreated());  // 预期 HTTP 响应状态码为 201 Created

		// 断言：确保 ProductRepository 中保存了一个产品
		Assertions.assertTrue(productRepository.findAll().size() == 1);
	}

	// 辅助方法：创建一个示例产品请求
	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("iphone 13")
				.description("iphone 13")
				.price(BigDecimal.valueOf(1200))
				.build();
	}
}
