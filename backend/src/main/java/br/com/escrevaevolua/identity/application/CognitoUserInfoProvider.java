package br.com.escrevaevolua.identity.application;

public interface CognitoUserInfoProvider {

    UserInfo get(String accessToken);

    record UserInfo(String sub, String email, boolean email_verified) {
    }
}