package com.example.tp20.car;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(properties = {"spring.profiles.active=demo", "eureka.client.enabled=false"})
@AutoConfigureMockMvc
class CarControllerTest {
    @Autowired MockMvc mvc;
    @Autowired RestTemplate restTemplate;

    @Test
    void enrichesCarThroughGatewayAndReportsMissingCar() throws Exception {
        mvc.perform(post("/api/car").contentType(MediaType.APPLICATION_JSON)
                .content("{\"brand\":\"Toyota\",\"model\":\"Corolla\",\"matricule\":\"A-123\",\"clientId\":1}"))
                .andExpect(status().isCreated());

        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("http://localhost:8888/SERVICE-CLIENT/api/client/1"))
                .andRespond(withSuccess("{\"id\":1,\"nom\":\"Ahmed\",\"age\":30}", MediaType.APPLICATION_JSON));
        mvc.perform(get("/api/car/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.nom").value("Ahmed"));
        server.verify();
        mvc.perform(get("/api/car/999"))
                .andExpect(status().isNotFound());
    }
}
