package br.com.escrevaevolua.identity.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escrevaevolua.identity.application.AccountAdminService;
import br.com.escrevaevolua.identity.application.IdentityAuditLog;
import br.com.escrevaevolua.identity.application.IdentityRepository;
import br.com.escrevaevolua.identity.application.IdentityService;
import br.com.escrevaevolua.identity.application.InvitationAdminService;

@Configuration
public class IdentityApplicationConfiguration {
    @Bean
    IdentityService identityService(IdentityRepository repository) {
        return new IdentityService(repository);
    }

    @Bean
    InvitationAdminService invitationAdminService(IdentityRepository repository, IdentityAuditLog auditLog) {
        return new InvitationAdminService(repository, auditLog);
    }

    @Bean
    AccountAdminService accountAdminService(IdentityRepository repository) {
        return new AccountAdminService(repository);
    }
}
