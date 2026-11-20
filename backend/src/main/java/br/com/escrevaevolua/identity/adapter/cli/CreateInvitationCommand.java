package br.com.escrevaevolua.identity.adapter.cli;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import br.com.escrevaevolua.identity.application.InvitationAdminService;

@Component
@Command(name = "invitation-create", description = "Cria convite e exibe o código uma única vez")
public class CreateInvitationCommand implements Callable<Integer> {

    @Option(names = "--email", required = true)
    String email;

    private final InvitationAdminService invitationAdminService;
    private final String operator;

    @Autowired
    public CreateInvitationCommand(InvitationAdminService invitationAdminService,
            @Value("${INVITATION_OPERATOR:}") String operator) {
        this.invitationAdminService = invitationAdminService;
        this.operator = operator;
    }

    @Override
    public Integer call() {
        var invitation = invitationAdminService.create(email, operator);
        System.out.printf("ID: %s%nCódigo: %s%nExpira em: %s%n",
                invitation.id(), invitation.code(), invitation.expiresAt());
        return 0;
    }
}
