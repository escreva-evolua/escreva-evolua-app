package br.com.escrevaevolua;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escrevaevolua.identity.adapter.cli.AccountStatusCommand;
import br.com.escrevaevolua.identity.adapter.cli.CreateInvitationCommand;
import br.com.escrevaevolua.identity.adapter.cli.RevokeInvitationCommand;
import br.com.escrevaevolua.identity.application.AccountAdminService;
import br.com.escrevaevolua.identity.application.InvitationAdminService;

class BackendCliRunnerTest {

    @Test
    void dispatchesDocumentedRootAndTopLevelInvitationCommands() {
        InvitationAdminService invitationService = mock(InvitationAdminService.class);
        UUID invitationId = UUID.randomUUID();
        when(invitationService.create("adult@example.com", "operator-1"))
                .thenReturn(new InvitationAdminService.CreatedInvitation(invitationId, "one-time-code",
                        Instant.parse("2026-11-30T12:00:00Z")));
        var runner = new BackendApplication.IdentityCliRunner(
                new CreateInvitationCommand(invitationService, "operator-1"),
                new RevokeInvitationCommand(invitationService, "operator-1"),
                new AccountStatusCommand(mock(AccountAdminService.class), "operator-1"));
        PrintStream originalOut = System.out;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();

        try (PrintStream captured = new PrintStream(stdout, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            assertEquals(0, runner.execute(new String[] {"identity", "invitation-create", "--email", "adult@example.com"}));
            assertEquals(0, runner.execute(new String[] {"invitation-create", "--email", "adult@example.com"}));
        } finally {
            System.setOut(originalOut);
        }

        assertEquals(2, stdout.toString(StandardCharsets.UTF_8).split("Código: one-time-code", -1).length - 1);
        verify(invitationService, times(2)).create("adult@example.com", "operator-1");
    }
}
