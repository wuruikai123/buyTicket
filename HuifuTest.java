import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.*;

public class HuifuTest {
    static final String SYS_ID     = "6666000183050701";
    static final String PRODUCT_ID = "PAYUN";
    static final String HUIFU_ID   = "6666000183050701";
    static final String PRIVATE_KEY = 