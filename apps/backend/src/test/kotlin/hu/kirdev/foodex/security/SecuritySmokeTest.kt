package hu.kirdev.foodex.security

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class SecuritySmokeTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `homepage is public`() {
        mockMvc.get("/api/homepage")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `unauthenticated POST openings is rejected`() {
        // Without login: 401 (API entry point) or 403 (CSRF). Must not be 200.
        mockMvc.post("/api/openings") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "cookingClubId": 403,
                  "maxMembers": 3,
                  "opening": "2030-01-01T10:00:00",
                  "closing": "2030-01-01T14:00:00",
                  "place": "kitchen",
                  "comment": ""
                }
            """.trimIndent()
        }.andExpect {
            status { is4xxClientError() }
        }
    }

    @Test
    fun `unauthenticated GET config is rejected`() {
        mockMvc.get("/api/config")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
