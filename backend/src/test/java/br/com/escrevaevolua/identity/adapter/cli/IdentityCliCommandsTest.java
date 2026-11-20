package br.com.escrevaevolua.identity.adapter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escrevaevolua.identity.application.AccountAdminService;
import br.com.escrevaevolua.identity.application.InvitationAdminService;
import picocli.CommandLine;

class IdentityCliCommandsTest {

    @Test
    void createInvitationParsesArgumentsPrintsOneTimeCodeAndReturnsSuccess() {
        InvitationAdminService service = mock(InvitationAdminService.class);
        UUID id = UUID.randomUUID();
        Instant expiresAt = Instant.parse("2026-11-30T12:00:00Z");
        when(service.create("adult@example.com", "operator-1"))
                .thenReturn(new InvitationAdminService.CreatedInvitation(id, "one-time-code", expiresAt));
        CreateInvitationCommand command = new CreateInvitationCommand(service, "operator-1");

        CapturedOutput result = execute(command, "--email", "adult@example.com");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("ID: " + id));
        assertTrue(result.stdout().contains("Código: one-time-code"));
        assertTrue(result.stdout().contains("Expira em: " + expiresAt));
        verify(service).create("adult@example.com", "operator-1");
    }

    @Test
    void revokeInvitationParsesUuidPrintsConfirmationAndReturnsSuccess() {
        InvitationAdminService service = mock(InvitationAdminService.class);
        UUID id = UUID.randomUUID();
        RevokeInvitationCommand command = new RevokeInvitationCommand(service, "operator-1");

        CapturedOutput result = execute(command, "--id", id.toString());

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("Convite revogado."));
        verify(service).revoke(id, "operator-1");
    }

    @Test
    void accountStatusParsesUuidAndLifecycleAction() {
        AccountAdminService service = mock(AccountAdminService.class);
        UUID id = UUID.randomUUID();
        AccountStatusCommand command = new AccountStatusCommand(service, "operator-1");

        CapturedOutput result = execute(command, "--account-id", id.toString(), "--action", "BLOCK");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("Estado da conta atualizado."));
        verify(service).transition(id, AccountAdminService.Action.BLOCK, "operator-1");
    }

    @Test
    void invalidOrMissingArgumentsReturnPicocliUsageError() {
        InvitationAdminService invitationService = mock(InvitationAdminService.class);
        AccountAdminService accountService = mock(AccountAdminService.class);

        assertEquals(2, execute(new CreateInvitationCommand(invitationService, "operator-1")).exitCode());
        assertEquals(2, execute(new RevokeInvitationCommand(invitationService, "operator-1"), "--id", "bad-uuid").exitCode());
        assertEquals(2, execute(new AccountStatusCommand(accountService, "operator-1"),
                "--account-id", UUID.randomUUID().toString(), "--action", "UNKNOWN").exitCode());
    }

    private static CapturedOutput execute(Object command, String... args) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(stdout, true, StandardCharsets.UTF_8);
                PrintStream err = new PrintStream(stderr, true, StandardCharsets.UTF_8)) {
            System.setOut(out);
            System.setErr(err);
            int exitCode = new CommandLine(command).execute(args);
            return new CapturedOutput(exitCode, stdout.toString(StandardCharsets.UTF_8), stderr.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    private record CapturedOutput(int exitCode, String stdout, String stderr) {
    }
}
