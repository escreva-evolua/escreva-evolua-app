package br.com.escrevaevolua.identity.adapter.cli;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import br.com.escrevaevolua.identity.application.InvitationAdminService;

@Component
@Command(name = "invitation-revoke", description = "Revoga convite ainda pendente")
public class RevokeInvitationCommand implements Callable<Integer> {

    @Option(names = "--id", required = true)
    UUID invitationId;

    private final InvitationAdminService invitationAdminService;
    private final String operator;

    @Autowired
    public RevokeInvitationCommand(InvitationAdminService invitationAdminService,
            @Value("${INVITATION_OPERATOR:}") String operator) {
        this.invitationAdminService = invitationAdminService;
        this.operator = operator;
    }

    @Override
    public Integer call() {
        invitationAdminService.revoke(invitationId, operator);
        System.out.println("Convite revogado.");
        return 0;
    }
}
