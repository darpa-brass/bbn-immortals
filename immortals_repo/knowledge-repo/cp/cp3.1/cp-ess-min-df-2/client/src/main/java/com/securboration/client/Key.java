package com.securboration.client;


public enum Key {
    
    SERVER_ENDPOINT_URL("http://localhost:8080/ws"),
    MESSAGES_TO_SEND_DIR(null),
    MESSAGE_SENT_DIR("./cp3.1/messages/outbox"),
    MESSAGE_RECEIVED_DIR("./cp3.1/messages/inbox"),
    REPORT_DIR("./cp3.1/report"),
    ;
    
    private final String defaultValue;
    
    private Key(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getValue() {
        final String key = this.name();
        String value = System.getProperty(key);
        
        boolean fromDefault = false;
        if(value == null) {
            if(defaultValue == null) {
                throw new RuntimeException(
                    "no value defined for key " + this.name()
                    );
            }
            value = defaultValue;
        }
        
        System.out.printf("%s = %s%s\n",key,value,fromDefault?"(from default)":"(from property override)");
        return value;
    }

}
