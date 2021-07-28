package org.upgrad.upstac.testrequests.consultation;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;
import javax.validation.ConstraintViolationException;
import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);

    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;

    @Autowired
    TestRequestFlowService  testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;

    /*
    * Method to get the list of test requests having status as 'LAB_TEST_COMPLETED'
    *
    */
    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations()  {
        return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
    }

    /*
     * Method to get the list of test requests assigned to the current doctor
     *
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor()  {
        User loggedInUser = userLoggedInService.getLoggedInUser();
        return testRequestQueryService.findByDoctor(loggedInUser);
    }

    /*
     * Method to assign a particular test request to the current doctor
     *
     */
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {
        TestRequest testRequest = new TestRequest();
        try {
            User loggedInUser = userLoggedInService.getLoggedInUser();
            testRequest = testRequestUpdateService.assignForConsultation(id, loggedInUser);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
        return testRequest;
    }

    /*
     * Method to update the result of the current test request id with test doctor comments
     *
     */
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id,@RequestBody CreateConsultationRequest testResult) {
        try {
            User loggedInUser = userLoggedInService.getLoggedInUser();
            return testRequestUpdateService.updateConsultation(id, testResult, loggedInUser);

        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }
}
