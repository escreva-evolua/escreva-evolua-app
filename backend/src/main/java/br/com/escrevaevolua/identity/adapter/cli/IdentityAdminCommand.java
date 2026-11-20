package br.com.escrevaevolua.identity.adapter.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "identity", mixinStandardHelpOptions = true)
public class IdentityAdminCommand implements Runnable {

    @Override
    public void run() {
        // Subcommands are installed by IdentityCliRunner from the Spring context.
    }
}
