package cn.cjgl.constant;

public  class Constant {
    public static final String authorize="https://dev.fitbit.com/build/reference/web-api/authorization/authorize/";
    public static final String get_access_token="https://api.fitbit.com/oauth2/token";

    public static final String FITBIT_API_BASE_URL = "https://api.fitbit.com/1/user/";
    public static final String FIRBASE_BUCKET_NAME ="fitbitData";

    public static final String HeartRateFileName = "fitbit_HeartRate.text";
    public static final String ECGLogFileName  = "fitbit_ECGLog.text";

    public static final String S3_BUCKET_NAME="mosaicdatabucket";
    public static final String S3_DATA_TYPE_HEARTRATE="heartrate";
    public static final String S3_DATA_TYPE_AUDIO="audio";
}
