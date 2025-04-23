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

         // Authorization tests for /api/ucsborganization/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
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
    }