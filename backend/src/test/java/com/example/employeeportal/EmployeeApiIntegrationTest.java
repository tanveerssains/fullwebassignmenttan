package com.example.employeeportal;

import com.example.employeeportal.entity.Department;
import com.example.employeeportal.entity.Project;
import com.example.employeeportal.repository.DepartmentRepository;
import com.example.employeeportal.repository.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.seed.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployeeApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Department engineering;
    private Project nova;

    @BeforeEach
    void setUp() {
        engineering = departmentRepository.save(new Department("Engineering", "Bengaluru"));
        nova = projectRepository.save(new Project("Nova", "Acme Corp", "ACTIVE"));
    }

    @Test
    void listsEmployeesAndSearchesByNameOrCode() throws Exception {
        createEmployee("EMP201", "Riya", "Sen", "riya.sen@example.com");
        createEmployee("EMP202", "Arjun", "Bose", "arjun.bose@example.com");

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/api/employees").param("search", "EMP201"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName").value("Riya Sen"));

        mockMvc.perform(get("/api/employees").param("search", "arjun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("arjun.bose@example.com"));
    }

    @Test
    void createsReadsUpdatesAndDeletesEmployee() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload("EMP203", "neha.gupta@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/employees/\\d+")))
                .andExpect(jsonPath("$.employeeCode").value("EMP203"))
                .andExpect(jsonPath("$.fullName").value("Neha Gupta"))
                .andExpect(jsonPath("$.department.name").value("Engineering"))
                .andExpect(jsonPath("$.project.name").value("Nova"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long id = created.get("id").asLong();

        mockMvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("neha.gupta@example.com"));

        Map<String, Object> updatePayload = validPayload("EMP203", "neha.gupta@example.com");
        updatePayload.put("designation", "Lead Software Engineer");
        updatePayload.put("salary", 105000);
        updatePayload.put("status", "ON_LEAVE");

        mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation").value("Lead Software Engineer"))
                .andExpect(jsonPath("$.salary").isNumber())
                .andExpect(jsonPath("$.status").value("ON_LEAVE"));

        mockMvc.perform(delete("/api/employees/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("was not found")));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void rejectsInvalidEmployee() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("employeeCode", "");
        payload.put("firstName", "");
        payload.put("lastName", "");
        payload.put("email", "not-an-email");
        payload.put("phone", "abc");
        payload.put("designation", "");
        payload.put("salary", 0);
        payload.put("dateOfJoining", LocalDate.now().plusDays(5).toString());
        payload.put("status", "ACTIVE");
        payload.put("departmentId", engineering.getId());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please correct the highlighted fields."))
                .andExpect(jsonPath("$.fieldErrors.employeeCode").value("Employee code is required"))
                .andExpect(jsonPath("$.fieldErrors.email").value("Enter a valid email address"))
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.salary").exists())
                .andExpect(jsonPath("$.fieldErrors.dateOfJoining").value("Date of joining cannot be in the future"));
    }

    @Test
    void rejectsDuplicateEmailAndEmployeeCode() throws Exception {
        createEmployee("EMP204", "Vikram", "Desai", "vikram.desai@example.com");

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload("EMP205", "vikram.desai@example.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Email")));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload("EMP204", "new.person@example.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Employee code")));
    }

    @Test
    void returnsNotFoundForUnknownDepartmentOrEmployee() throws Exception {
        Map<String, Object> payload = validPayload("EMP206", "zoya.khan@example.com");
        payload.put("departmentId", 99999);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Department")));

        mockMvc.perform(get("/api/employees/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void exposesDepartmentAndProjectLookups() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Engineering"));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Nova"));
    }

    private void createEmployee(String code, String firstName, String lastName, String email) throws Exception {
        Map<String, Object> payload = validPayload(code, email);
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());
    }

    private Map<String, Object> validPayload(String code, String email) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("employeeCode", code);
        payload.put("firstName", "Neha");
        payload.put("lastName", "Gupta");
        payload.put("email", email);
        payload.put("phone", "+91 98765 43210");
        payload.put("designation", "Software Engineer");
        payload.put("salary", 75000);
        payload.put("dateOfJoining", "2023-06-15");
        payload.put("status", "ACTIVE");
        payload.put("departmentId", engineering.getId());
        payload.put("projectId", nova.getId());
        return payload;
    }
}
