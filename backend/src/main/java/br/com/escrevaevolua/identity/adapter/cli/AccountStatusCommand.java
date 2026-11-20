package br.com.escrevaevolua.identity.adapter.cli;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import br.com.escrevaevolua.identity.application.AccountAdminService;

@Component
@Command(name = "account-status", description = "Bloqueia, reativa ou desativa uma conta local")
public class AccountStatusCommand implements Callable<Integer> {

    @Option(names = "--account-id", required = true)
    UUID accountId;

    @Option(names = "--action", required = true)
    AccountAdminService.Action action;

    private final AccountAdminService accountAdminService;
    private final String operator;

    @Autowired
    public AccountStatusCommand(AccountAdminService accountAdminService,
            @Value("${INVITATION_OPERATOR:}") String operator) {
        this.accountAdminService = accountAdminService;
        this.operator = operator;
    }

    @Override
    public Integer call() {
        accountAdminService.transition(accountId, action, operator);
        System.out.println("Estado da conta atualizado.");
        return 0;
    }
}
