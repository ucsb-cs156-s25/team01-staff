package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsborganization/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?orgCode=sbHacks"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

         // Authorization tests for /api/ucsborganization/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }
        

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganization sbHacks = UCSBOrganization.builder()
                                .orgCode("SBHacks")
                                .orgTranslationShort("SBHacks: UCSB")
                                .orgTranslation("SB Hacks: UCSB's Largest Hackathon")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("SBHacks"))).thenReturn(Optional.of(sbHacks));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=SBHacks"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("SBHacks"));
                String expectedJson = mapper.writeValueAsString(sbHacks);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
        
        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationRepository.findById(eq("DataScienceClub"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=DataScienceClub"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("DataScienceClub"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with id DataScienceClub not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

    
        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_organizations() throws Exception {

                // arrange

                UCSBOrganization sbHacks = UCSBOrganization.builder()
                                .orgCode("SBHacks")
                                .orgTranslationShort("SBHacks: UCSB")
                                .orgTranslation("SB Hacks: UCSB's Largest Hackathon")
                                .inactive(false)
                                .build();

                UCSBOrganization test = UCSBOrganization.builder()
                                .orgCode("test")
                                .orgTranslationShort("test short")
                                .orgTranslation("test long")
                                .inactive(true)
                                .build();

                ArrayList<UCSBOrganization> expectedOrganization = new ArrayList<>();
                expectedOrganization.addAll(Arrays.asList(sbHacks, test));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganization);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganization);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organization() throws Exception {
                // arrange

                UCSBOrganization sbHacks = UCSBOrganization.builder()
                                .orgCode("SBHacks")
                                .orgTranslationShort("SBHacks: UCSB")
                                .orgTranslation("SB Hacks: UCSB's Largest Hackathon")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.save(eq(sbHacks))).thenReturn(sbHacks);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganization/post?orgCode=SBHacks&orgTranslationShort=SBHacks: UCSB&orgTranslation=SB Hacks: UCSB's Largest Hackathon&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(sbHacks);
                String expectedJson = mapper.writeValueAsString(sbHacks);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_organization() throws Exception {
                // arrange

                UCSBOrganization sbHacks = UCSBOrganization.builder()
                                .orgCode("SBHacks")
                                .orgTranslationShort("SBHacks: UCSB")
                                .orgTranslation("SB Hacks: UCSB's Largest Hackathon")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("SBHacks"))).thenReturn(Optional.of(sbHacks));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=SBHacks")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("SBHacks");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id SBHacks deleted", json.get("message"));
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=munger-hall")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
    }

     @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_organization() throws Exception {
                // arrange

                UCSBOrganization sbHacks = UCSBOrganization.builder()
                                .orgCode("SBHacks")
                                .orgTranslationShort("SBHacks: UCSB")
                                .orgTranslation("SB Hacks: UCSB's Largest Hackathon")
                                .inactive(true)
                                .build();

                UCSBOrganization sbHacksEdited = UCSBOrganization.builder()
                                .orgCode("SBHacks")
                                .orgTranslationShort("SBHacks: UCSB edit")
                                .orgTranslation("SB Hacks: UCSB's Largest Hackathon edited")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(sbHacksEdited);

                when(ucsbOrganizationRepository.findById(eq("SBHacks"))).thenReturn(Optional.of(sbHacks));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=SBHacks")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("SBHacks");
                verify(ucsbOrganizationRepository, times(1)).save(sbHacksEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganization editedOrganization = UCSBOrganization.builder()
                                .orgCode("munger-hall")
                                .orgTranslationShort("mh")
                                .orgTranslation("munger")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(editedOrganization);

                when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=munger-hall")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
                
        }
}