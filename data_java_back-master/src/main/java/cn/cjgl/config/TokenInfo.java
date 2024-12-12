package cn.cjgl.config;

public class TokenInfo {
    private static String  access_token=null;
    private static Integer expires_in=null;
    private static String  refresh_token=null;
    private static String  scope=null;
    private static String  token_type=null;
    private static String  user_id=null;

    public static String getAccess_token() {
        return access_token;
    }

    public static void setAccess_token(String access_token) {
        TokenInfo.access_token = access_token;
    }

    public static Integer getExpires_in() {
        return expires_in;
    }

    public static void setExpires_in(Integer expires_in) {
        TokenInfo.expires_in = expires_in;
    }

    public static String getRefresh_token() {
        return refresh_token;
    }

    public static void setRefresh_token(String refresh_token) {
        TokenInfo.refresh_token = refresh_token;
    }

    public static String getScope() {
        return scope;
    }

    public static void setScope(String scope) {
        TokenInfo.scope = scope;
    }

    public static String getToken_type() {
        return token_type;
    }

    public static void setToken_type(String token_type) {
        TokenInfo.token_type = token_type;
    }

    public static String getUser_id() {
        return user_id;
    }

    public static void setUser_id(String user_id) {
        TokenInfo.user_id = user_id;
    }
}
