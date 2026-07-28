package com.alin.lin.service;

import com.alin.lin.dto.AddressChangeDto;
import com.alin.lin.dto.AddressChangeRequest;
import com.alin.lin.dto.CreateChangeCaseDto;
import com.alin.lin.dto.CreateChangeCaseRequest;
import com.alin.lin.dto.MainAmountChangeDto;
import com.alin.lin.dto.MainAmountChangeRequest;
import com.alin.lin.dto.PolicyChangeCaseDetailDto;
import com.alin.lin.dto.UpdateChangeCaseStatusRequest;
import com.alin.lin.exception.ChangeCaseConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
@Testcontainers(disabledWithoutDocker = true)
class PolicyChangeWorkflowIntegrationTest {
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("main")
            .withUsername("pos")
            .withPassword("pos-test")
            // 稽核表使用資料庫觸發器保護不可竄改；測試容器需允許非 SUPER 帳號建立觸發器。
            .withCommand("--log-bin-trust-function-creators=ON");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/local");
        registry.add("pos.security.enabled", () -> false);
    }

    @Autowired
    private PolicyChangeService policyChangeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetBusinessData() {
        jdbcTemplate.update("DELETE FROM policy_change_record_snapshot");
        jdbcTemplate.update("DELETE FROM policy_change_field");
        jdbcTemplate.update("DELETE FROM policy_change_item");
        jdbcTemplate.update("DELETE FROM policy_change_acceptance");
        jdbcTemplate.update("DELETE FROM policy_change_case_reservation_item");
        jdbcTemplate.update("DELETE FROM policy_change_case_reservation");
        jdbcTemplate.update("DELETE FROM policy_change_case_sequence");
        jdbcTemplate.update("""
                UPDATE policy_contact_address
                SET postal_code = '100001',
                    address_text = '臺北市中正區重慶南路一段１號'
                WHERE policy_no = 'P000000001'
                  AND policy_seq = 1
                  AND address_type_code = '01'
                """);
    }

    @Test
    void concurrentCaseNumbersAreUnique() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<Callable<String>> tasks = java.util.stream.IntStream.range(0, 12)
                    .mapToObj(index -> (Callable<String>) () -> createAddressCase().getChangeCaseNo())
                    .toList();
            List<Future<String>> futures = executor.invokeAll(tasks);
            Set<String> caseNumbers = new HashSet<>();
            for (Future<String> future : futures) {
                caseNumbers.add(future.get());
            }
            assertEquals(12, caseNumbers.size());
            assertTrue(caseNumbers.stream().allMatch(caseNo -> caseNo.matches("C\\d{7}\\d{3,}")));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void noChangeRemovesPendingDraft() {
        CreateChangeCaseDto changeCase = createAddressCase();
        AddressChangeDto result = saveCommunicationAddress(changeCase.getChangeCaseNo(), "臺北市中正區重慶南路一段１號");

        assertEquals(0, result.getChangedFieldCount());
        assertFalse(policyChangeService.findChangeCases("P000000001").stream()
                .anyMatch(item -> item.getChangeCaseNo().equals(changeCase.getChangeCaseNo())));
    }

    @Test
    void repeatedSaveKeepsOnlyLatestDraftValue() {
        CreateChangeCaseDto changeCase = createAddressCase();
        saveCommunicationAddress(changeCase.getChangeCaseNo(), "臺北市中正區重慶南路一段２號");
        saveCommunicationAddress(changeCase.getChangeCaseNo(), "臺北市中正區重慶南路一段３號");

        PolicyChangeCaseDetailDto detail = policyChangeService.findChangeCaseDetail(
                "P000000001", 1, changeCase.getChangeCaseNo()
        );
        assertEquals(1, detail.getChangedFieldNames().stream()
                .filter(field -> "full_width_address".equals(field.getChangedFieldName()))
                .count());
        assertTrue(detail.getChangedFieldNames().stream()
                .anyMatch(field -> "臺北市中正區重慶南路一段３號".equals(field.getContentAfter())));
    }

    @Test
    void oneCaseNumberStoresAddressAndMainAmountChanges() {
        CreateChangeCaseDto changeCase = policyChangeService.createChangeCase(CreateChangeCaseRequest.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .changeItemCodes(List.of("001", "002"))
                .build());

        saveCommunicationAddress(changeCase.getChangeCaseNo(), "臺北市中正區重慶南路一段２號");
        MainAmountChangeDto mainAmountResult = policyChangeService.saveMainAmountChange(
                changeCase.getChangeCaseNo(), MainAmountChangeRequest.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .insuredAmount(new java.math.BigDecimal("1100000"))
                .build()
        );
        assertEquals(1, mainAmountResult.getChangedFieldCount());

        PolicyChangeCaseDetailDto detail = policyChangeService.findChangeCaseDetail(
                "P000000001", 1, changeCase.getChangeCaseNo()
        );
        assertEquals(Set.of("001", "002"), detail.getChangedFieldNames().stream()
                .map(field -> field.getChangeItemCode())
                .collect(java.util.stream.Collectors.toSet()));
        assertEquals(1, detail.getChangedFieldNames().stream()
                .filter(field -> "002".equals(field.getChangeItemCode()))
                .count());
        assertEquals(Set.of("policy_coverage.000.insured_amount"),
                detail.getChangedFieldNames().stream()
                        .filter(field -> "002".equals(field.getChangeItemCode()))
                        .map(field -> field.getChangedFieldName())
                        .collect(java.util.stream.Collectors.toSet()));
        assertEquals(1, detail.getChangedRecordTypes().stream()
                .filter(file -> "002".equals(file.getChangeItemCode()))
                .count());
        assertTrue(detail.getChangedRecordTypes().stream()
                .filter(file -> "002".equals(file.getChangeItemCode()))
                .flatMap(file -> file.getSnapshotFields().stream())
                .anyMatch(field -> "insuredAmount".equals(field.getJsonKey())
                        && amountEquals("1000000", field.getContentBefore())
                        && amountEquals("1100000", field.getContentAfter())));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM policy_change_acceptance WHERE change_case_no = ?",
                Integer.class,
                changeCase.getChangeCaseNo()
        ));
    }

    @Test
    void completedCaseAllowsAFollowingCaseToApplyWithoutOverwritingAuditHistory() {
        CreateChangeCaseDto olderCase = createAddressCase();
        saveCommunicationAddress(olderCase.getChangeCaseNo(), "臺北市中正區重慶南路一段２號");
        complete(olderCase.getChangeCaseNo());

        CreateChangeCaseDto newerCase = createAddressCase();
        saveCommunicationAddress(newerCase.getChangeCaseNo(), "臺北市中正區重慶南路一段３號");
        complete(newerCase.getChangeCaseNo());

        assertEquals(
                "臺北市中正區重慶南路一段３號",
                policyChangeService.findPolicyDetail("P000000001", 1)
                        .getCommunicationAddress()
                        .getAddressText()
        );
    }

    @Test
    void concurrentApplicationsOfSameAddressAllowOnlyOneCaseToEnterThePendingQueue() throws Exception {
        CreateChangeCaseDto firstCase = createAddressCase();
        CreateChangeCaseDto secondCase = createAddressCase();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> firstResult = executor.submit(
                    () -> saveAddressAfterSignal(firstCase.getChangeCaseNo(), "臺北市中正區重慶南路一段２號", ready, start)
            );
            Future<String> secondResult = executor.submit(
                    () -> saveAddressAfterSignal(secondCase.getChangeCaseNo(), "臺北市中正區重慶南路一段３號", ready, start)
            );
            ready.await();
            start.countDown();

            assertEquals(Set.of("PENDING", "CONFLICT"), Set.of(firstResult.get(), secondResult.get()));

            List<String> statuses = policyChangeService.findChangeCases("P000000001").stream()
                    .map(changeCase -> changeCase.getAcceptanceStatus())
                    .toList();
            assertEquals(1, statuses.stream().filter("P"::equals).count());
        } finally {
            executor.shutdownNow();
        }
    }

    private CreateChangeCaseDto createAddressCase() {
        return policyChangeService.createChangeCase(CreateChangeCaseRequest.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .changeItemCodes(java.util.List.of("001"))
                .build());
    }

    private AddressChangeDto saveCommunicationAddress(String changeCaseNo, String address) {
        return policyChangeService.saveAddressChange(changeCaseNo, AddressChangeRequest.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .addressTypeCode("01")
                .postalCode("100001")
                .addressText(address)
                .build());
    }

    private String saveAddressAfterSignal(
            String changeCaseNo,
            String address,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        ready.countDown();
        try {
            start.await();
            saveCommunicationAddress(changeCaseNo, address);
            return "PENDING";
        } catch (ChangeCaseConflictException exception) {
            return "CONFLICT";
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private boolean amountEquals(String expected, String actual) {
        return actual != null
                && new java.math.BigDecimal(expected).compareTo(new java.math.BigDecimal(actual)) == 0;
    }

    private void complete(String changeCaseNo) {
        policyChangeService.updateChangeCaseStatus(changeCaseNo, UpdateChangeCaseStatusRequest.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .acceptanceStatus("S")
                .build());
    }

    private String completeAfterSignal(
            String changeCaseNo,
            CountDownLatch ready,
            CountDownLatch start
    ) throws InterruptedException {
        ready.countDown();
        start.await();
        try {
            complete(changeCaseNo);
            return "COMPLETED";
        } catch (ChangeCaseConflictException exception) {
            return "CONFLICT";
        }
    }
}
