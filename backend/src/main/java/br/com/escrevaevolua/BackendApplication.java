package br.com.escrevaevolua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import br.com.escrevaevolua.identity.adapter.cli.AccountStatusCommand;
import br.com.escrevaevolua.identity.adapter.cli.CreateInvitationCommand;
import br.com.escrevaevolua.identity.adapter.cli.IdentityAdminCommand;
import br.com.escrevaevolua.identity.adapter.cli.RevokeInvitationCommand;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
        if (args.length > 0 && isIdentityCli(args[0])) {
            int exitCode = context.getBean(IdentityCliRunner.class).execute(args);
            System.exit(SpringApplication.exit(context, () -> exitCode));
        }
    }

    private static boolean isIdentityCli(String firstArgument) {
        return "identity".equals(firstArgument) || "invitation-create".equals(firstArgument)
                || "invitation-revoke".equals(firstArgument) || "account-status".equals(firstArgument);
    }

    @Component
    static class IdentityCliRunner {
        private final CreateInvitationCommand create;
        private final RevokeInvitationCommand revoke;
        private final AccountStatusCommand status;

        @Autowired
        IdentityCliRunner(CreateInvitationCommand create, RevokeInvitationCommand revoke, AccountStatusCommand status) {
            this.create = create;
            this.revoke = revoke;
            this.status = status;
        }

        int execute(String[] args) {
            CommandLine line = new CommandLine(new IdentityAdminCommand());
            line.addSubcommand("invitation-create", create);
            line.addSubcommand("invitation-revoke", revoke);
            line.addSubcommand("account-status", status);
            int firstCommandArgument = "identity".equals(args[0]) ? 1 : 0;
            return line.execute(java.util.Arrays.copyOfRange(args, firstCommandArgument, args.length));
        }
    }
}
